package cn.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 修正后的简易连接池
 * 优化点：
 * 1. 修复异常导致计数器不回滚的问题
 * 2. 修复重复 close 导致连接池污染的问题
 * 3. 队列存放物理连接，借出时创建临时代理，隔离状态
 * 4. 使用 ArrayBlockingQueue 减少内存碎片
 */
@Slf4j
public class MiniHikariPool {

    private final SimplePoolConfig config;

    // 存放“物理连接”而非代理对象
    private final BlockingQueue<Connection> idleQueue;

    private final AtomicInteger totalConnections = new AtomicInteger(0);
    private volatile boolean isShutdown = false;

    public MiniHikariPool(SimplePoolConfig config) {
        this.config = config;
        // 使用 ArrayBlockingQueue，性能更稳，适合固定大小的资源池
        this.idleQueue = new ArrayBlockingQueue<>(config.getMaxSize());
        init();
    }

    private void init() {
        try {
            Class.forName(config.getDriverClassName());
            for (int i = 0; i < config.getCoreSize(); i++) {
                Connection conn = createRealConnection();
                // 确保 offer 成功，或者处理失败情况（这里简化为忽略失败，但在总数中体现）
                if (conn != null) {
                    if (idleQueue.offer(conn)) {
                        totalConnections.incrementAndGet();
                    } else {
                        // 极端情况：核心数 > 最大数，关闭多余连接
                        closeRealConnection(conn);
                    }
                }
            }
            log.info("连接池初始化完成，当前连接数: {}", totalConnections.get());
        } catch (Exception e) {
            throw new RuntimeException("连接池初始化失败", e);
        }
    }

    public Connection getConnection() throws SQLException {
        if (isShutdown) throw new SQLException("连接池已关闭");

        long startTime = System.currentTimeMillis();

        // 1. 尝试直接获取空闲连接
        Connection realConn = idleQueue.poll();
        if (realConn != null) {
            // 拿到物理连接后，验证连接是否有效（模拟 JDBC4 isValid）
            if (isValid(realConn)) {
                return wrapConnection(realConn);
            } else {
                // 连接失效，丢弃并减少计数，进入后续创建流程
                closeRealConnection(realConn);
                totalConnections.decrementAndGet();
            }
        }

        // 2. 队列为空，尝试创建新连接（CAS 乐观锁）
        while (true) {
            int currentCount = totalConnections.get();
            if (currentCount < config.getMaxSize()) {
                if (totalConnections.compareAndSet(currentCount, currentCount + 1)) {
                    // 获取令牌成功，去创建连接
                    try {
                        realConn = createRealConnection();
                        return wrapConnection(realConn);
                    } catch (Exception e) {
                        // 【关键修复】创建失败，必须回滚计数！
                        totalConnections.decrementAndGet();
                        throw new SQLException("获取连接失败: " + e.getMessage(), e);
                    }
                }
                // CAS 失败，说明有其他线程抢先了，循环重试或进入等待逻辑
            } else {
                // 已达最大连接数，退出 CAS 循环，进入等待
                break;
            }
        }

        // 3. 阻塞等待空闲连接
        try {
            long remainingTime = config.getTimeoutMs() - (System.currentTimeMillis() - startTime);
            realConn = idleQueue.poll(Math.max(0, remainingTime), TimeUnit.MILLISECONDS);
            if (realConn == null) {
                throw new SQLException("获取连接超时，请检查 connection-timeout 配置或数据库状态");
            }
            // 拿到连接同样要包装
            return wrapConnection(realConn);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接等待被中断");
        }
    }

    /**
     * 将物理连接包装为代理对象
     */
    private Connection wrapConnection(Connection realConn) {
        return (Connection) Proxy.newProxyInstance(
                MiniHikariPool.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(realConn, this)
        );
    }

    /**
     * 归还连接（仅由 ConnectionHandler 调用）
     */
    protected void recycle(Connection realConn) {
        if (isShutdown) {
            closeRealConnection(realConn);
            return;
        }

        // 简单的健康检查（可选）
        if (!isValid(realConn)) {
            closeRealConnection(realConn);
            totalConnections.decrementAndGet();
            return;
        }

        boolean success = idleQueue.offer(realConn);
        if (!success) {
            // 理论上不应发生，除非计数器逻辑有误。防御性编程：关闭多余连接
            log.warn("归还连接失败，队列已满。销毁连接。");
            closeRealConnection(realConn);
            totalConnections.decrementAndGet();
        }
    }

    private boolean isValid(Connection conn) {
        try {
            // 发送心跳包或简单判断 isClosed
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private Connection createRealConnection() throws SQLException {
        return DriverManager.getConnection(config.getUrl(), config.getUsername(), config.getPassword());
    }

    private void closeRealConnection(Connection conn) {
        try {
            if (conn != null) conn.close();
        } catch (SQLException ignored) {}
    }

    /**
     * 代理处理器：重点加入了 AtomicBoolean 防止重复关闭
     */
    static class ConnectionHandler implements InvocationHandler {
        private final Connection realConn;
        private final MiniHikariPool pool;
        // 【关键修复】防止重复 close 导致连接池污染
        private final AtomicBoolean isClosed = new AtomicBoolean(false);

        public ConnectionHandler(Connection realConn, MiniHikariPool pool) {
            this.realConn = realConn;
            this.pool = pool;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            String methodName = method.getName();

            if ("close".equals(methodName)) {
                // CAS 操作：只有第一次调用 close 能成功设为 true 并执行归还
                if (isClosed.compareAndSet(false, true)) {
                    pool.recycle(realConn);
                }
                return null;
            }

            // 拦截 isClosed 方法，返回代理对象的状态
            if ("isClosed".equals(methodName)) {
                return isClosed.get();
            }

            // 如果连接逻辑上已关闭，禁止调用其他方法
            if (isClosed.get()) {
                throw new SQLException("Connection has been closed");
            }

            return method.invoke(realConn, args);
        }
    }
}
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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 简易数据库连接池实现
 * 模拟 HikariCP 的核心借还逻辑，练习 JUC 编程
 */
@Slf4j
public class SimpleHikariPool {

    // --- 配置参数 ---
    private final String url;
    private final String username;
    private final String password;
    private final int maxPoolSize;
    private final long connectionTimeout; // 毫秒

    // --- JUC 核心组件 ---

    // 1. 核心容器：存放空闲连接。
    // 使用 BlockingQueue 是实现生产者-消费者模型最简单的方式。
    // 获取连接是消费者(take/poll)，归还连接是生产者(offer/put)。
    private final BlockingQueue<Connection> idleQueue;

    // 2. 计数器：记录当前已创建的连接总数（包括空闲的和正在被使用的）。
    // 使用 AtomicInteger 保证在高并发下计数准确，避免创建超过 maxPoolSize 的连接。
    private final AtomicInteger totalConnectionCount = new AtomicInteger(0);

    // 3. 锁对象：用于简单的同步控制（本例主要依靠 Queue 的锁，这里用于初始化等）
    private final Object lock = new Object();

    public SimpleHikariPool(String url, String username, String password, int maxPoolSize) {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxPoolSize = maxPoolSize;
        this.connectionTimeout = 30000; // 默认 30秒超时

        // 初始化阻塞队列，容量为最大连接数
        // 公平锁设置为 true 可以防止线程饥饿，但会稍微降低吞吐量，类似 ReentrantLock 的公平模式
        this.idleQueue = new ArrayBlockingQueue<>(maxPoolSize, true);

        log.info("连接池初始化完成。最大连接数: {}", maxPoolSize);
    }

    /**
     * 获取连接 (核心方法)
     */
    public Connection getConnection() throws SQLException {
        long startTime = System.currentTimeMillis();

        // 1. 尝试从空闲队列中获取（非阻塞）
        Connection conn = idleQueue.poll();
        if (conn != null) {
            // 检查连接有效性（模拟 Hikari 的 isValid）
            if (isValid(conn)) {
                return conn;
            } else {
                // 连接失效，丢弃并减少计数，重新进入获取逻辑
                closePhysicalConnection(conn);
                totalConnectionCount.decrementAndGet();
                return getConnection(); // 递归重试
            }
        }

        // 2. 如果没有空闲连接，判断是否可以创建新连接
        // 使用 CAS (Compare And Swap) 乐观锁思想或直接判断
        if (totalConnectionCount.get() < maxPoolSize) {
            // 双重检查锁或其他方式确保不超过最大数，但在 Atomic 下我们可以先允许 increment
            // 这里为了简单，使用同步块确保创建逻辑的原子性，避免瞬间爆发创建超出阈值
            synchronized (lock) {
                if (totalConnectionCount.get() < maxPoolSize) {
                    Connection newConn = createPhysicalConnection();
                    // 包装成代理连接
                    Connection proxyConn = createProxyConnection(newConn);
                    totalConnectionCount.incrementAndGet();
                    log.debug("创建新连接，当前总数: {}", totalConnectionCount.get());
                    return proxyConn;
                }
            }
        }

        // 3. 如果无法创建新连接（池满了），则阻塞等待空闲连接归还
        try {
            long remaining = connectionTimeout - (System.currentTimeMillis() - startTime);
            // poll(timeout) 是 BlockingQueue 的核心 JUC 方法，底层使用 LockSupport.parkNanos 实现挂起
            conn = idleQueue.poll(Math.max(0, remaining), TimeUnit.MILLISECONDS);

            if (conn == null) {
                throw new SQLException("获取连接超时！等待时间: " + connectionTimeout + "ms");
            }

            if (isValid(conn)) {
                return conn;
            } else {
                // 获取到的连接失效，重试
                closePhysicalConnection(conn);
                totalConnectionCount.decrementAndGet();
                return getConnection();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接线程被中断", e);
        }
    }

    /**
     * 归还连接到池中
     * @param physicalConnection 物理连接对象
     */
    private void recycle(Connection physicalConnection) {
        if (physicalConnection != null) {
            // offer 是非阻塞的，但这里我们预期队列不会满（除非代码逻辑有 bug 导致连接泄漏）
            // 如果连接池关闭了，这里应该销毁连接
            boolean success = idleQueue.offer(physicalConnection);
            if (!success) {
                log.warn("连接归还失败，队列已满？这将导致连接泄漏！销毁该连接。");
                closePhysicalConnection(physicalConnection);
                totalConnectionCount.decrementAndGet();
            } else {
                log.debug("连接已归还。当前可用连接数: {}", idleQueue.size());
            }
        }
    }

    /**
     * 创建物理连接
     */
    private Connection createPhysicalConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    /**
     * 关闭物理连接
     */
    private void closePhysicalConnection(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("关闭物理连接失败", e);
        }
    }

    /**
     * 检查连接是否有效
     */
    private boolean isValid(Connection conn) {
        try {
            // JDBC 4.0 API，3秒超时
            return conn != null && conn.isValid(3);
        } catch (SQLException e) {
            return false;
        }
    }

    /**
     * 使用动态代理包装 Connection
     * 核心目的：拦截 close() 方法
     */
    private Connection createProxyConnection(Connection realConn) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(realConn)
        );
    }

    /**
     * 代理处理器 (InvocationHandler)
     */
    private class ConnectionHandler implements InvocationHandler {
        private final Connection realConnection;

        public ConnectionHandler(Connection realConnection) {
            this.realConnection = realConnection;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 拦截 close 方法
            if ("close".equals(method.getName())) {
                // 不真正关闭，而是归还给池
                SimpleHikariPool.this.recycle(realConnection);
                return null;
            }

            // 拦截 isClosed 方法 (可选，为了逻辑严谨)
            if ("isClosed".equals(method.getName())) {
                return false;
            }

            // 其他方法直接透传给物理连接
            // 如果想模拟得更像，还需要拦截 unwrap, toString 等方法
            return method.invoke(realConnection, args);
        }
    }
}

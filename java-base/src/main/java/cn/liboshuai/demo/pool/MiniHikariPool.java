package cn.liboshuai.demo.pool;


import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模仿 HikariCP 的简易连接池
 * 核心 JUC 知识点：BlockingQueue, AtomicInteger
 */
@Slf4j
public class MiniHikariPool {

    private final SimplePoolConfig config;

    // JUC 考点1：使用阻塞队列存放空闲连接
    // 相比 ArrayList，它是线程安全的，且提供了 poll(timeout) 方法实现获取超时
    private final BlockingQueue<Connection> idleQueue;

    // JUC 考点2：使用 AtomicInteger 保证总连接数统计的原子性
    // 避免多线程并发创建连接导致突破 maxSize
    private final AtomicInteger totalConnections = new AtomicInteger(0);

    // 标记池是否关闭
    private volatile boolean isShutdown = false;

    public MiniHikariPool(SimplePoolConfig config) {
        this.config = config;
        this.idleQueue = new LinkedBlockingQueue<>(config.getMaxSize());
        init();
    }

    private void init() {
        try {
            Class.forName(config.getDriverClassName());
            // 预热：初始化核心连接数
            for (int i = 0; i < config.getCoreSize(); i++) {
                Connection conn = createRealConnection();
                if (conn != null) {
                    idleQueue.offer(createProxyConnection(conn));
                    totalConnections.incrementAndGet();
                }
            }
            log.info("连接池初始化完成，当前连接数: {}", totalConnections.get());
        } catch (Exception e) {
            throw new RuntimeException("连接池初始化失败", e);
        }
    }

    /**
     * 获取连接（对外暴露的方法）
     */
    public Connection getConnection() throws SQLException {
        if (isShutdown) {
            throw new SQLException("连接池已关闭");
        }

        long startTime = System.currentTimeMillis();

        // 1. 尝试从空闲队列获取
        Connection proxyConn = idleQueue.poll();
        if (proxyConn != null) {
            return proxyConn;
        }

        // 2. 如果队列为空，检查是否可以扩容
        // 使用 CAS 思想：先判断，再尝试增加计数，如果增加后超标则回滚
        if (totalConnections.get() < config.getMaxSize()) {
            // 双重检查或乐观锁逻辑：尝试增加计数
            int newCount = totalConnections.incrementAndGet();
            if (newCount <= config.getMaxSize()) {
                // 获得了创建名额
                Connection realConn = createRealConnection();
                return createProxyConnection(realConn);
            } else {
                // 没抢到名额（被其他线程抢先了），回滚计数
                totalConnections.decrementAndGet();
            }
        }

        // 3. 如果没空闲且无法扩容，进入阻塞等待（JUC 核心：带超时的阻塞）
        try {
            long remainingTime = config.getTimeoutMs() - (System.currentTimeMillis() - startTime);
            // poll 是带超时的获取，take 是死等
            proxyConn = idleQueue.poll(Math.max(0, remainingTime), TimeUnit.MILLISECONDS);
            if (proxyConn == null) {
                throw new SQLException("获取连接超时，当前排队等待中...");
            }
            return proxyConn;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取连接被中断");
        }
    }

    /**
     * 归还连接（仅包内可见或通过代理调用）
     */
    protected void recycle(Connection proxyConn) {
        if (isShutdown) {
            closeRealConnection(proxyConn);
            return;
        }
        // 将连接放回队列，唤醒等待的线程
        boolean success = idleQueue.offer(proxyConn);
        if (!success) {
            // 理论上不应该发生，除非归还了多余的连接，或者逻辑错误
            log.warn("归还连接失败，队列已满？关闭该连接");
            closeRealConnection(proxyConn);
            totalConnections.decrementAndGet();
        }
    }

    /**
     * 创建真实的物理连接
     */
    private Connection createRealConnection() throws SQLException {
        return DriverManager.getConnection(
                config.getUrl(), config.getUsername(), config.getPassword()
        );
    }

    /**
     * JUC 伴侣：动态代理
     * 拦截 close 方法，将其行为改变为“归还到队列”
     */
    private Connection createProxyConnection(Connection realConn) {
        return (Connection) Proxy.newProxyInstance(
                MiniHikariPool.class.getClassLoader(),
                new Class[]{Connection.class},
                new ConnectionHandler(realConn, this)
        );
    }

    private void closeRealConnection(Connection proxyConn) {
        // 这里需要剥离代理拿到真实连接去关闭，简化起见直接模拟
        // 在真实 HikariCP 中有 unwrap 逻辑
        log.debug("销毁物理连接");
    }

    // 代理处理器
    static class ConnectionHandler implements InvocationHandler {
        private final Connection realConn;
        private final MiniHikariPool pool;

        public ConnectionHandler(Connection realConn, MiniHikariPool pool) {
            this.realConn = realConn;
            this.pool = pool;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            // 拦截 close 方法
            if ("close".equals(method.getName())) {
                pool.recycle((Connection) proxy);
                return null;
            }
            // 拦截 isClosed 方法，如果连接在池里，逻辑上它是"open"的，但此处简化透传

            // 其他方法直接透传给真实连接
            return method.invoke(realConn, args);
        }
    }
}

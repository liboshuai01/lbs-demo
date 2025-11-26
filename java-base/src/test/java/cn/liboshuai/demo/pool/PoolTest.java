package cn.liboshuai.demo.pool;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PoolTest {

    // TODO: 请修改为你的真实数据库配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test_db?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "password";

    @Test
    public void testSimpleQuery() throws SQLException {
        SimpleHikariPool pool = new SimpleHikariPool(DB_URL, DB_USER, DB_PASS, 2);

        // 第一次获取，应该触发创建
        try (Connection conn = pool.getConnection()) {
            Assert.assertNotNull(conn);
            log.info("获取到连接: {}", conn);
            executeSql(conn);
        } // 退出 try-with-resources 块时，会自动调用 conn.close()，触发我们的动态代理回收逻辑

        // 第二次获取，应该复用同一个物理连接（可以通过日志或 debug 观察）
        try (Connection conn = pool.getConnection()) {
            Assert.assertNotNull(conn);
            log.info("再次获取到连接: {}", conn);
            executeSql(conn);
        }
    }

    /**
     * 高并发压力测试：
     * 模拟 50 个线程同时抢夺只有 5 个连接的连接池。
     * 这将测试 BlockingQueue 的阻塞等待能力和连接回收能力。
     */
    @Test
    public void testHighConcurrency() throws InterruptedException {
        int threadCount = 50; // 模拟50个并发请求
        int poolSize = 5;     // 连接池只有5个连接

        SimpleHikariPool pool = new SimpleHikariPool(DB_URL, DB_USER, DB_PASS, poolSize);

        // JUC: ExecutorService 线程池
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        // JUC: CountDownLatch 用于等待所有线程完成
        CountDownLatch latch = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        long start = System.currentTimeMillis();

        for (int i = 0; i < threadCount; i++) {
            final int threadId = i;
            executor.execute(() -> {
                try {
                    // 模拟业务耗时，持有连接一段时间，让连接池产生“饥饿”
                    try (Connection conn = pool.getConnection()) {
                        log.info("线程-{} 抢到了连接！", threadId);
                        executeSql(conn);
                        Thread.sleep(100); // 持有连接 100ms
                    }
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("线程-{} 获取连接失败: {}", threadId, e.getMessage());
                    failCount.incrementAndGet();
                } finally {
                    latch.countDown(); // 完成一个任务，倒计数减一
                }
            });
        }

        // 阻塞主线程，直到 latch 倒数为 0 (即所有子线程跑完)
        latch.await();
        long end = System.currentTimeMillis();

        log.info("压测结束。耗时: {}ms", (end - start));
        log.info("成功次数: {}, 失败次数: {}", successCount.get(), failCount.get());

        // 验证：因为有等待超时机制，理论上只要超时设置合理，所有请求最终都应该成功
        Assert.assertEquals("所有请求应该都成功", threadCount, successCount.get());
        Assert.assertEquals("不应该有失败请求", 0, failCount.get());

        executor.shutdown();
    }

    private void executeSql(Connection conn) {
        try (Statement stmt = conn.createStatement()) {
            // 简单查询一个常量，不需要建表
            ResultSet rs = stmt.executeQuery("SELECT 1");
            while (rs.next()) {
                // do nothing
            }
        } catch (SQLException e) {
            log.error("SQL执行失败", e);
        }
    }
}
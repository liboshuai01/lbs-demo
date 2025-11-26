package cn.liboshuai.demo.pool;


import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class SimpleJucPoolTest {
    // 请替换为你本地真实的数据库配置
    private static final String DB_URL = "jdbc:mysql://localhost:3306/test?useSSL=false&serverTimezone=UTC";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "123456";

    @Test
    public void testConcurrentGetConnection() throws InterruptedException {
        // 定义连接池：最大5个连接，超时时间2秒
        SimpleJucPool pool = new SimpleJucPool(DB_URL, DB_USER, DB_PASS, 5, 2000);

        // 模拟 20 个线程同时去抢 5 个连接
        int clientCount = 20;
        CountDownLatch startSignal = new CountDownLatch(1); // 统一发令枪
        CountDownLatch doneSignal = new CountDownLatch(clientCount); // 等待所有人做完

        ExecutorService executor = Executors.newFixedThreadPool(clientCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < clientCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    // 等待发令枪响
                    startSignal.await();

                    log.info("Client-{} 尝试获取连接...", index);
                    long start = System.currentTimeMillis();

                    // 获取连接 (try-with-resources 会自动调用 close，触发我们的动态代理归还逻辑)
                    try (Connection conn = pool.getConnection()) {
                        long cost = System.currentTimeMillis() - start;
                        log.info("Client-{} 成功获取连接! 耗时: {}ms, 当前对象: {}", index, cost, conn);

                        // 模拟业务操作耗时 500ms
                        // 因为只有5个连接，每次占用500ms，后续的线程必然会排队或超时
                        Thread.sleep(500);

                        successCount.incrementAndGet();
                    } catch (SQLException e) {
                        log.error("Client-{} 获取连接失败: {}", index, e.getMessage());
                        failCount.incrementAndGet();
                    }

                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    doneSignal.countDown();
                }
            });
        }

        log.info("准备开始高并发测试...");
        Thread.sleep(1000);
        startSignal.countDown(); // 发令枪响，所有线程瞬间开抢

        doneSignal.await(); // 等待所有线程结束
        executor.shutdown();

        log.info("============== 测试结果 ==============");
        log.info("总请求数: {}", clientCount);
        log.info("成功获取: {}", successCount.get());
        log.info("失败(超时): {}", failCount.get());
        log.info("====================================");
    }
}
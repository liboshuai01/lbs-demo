package cn.liboshuai.demo.cf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class RacingServicesDemo {

    public static final Logger log = LoggerFactory.getLogger(RacingServicesDemo.class);

    record Weather(String sourceRegion, double temperature) {}
    public static final Random RANDOM = new Random();

    private static CompletableFuture<Weather> fetchWeatherAsync(String region, int forcedLatency, Executor executor) {
        return CompletableFuture.supplyAsync(() -> {
            log.info("开始连接 [{}] 服务... (延迟: {}ms)", region, forcedLatency);
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            if (region.contains("FAIL")) {
                log.info("[{}] 服务查询失败！", region);
                throw new RuntimeException("Service " + region + " is down");
            }
            log.info("[{}] 服务成功返回。", region);
            return new Weather(region, 18.0 + RANDOM.nextDouble() * 5); // 18-23 度
        }, executor);
    }

    private static void runRace(CompletableFuture<Weather> usFuture, CompletableFuture<Weather> euFuture) {
        List<CompletableFuture<Weather>> futures = List.of(usFuture, euFuture);
        CompletableFuture<Object> firstToFinishFuture = CompletableFuture.anyOf(usFuture, euFuture);
        CompletableFuture<Void> finalChain = firstToFinishFuture.thenAccept(firstResult -> {
            Weather weather = (Weather) firstResult;
            log.info("--- 竞速 [成功]! 最快的结果来自: [{}] ---", weather.sourceRegion());
            log.info("  -> 天气: {}°C", String.format("%.1f", weather.temperature()));
            cancelSlowFutures(futures, firstToFinishFuture);
        }).exceptionally(ex -> {
            log.info("--- 竞速 [失败]! 第一个完成的任务抛出了异常 ---");
            log.info("  -> 错误: {}", ex.getCause().getMessage());
            return null;
        });

        try {
            // 阻塞，直到 .thenAccept 或 .exceptionally 执行完毕
            finalChain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.info("主线程等待时出错: {}", e.getMessage());
        }
    }

    /**
     * 辅助方法：取消那些比 "获胜者" 慢的 Future
     */
    private static void cancelSlowFutures(
            List<CompletableFuture<Weather>> futures,
            CompletableFuture<Object> winnerFuture) {

        log.info("...正在取消其他慢速任务...");
        for (CompletableFuture<Weather> future : futures) {
            // Java 8 没有 .isDone()，但我们可以用 winnerFuture
            // (Java 9+ 可以用 future.resultNow() 或 winnerFuture.resultNow())
            // 这里的逻辑是：如果这个 future 不是获胜者，就取消它

            // 简单起见，我们直接尝试取消所有 *未完成* 的
            // (包括获胜者，但取消一个已完成的 Future 是无害的)
            if (!future.isDone()) {
                future.cancel(true); // true = 尝试中断线程
                if (future.isCancelled()) {
                    log.info("  -> 成功取消了一个慢速任务。");
                }
            }
        }
    }

    // ----------------------------------------------------------------
    public static void main(String[] args) {
        log.info("主线程开始运行...");

        ExecutorService ioExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + t.getId());
            t.setDaemon(true);
            return t;
        });

        // ================================================================
        // 演示 1: US 服务更快
        // ================================================================
        log.info("\n--- 演示 1: US (1000ms) vs EU (1500ms) ---");
        runRace(
                fetchWeatherAsync("us-east", 1000, ioExecutor),
                fetchWeatherAsync("eu-west", 1500, ioExecutor)
        );

        // ================================================================
        // 演示 2: EU 服务更快 (且 US 服务最终会失败)
        // ================================================================
        log.info("\n--- 演示 2: US (1500ms, FAIL) vs EU (1000ms) ---");
        runRace(
                fetchWeatherAsync("us-east-FAIL", 1500, ioExecutor),
                fetchWeatherAsync("eu-west", 1000, ioExecutor)
        );

        // ================================================================
        // 演示 3: 两个服务都失败
        // ================================================================
        log.info("\n--- 演示 3: US (1000ms, FAIL) vs EU (800ms, FAIL) ---");
        runRace(
                fetchWeatherAsync("us-east-FAIL", 1000, ioExecutor),
                fetchWeatherAsync("eu-west-FAIL", 800, ioExecutor)
        );

        ioExecutor.shutdown();
        log.info("\n主线程结束。");
    }
}

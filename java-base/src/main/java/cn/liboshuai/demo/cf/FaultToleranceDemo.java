package cn.liboshuai.demo.cf;

import cn.liboshuai.demo.cf.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FaultToleranceDemo {

    public static final Logger log = LoggerFactory.getLogger(FaultToleranceDemo.class);

    record Config(String databaseUrl, int connectionPoolSize) {}

    private static CompletableFuture<Config> fetchConfigFromPrimary(Executor executor) {
        return CompletableFuture.supplyAsync(
                FunctionUtils.uncheckedSupplier(
                        () -> {
                            log.info("开始尝试连接 [主] 配置服务...");
                            TimeUnit.SECONDS.sleep(1);
                            log.info("[主] 服务连接失败！抛出异常。");
                            throw new RuntimeException("主配置服务不可达（500 Error）");
                        }
                )
                , executor);
    }

    private static Config getLocalDefaultConfig() {
        log.info("警告：无法获取主配置，正在加载本地 [默认] 配置...");
        return new Config("jdbc:h2:mem:fallback_db", 5);
    }

    public static void main(String[] args) {
        log.info("主线程开始运行...");
        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + threadCount.getAndIncrement());
            return t;
        });
        log.info("异步任务开始组装");
        CompletableFuture<Void> cf = fetchConfigFromPrimary(ioExecutor)
                .thenApply(
                        config -> {
                            log.info("[成功路径] 严重：这个日志永远不应该被打印出来！");
                            return config;
                        }
                ).exceptionally(ex -> {
                    log.error("[容错路径] 捕获到上游异常: {}", ex.getMessage());
                    return getLocalDefaultConfig();
                })
                .thenAccept(config -> {
                    log.info("[最终消费] 成功获取到配置。");
                    log.info("  -> 应用启动... 使用配置: {}", config.databaseUrl());
                    log.info("  -> 连接池大小: {}", config.connectionPoolSize());
                });
        log.info("异步任务已经提交");
        try {
            cf.get(5, TimeUnit.SECONDS);
            log.info("异步任务执行完毕");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("主线程等待异步任务时错误：", e);
        } finally {
            log.info("关闭线程池");
            ioExecutor.shutdown();
        }
        /*
        控制台输出结果:
            1. [main]主线程开始运行...
            2. [main]异步任务开始组装
            3. [io-executor-0]开始尝试连接 [主] 配置服务...
            4. [main]异步任务已经提交
            5. [io-executor-0][主] 服务连接失败！抛出异常。
            6. [io-executor-0][容错路径] 捕获到上游异常
            7. [io-executor-0]警告：无法获取主配置，正在加载本地 [默认] 配置...
            8. [io-executor-0][最终消费] 成功获取到配置。
            9. [main]异步任务执行完毕
            10. [main]关闭线程池
         */
    }
}

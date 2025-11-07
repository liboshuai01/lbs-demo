package cn.liboshuai.demo.cf;

import cn.liboshuai.demo.cf.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CombineIndependentCallsDemo {

    private static final Logger log = LoggerFactory.getLogger(CombineIndependentCallsDemo.class);

    record VisitorStats(long uniqueVisitors, long pageViews){}

    record SalesTotal(double totalAmount) {}

    record Dashboard(VisitorStats stats, SalesTotal sales) {}

    private static CompletableFuture<VisitorStats> getVisitorStats(Executor executor) {
        return CompletableFuture.supplyAsync(
                FunctionUtils.uncheckedSupplier(
                        () -> {
                            log.info("开始查询 访问者统计数据...");
                            TimeUnit.SECONDS.sleep(1);
                            log.info("查询访问者统计数据完成");
                            return new VisitorStats(1500, 7500);
                        }
                ),
                executor
        );
    }

    private static CompletableFuture<SalesTotal> getSalesTotal(Executor executor) {
        return CompletableFuture.supplyAsync(
                FunctionUtils.uncheckedSupplier(
                        () -> {
                            log.info("开始查询 销售总额...");
                            TimeUnit.SECONDS.sleep(2);
                            log.info("销售总额 查询完毕...");
                            return new SalesTotal(9860.50);
                        }
                ),
                executor
        );
    }

    public static void main(String[] args) {
        log.info("主线程开始运行...");

        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + threadCount.getAndIncrement());
            return t;
        });

        log.info("组装异步任务visitorStatsCf");
        CompletableFuture<VisitorStats> visitorStatsCf = getVisitorStats(ioExecutor);
        log.info("提交异步任务visitorStatsCf");
        log.info("组装异步任务salesTotalCf");
        CompletableFuture<SalesTotal> salesTotalCf = getSalesTotal(ioExecutor);
        log.info("提交异步任务salesTotalCf");

        log.info("组装异步任务finalCf");
        CompletableFuture<Void> finalCf = visitorStatsCf.thenCombine(salesTotalCf, (stats, sales) -> {
            log.info("两个任务都已经完成了，开始合并数据了...");
            return new Dashboard(stats, sales);
        }).thenAccept(
                dashboard -> {
                    log.info("=== 仪表盘加载成功 ===");
                    log.info("  访问者统计: {} 独立访客", dashboard.stats().uniqueVisitors());
                    log.info("  销售总额: ¥{}", dashboard.sales().totalAmount());
                    log.info("=======================");
                }
        );
        log.info("提交异步任务finalCf");

        try {
            finalCf.get(5, TimeUnit.SECONDS);
            log.info("等待异步任务finalCf执行结束");
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            log.error("主线程等待异步任务执行完毕时报错了: ", e);
        } finally {
            log.info("关闭线程池");
            ioExecutor.shutdown();
        }

        /*
        控制台执行结果:
            1.
         */
    }
}

package cn.liboshuai.demo.cf;

import cn.liboshuai.demo.cf.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 演示 CompletableFuture 的基本链式调用：
 * supplyAsync (创建) -> thenApply/thenApplyAsync (转换) -> thenAccept (消费)
 * 场景：模拟电商应用
 * 1. 异步根据用户ID获取 User 对象
 * 2. 异步根据 User 对象获取其订单 List<Order>
 * 3. 同步转换，计算订单数量
 * 4. 异步消费，打印最终结果
 */
public class BasicChainingDemo {

    public static final Logger log = LoggerFactory.getLogger(BasicChainingDemo.class);

    record User(long id, String name) {
    }

    record Order(String id, long userId, String item) {
    }

    /**
     * 模拟从数据库或API中获取用户
     */
    private static User fetchUser(long id) throws InterruptedException {
        log.info("开始查询用户id：{}", id);
        TimeUnit.SECONDS.sleep(1);
        log.info("用户查询完毕。");
        return new User(id, "用户李" + id);
    }

    /**
     * 模拟根据用户获取其订单列表
     */
    private static List<Order> fetchOrders(User user) throws InterruptedException{
        log.info("开始查询{}的订单", user);
        TimeUnit.SECONDS.sleep(2);
        log.info("订单查询完毕。");
        return List.of(
                new Order("order-123", user.id(), "笔记本电脑"),
                new Order("order-456", user.id(), "机械键盘")
        );
    }

    public static void main(String[] args) {
        log.info("主线程开始运行...");
        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread thread = new Thread(r);
            thread.setName("io-executor-" + threadCount.getAndAdd(1));
            return thread;
        });
        long userId = 101L;
        log.info("开始组装异步任务链条Cf....");
        CompletableFuture<Void> cf = CompletableFuture.supplyAsync(FunctionUtils.uncheckedSupplier(() -> fetchUser(userId)), ioExecutor)
                .thenApplyAsync(FunctionUtils.uncheckedFunction(BasicChainingDemo::fetchOrders), ioExecutor)
                .thenApply(orders -> {
                    log.info("开始同步计算订单数量...");
                    return orders.size();
                })
                .thenAccept(orderSize -> {
                    log.info("任务完成！{}用户共有{}个订单", userId, orderSize);
                });
        log.info("提交异步任务链条cf...");
        try {
            cf.get(5, TimeUnit.SECONDS);
            log.info("异步任务执行完毕...");
        } catch (InterruptedException | TimeoutException | ExecutionException e) {
            log.error("主线程等待异步任务完成时出现异常：", e);
        } finally {
            log.info("关闭线程池");
            ioExecutor.shutdown();
        }
        /*
        控制台结果：
            1. [main]主线程开始运行...
            2. [main]开始组装异步任务链条Cf....
            3. [main]提交异步任务链条cf...
            4. [io-executor-0]开始查询用户id：101
            5. [io-executor-0]用户查询完毕。
            6. [io-executor-1]开始查询User[id=101, name=用户李101]的订单
            7. [io-executor-1]订单查询完毕。
            8. [io-executor-1]开始同步计算订单数量...
            9. [io-executor-1]任务完成！101用户共有2个订单
            10. [mian]异步任务执行完毕...
            11. [mian]关闭线程池
         */
    }

}

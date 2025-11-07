package cn.liboshuai.demo.cf;

import cn.liboshuai.demo.cf.util.FunctionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class DependentCallsDemo {

    public static final Logger log = LoggerFactory.getLogger(DependentCallsDemo.class);

    record User(long id, String name, String friendListId) {
    }

    record Friend(long id, String name) {
    }

    private static CompletableFuture<User> getUserProfile(long id, Executor executor) {
        return CompletableFuture.supplyAsync(FunctionUtils.uncheckedSupplier(() -> {
            log.info("开始查询用户Id为{}的简介信息", id);
            TimeUnit.SECONDS.sleep(1);
            log.info("用户简介信息查询完毕");
            return new User(id, "用户李" + id, "2,3,4");
        }), executor);
    }

    private static CompletableFuture<List<Friend>> getFriendList(String friendListId, Executor executor) {
        return CompletableFuture.supplyAsync(FunctionUtils.uncheckedSupplier(() -> {
            log.info("开始查询朋友id为{}的好友列表", friendListId);
            TimeUnit.SECONDS.sleep(2);
            log.info("好友列表查询完毕");
            return List.of(new Friend(2, "朋友2"), new Friend(3, "朋友3"), new Friend(4, "朋友4"));
        }), executor);
    }


    public static void main(String[] args) {
        log.info("主线程开始运行");
        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(3, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + threadCount.getAndIncrement());
            return t;
        });
        long userId = 101L;
        log.info("开始组装异步任务链");
        CompletableFuture<Void> cf = getUserProfile(userId, ioExecutor).thenCompose(user -> {
            log.info("已经成功获取到用户id，现在开始获取好友列表了。");
            return getFriendList(user.friendListId, ioExecutor);
        }).thenAccept(friends -> {
            log.info("任务链完成！{}用户的好友列表共计{}个人", userId, friends.size());
            friends.forEach(friend -> log.info("    -> {}", friend.name));
        });
        log.info("提交异步任务链");
        try {
            cf.get(5, TimeUnit.SECONDS);
            log.info("异步任务链执行结束");
        } catch (Exception e) {
            log.error("主线程等待异步任务时出错：", e);
        } finally {
            log.info("关闭线程池");
            ioExecutor.shutdown();
        }
        /*
        控制台执行结果：
            1. [main]主线程开始运行
            2. [main]开始组装异步任务链
            3. [main]提交异步任务链
            4. [io-executor-0]开始查询用户Id为101的简介信息
            5. [io-executor-0]用户简介信息查询完毕
            6. [io-executor-0]已经成功获取到用户id，现在开始获取好友列表了。
            7. [io-executor-1]开始查询朋友id为2,3,4的好友列表
            8. [io-executor-1]好友列表查询完毕
            9. [io-executor-1]任务链完成！101用户的好友列表共计3个人
            10. [io-executor-1]    -> 朋友2
            11. [io-executor-1]    -> 朋友3
            12. [io-executor-1]    -> 朋友4
            13. [main]异步任务链执行结束
            14. [main]关闭线程池
         */
    }
}

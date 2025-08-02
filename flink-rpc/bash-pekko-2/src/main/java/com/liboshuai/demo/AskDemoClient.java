// src/main/java/com/liboshuai/demo/ask/AskDemoClient.java
package com.liboshuai.demo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;

public class AskDemoClient {

    private static final Logger log = LoggerFactory.getLogger(AskDemoClient.class);

    public static void main(String[] args) throws InterruptedException {
        // 配置客户端系统，使用随机端口
        Config config = ConfigFactory.parseString("pekko.remote.artery.canonical.port = 25523")
                .withFallback(ConfigFactory.load());

        final ActorSystem system = ActorSystem.create("ClientSystem", config);
        log.info("ClientSystem for 'ask' demo started.");

        //  **重要**：路径指向我们新创建的 Actor "greeterWithReply"
        final String remotePath = "pekko://RemoteSystem@127.0.0.1:25522/user/greeterWithReply";
        final ActorSelection remoteActor = system.actorSelection(remotePath);

        // 创建请求消息
        AskForGreeting request = new AskForGreeting("Pekko Ask Pattern");
        // 定义超时时间
        Duration timeout = Duration.ofSeconds(5);

        log.info("Sending message to {} using 'ask' pattern.", remotePath);

        // **核心：使用 Patterns.ask 发送消息**
        // 它返回一个 CompletionStage，代表一个未来的结果
        CompletionStage<Object> responseFuture = Patterns.ask(remoteActor, request, timeout);
        log.info("Ask request sent. Waiting for the response...");

        // 由于是异步操作，我们需要一种方式来等待结果，以防 main 线程提前退出
        // CountDownLatch 是一个很好的选择
        final CountDownLatch latch = new CountDownLatch(1);

        // 处理 CompletionStage 的结果
        responseFuture.whenComplete((response, error) -> {
            if (error != null) {
                // 处理错误，例如超时 (AskTimeoutException)
                log.error("Ask request failed!", error);
            } else {
                // 处理成功响应
                if (response instanceof GreetingReply) {
                    GreetingReply reply = (GreetingReply) response;
                    log.info(">>> Successfully received reply: '{}'", reply.getMessage());
                } else {
                    log.warn("Received an unexpected response type: {}", response.getClass().getName());
                }
            }
            // 无论成功还是失败，都减少 latch 的计数，让 main 线程可以继续
            latch.countDown();
        });

        // 阻塞 main 线程，直到异步操作完成
        latch.await();

        // 最终关闭 ActorSystem
        system.terminate();
        log.info("ClientSystem terminated.");
    }
}

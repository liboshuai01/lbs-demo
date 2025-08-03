package com.liboshuai.demo.client;

import com.liboshuai.demo.common.RequestData;
import com.liboshuai.demo.common.ResponseData;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@Slf4j
public class ClientMain {
    public static void main(String[] args) {
        // 1. 加载配置, 为客户端指定一个不同于服务端的端口 (25521)
        Config config = ConfigFactory.parseString(
                "pekko.remote.artery.canonical.port = 25523"
        ).withFallback(ConfigFactory.load());

        // 2. 创建客户端 ActorSystem
        ActorSystem clientSystem = ActorSystem.create("clientSystem", config);

        // 3. 定位(查找)远程服务端 Actor
        String serverPath = "pekko://serverSystem@127.0.0.1:25522/user/serverActor";
        ActorSelection serverActorSelection = clientSystem.actorSelection(serverPath);

        log.info("客户端已启动，准备向 {} 发送消息", serverPath);

        // 4. 从控制台读取用户输入
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            // 定义 ask 的超时时间
            final Duration timeout = Duration.ofSeconds(5);

            while (true) {
                log.info(">>> 请在控制台输入要发送的消息, 输入 'exit' 退出 <<<");
                line = reader.readLine();

                if (line == null || "exit".equalsIgnoreCase(line.trim())) {
                    break;
                }

                // 5. 使用 Patterns.ask 模式发送消息
                // ask 方法会返回一个 CompletionStage，代表一个未来的计算结果
                RequestData request = new RequestData(line);
                CompletionStage<Object> future = Patterns.ask(serverActorSelection, request, timeout);

                log.info("已向服务端发送消息: [{}], 正在等待响应...", line);

                // 6. 同步等待并处理结果
                // 为了在控制台应用中清晰地展示请求-响应流程，我们在这里阻塞等待结果。
                // 在真实的应用中 (如Web服务)，通常会使用 whenComplete 等非阻塞方式处理 future。
                try {
                    // toCompletableFuture().get() 会阻塞当前线程直到结果返回或超时
                    ResponseData response = (ResponseData) future.toCompletableFuture().get(timeout.toMillis(), TimeUnit.MILLISECONDS);
                    log.info("从服务端接收到同步响应: [{}]", response.getResponse());
                } catch (TimeoutException e) {
                    log.error("请求超时！服务端未在 {} 秒内响应。", timeout.getSeconds());
                } catch (Exception e) {
                    // 其他异常，例如序列化失败或远程Actor不存在
                    log.error("请求未能成功完成", e);
                }
            }
        } catch (IOException e) {
            log.error("等待输入时发生错误。", e);
        } finally {
            log.info("客户端正在关闭...");
            clientSystem.terminate();
        }
    }
}
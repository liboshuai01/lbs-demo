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
import java.util.concurrent.TimeoutException;

@Slf4j
public class ClientMain {
    public static void main(String[] args) {
        // 1. 加载配置, 为客户端指定一个不同于服务端的端口 (25523)
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

                log.info("已向服务端发送消息: [{}], 等待异步响应...", line);

                // 6. 【异步处理】使用 whenComplete 回调来处理结果，避免阻塞主线程
                // 在生产实践中，这种非阻塞的方式是首选。
                // 主线程在发送消息后可以继续执行其他任务 (在这个控制台应用里，是继续等待下一次输入)。
                future.whenComplete((response, error) -> {
                    // 这个回调会在 future 完成时（无论是成功还是失败）被执行
                    if (error != null) {
                        // 如果 error 不为 null，说明处理过程中发生了异常
                        if (error instanceof TimeoutException) {
                            log.error("请求超时！服务端未在 {} 秒内响应。", timeout.getSeconds());
                        } else {
                            // 其他异常，例如序列化失败或远程Actor不存在
                            log.error("请求未能成功完成", error);
                        }
                    } else {
                        // 如果 error 为 null，说明成功接收到了响应
                        // 注意：这里需要进行类型转换
                        ResponseData responseData = (ResponseData) response;
                        log.info("从服务端接收到异步响应: [{}]", responseData.getResponse());
                    }
                });
            }
        } catch (IOException e) {
            log.error("等待输入时发生错误。", e);
        } finally {
            log.info("客户端正在关闭...");
            clientSystem.terminate();
        }
    }
}
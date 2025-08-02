package com.liboshuai.demo;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.apache.pekko.pattern.Patterns;
import org.apache.pekko.util.Timeout;

import java.time.Duration;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

public class AskMain {

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("AskExampleSystem");
        final ActorRef calculator = system.actorOf(CalculatorActor.props(), "calculator");

        // ask 操作需要一个超时时间，以防止永久等待
        final Timeout timeout = Timeout.create(Duration.ofSeconds(3));

        // --- 示例 1: ask for addition ---
        final Add addMessage = new Add(10, 5);
        // Patterns.ask() 返回一个 CompletionStage
        CompletionStage<Object> addFuture = (CompletionStage<Object>) Patterns.ask(calculator, addMessage, timeout);

        System.out.println("Asking for 10 + 5...");

        // 处理返回的结果
        // 使用 CompletionStage 来异步处理结果
        addFuture.whenComplete((response, error) -> {
            if (error == null) {
                // 检查响应类型，确保是我们期望的
                if (response instanceof CalculationResult) {
                    CalculationResult result = (CalculationResult) response;
                    System.out.println(">> Addition successful. Result: " + result.result);
                } else {
                    System.err.println(">> Received unexpected response type: " + response.getClass().getName());
                }
            } else {
                // 如果 Actor 未在超时时间内回复，这里会收到一个 TimeoutException
                System.err.println(">> Failed to get addition result: " + error.getMessage());
            }
        });


        // --- 示例 2: ask for subtraction and block for result (不推荐在生产代码中阻塞，仅为演示) ---
        final Subtract subtractMessage = new Subtract(100, 42);
        CompletionStage<Object> subtractFuture = (CompletionStage<Object>) Patterns.ask(calculator, subtractMessage, timeout);

        System.out.println("\nAsking for 100 - 42 and waiting for the result...");

        try {
            // .toCompletableFuture().get() 会阻塞当前线程直到结果返回或超时
            CalculationResult result = (CalculationResult) subtractFuture.toCompletableFuture().get();
            System.out.println(">> Subtraction successful (blocking). Result: " + result.result);
        } catch (InterruptedException | ExecutionException e) {
            System.err.println(">> Failed to get subtraction result: " + e.getMessage());
        }


        // 等待异步操作完成，然后关闭系统
        System.out.println("\n>>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}

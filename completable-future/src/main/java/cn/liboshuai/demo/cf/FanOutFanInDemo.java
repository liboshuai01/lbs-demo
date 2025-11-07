package cn.liboshuai.demo.cf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class FanOutFanInDemo {

    public static final Logger log = LoggerFactory.getLogger(FanOutFanInDemo.class);

    record Document(int id, String content) {}

    record ProcessingReceipt(int docId, String status, int processingTimeMs) {}

    private static final Random RANDOM = new Random();


    private static CompletableFuture<ProcessingReceipt> processDocumentAsync(
            Document doc, Executor executor) {

        return CompletableFuture.supplyAsync(() -> {
            log.info("开始处理 [Doc {}]...", doc.id());
            int processingTimeMs = 500 + RANDOM.nextInt(1000); // 0.5s ~ 1.5s

            try {
                Thread.sleep(processingTimeMs);
            } catch (InterruptedException e) { /* ... */ }

            // 模拟一个随机的失败
            if (doc.content().contains("FAIL")) {
                log.info("处理 [Doc {}]... 失败! (内容包含FAIL)", doc.id());
                throw new RuntimeException("ProcessingError: Doc " + doc.id() + " is invalid");
            }

            log.info("完成处理 [Doc {}]。 ({}ms)", doc.id(), processingTimeMs);
            return new ProcessingReceipt(doc.id(), "Processed", processingTimeMs);
        }, executor);
    }

    private static void runBatch(List<Document> documents, Executor ioExecutor) {
        log.info("开始扇出 (Fan-out) {} 个任务", documents.size());

        List<CompletableFuture<ProcessingReceipt>> completableFutures = documents.stream().map(document -> processDocumentAsync(document, ioExecutor))
                .toList();
        CompletableFuture<Void> allDoneFuture = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<Void> finalChain = allDoneFuture.thenAccept(v -> {
            // *** 成功路径 ***
            // 当 allOf() *成功* 完成时，这个块会执行
            log.info("--- 批量任务 [成功] 完成！---");
            log.info("生成总结报告:");
            List<ProcessingReceipt> receipts = completableFutures.stream().map(CompletableFuture::join).toList();
            receipts.forEach(receipt -> log.info("    -> [Doc {} ]状态: {}", receipt.docId, receipt.status));
        }).exceptionally(ex -> {
            log.info("--- 批量任务 [失败]! ---");
            log.info("    -> 错误：{}", ex.getCause().getMessage());

            log.info("    -> 部分结果:");
            for (CompletableFuture<ProcessingReceipt> future : completableFutures) {
                if (future.isDone() && !future.isCompletedExceptionally()) {
                    log.info("    -> [Doc {}]已成功", future.join().docId);
                }
            }
            return null;
        });
        try {
            finalChain.get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("主线程等待时出错：", e);
        }
    }

    public static void main(String[] args) {
        log.info("主线程开始运行...");
        AtomicInteger threadCount = new AtomicInteger(0);
        ExecutorService ioExecutor = Executors.newFixedThreadPool(4, r -> {
            Thread t = new Thread(r);
            t.setName("io-executor-" + threadCount.getAndIncrement());
            return t;
        });

        // ================================================================
        // 演示 1: 成功的批量任务
        // ================================================================
        log.info("\n--- 开始演示 [成功] 的批量任务 ---");
        List<Document> successBatch = List.of(
                new Document(1, "content..."),
                new Document(2, "content..."),
                new Document(3, "content...")
        );
        runBatch(successBatch, ioExecutor);

        // ================================================================
        // 演示 2: 失败的批量任务 (一个任务会失败)
        // ================================================================
        log.info("\n--- 开始演示 [失败] 的批量任务 ---");
        List<Document> failingBatch = List.of(
                new Document(10, "content..."),
                new Document(11, "content... FAIL ..."),
                new Document(12, "content...")
        );
        runBatch(failingBatch, ioExecutor);

        ioExecutor.shutdown();
        log.info("\n主线程结束");
    }

    /*
    控制台结束输出:
        1. 主线程开始运行...
        2. --- 开始演示 [成功] 的批量任务 ---
        3. --- 开始演示 [失败] 的批量任务 ---
        4. 开始扇出 (Fan-out) 3 个任务
     */
}

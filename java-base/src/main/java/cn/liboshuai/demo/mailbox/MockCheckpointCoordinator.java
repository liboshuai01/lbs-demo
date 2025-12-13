package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 模拟 JobMaster 端的 Checkpoint 协调器.
 * 定期向 Task 发送 Checkpoint 请求.
 */
@Slf4j
public class MockCheckpointCoordinator extends Thread {
    private final MailboxExecutor taskExecutor;

    private volatile boolean running = true;

    public MockCheckpointCoordinator(MailboxExecutor taskExecutor) {
        super("Checkpoint-Coordinator");
        this.taskExecutor = taskExecutor;
    }

    @Override
    public void run() {
        long checkpointId = 1;
        while (running) {
            try {
                // 每 1.5 秒触发一次 Checkpoint
                TimeUnit.MILLISECONDS.sleep(1500);
                long currentId = checkpointId++;
                log.info("[JM] Triggering Checkpoint {}", currentId);

                // 提交给 Task 主线程执行 (线程安全!)
                taskExecutor.execute(() -> {
                    log.info(" >>> [MainThread] Checkpoint {} 进行中 (状态快照)", currentId);
                    // 模拟 Checkpoint 耗时
                    try {
                        TimeUnit.MILLISECONDS.sleep(50);
                    } catch (InterruptedException e) {Thread.currentThread().interrupt();}
                }, "Checkpoint-" + currentId);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }
}

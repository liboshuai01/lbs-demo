package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CheckpointScheduler extends Thread {

    private final StreamTask task;
    private final MailboxExecutor controlMailboxExecutor;
    private volatile boolean running = true;

    public CheckpointScheduler(StreamTask task) {
        super("Checkpoint-Thread");
        this.task = task;
        this.controlMailboxExecutor = task.getControlMailboxExecutor();
    }

    @Override
    public void run() {
        long checkpointId = 0;
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            long id = ++checkpointId;
            log.info("[JM] 触发 Checkpoint {}", id);
            controlMailboxExecutor.execute(() -> task.performCheckpoint(id), "Checkpoint-" + id);
        }
    }

    public void shutdown() {
        this.running = false;
        this.interrupt();
    }
}

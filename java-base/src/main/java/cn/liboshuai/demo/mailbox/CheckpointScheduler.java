package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CheckpointScheduler extends Thread {

    private final StreamTask streamTask;
    private final MailboxExecutor controlMailboxExecutor;
    private volatile boolean running = true;

    public CheckpointScheduler(StreamTask streamTask) {
        this.streamTask = streamTask;
        this.controlMailboxExecutor = streamTask.getControlMailboxExecutor();
    }


    @Override
    public void run() {
        long checkpointId = 0;
        while (running) {
            try {
                TimeUnit.SECONDS.sleep(2);
                long id = ++checkpointId;
                log.info("[JM] 触发 Checkpoint {}", id);
                controlMailboxExecutor.execute(() -> streamTask.performCheckpoint(id), "Checkpoint-" + id);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        this.running = false;
        this.interrupt();
    }
}

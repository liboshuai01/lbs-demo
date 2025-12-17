package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CounterStreamTask extends StreamTask implements DataOutput {

    private long recordCount;
    private final StreamInputProcessor streamInputProcessor;

    public CounterStreamTask(MiniInputGate inputGate) {
        super();
        this.streamInputProcessor = new StreamInputProcessor(inputGate, this);
    }

    @Override
    public void runDefaultAction(Controller controller) {
        streamInputProcessor.runDefaultAction(controller);
    }

    @Override
    public void performCheckpoint(long checkpointId) {
        log.info("  >>> [Checkpoint Starting] ID: {}, 当前状态值: {}", checkpointId, recordCount);
        try {
            TimeUnit.MILLISECONDS.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info("  <<< [Checkpoint Finished] ID: {} 完成", checkpointId);
    }

    @Override
    public void processRecord(String record) {
        this.recordCount++;

        if (recordCount % 10 == 0) {
            log.info("Task 处理进度: {} 条", recordCount);
        }
    }
}

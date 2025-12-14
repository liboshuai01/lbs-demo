package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

@Slf4j
public class CounterStreamTask extends StreamTask implements DataOutput{

    private final StreamInputProcessor inputProcessor;
    private long recordCount;

    public CounterStreamTask(MiniInputGate inputGate) {
        super();
        this.inputProcessor = new StreamInputProcessor(inputGate, this);
    }

    @Override
    public void runDefaultAction(Controller controller) {
        inputProcessor.runDefaultAction(controller);
    }

    @Override
    public void processRecord(String record) {
        this.recordCount++;
        if (recordCount % 10 == 0) {
            log.info("Task 处理进度: {} 条", recordCount);
        }
    }

    @Override
    void performCheckpoint(long checkpointId) {
        log.info(" >>> [Checkpoint Starting] ID: {}, 当前状态值: {}", checkpointId, recordCount);
        try {
            TimeUnit.MILLISECONDS.sleep(50);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        log.info(" <<< [Checkpoint Finished] ID: {} 完成", checkpointId);
    }
}

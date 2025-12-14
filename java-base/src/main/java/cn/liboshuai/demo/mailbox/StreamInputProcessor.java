package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

@Slf4j
public class StreamInputProcessor implements MailboxDefaultAction{

    private final MiniInputGate inputGate;
    private final DataOutput dataOutput;

    public StreamInputProcessor(MiniInputGate inputGate, DataOutput dataOutput) {
        this.inputGate = inputGate;
        this.dataOutput = dataOutput;
    }

    @Override
    public void runDefaultAction(Controller controller) {
        String record = inputGate.pollNext();
        if (record != null) {
            dataOutput.processRecord(record);
        } else {
            CompletableFuture<Void> availableComputeFuture = inputGate.getAvailableComputeFuture();
            if (availableComputeFuture.isDone()) {
                return;
            }
            controller.suspendDefaultAction();
            availableComputeFuture.thenRun(() ->  ((MailboxProcessor) controller).resumeDefaultAction());
        }
    }
}

package cn.liboshuai.demo.mailbox;

import java.util.concurrent.CompletableFuture;

public class StreamInputProcessor implements MailboxDefaultAction {

    private final MiniInputGate inputGate;
    private final DataOutput output;

    public StreamInputProcessor(MiniInputGate inputGate, DataOutput output) {
        this.inputGate = inputGate;
        this.output = output;
    }

    @Override
    public void runDefaultAction(Controller controller) {
        String record = inputGate.pollNext();
        if (record != null) {
            output.processRecord(record);
        } else {
            CompletableFuture<Void> availabilityFuture = inputGate.getAvailabilityFuture();
            if (availabilityFuture.isDone()) {
                return;
            }
            controller.suspendDefaultAction();
            availabilityFuture.thenRun(() ->
                    ((MailboxProcessor) controller).resumeDefaultAction());
        }
    }
}

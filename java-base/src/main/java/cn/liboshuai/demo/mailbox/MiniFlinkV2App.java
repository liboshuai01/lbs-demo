package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiniFlinkV2App {
    public static void main(String[] args) {
        MiniInputGate inputGate = new MiniInputGate();
        StreamTask task = new CounterStreamTask(inputGate);
        NettyDataProducer netty = new NettyDataProducer(inputGate);
        netty.start();
        CheckpointScheduler checkpointScheduler = new CheckpointScheduler(task);
        checkpointScheduler.start();
        try {
            task.invoke();
        } catch (Exception e) {
            log.error("崩溃: ", e);
        } finally {
            netty.shutdown();
            checkpointScheduler.shutdown();
        }
    }
}

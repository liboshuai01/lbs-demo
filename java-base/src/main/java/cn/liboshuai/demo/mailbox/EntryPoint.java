package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EntryPoint {
    public static void main(String[] args) {
        log.info("==== Flink Mailbox 模型深度模拟启动 ===");

        MiniInputGate inputGate = new MiniInputGate();
        StreamTask task = new CounterStreamTask(inputGate);
        NettyDataProducer netty = new NettyDataProducer(inputGate);
        CheckpointScheduler checkpoint = new CheckpointScheduler(task);
        netty.start();
        checkpoint.start();
        try {
            task.invoke();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            netty.shutdown();
            checkpoint.shutdown();
        }
    }
}

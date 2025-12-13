package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MiniFlinkComplexApp {
    public static void main(String[] args) {
        log.info("=== 启动 Mini-Flink 中 (高级模式) ===");
        // 1. 准备基础设施
        MockInputGate inputGate = new MockInputGate();

        // 注意: StreamTask 必须在主线程创建, 因为它会绑定 Thread.currentThread()
        SourceStreamTask sourceTask = new SourceStreamTask(inputGate);

        // 2. 启动 Netty 线程 (生产者)
        MockNettyThread nettyThread = new MockNettyThread(inputGate);
        nettyThread.start();

        // 3. 启动 Checkpoint 协调器 (控制信号发送者)
        // 传入 Task 的控制执行器 (Control Executor)
        MockCheckpointCoordinator checkpointCoordinator = new MockCheckpointCoordinator(sourceTask.getControlMailboxExecutor());
        checkpointCoordinator.start();

        // 4. 运行任务主循环 (这将阻塞当前主线程)
        // 在此期间, 主线程会在 "处理数据", "处理Checkpoint" 和 "挂起等待" 三种状态间切换
        try {
            sourceTask.invoke();
        } catch (Exception e) {
           log.error("", e);
        } finally {
            // 清理资源
            nettyThread.shutdown();
            checkpointCoordinator.shutdown();
        }
    }
}

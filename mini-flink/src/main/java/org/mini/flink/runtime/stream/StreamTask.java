package org.mini.flink.runtime.stream;

import org.mini.flink.api.Collector;
import org.mini.flink.api.Operator;
import org.mini.flink.api.Sink;
import org.mini.flink.api.Source;
import org.mini.flink.runtime.jobmanager.JobManager;
import org.mini.flink.runtime.state.CheckpointBarrier;
import org.mini.flink.runtime.state.Stateful;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 * 在 TaskManager 中执行的核心任务单元。它包装了 Source, Operator, 或 Sink 的逻辑。
 */
public class StreamTask implements Runnable {
    private static final Logger LOG = Logger.getLogger(StreamTask.class.getName());

    private final String taskName;
    private final Serializable logic;
    private final List<DataChannel> inputs;
    private final List<DataChannel> outputs;
    private final JobManager jobManager;
    private volatile boolean running = true;

    // 【JUC知识点】: 读写锁，用于在状态快照时保护状态的一致性。
    // 快照时获取写锁，阻止所有读写操作；平时处理数据时获取读锁。
    private final ReentrantReadWriteLock stateLock = new ReentrantReadWriteLock();
    // 【JUC知识点】: ConcurrentHashMap 作为内存状态存储，保证多并发任务下的线程安全。
    private Map<String, Object> operatorState;

    public StreamTask(String taskName, Serializable logic, List<DataChannel> inputs, List<DataChannel> outputs, JobManager jobManager) {
        this.taskName = taskName;
        this.logic = logic;
        this.inputs = inputs;
        this.outputs = outputs;
        this.jobManager = jobManager;
        if (logic instanceof Stateful) {
            this.operatorState = new ConcurrentHashMap<>();
            ((Stateful) logic).initializeState(this.operatorState);
        }
    }

    @Override
    public void run() {
        try {
            LOG.info("任务 [" + taskName + "] 开始运行...");
            if (logic instanceof Source) {
                runSource();
            } else if (logic instanceof Operator) {
                runOperator();
            } else if (logic instanceof Sink) {
                runSink();
            }
        } catch (InterruptedException e) {
            LOG.warning("任务 [" + taskName + "] 被中断。");
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            LOG.severe("任务 [" + taskName + "] 发生严重错误: " + e.getMessage());
            e.printStackTrace();
        } finally {
            LOG.info("任务 [" + taskName + "] 停止运行。");
        }
    }

    private void runSource() throws Exception {
        Source<?> source = (Source<?>) logic;
        Collector<Object> collector = new OutputCollector(outputs, null);
        source.run((Collector) collector);
    }

    private void runOperator() throws Exception {
        Operator<Object, Object> operator = (Operator<Object, Object>) logic;
        Collector<Object> collector = new OutputCollector(outputs, operator);
        // Operator 通常只有一个输入
        DataChannel inputChannel = inputs.get(0);
        while (running) {
            Object record = inputChannel.pop(); // 从上游阻塞式地获取数据

            stateLock.readLock().lock(); // 获取读锁，允许多个读操作并发
            try {
                if (record instanceof CheckpointBarrier) {
                    handleCheckpointBarrier((CheckpointBarrier) record, collector);
                } else {
                    operator.process(record, collector);
                }
            } finally {
                stateLock.readLock().unlock();
            }
        }
    }

    private void runSink() throws Exception {
        Sink<Object> sink = (Sink<Object>) logic;
        DataChannel inputChannel = inputs.get(0);
        while (running) {
            Object record = inputChannel.pop();
            if (record instanceof CheckpointBarrier) {
                handleCheckpointBarrier((CheckpointBarrier) record, null);
            } else {
                sink.invoke(record);
            }
        }
    }

    private void handleCheckpointBarrier(CheckpointBarrier barrier, Collector<Object> collector) throws InterruptedException {
        LOG.info(String.format("任务 [%s] 收到检查点屏障: %d", taskName, barrier.getCheckpointId()));

        // 1. 执行状态快照
        if (logic instanceof Stateful) {
            stateLock.writeLock().lock(); // 获取写锁，暂停所有数据处理
            try {
                LOG.info(String.format("任务 [%s] 正在为检查点 %d 进行状态快照...", taskName, barrier.getCheckpointId()));
                Map<String, Object> stateSnapshot = ((Stateful) logic).snapshotState();
                // 2. 将快照报告给 JobManager
                jobManager.acknowledgeCheckpoint(barrier.getCheckpointId(), taskName, stateSnapshot);
            } finally {
                stateLock.writeLock().unlock();
            }
        } else {
            // 无状态算子直接确认
            jobManager.acknowledgeCheckpoint(barrier.getCheckpointId(), taskName, null);
        }

        // 3. 将屏障广播到下游
        if (collector != null) {
            ((OutputCollector) collector).broadcastBarrier(barrier);
            LOG.info(String.format("任务 [%s] 已将屏障 %d 广播到下游", taskName, barrier.getCheckpointId()));
        }
    }

    public void stop() {
        this.running = false;
    }

    /**
     * 内部收集器实现
     */
    private class OutputCollector implements Collector<Object> {
        private final List<DataChannel> outputChannels;
        private final Object operatorLogic;

        public OutputCollector(List<DataChannel> outputChannels, Object operatorLogic) {
            // 【JUC知识点】: 使用 CopyOnWriteArrayList 保证广播时的线程安全。
            // 虽然在此单线程模型中不是必须，但在真实 Flink 中，输出目标可能是动态变化的。
            this.outputChannels = outputChannels != null ? new CopyOnWriteArrayList<>(outputChannels) : new CopyOnWriteArrayList<>();
            this.operatorLogic = operatorLogic;
        }

        @Override
        public void collect(Object record) {
            // 分发到所有下游 channel
            for (DataChannel channel : outputChannels) {
                try {
                    channel.push(record);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warning("Collect interrupted");
                }
            }
        }

        public void broadcastBarrier(CheckpointBarrier barrier) {
            LOG.info(String.format("任务 [%s] 的 Collector 正在广播屏障 %d", taskName, barrier.getCheckpointId()));
            for (DataChannel channel : outputChannels) {
                try {
                    // 这里我们简单地把 barrier 放在数据前面，实际 Flink 有更复杂的对齐逻辑
                    channel.push(barrier);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    LOG.warning("Barrier broadcast interrupted");
                }
            }
        }
    }
}
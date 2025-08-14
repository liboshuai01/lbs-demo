package org.mini.flink.runtime.jobmanager;

import java.util.Map;
import java.util.concurrent.*;
import java.util.logging.Logger;

/**
 * 检查点协调器，由 JobManager 持有，负责触发和管理检查点。
 */
public class CheckpointCoordinator {
    private static final Logger LOG = Logger.getLogger(CheckpointCoordinator.class.getName());

    // 【JUC知识点】: ScheduledExecutorService 用于周期性地触发检查点。
    private final ScheduledExecutorService checkpointTrigger;
    // 【JUC知识点】: ConcurrentHashMap 用于存储每个正在进行的检查点的状态。
    // Key 是 checkpointId，Value 是一个 CountDownLatch，用于等待所有任务完成快照。
    private final ConcurrentMap<Long, CheckpointStatus> pendingCheckpoints;
    private final JobManager jobManager;
    private long lastCheckpointId = 0;

    public CheckpointCoordinator(JobManager jobManager) {
        this.jobManager = jobManager;
        this.checkpointTrigger = Executors.newSingleThreadScheduledExecutor();
        this.pendingCheckpoints = new ConcurrentHashMap<>();
    }

    public void start(long intervalMillis) {
        checkpointTrigger.scheduleAtFixedRate(this::triggerCheckpoint, intervalMillis, intervalMillis, TimeUnit.MILLISECONDS);
        LOG.info("检查点协调器已启动，每 " + intervalMillis + "ms 触发一次检查点。");
    }

    private void triggerCheckpoint() {
        long checkpointId = ++lastCheckpointId;
        int totalTasks = jobManager.getTotalTasks();
        LOG.info(String.format("正在触发检查点 %d，需要 %d 个任务确认。", checkpointId, totalTasks));

        // 【JUC知识点】: CountDownLatch 是完美的工具，用于等待一组异步事件的完成。
        // 这里我们等待所有任务都报告它们完成了对该 checkpointId 的快照。
        CountDownLatch latch = new CountDownLatch(totalTasks);
        CheckpointStatus status = new CheckpointStatus(latch);
        pendingCheckpoints.put(checkpointId, status);

        jobManager.triggerCheckpoint(checkpointId);

        // 异步等待结果
        CompletableFuture.runAsync(() -> {
            try {
                // 设置一个超时时间，防止无限等待
                boolean allAcknowledged = latch.await(10, TimeUnit.SECONDS);
                if (allAcknowledged) {
                    LOG.info("检查点 " + checkpointId + " 已成功完成！所有任务已确认。");
                    LOG.info("完成的状态快照: " + status.getCheckpointState());
                } else {
                    LOG.warning("检查点 " + checkpointId + " 超时！未能收到所有任务的确认。");
                }
            } catch (InterruptedException e) {
                LOG.warning("检查点 " + checkpointId + " 等待过程被中断。");
                Thread.currentThread().interrupt();
            } finally {
                pendingCheckpoints.remove(checkpointId);
            }
        });
    }

    public void acknowledgeCheckpoint(long checkpointId, String taskName, Map<String, Object> stateSnapshot) {
        CheckpointStatus status = pendingCheckpoints.get(checkpointId);
        if (status != null) {
            LOG.info(String.format("收到任务 [%s] 对检查点 %d 的确认。", taskName, checkpointId));
            if (stateSnapshot != null) {
                status.addState(taskName, stateSnapshot);
            }
            status.getLatch().countDown(); // 一个任务完成，latch 减一
        } else {
            LOG.warning(String.format("收到对未知或已过期检查点 %d 的确认，来自任务 [%s]。", checkpointId, taskName));
        }
    }

    public void stop() {
        checkpointTrigger.shutdownNow();
    }

    /**
     * 内部类，用于保存单个检查点的状态
     */
    private static class CheckpointStatus {
        private final CountDownLatch latch;
        private final Map<String, Map<String, Object>> checkpointState = new ConcurrentHashMap<>();

        CheckpointStatus(CountDownLatch latch) {
            this.latch = latch;
        }

        CountDownLatch getLatch() {
            return latch;
        }

        void addState(String taskName, Map<String, Object> state) {
            checkpointState.put(taskName, state);
        }

        Map<String, Map<String, Object>> getCheckpointState() {
            return checkpointState;
        }
    }
}
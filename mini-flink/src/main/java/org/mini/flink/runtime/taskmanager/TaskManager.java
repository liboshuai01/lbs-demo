package org.mini.flink.runtime.taskmanager;

import org.mini.flink.runtime.stream.StreamTask;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.logging.Logger;

/**
 * TaskManager 负责执行 JobManager 分配的任务。
 */
public class TaskManager {
    private static final Logger LOG = Logger.getLogger(TaskManager.class.getName());
    private final int numberOfSlots;

    // 【JUC知识点】: 使用 ThreadPoolExecutor 作为 TaskManager 的核心。
    // 这里的 corePoolSize 和 maximumPoolSize 设置为 numberOfSlots，
    // 完美模拟了 Flink 中 TaskManager 拥有固定数量的 TaskSlot。
    // 每个提交到这个线程池的 Runnable 就是一个 Task。
    private final ExecutorService taskExecutor;

    public TaskManager(int numberOfSlots) {
        this.numberOfSlots = numberOfSlots;
        this.taskExecutor = Executors.newFixedThreadPool(numberOfSlots);
        LOG.info("TaskManager 已启动，拥有 " + numberOfSlots + " 个任务槽。");
    }

    public void submitTask(StreamTask task) {
        LOG.info("TaskManager 接收到任务: " + task.getClass().getSimpleName() + "，并提交到任务槽执行。");
        taskExecutor.submit(task);
    }

    public void shutdown() {
        LOG.info("TaskManager 正在关闭...");
        taskExecutor.shutdownNow();
    }
}
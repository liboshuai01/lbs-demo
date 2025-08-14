package org.mini.flink.runtime.taskmanager;

import org.mini.flink.runtime.stream.StreamTask;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 任务槽，代表 TaskManager 中的一个执行资源。
 * 这里简化为一个单线程的 Executor。
 */
public class TaskSlot {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private Future<?> taskFuture;

    public boolean isAvailable() {
        return taskFuture == null || taskFuture.isDone();
    }

    public void execute(StreamTask task) {
        if (!isAvailable()) {
            throw new IllegalStateException("任务槽已被占用!");
        }
        this.taskFuture = executor.submit(task);
    }

    public void shutdown() {
        executor.shutdownNow();
    }
}
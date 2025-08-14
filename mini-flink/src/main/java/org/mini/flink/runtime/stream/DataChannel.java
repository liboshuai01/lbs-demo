package org.mini.flink.runtime.stream;

import org.mini.flink.runtime.state.CheckpointBarrier;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 数据通道，用于在任务之间传输数据。
 * 【JUC知识点】: 使用 BlockingQueue 模拟 Flink 的网络缓冲和反压机制。
 * 当队列满时，上游的 put 操作会阻塞，实现反压。
 * 当队列空时，下游的 take 操作会阻塞，等待数据。
 * Flink 的真实实现是基于 Netty 和信用机制的复杂网络栈，远比这个复杂。
 */
public class DataChannel {
    private final BlockingQueue<Object> queue;

    public DataChannel(int capacity) {
        this.queue = new LinkedBlockingQueue<>(capacity);
    }

    public void push(Object record) throws InterruptedException {
        queue.put(record); // 队列满时阻塞
    }

    public Object pop() throws InterruptedException {
        return queue.take(); // 队列空时阻塞
    }

    public void broadcastBarrier(CheckpointBarrier barrier) {
        // 简单地清空队列并在队首插入 barrier，实际 Flink 不会清空数据
        queue.clear();
        queue.offer(barrier);
    }
}
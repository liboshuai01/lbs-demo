package cn.liboshuai.demo.mailbox;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class MiniInputGate {

    private final Queue<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    @Getter
    private CompletableFuture<Void> availabilityFuture = new CompletableFuture<>();

    public void pushData(String record) {
        lock.lock();
        try {
            queue.add(record);
            if (!availabilityFuture.isDone()) {
                availabilityFuture.complete(null);
            }
        } finally {
            lock.unlock();
        }

    }

    public String pollNext() {
        lock.lock();
        try {
            String record = queue.poll();
            if (queue.isEmpty() && availabilityFuture.isDone()) {
                availabilityFuture = new CompletableFuture<>();
            }
            return record;
        } finally {
            lock.unlock();
        }
    }
}

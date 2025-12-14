package cn.liboshuai.demo.mailbox;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class MiniInputGate {

    private final Deque<String> queue = new ArrayDeque<>();
    private final ReentrantLock lock = new ReentrantLock();
    private CompletableFuture<Void> availableComputeFuture = new CompletableFuture<>();

    public void pushData(String data) {
        lock.lock();
        try {
            queue.addLast(data);
            if (!availableComputeFuture.isDone()) {
                availableComputeFuture.complete(null);
            }
        } finally {
            lock.unlock();
        }
    }

    public String pollNext() {
        lock.lock();
        try {
            String data = queue.pollFirst();
            if (queue.isEmpty()) {
                if (availableComputeFuture.isDone()) {
                    availableComputeFuture = new CompletableFuture<>();
                }
            }
            return data;
        } finally {
            lock.unlock();
        }
    }

    public CompletableFuture<Void> getAvailableComputeFuture() {
        lock.lock();
        try {
            return this.availableComputeFuture;
        } finally {
            lock.unlock();
        }
    }

}

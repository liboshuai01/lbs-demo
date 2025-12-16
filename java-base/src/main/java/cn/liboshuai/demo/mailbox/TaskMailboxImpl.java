package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class TaskMailboxImpl implements TaskMailbox {

    private final Queue<Mail> queue = new PriorityQueue<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notEmpty = lock.newCondition();
    private final Thread mailboxThread;
    private State state = State.OPEN;

    public TaskMailboxImpl(Thread mailboxThread) {
        this.mailboxThread = mailboxThread;
    }

    @Override
    public boolean hasMail() {
        lock.lock();
        try {
            return !queue.isEmpty();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public Optional<Mail> tryTake(int priority) {
        checkMailboxThread();
        lock.lock();
        try {
            Mail head = queue.peek();
            if (head == null) {
                return Optional.empty();
            }
            if (head.getPriority() > priority) {
                return Optional.empty();
            }
            return Optional.ofNullable(queue.poll());
        } finally {
            lock.unlock();
        }
    }

    private boolean isQueueEmptyAndPriorityTooLow(int priority) {
        Mail head = queue.peek();
        if (head == null) {
            return true;
        }
        return head.getPriority() > priority;
    }

    @Override
    public Mail take(int priority) throws InterruptedException {
        checkMailboxThread();
        lock.lock();
        try {
            while (isQueueEmptyAndPriorityTooLow(priority)) {
                if (state == State.CLOSE) {
                    throw new IllegalStateException("taskMailbox已经关闭了!");
                }
                notEmpty.await();
            }
            return queue.poll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(Mail mail) {
        lock.lock();
        try {
            if (state == State.CLOSE) {
                log.warn("邮件已关闭, 正在丢弃邮件: {}", mail);
                return;
            }
            queue.add(mail);
            notEmpty.signal();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void close() {
        lock.lock();
        try {
            state = State.CLOSE;
            queue.clear();
            notEmpty.signalAll();
        } finally {
            lock.unlock();
        }
    }

    private void checkMailboxThread() {
        Thread currentThread = Thread.currentThread();
        if (currentThread != mailboxThread) {
            throw new IllegalStateException(
                    "非法线程访问" +
                            "预期: " + mailboxThread.getName() +
                            "实际: " + currentThread.getName()
            );
        }
    }
}

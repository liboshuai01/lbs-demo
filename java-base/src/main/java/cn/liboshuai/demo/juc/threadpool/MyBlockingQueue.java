package cn.liboshuai.demo.juc.threadpool;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;


public class MyBlockingQueue<T> {
    private final Deque<T> queue = new ArrayDeque<>();
    private final long capacity;

    private final ReentrantLock lock = new ReentrantLock();

    private final Condition notEmpty = lock.newCondition();
    private final Condition notFull = lock.newCondition();

    public MyBlockingQueue(long capacity) {
        this.capacity = capacity;
    }

    public void put(T t) {

    }

    public T task() {

        return null;
    }

    public boolean offer(T t, long timeout, TimeUnit timeUnit) {

        return false;
    }

    public T poll(long timeout, TimeUnit timeUnit) {

        return null;
    }
}

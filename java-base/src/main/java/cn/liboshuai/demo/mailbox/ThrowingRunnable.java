package cn.liboshuai.demo.mailbox;

@FunctionalInterface
public interface ThrowingRunnable<E extends Throwable> {
    void run() throws E;
}

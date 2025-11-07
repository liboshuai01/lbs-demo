package cn.liboshuai.demo.cf.util;


@FunctionalInterface
public interface RunnableWithException extends ThrowingRunnable<Exception> {

    @Override
    void run() throws Exception;
}

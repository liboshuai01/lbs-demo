package cn.liboshuai.demo.cf.function;


@FunctionalInterface
public interface RunnableWithException extends ThrowingRunnable<Exception> {

    @Override
    void run() throws Exception;
}

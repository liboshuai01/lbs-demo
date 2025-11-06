package cn.liboshuai.demo.cf.function;


@FunctionalInterface
public interface FunctionWithException<T, R, E extends Throwable> {

    R apply(T value) throws E;
}

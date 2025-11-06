package cn.liboshuai.demo.cf.function;


@FunctionalInterface
public interface SupplierWithException<R, E extends Throwable> {

    R get() throws E;
}

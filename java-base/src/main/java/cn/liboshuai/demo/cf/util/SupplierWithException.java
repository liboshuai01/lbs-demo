package cn.liboshuai.demo.cf.util;


@FunctionalInterface
public interface SupplierWithException<R, E extends Throwable> {

    R get() throws E;
}

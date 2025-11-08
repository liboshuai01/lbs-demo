package cn.liboshuai.demo.generic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;

public class BridgeMethod {

    private static final Logger log = LoggerFactory.getLogger(BridgeMethod.class);

    interface Notifier<T> {
        void notify(T data);
    }

    static class StringNotifier implements Notifier<String> {

        @Override
        public void notify(String data) {
            log.info("通信信息: {}", data);
        }
    }

    public static void main(String[] args) {
        StringNotifier stringNotifier = new StringNotifier();

        Class<? extends StringNotifier> stringNotifierClass = stringNotifier.getClass();
        Method[] methods = stringNotifierClass.getDeclaredMethods();
        for (Method m : methods) {
            log.info("方法名: {}", m.getName());
            log.info("  - 参数类型: {}", java.util.Arrays.toString(m.getParameterTypes()));

            // 关键：Method 类提供了一个方法来检查它是否是桥接方法
            log.info("  - 是桥接方法 (isBridge) 吗?  ==> {}", m.isBridge());

            log.info("  - 完整签名: {}", m);
            log.info("--------------------");
        }
    }
}

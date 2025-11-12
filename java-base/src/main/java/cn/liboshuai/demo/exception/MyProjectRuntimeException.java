package cn.liboshuai.demo.exception;

import java.io.Serial;

/**
 * 3. 自定义“非受检”异常的基类 (运行时异常)
 * 类似于 Flink 的 FlinkRuntimeException。
 * 用于表示程序逻辑错误或不应发生的内部状态错误，
 * 调用者不需要强制捕获。
 */
public class MyProjectRuntimeException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    public MyProjectRuntimeException(String message) {
        super(message);
    }

    public MyProjectRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}

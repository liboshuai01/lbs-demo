package cn.liboshuai.demo.mailbox;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Mail {
    private final ThrowingRunnable<? extends Exception> runnable;
    @Getter
    private final int priority;
    private final String description;

    public Mail(ThrowingRunnable<? extends Exception> runnable, int priority, String description) {
        this.runnable = runnable;
        this.priority = priority;
        this.description = description;
    }

    public void run() throws Exception {
        runnable.run();
    }

    @Override
    public String toString() {
        return description;
    }
}

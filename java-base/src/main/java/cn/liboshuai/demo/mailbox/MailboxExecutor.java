package cn.liboshuai.demo.mailbox;

@FunctionalInterface
public interface MailboxExecutor {
    void execute(ThrowingRunnable<? extends Exception> command, String description);
}

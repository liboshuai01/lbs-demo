package cn.liboshuai.demo.mailbox;

public interface MailboxExecutor {
    void execute(ThrowingRunnable<? extends Exception> command, String description);
}

package cn.liboshuai.demo.mailbox;

public class MailboxExecutorImpl implements MailboxExecutor {

    private final TaskMailbox taskMailbox;
    private final int priority;

    public MailboxExecutorImpl(TaskMailbox taskMailbox, int priority) {
        this.taskMailbox = taskMailbox;
        this.priority = priority;
    }

    @Override
    public void execute(ThrowingRunnable<? extends Exception> command, String description) {
        taskMailbox.put(new Mail(command, priority, description));
    }
}

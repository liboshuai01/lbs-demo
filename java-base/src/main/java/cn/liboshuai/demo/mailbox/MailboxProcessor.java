package cn.liboshuai.demo.mailbox;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MailboxProcessor implements MailboxDefaultAction.Controller {

    private static final int MIN_PRIORITY = 0;
    private static final int DEFAULT_PRIORITY = 1;
    private final TaskMailbox mailbox;
    private final MailboxDefaultAction defaultAction;
    @Getter
    private final MailboxExecutor defaultMailboxExecutor;
    @Getter
    private final MailboxExecutor minMailboxExecutor;
    private boolean isAvailableDefaultAction = true;

    public MailboxProcessor(TaskMailbox mailbox, MailboxDefaultAction defaultAction) {
        this.mailbox = mailbox;
        this.defaultAction = defaultAction;
        this.defaultMailboxExecutor = new MailboxExecutorImpl(mailbox, DEFAULT_PRIORITY);
        this.minMailboxExecutor = new MailboxExecutorImpl(mailbox, MIN_PRIORITY);
    }

    public void runMailboxLoop() {
        while (true) {

        }
    }

    @Override
    public void suspendDefaultAction() {
        isAvailableDefaultAction = false;
    }

    public void resumeDefaultAction() {
        minMailboxExecutor.execute(() -> isAvailableDefaultAction = true, "恢复执行默认行为");
    }
}

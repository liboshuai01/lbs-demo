package cn.liboshuai.demo.mailbox;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class MailboxProcessor implements MailboxDefaultAction.Controller {

    private static final int CONTROl_PRIORITY = 0;
    private static final int DEFAULT_PRIORITY = 1;
    private final TaskMailbox mailbox;
    private final MailboxDefaultAction defaultAction;
    @Getter
    private final MailboxExecutor defaultMailboxExecutor;
    @Getter
    private final MailboxExecutor controlMailboxExecutor;
    private boolean isAvailableDefaultAction = true;

    public MailboxProcessor(TaskMailbox mailbox, MailboxDefaultAction defaultAction) {
        this.mailbox = mailbox;
        this.defaultAction = defaultAction;
        this.defaultMailboxExecutor = new MailboxExecutorImpl(mailbox, DEFAULT_PRIORITY);
        this.controlMailboxExecutor = new MailboxExecutorImpl(mailbox, CONTROl_PRIORITY);
    }

    public void runMailboxLoop() throws Exception {
        while (true) {
            while (processMail()) {

            }
            if (isAvailableDefaultAction) {
                defaultAction.runDefaultAction(this);
            } else {
                Mail mail = mailbox.take(DEFAULT_PRIORITY);
                mail.run();
            }
        }
    }

    private boolean processMail() throws Exception {
        Optional<Mail> mail = mailbox.tryTake(CONTROl_PRIORITY);
        if (mail.isPresent()) {
            mail.get().run();
            return true;
        }
        return false;
    }

    @Override
    public void suspendDefaultAction() {
        isAvailableDefaultAction = false;
    }

    public void resumeDefaultAction() {
        controlMailboxExecutor.execute(() -> isAvailableDefaultAction = true, "恢复执行默认行为");
    }
}

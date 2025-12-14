package cn.liboshuai.demo.mailbox;

import java.util.Optional;

public class MailboxProcessor implements MailboxDefaultAction.Controller {

    private final MailboxDefaultAction defaultAction;
    private final TaskMailbox mailbox;
    private final MailboxExecutor mailboxExecutor;
    private boolean isDefaultActionAvailable = true;

    public MailboxProcessor(MailboxDefaultAction defaultAction, TaskMailbox mailbox) {
        this.defaultAction = defaultAction;
        this.mailbox = mailbox;
        this.mailboxExecutor = new MailboxExecutorImpl(mailbox, 0);
    }

    public void runMailboxLoop() throws Exception {
        while (true) {
            while (mailbox.hashMail()) {
                Optional<Mail> mail = mailbox.tryTake(0);
                if (mail.isPresent()) {
                    mail.get().run();
                }
            }
            if (isDefaultActionAvailable) {
                defaultAction.runDefaultAction(this);
            } else {
                Mail mail = mailbox.take(0);
                mail.run();
            }
        }
    }

    @Override
    public void suspendDefaultAction() {
        isDefaultActionAvailable = false;
    }

    public void resumeDefaultAction() {
        mailboxExecutor.execute(() -> isDefaultActionAvailable = true, "恢复数据处理行为");
    }
}

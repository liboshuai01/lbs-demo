package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

@Slf4j
public class MailboxProcessor implements MailboxDefaultAction.Controller {

    private final MailboxExecutor mailboxExecutor;
    private final TaskMailbox mailbox;
    private final MailboxDefaultAction defaultAction;

    private boolean isDefaultActionAvailable = true;

    public MailboxProcessor(TaskMailbox mailbox, MailboxDefaultAction defaultAction) {
        this.mailbox = mailbox;
        this.defaultAction = defaultAction;
        this.mailboxExecutor = new MailboxExecutorImpl(mailbox, 0);
    }

    public void runMailboxLoop() throws Exception {
        while (true) {
            while (mailbox.hasMail()) {
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
        mailboxExecutor.execute(() -> isDefaultActionAvailable = true, "恢复执行默认操作");
    }
}

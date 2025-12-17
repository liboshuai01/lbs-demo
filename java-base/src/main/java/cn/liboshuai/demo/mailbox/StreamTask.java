package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StreamTask implements MailboxDefaultAction {

    private final MailboxProcessor mailboxProcessor;
    private final TaskMailbox mailbox;

    protected StreamTask() {
        this.mailbox = new TaskMailboxImpl(Thread.currentThread());
        this.mailboxProcessor = new MailboxProcessor(mailbox, this);
    }

    public void invoke() throws Exception {
        log.info("[StreamTask] 任务已启动.");
        try {
            mailboxProcessor.runMailboxLoop();
        } finally {
            close();
        }
    }

    private void close() {
        log.info("[StreamTask] 结束.");
        mailbox.close();
    }

    public MailboxExecutor getControlMailboxExecutor() {
        return mailboxProcessor.getControlMailboxExecutor();
    }

    public MailboxExecutor getDefaultMailboxExecutor() {
        return mailboxProcessor.getDefaultMailboxExecutor();
    }

    public abstract void performCheckpoint(long checkpointId);
}

package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StreamTask implements MailboxDefaultAction {
    private final TaskMailbox mailbox;
    private final MailboxProcessor mailboxProcessor;

    protected StreamTask() {
        this.mailbox = new TaskMailboxImpl(Thread.currentThread());
        this.mailboxProcessor = new MailboxProcessor(this, mailbox);
    }

    public void invoke() throws Exception {
        log.info("[StreamTask] 任务已启动, 正在进入邮箱循环...");
        try {
            mailboxProcessor.runMailboxLoop();
        } catch (Exception e) {
            log.error("", e);
        } finally {
            close();
        }
    }

    private void close() {
        log.info("[StreamTask] 任务已完成/结束");
        mailbox.close();
    }

    public MailboxExecutor getControlMailboxExecutor() {
        return new MailboxExecutorImpl(mailbox, 10);
    }
}

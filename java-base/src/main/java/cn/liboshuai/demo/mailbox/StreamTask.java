package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class StreamTask implements MailboxDefaultAction {

    private final TaskMailbox mailbox;
    private final MailboxProcessor mailboxProcessor;


    public StreamTask() {
        this.mailbox = new TaskMailboxImpl(Thread.currentThread());
        this.mailboxProcessor = new MailboxProcessor(mailbox, this);
    }

    public void invoke() throws Exception {
        log.info("[StreamTask] 任务已经启动, 正在进入邮箱循环...");
        try {
            mailboxProcessor.runMailboxLoop();
        } catch (Exception e) {
            log.error("[StreamTask] 主循环异常：" + e.getMessage());
            throw e;
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

    abstract void performCheckpoint(long checkpointId);
}

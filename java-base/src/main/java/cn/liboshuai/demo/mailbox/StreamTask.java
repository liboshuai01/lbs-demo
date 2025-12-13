package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

/**
 * 所有任务的基类
 * 负责组装 MailboxProcessor Executor 并管理生命周期
 */
@Slf4j
public abstract class StreamTask implements MailboxDefaultAction {
    protected final TaskMailbox taskMailbox;
    protected final MailboxProcessor mailboxProcessor;
    protected final MailboxExecutor mainExecutor;

    protected volatile boolean isRunning = true;

    protected StreamTask() {
        // 1. 获取当前线程作为主线程
        Thread currentThread = Thread.currentThread();
        // 2. 初始化邮箱
        this.taskMailbox = new TaskMailboxImpl(currentThread);
        // 3. 初始化处理器 (将 this 作为 DefaultAction 传入)
        this.mailboxProcessor = new MailboxProcessor(this, taskMailbox);
        // 4. 获取主线程 Executor
        this.mainExecutor = mailboxProcessor.getMainExecutor();
    }

    /**
     * 任务执行的主入口
     */
    public final void invoke() throws Exception {
        log.info("[StreamTask] 任务已经启动. 进入 Mailbox 循环...");
        try {
            // 启动主循环, 直到任务取消或完成
            mailboxProcessor.runMailboxLoop();
        } catch (Exception e) {
            log.error("[StreamTask] Mailbox 循环出现异常: {}", e.getMessage());
            throw e;
        } finally {
            close();
        }
    }

    public void cancel() {
        this.isRunning = false;
        taskMailbox.close();
    }

    protected void close() {
        log.info("[StreamTask] 任务 已完成或已关闭.");
        taskMailbox.close();
    }

    /**
     * 获取用于提交 Checkpoint 等控制消息的 Executor (高优先级)
     */
    public MailboxExecutor getControlMailboxExecutor() {
        // 假设优先级 10 用于控制消息
        return new MailboxExecutorImpl(taskMailbox, 10);
    }

}

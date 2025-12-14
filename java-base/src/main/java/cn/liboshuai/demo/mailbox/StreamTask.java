package cn.liboshuai.demo.mailbox;

import lombok.extern.slf4j.Slf4j;

/**
 * 任务基类。
 * 修改点：明确区分 Control Flow (优先级0) 和 Data Flow (优先级1) 的 Executor 配置.
 */
@Slf4j
public abstract class StreamTask implements MailboxDefaultAction {

    protected final TaskMailbox mailbox;
    protected final MailboxProcessor mailboxProcessor;
    protected final MailboxExecutor defaultMailboxExecutor;
    protected final MailboxExecutor minDefaultMailboxExecutor;

    public StreamTask() {
        Thread currentThread = Thread.currentThread();
        this.mailbox = new TaskMailboxImpl(currentThread);
        this.mailboxProcessor = new MailboxProcessor(this, mailbox);
        // 主执行器 (用于 task 内部自提交) 跟随 Processor 的默认优先级 (1)
        this.defaultMailboxExecutor = mailboxProcessor.getDefaultMailboxExecutor();
        // 系统执行器 (用于Checkpoint 等系统任务) 优先级为0
        this.minDefaultMailboxExecutor = mailboxProcessor.getMinDefaultMailboxExecutor();
    }

    /**
     * 任务执行的主入口
     */
    public final void invoke() throws Exception {
        log.info("[StreamTask] 任务已启动。正在进入邮箱循环...");
        try {
            // 启动主循环
            mailboxProcessor.runMailboxLoop();
        } catch (Exception e) {
            log.error("[StreamTask] 主循环异常：" + e.getMessage());
            throw e;
        } finally {
            close();
        }
    }

    private void close() {
        log.info("[StreamTask] 任务已完成/结束。");
        mailbox.close();
    }

    /**
     * 获取用于提交 Checkpoint 等控制消息的 Executor (高优先级)
     * 修改点：使用 MailboxProcessor.MIN_PRIORITY (0)
     */
    public MailboxExecutor getControlMailboxExecutor() {
        return this.minDefaultMailboxExecutor;
    }

    // 子类实现具体的处理逻辑
    @Override
    public abstract void runDefaultAction(Controller controller) throws Exception;

    // 执行 Checkpoint 行为, 由子类实现
    public abstract void performCheckpoint(long checkpointId);
}

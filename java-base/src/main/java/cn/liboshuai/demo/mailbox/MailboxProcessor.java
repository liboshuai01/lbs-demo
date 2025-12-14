package cn.liboshuai.demo.mailbox;

import lombok.Getter;

import java.util.Optional;

/**
 * 邮箱处理器
 * 核心修改: runMailboxLoop 循环逻辑, 利用 priority 参数实现"系统消息抢占".
 */
public class MailboxProcessor implements MailboxDefaultAction.Controller {

    // Flink 风格优先级常量
    // MIN_PRIORITY (0) 用于 Checkpoint/Control 消息
    public static final int MIN_PRIORITY = 0;
    // DEFAULT_PRIORITY (1) 用于 普通数据处理
    public static final int DEFAULT_PRIORITY = 1;

    private final MailboxDefaultAction defaultAction;
    private final TaskMailbox mailbox;

    @Getter
    private final MailboxExecutor defaultMailboxExecutor;
    @Getter
    private final MailboxExecutor minDefaultMailboxExecutor;

    private boolean isDefaultActionAvailable = true;

    public MailboxProcessor(MailboxDefaultAction defaultAction, TaskMailbox mailbox) {
        this.defaultAction = defaultAction;
        this.mailbox = mailbox;
        // 主线程默认执行器, 优先级设置为1 (低)
        this.defaultMailboxExecutor = new MailboxExecutorImpl(mailbox, DEFAULT_PRIORITY);
        // 系统级别执行器, 优先级设置为0 (高)
        this.minDefaultMailboxExecutor = new MailboxExecutorImpl(mailbox, MIN_PRIORITY);
    }

    /**
     * 启动主循环 (The Main Loop)
     * 模仿 Flink 的 MailboxProcessor#runMailboxLoop
     */
    public void runMailboxLoop() throws Exception {
        while (true) {
            // 阶段 1: [关键] 强制排干所有高优先级 (System/Checkpoint) 邮件
            // 调用 tryTake(MIN_PRIORITY=0), 这意味着如果邮箱里只有 DataMail(1),
            // tryTake 会根据 TaskMailboxImpl 的逻辑返回 Empty, 从而跳出这个 while, 去执行 defaultAction.
            // 但如果邮箱里有 Checkpoint(0), 这里会一直循环直到处理完
            while (processMail(mailbox, MIN_PRIORITY)) {
                // 循环内持续处理高优先级邮件
            }
            // 阶段2: 执行默认动作 (处理一条数据)
            if (isDefaultActionAvailable) {
                // 运行一小步数据处理 (processRecord)
                // 这一步结束后, 循环会回到开头, 再次检查是否有新的 Checkpoint 插队
                defaultAction.runDefaultAction(this);
            } else {
                // 阶段3: 如果没数据了 (Suspended)
                // 我们阻塞等待 **任何** 优先级的邮件 (通常是 Resume 信号, 可能是 0 也可能是 1)
                Mail mail = mailbox.take(DEFAULT_PRIORITY);
                mail.run();
            }
        }
    }

    /**
     * 尝试取出一封指定优先级的信并执行
     */
    private boolean processMail(TaskMailbox mailbox, int priority) throws Exception {
        Optional<Mail> mail = mailbox.tryTake(priority);
        if (mail.isPresent()) {
            mail.get().run();
            return true;
        }
        return false;
    }

    @Override
    public void suspendDefaultAction() {
        isDefaultActionAvailable = false;
    }

    public void resumeDefaultAction() {
        minDefaultMailboxExecutor.execute(() -> isDefaultActionAvailable = true,
                "恢复执行默认操作");
    }
}

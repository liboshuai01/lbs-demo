package cn.liboshuai.demo.mailbox;

import lombok.Getter;

import java.util.Optional;

public class MailboxProcessor implements MailboxDefaultAction.Controller{

    private final MailboxDefaultAction defaultAction;
    private final TaskMailbox mailbox;

    @Getter
    private final MailboxExecutor mainExecutor;

    // 标记默认动作是否可用 (是否可以执行 processInput)
    private boolean isDefaultActionAvailable = true;

    public MailboxProcessor(MailboxDefaultAction defaultAction, TaskMailbox mailbox) {
        this.defaultAction = defaultAction;
        this.mailbox = mailbox;
        mainExecutor = new MailboxExecutorImpl(mailbox, 0);
    }

    /**
     * 启动主循环 (The Mail Loop)
     */
    public void runMailboxLoop() throws Exception {
        while (true) {
            // 阶段1 : 处理所有积压的邮件 (系统事件优先)
            // 我们使用 tryTake 非阻塞地把邮箱清空
            while (mailbox.hasMail()) {
                Optional<Mail> mail = mailbox.tryTake(0);
                if (mail.isPresent()) {
                    mail.get().run();
                }
            }
            // 阶段2: 执行默认动作 (数据处理)
            if (isDefaultActionAvailable) {
                // 执行一小步数据处理
                defaultAction.runDefaultAction(this);
            } else {
                // 阶段3: 如果没事干 (DefaultAction 被挂起), 就阻塞等待新邮件
                // take() 会让线程睡着, 直到有外部线程 put()
                Mail mail = mailbox.take(0);
                mail.run();
            }
        }
    }

    /**
     * 只会在主线程中被调用, 所以不需要加锁, 也不需要丢到taskMailbox中
     */
    @Override
    public void suspendDefaultAction() {
        // 收到暂停请求
        this.isDefaultActionAvailable = false;
    }

    /**
     * 这是一个用于恢复默认动作的辅助方法.
     * 外部线程 (如 Netty) 调用此方法来"唤醒"主线程.
     * 因为这个方法会被外部线程调用, 为了避免加锁的性能问题, 所以需要丢到taskMailbox中
     */
    public void resumeDefaultAction() {
        mainExecutor.execute(() -> {
            this.isDefaultActionAvailable = true;
        }, "恢复 Default Action");
    }
}

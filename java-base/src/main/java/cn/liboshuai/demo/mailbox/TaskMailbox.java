package cn.liboshuai.demo.mailbox;

import java.util.Optional;

public interface TaskMailbox {

    /**
     * 邮箱中是否包含邮件
     */
    boolean hasMail();

    /**
     * 非阻塞式获取邮件 (如果没有则返回 Empty)
     */
    Optional<Mail> tryTake(int priority);

    /**
     * 阻塞式获取邮件 (如果为空则等待, 指导有邮件或邮箱关闭)
     */
    Mail take(int priority) throws InterruptedException;

    /**
     * 放入邮件 (任何线程都可调用)
     */
    void put(Mail mail);

    /**
     * 关闭邮箱, 不再接收新邮件, 并唤醒所有等待线程
     */
    void close();

    /**
     * 邮箱状态
     */
    enum State {
        OPEN,
        QUIESCED, // 暂停处理
        CLOSED // 彻底关闭
    }

}

package cn.liboshuai.demo.mailbox;

import java.util.Optional;

public interface TaskMailbox {
    boolean hashMail();
    void put(Mail mail);
    Optional<Mail> tryTake(int priority);
    Mail take(int priority) throws InterruptedException;
    void close();

    enum State {
        OPEN,
        QUIESCED,
        CLOSE
    }
}

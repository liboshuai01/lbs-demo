package cn.liboshuai.demo.mailbox;

import java.util.Optional;

public interface TaskMailbox {
    boolean hasMail();
    Optional<Mail> tryTake(int priority) throws InterruptedException;
    Mail take(int priority) throws InterruptedException;
    void put(Mail mail);
    void close();
    enum State {
        OPEN,
        QUIESCED,
        CLOSE
    }
}

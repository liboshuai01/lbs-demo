package cn.liboshuai.demo.mailbox;

public interface MailboxDefaultAction {

    void runDefaultAction(Controller controller);

    interface Controller {
        void suspendDefaultAction();
    }

}

package cn.liboshuai.demo.mailbox;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NettyDataProducer extends Thread {

    private final MiniInputGate inputGate;

    private volatile boolean running = true;

    public NettyDataProducer(MiniInputGate inputGate) {
        super("Netty-Thread");
        this.inputGate = inputGate;
    }

    @Override
    public void run() {
        long recordCount = 0;
        Random random = new Random();
        while (running) {
            int sleep = random.nextInt(10) < 5 ? 200 : 5;
            try {
                TimeUnit.MILLISECONDS.sleep(sleep);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            inputGate.pushData("record-" + (++recordCount));
        }
    }

    public void shutdown() {
        running = false;
        this.interrupt();
    }


}

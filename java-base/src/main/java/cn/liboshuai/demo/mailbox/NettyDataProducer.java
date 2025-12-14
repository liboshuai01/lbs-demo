package cn.liboshuai.demo.mailbox;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class NettyDataProducer extends Thread {

    private final MiniInputGate inputGate;

    private volatile boolean running = true;

    public NettyDataProducer(MiniInputGate inputGate) {
        this.inputGate = inputGate;
    }

    @Override
    public void run() {
        long count = 0;
        Random random = new Random();
        while (running) {
            String data = "Record-" + (++count);
            inputGate.pushData(data);
            int sleepTime = random.nextInt(10) < 5 ? 2000 : 10;
            try {
                TimeUnit.MILLISECONDS.sleep(sleepTime);
            } catch (InterruptedException e) {
                break;
            }
        }
    }

    public void shutdown() {
        this.running = false;
        this.interrupt();
    }
}

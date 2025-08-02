package com.liboshuai.demo;

import org.apache.pekko.actor.typed.ActorRef;
import org.apache.pekko.actor.typed.ActorSystem;
import org.apache.pekko.actor.typed.Behavior;
import org.apache.pekko.actor.typed.javadsl.AbstractBehavior;
import org.apache.pekko.actor.typed.javadsl.ActorContext;
import org.apache.pekko.actor.typed.javadsl.Behaviors;
import org.apache.pekko.actor.typed.javadsl.Receive;

// --- 消息定义 (必须是不可变的) ---
interface Command {}
final class Ping implements Command {
    public final ActorRef<Pong> replyTo;
    public Ping(ActorRef<Pong> replyTo) {
        this.replyTo = replyTo;
    }
}
final class Pong implements Command {
    // Pong 消息本身不带数据，只是一个信号
}


// --- PongActor 的定义 ---
class PongActor extends AbstractBehavior<Ping> {

    // 工厂方法，用于创建 Behavior
    public static Behavior<Ping> create() {
        return Behaviors.setup(PongActor::new);
    }

    private PongActor(ActorContext<Ping> context) {
        super(context);
    }

    // 定义如何处理接收到的消息
    @Override
    public Receive<Ping> createReceive() {
        return newReceiveBuilder()
                .onMessage(Ping.class, this::onPing)
                .build();
    }

    private Behavior<Ping> onPing(Ping message) {
        getContext().getLog().info("Received Ping!");
        // 回复一个 Pong 消息
        message.replyTo.tell(new Pong());
        return this; // 保持当前行为，继续接收下一条消息
    }
}


// --- PingActor 的定义 ---
class PingActor extends AbstractBehavior<Pong> {

    private final ActorRef<Ping> ponger;
    private int counter = 0;

    public static Behavior<Pong> create(ActorRef<Ping> ponger) {
        return Behaviors.setup(context -> new PingActor(context, ponger));
    }

    private PingActor(ActorContext<Pong> context, ActorRef<Ping> ponger) {
        super(context);
        this.ponger = ponger;
    }

    @Override
    public Receive<Pong> createReceive() {
        return newReceiveBuilder()
                .onMessage(Pong.class, this::onPong)
                .build();
    }

    private Behavior<Pong> onPong(Pong message) {
        counter++;
        getContext().getLog().info("Received Pong {}!", counter);
        if (counter == 3) {
            // 收到3次 Pong 后，停止自己
            getContext().getLog().info("Stopping...");
            return Behaviors.stopped();
        } else {
            // 继续发送 Ping
            ponger.tell(new Ping(getContext().getSelf()));
            return this;
        }
    }
}


// --- 主程序，启动 ActorSystem 和 Actor ---
public class PekkoPingPongExample {
    public static void main(String[] args) {
        // 1. 创建 ActorSystem，这是所有 Actor 的家
        // 使用一个 Guardian Actor 作为所有用户 Actor 的根
        ActorSystem<Command> system = ActorSystem.create(
                Behaviors.setup(context -> {
                    // 2. 在 System 中创建 PongActor
                    ActorRef<Ping> ponger = context.spawn(PongActor.create(), "ponger");

                    // 3. 创建 PingActor，并告诉它 PongActor 的地址
                    ActorRef<Pong> pinger = context.spawn(PingActor.create(ponger), "pinger");

                    // 4. 发送第一条消息，启动整个流程
                    // 注意这里的消息是发给 ponger 的，但是 replyTo 是 pinger 的地址
                    ponger.tell(new Ping(pinger));

                    return Behaviors.empty();
                }), "PingPongSystem"
        );
    }
}
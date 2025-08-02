// src/main/java/com/liboshuai/demo/ClientActor.java

package com.liboshuai.demo;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(ClientActor.class);

    private final String remotePath;
    private final ActorSelection remoteGreeter;

    // 构造函数，接收远程 Actor 的路径
    public ClientActor(String remotePath) {
        this.remotePath = remotePath;
        this.remoteGreeter = getContext().actorSelection(remotePath);
    }

    public static Props props(String remotePath) {
        return Props.create(ClientActor.class, () -> new ClientActor(remotePath));
    }

    // 在 Actor 启动时发送消息
    @Override
    public void preStart() {
        log.info("正在向远程 Actor 发送消息，地址：{}", remotePath);
        remoteGreeter.tell(new Greet("Pekko User"), getSelf());
    }

    @Override
    public Receive createReceive() {
        // 这个 Actor 目前不处理任何返回消息，但可以扩展
        return receiveBuilder()
                .matchAny(o -> log.warn("ClientActor 收到未知消息: {}", o))
                .build();
    }
}

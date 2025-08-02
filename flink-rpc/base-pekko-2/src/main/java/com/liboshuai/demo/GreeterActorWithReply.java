// src/main/java/com/liboshuai/demo/ask/GreeterActorWithReply.java
package com.liboshuai.demo;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreeterActorWithReply extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(GreeterActorWithReply.class);

    public static Props props() {
        return Props.create(GreeterActorWithReply.class, GreeterActorWithReply::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(AskForGreeting.class, request -> {
                    // 记录收到的请求
                    log.info("从发送者 {} 收到了问候请求：'{}'", getSender(), request.name); // Received a greeting request for '{}' from sender: {}

                    // 准备回复消息
                    String replyMessage = String.format("你好，%s！这是来自 GreeterActor 的回复。", request.name); // Hello, %s! This is a reply from the GreeterActor.
                    GreetingReply reply = new GreetingReply(replyMessage);

                    // **核心：将回复消息发送给原始的发送者**
                    getSender().tell(reply, getSelf());
                })
                .matchAny(o -> log.warn("收到了未知类型的消息：{}", o.getClass().getName())) // Received unknown message of type: {}
                .build();
    }
}

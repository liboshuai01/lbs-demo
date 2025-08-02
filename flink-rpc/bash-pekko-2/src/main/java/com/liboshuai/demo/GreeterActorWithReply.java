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
                    log.info("Received a greeting request for '{}' from sender: {}", request.name, getSender());

                    // 准备回复消息
                    String replyMessage = String.format("Hello, %s! This is a reply from the GreeterActor.", request.name);
                    GreetingReply reply = new GreetingReply(replyMessage);

                    // **核心：将回复消息发送给原始的发送者**
                    getSender().tell(reply, getSelf());
                })
                .matchAny(o -> log.warn("Received unknown message of type: {}", o.getClass().getName()))
                .build();
    }
}

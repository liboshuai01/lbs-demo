// src/main/java/com/liboshuai/demo/GreeterActor.java

package com.liboshuai.demo;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GreeterActor extends AbstractActor {

    private static final Logger log = LoggerFactory.getLogger(GreeterActor.class);

    public static Props props() {
        return Props.create(GreeterActor.class, GreeterActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greet.class, greet -> {
                    // 使用日志记录，可以包含更多上下文信息
                    log.info("Hello, {}! (from sender: {})", greet.name, getSender());
                })
                .matchAny(o -> log.warn("Received unknown message of type: {}", o.getClass().getName()))
                .build();
    }
}

package com.liboshuai.demo;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

public class GreeterActor extends AbstractActor {

    // Props is a configuration object used to create an Actor. It's good practice.
    public static Props props() {
        return Props.create(GreeterActor.class, GreeterActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Greet.class, greet -> {
                    System.out.printf("Hello, %s! (from %s)%n", greet.name, getSelf());
                })
                .matchAny(o -> System.out.println("Received unknown message"))
                .build();
    }
}

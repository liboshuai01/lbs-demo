package com.liboshuai.demo;

import org.apache.pekko.actor.AbstractActor;
import org.apache.pekko.actor.Props;

public class CalculatorActor extends AbstractActor {

    public static Props props() {
        return Props.create(CalculatorActor.class, CalculatorActor::new);
    }

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Add.class, add -> {
                    int result = add.a + add.b;
                    System.out.printf("CalculatorActor: Received Add(%d, %d), sending back result %d%n", add.a, add.b, result);
                    // 将结果发送回请求方
                    // getSender() 是对此次 ask 操作的临时 Actor 的引用
                    getSender().tell(new CalculationResult(result), getSelf());
                })
                .match(Subtract.class, sub -> {
                    int result = sub.a - sub.b;
                    System.out.printf("CalculatorActor: Received Subtract(%d, %d), sending back result %d%n", sub.a, sub.b, result);
                    // 将结果发送回请求方
                    getSender().tell(new CalculationResult(result), getSelf());
                })
                .build();
    }
}

// src/main/java/com/liboshuai/demo/ask/RemoteSystemMainWithAsk.java
package com.liboshuai.demo;

import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteSystemMainWithAsk {

    private static final Logger log = LoggerFactory.getLogger(RemoteSystemMainWithAsk.class);

    public static void main(String[] args) {
        // 使用 application.conf 配置启动远程系统
        final ActorSystem system = ActorSystem.create("RemoteSystem", ConfigFactory.load());
        log.info("RemoteSystem for 'ask' demo is ready.");

        // 创建一个可被远程访问的、能够回复消息的 Actor
        // 我们给它一个新名字 "greeterWithReply" 以区别于旧的 Actor
        final ActorRef remoteGreeter = system.actorOf(GreeterActorWithReply.props(), "greeterWithReply");

        log.info("Remote Greeter actor with reply created. Full path: {}", remoteGreeter.path());

        log.info(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}

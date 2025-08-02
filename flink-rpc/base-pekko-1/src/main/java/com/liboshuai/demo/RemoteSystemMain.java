// src/main/java/com/liboshuai/demo/RemoteSystemMain.java

package com.liboshuai.demo;

import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RemoteSystemMain {

    private static final Logger log = LoggerFactory.getLogger(RemoteSystemMain.class);

    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("RemoteSystem", ConfigFactory.load());
        log.info("RemoteSystem is ready.");

        // 创建一个可被远程访问的 Actor
        final ActorRef remoteGreeter = system.actorOf(GreeterActor.props(), "remoteGreeter");

        log.info("Remote Greeter actor created. Full path: {}", remoteGreeter.path());

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

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
        log.info("远程系统已就绪。");

        // 创建一个可被远程访问的 Actor
        final ActorRef remoteGreeter = system.actorOf(GreeterActor.props(), "remoteGreeter");

        log.info("远程 Greeter actor 已创建。完整路径: {}", remoteGreeter.path());

        log.info(">>> 按回车键退出 <<<");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}

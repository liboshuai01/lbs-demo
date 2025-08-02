package com.liboshuai.demo;

import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorSystem;

public class RemoteSystemMain {
    public static void main(String[] args) {
        // 加载 application.conf
        // 创建 ActorSystem，名字为 "RemoteSystem"
        final ActorSystem system = ActorSystem.create("RemoteSystem", ConfigFactory.load());
        System.out.println("RemoteSystem is ready.");

        // 创建一个可被远程访问的 Actor
        // 名字 "remoteGreeter" 很重要，客户端将通过这个名字找到它
        system.actorOf(GreeterActor.props(), "remoteGreeter");

        System.out.println("Remote Greeter actor created. Full path: " +
                "pekko://RemoteSystem@127.0.0.1:25520/user/remoteGreeter");

        System.out.println(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            system.terminate();
        }
    }
}

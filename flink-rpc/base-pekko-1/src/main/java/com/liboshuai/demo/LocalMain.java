package com.liboshuai.demo;

import org.apache.pekko.actor.ActorRef;
import org.apache.pekko.actor.ActorSystem;

public class LocalMain {
    public static void main(String[] args) {
        // 1. 创建 ActorSystem
        //    ActorSystem 是重量级对象，整个应用通常只需要一个。
        //    "MyLocalSystem" 是 ActorSystem 的名字。
        final ActorSystem system = ActorSystem.create("MyLocalSystem");
        System.out.println("ActorSystem created.");

        try {
            // 2. 创建 Actor, 得到 ActorRef
            //    "greeter" 是这个 Actor 实例在系统中的名字。
            final ActorRef greeterActor = system.actorOf(GreeterActor.props(), "greeter");
            System.out.println("GreeterActor created: " + greeterActor.path());

            // 3. 发送消息 (Tell, fire-and-forget)
            //    Actor 模型的核心是异步消息传递。
            greeterActor.tell(new Greet("Pekko"), ActorRef.noSender());
            greeterActor.tell(new Greet("World"), ActorRef.noSender());

            // 等待一会，让 Actor 有时间处理消息
            System.out.println(">>> Press ENTER to exit <<<");
            System.in.read();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 4. 关闭 ActorSystem
            //    这会停止所有 Actor 并释放资源。
            system.terminate();
            System.out.println("ActorSystem terminated.");
        }
    }
}

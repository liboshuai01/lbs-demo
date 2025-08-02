package com.liboshuai.demo;

import org.apache.pekko.actor.ActorSelection;
import org.apache.pekko.actor.ActorSystem;

public class ClientSystemMain {
    public static void main(String[] args) {
        // 客户端也需要一个 ActorSystem，但它不需要监听特定端口 (port=0
        // 会让系统随机选择一个可用端口)
        final ActorSystem system = ActorSystem.create("ClientSystem");
        System.out.println("ClientSystem started.");

        // 定义远程 Actor 的完整路径
        // 格式: pekko://<ActorSystemName>@<hostname>:<port>/user/<ActorName>
        final String remotePath = "pekko://RemoteSystem@127.0.0.1:25520/user/remoteGreeter";

        // 使用 ActorSelection 来获取远程 Actor 的引用
        // ActorSelection 是一个“惰性”引用，发送消息时才会去解析地址
        final ActorSelection remoteGreeter = system.actorSelection(remotePath);
        System.out.println("Sending message to remote actor at " + remotePath);

        // 发送消息到远程 Actor
        remoteGreeter.tell(new Greet("Remote User"), null);

        // 短暂等待后关闭客户端系统
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            system.terminate();
            System.out.println("ClientSystem terminated.");
        }
    }
}

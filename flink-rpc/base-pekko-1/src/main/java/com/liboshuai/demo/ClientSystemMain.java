// src/main/java/com/liboshuai/demo/ClientSystemMain.java

package com.liboshuai.demo;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.pekko.actor.ActorSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientSystemMain {

    private static final Logger log = LoggerFactory.getLogger(ClientSystemMain.class);

    public static void main(String[] args) {
        // 创建一个自定义配置，覆盖 application.conf 中的端口设置
        // 将端口设置为 0，让 Pekko 随机选择一个可用端口
        Config config = ConfigFactory.parseString(
                "pekko.remote.artery.canonical.port = 25521"
        ).withFallback(ConfigFactory.load());

        // 使用自定义配置创建客户端 ActorSystem
        final ActorSystem system = ActorSystem.create("ClientSystem", config);
        log.info("ClientSystem started.");

        // 定义远程 Actor 的完整路径
        final String remotePath = "pekko://RemoteSystem@127.0.0.1:25520/user/remoteGreeter";

        // 创建一个 ClientActor 来管理与远程 Actor 的交互
        system.actorOf(ClientActor.props(remotePath), "clientActor");

        log.info(">>> Press ENTER to exit <<<");
        try {
            System.in.read();
        } catch (Exception e) {
            // 在实际应用中，这里也应该使用日志
            e.printStackTrace();
        } finally {
            system.terminate();
            log.info("ClientSystem terminated.");
        }
    }
}

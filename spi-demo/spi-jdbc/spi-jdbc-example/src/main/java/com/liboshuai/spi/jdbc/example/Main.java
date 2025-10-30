package com.liboshuai.spi.jdbc.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;


public class Main {
    public static void main(String[] args) {
        System.out.println("--- 模拟JDBC SPI机制 ---");

        // 1. 定义我们的 LBS-JDBC URL
        // DriverManager 会用这个URL去询问所有通过SPI加载的驱动
        String url = "jdbc:lbs://localhost:1234/fakedb";

        // 2. (可选) 定义连接属性
        Properties info = new Properties();
        info.put("user", "lbs-user");
        info.put("password", "lbs-pass");

        // 3. 关键步骤：获取连接
        // 注意：我们在这里 *没有* 导入或引用 com.liboshuai.demo.jdbc.LbsDriver
        // 这就是SPI的解耦！
        // 我们只面向JDK标准的 java.sql.DriverManager 编程
        try (Connection connection = DriverManager.getConnection(url, info)) {

            // 如果 connection 不是 null，说明 DriverManager 成功
            // 找到了我们的 LbsDriver (通过SPI)，
            // LbsDriver.acceptsURL() 返回了 true，
            // LbsDriver.connect() 成功返回了 LbsConnection

            System.out.println("Main: 成功获取到连接！");
            System.out.println("Main: Connection class: " + connection.getClass().getName());

            // 4. 使用连接 (这里会调用 LbsConnection.isClosed())
            System.out.println("Main: Connection is closed? " + connection.isClosed());

            // 5. try-with-resources 会自动调用 LbsConnection.close()

        } catch (SQLException e) {
            throw new RuntimeException("Main: 获取连接失败！");
        }

        System.out.println("--- 演示结束 ---");
    }
}
package com.liboshuai.spi.jdbc.core;

import java.sql.*;
import java.util.Properties;
import java.util.logging.Logger;

public class LbsJdbc implements Driver {
    private static final String URL_PREFIX = "jdbc:lbs:";

    // 静态代码块：在旧的JDBC中，需要在这里注册驱动
    // static {
    //     try {
    //         DriverManager.registerDriver(new LbsDriver());
    //     } catch (SQLException e) {
    //         throw new RuntimeException("Can't register driver!");
    //     }
    // }
    // 在支持SPI的现代JDBC中，这个静态块是不必要的。
    // DriverManager会通过SPI自动加载我们。

    /**
     * 当 DriverManager 拿到一个URL时，它会询问所有已注册的驱动："你能处理这个URL吗？"
     * @param url "jdbc:lbs:..."
     */
    @Override
    public boolean acceptsURL(String url) throws SQLException {
        if (url == null) {
            return false;
        }
        // 如果URL以前缀 "jdbc:lbs:" 开头，我们就说"我能行！"
        return url.startsWith(URL_PREFIX);
    }

    /**
     * 如果 acceptsURL 返回 true，DriverManager 就会调用这个方法。
     */
    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (!acceptsURL(url)) {
            // 理论上，如果acceptsURL返回false，DM不应该调用它
            // 但作为安全检查，我们还是加上
            return null;
        }

        System.out.println("LbsDriver: 正在尝试连接到数据库...");
        System.out.println("LbsDriver: URL = " + url);
        System.out.println("LbsDriver: Properties = " + info);

        // 在这里，你可以解析URL和info，进行真实的Socket连接等
        // ...

        // 作为演示，我们只返回一个模拟的Connection
        System.out.println("LbsDriver: 连接成功！返回 LbsConnection。");
        return new LbsConnection(url);
    }


    // --- 以下是 Driver 接口的其他方法，作为练习，我们只做最小化实现 ---

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        return new DriverPropertyInfo[0];
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        // 我们当然不是完全兼容JDBC的 ;)
        return false;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new SQLFeatureNotSupportedException("我们不支持 getParentLogger");
    }
}

package cn.liboshuai.demo.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

/**
 * 5. 模拟一个服务，演示如何“抛出”和“处理”我们定义的异常
 */
public class MyServiceSimulator {

    private static final Logger log = LoggerFactory.getLogger(MyServiceSimulator.class);

    private boolean initialized = false;
    private final Properties config = new Properties();

    /**
     * 演示：抛出“受检”异常 (InvalidConfigurationException)
     * <p>
     * 注意：方法签名中必须使用 `throws InvalidConfigurationException` 来声明。
     *
     * @param configPath 配置文件路径
     */
    public void loadConfig(String configPath) throws InvalidConfigurationException {
        if (configPath == null || configPath.isEmpty()) {
            // 抛出一个新的、特定的异常
            throw new InvalidConfigurationException("Config path cannot be null or empty.");
        }

        try (FileInputStream fis = new FileInputStream(configPath)) {
            config.load(fis);

            // 假设我们要求配置中必须有 "server.port"
            if (config.getProperty("server.port") == null) {
                throw new InvalidConfigurationException("Configuration missing required key: 'server.port'");
            }

            // 模拟另一个解析错误
            Integer.parseInt(config.getProperty("server.port"));

            this.initialized = true;

        } catch (FileNotFoundException e) {
            // 关键：捕获底层 IO 异常，并将其“包装”为我们自定义的“受检”异常。
            // 我们提供了更清晰的上下文信息，并保留了原始异常 e。
            throw new InvalidConfigurationException("Config file not found at: " + configPath, e);
        } catch (NumberFormatException e) {
            // 关键：捕获另一个解析异常，并“包装”它
            throw new InvalidConfigurationException("Invalid port number: " + config.getProperty("server.port"), e);
        } catch (IOException e) {
            // 捕获通用的 IO 异常
            throw new InvalidConfigurationException("Failed to read config file: " + configPath, e);
        }
    }

    /**
     * 演示：抛出“非受检”异常 (InvalidOperationException)
     * <p>
     * 这是一个运行时异常，表示程序状态不正确（编程错误）。
     * 方法签名中“不需要”声明 `throws`。
     */
    public void run() {
        if (!initialized) {
            // 类似于 Flink 源码中的 RpcInvalidStateException
            throw new InvalidOperationException("Service is not initialized. Cannot run(). Did you forget to call loadConfig()?");
        }
        log.info("Service running with port: " + config.getProperty("server.port"));
    }

    /**
     * 6. Main 方法，演示如何“捕获”和“处理”异常
     */
    public static void main(String[] args) {
        MyServiceSimulator service = new MyServiceSimulator();

        // --- 场景 1: 处理“受检”异常 ---
        // 因为 loadConfig() 声明了 throws InvalidConfigurationException (继承自 MyProjectException),
        // 编译器强制我们必须在这里处理它。
        log.info("--- 尝试加载配置 (场景 1: 捕获受检异常) ---");
        try {
            // 模拟一个不存在的文件
            service.loadConfig("nonexistent-file.properties");

            // 如果 loadConfig 成功，我们才运行
            service.run();

        } catch (InvalidConfigurationException e) {
            // 捕获我们的配置异常，打印完整的堆栈，你会看到 "Caused by: java.io.FileNotFoundException..."
            log.error("配置失败 (Specific Catch): ", e);
        }

        log.info("\n--- 尝试运行服务 (场景 2: 捕获非受检异常) ---");

        // --- 场景 2: 处理“非受检”异常 ---
        // `run()` 方法可能会抛出 InvalidOperationException，它是一个 RuntimeException。
        // 编译器不强制我们捕获它，但如果我们预料到可能发生（如此处），
        // 我们可以（也应该）捕获它以防止程序崩溃。

        // 注意：因为场景 1 失败了，`service.initialized` 仍然是 false
        try {
            service.run();
        } catch (InvalidOperationException e) {
            log.error("操作失败 (Unchecked Catch): ", e);
        } catch (MyProjectRuntimeException e) {
            // 也可以捕获非受检的基类
            log.error("项目通用运行时异常: ", e);
        }
    }
}

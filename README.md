# lbs-demo: Java 技术栈演示集合

本项目是一个包含了多个独立模块的集合，旨在演示如何在 Java 和 Spring Boot 环境中集成和使用各种流行的后端技术和框架。每个子目录都是一个功能齐全、可独立运行的 Maven 项目。

## 项目结构

- **`flink-wordcount`**: 使用 Apache Flink 实现的经典单词计数批处理任务。
- **`maven-multi-env`**: 演示如何使用 Maven Profiles 和 Spring Profiles 为 Spring Boot 应用配置多套不同的环境（如 dev, uat, prod）。
- **`springboot-kafka`**: 整合 Spring Boot 与 Apache Kafka，实现了消息的生产和消费。
- **`springboot-mongodb`**: 整合 Spring Boot 与 MongoDB，并提供了在不同部署模式（单机、副本集、分片集群）下的配置示例。
- **`springboot-mybatisplus`**: 整合 Spring Boot 与 Mybatis-Plus，展示了如何使用这个强大的 ORM 框架简化数据库操作。
- **`springboot-mybatisplus-dynamic`**: 在 Mybatis-Plus 的基础上，实现了动态数据源的切换功能。
- **`springboot-redis`**: 整合 Spring Boot 与 Redis，并提供了在不同部署模式（单机、哨兵、集群）下的配置示例。
- **`springboot-sharding`**: 整合 Spring Boot 与 ShardingSphere-JDBC，演示了数据库分库分表的实现。

## 环境要求

在开始之前，请确保您已安装好以下软件：

- JDK 1.8+
- Maven 3.5+
- (可选) Docker，用于快速部署 MySQL, Redis, MongoDB, Kafka 等中间件。

## 快速开始

1.  **克隆项目**
    ```bash
    git clone <your-repository-url>
    cd lbs-demo
    ```

2.  **构建整个项目**
    在项目根目录下执行以下命令，编译并打包所有模块：
    ```bash
    mvn clean install
    ```

3.  **运行指定模块**
    进入您想运行的模块目录，然后使用 `spring-boot:run` 命令启动。例如，启动 `springboot-kafka` 模块：
    ```bash
    cd springboot-kafka
    mvn spring-boot:run
    ```
    每个模块的具体配置（如数据库连接、Kafka地址等）请参考其 `src/main/resources` 下的 `application.yml` 或 `application.yaml` 文件。

## 许可证

本项目遵循 [LICENSE](LICENSE) 文件中的许可协议。

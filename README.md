## 模块概览

本项目包含以下主要模块：

1.  **`maven-multi-env`**
    *   **说明:** Spring Boot 项目使用 Maven 进行多环境（如开发、测试、生产）配置的示例。演示如何通过 Maven Profile 和 Spring Boot 的 `application.properties`/`application.yml` 文件来实现不同环境的配置切换。

2.  **`springboot-kafka`**
    *   **说明:** Spring Boot 整合 Apache Kafka 的示例。演示了 Spring Boot 如何作为 Kafka 的生产者（Producer）发送消息和作为消费者（Consumer）接收消息。

3.  **`springboot-redis`**
    *   **说明:** Spring Boot 整合 Redis 的示例。涵盖了连接和操作 Redis 单体 (Standalone)、哨兵 (Sentinel) 和集群 (Cluster) 模式。展示了 Spring Data Redis 的常见用法。

## 技术栈

*   后端框架: Spring Boot
*   构建工具: Maven
*   消息队列: Apache Kafka
*   缓存/数据结构存储: Redis (Standalone, Sentinel, Cluster)
*   编程语言: Java

## 环境要求

*   JDK 8 或更高版本
*   Maven 3.x 或更高版本
*   根据需要运行 Kafka 和 ZooKeeper 服务 (用于 `springboot-kafka` 模块)
*   根据需要运行 Redis 服务 (单体、哨兵、集群) (用于 `springboot-redis` 模块)

## 构建项目

在项目根目录 (`lbs-demo`) 执行以下 Maven 命令：

```bash
mvn clean install -Dmaven.test.skip=true
```
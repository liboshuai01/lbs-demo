package com.liboshuai.demo.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.Consumer;
import org.springframework.kafka.listener.KafkaListenerErrorHandler;
import org.springframework.kafka.listener.ListenerExecutionFailedException;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaConsumerExceptionHandler")
public class KafkaConsumerExceptionHandler implements KafkaListenerErrorHandler {

    /**
     * 处理错误，不带 Consumer 对象
     */
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException e) {
        log.error("kafka消费消息时发生错误。消息内容: {}, 错误信息: {}", message, e.getMessage(), e);
        // 可以根据需要选择是否抛出异常
        // 例如：return null; 表示忽略错误
        // throw e;    // 抛出异常以触发重试机制
        return null;
    }

    /**
     * 处理错误，带 Consumer 对象
     */
    @Override
    public Object handleError(Message<?> message, ListenerExecutionFailedException exception, Consumer<?, ?> consumer) {
        log.error("kafka消费消息时发生错误。消息内容: {}, 错误信息: {}", message, exception.getMessage(), exception);
        // 可以根据需要选择处理方式，例如手动提交偏移量，或其他操作
        // 这里仅记录日志并返回 null
        return null;
    }
}

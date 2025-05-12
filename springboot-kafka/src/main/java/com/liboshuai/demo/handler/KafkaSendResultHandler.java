package com.liboshuai.demo.handler;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.support.ProducerListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component("kafkaSendResultHandler")
public class KafkaSendResultHandler implements ProducerListener<String, Object> {
    @Override
    public void onSuccess(ProducerRecord<String, Object> producerRecord, RecordMetadata recordMetadata) {
        // 记录成功发送的消息信息
        if (recordMetadata != null) {
            log.info("Kafka消息发送成功 - 主题: {}, 分区: {}, 偏移量: {}, 键: {}, 值: {}",
                    producerRecord.topic(),
                    recordMetadata.partition(),
                    recordMetadata.offset(),
                    producerRecord.key(),
                    producerRecord.value());
        } else {
            log.warn("Kafka消息发送成功，但RecordMetadata为null - 键: {}, 值: {}",
                    producerRecord.key(),
                    producerRecord.value());
        }
    }

    @Override
    public void onError(ProducerRecord<String, Object> producerRecord, RecordMetadata recordMetadata, Exception exception) {
        // 记录发送失败的消息信息及异常
        if (recordMetadata != null) {
            log.error("Kafka消息发送失败 - 主题: {}, 分区: {}, 偏移量: {}, 键: {}, 值: {}, 异常: {}",
                    producerRecord.topic(),
                    recordMetadata.partition(),
                    recordMetadata.offset(),
                    producerRecord.key(),
                    producerRecord.value(),
                    exception.getMessage(), exception);
        } else {
            log.error("Kafka消息发送失败 - RecordMetadata为null, 键: {}, 值: {}, 异常: {}",
                    producerRecord.key(),
                    producerRecord.value(),
                    exception.getMessage(), exception);
        }
    }
}

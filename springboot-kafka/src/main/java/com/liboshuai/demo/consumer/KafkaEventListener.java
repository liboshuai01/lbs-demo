package com.liboshuai.demo.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class KafkaEventListener {

    @KafkaListener(
            topics = "${demo.kafka.consumer-topic}",
            groupId = "${spring.kafka.consumer.group-id}",
            containerFactory = "kafkaListenerContainerFactory",
            errorHandler = "kafkaConsumerExceptionHandler"
    )
    public void onAlert(List<ConsumerRecord<String, String>> consumerRecordList, Acknowledgment ack) {
        for (ConsumerRecord<String, String> record : consumerRecordList) {
            // 打印消费的详细信息
            log.info("Consumed message - Topic: {}, Partition: {}, Offset: {}, Key: {}, Value: {}",
                    record.topic(),
                    record.partition(),
                    record.offset(),
                    record.key(),
                    record.value());
        }
        // 手动提交偏移量
        ack.acknowledge();
    }
}
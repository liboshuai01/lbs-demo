package com.liboshuai.demo.provider;

import com.liboshuai.demo.dto.KafkaEventDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Component
public class KafkaEventProvider {

    @Resource
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${demo.kafka.provider-topic}")
    private String providerTopic;

    /**
     * 批量上送事件信息到kafka
     */
    public void batchSend(List<KafkaEventDTO> kafkaEventDTOList) {
        if (CollectionUtils.isEmpty(kafkaEventDTOList)) {
            return;
        }
        kafkaEventDTOList.forEach(eventUploadDTO -> kafkaTemplate.send(providerTopic, eventUploadDTO));
    }
}
package com.kpn.ndsal.resourcemanager.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.kafka.config.BaseProducer;
import com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto;

@Service
public class DeleteLockResponseProducer extends BaseProducer<DeleteLockResponseDto> {

    public DeleteLockResponseProducer(
            @Value("${spring.kafka.producer.delete-lock-response-client-id}") String producerClientId,
            @Value("${spring.kafka.producer.delete-lock-response-topic}") String producerTopicName) {
        super(producerClientId, producerTopicName);
    }
}
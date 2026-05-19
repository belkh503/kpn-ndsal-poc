package com.kpn.ndsal.resourcemanager.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.kafka.config.BaseProducer;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;

@Service
public class AcquireLockResponseProducer extends BaseProducer<AcquireLockResponseDto> {

    public AcquireLockResponseProducer(
            @Value("${spring.kafka.producer.acquire-lock-response-client-id}") String producerClientId,
            @Value("${spring.kafka.producer.acquire-lock-response-topic}") String producerTopicName) {
        super(producerClientId, producerTopicName);
    }
}
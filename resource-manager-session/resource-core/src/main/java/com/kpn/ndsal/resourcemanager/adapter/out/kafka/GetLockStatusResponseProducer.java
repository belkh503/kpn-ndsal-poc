package com.kpn.ndsal.resourcemanager.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.kafka.config.BaseProducer;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto;

@Service
public class GetLockStatusResponseProducer extends BaseProducer<GetLockStatusResponseDto> {

    public GetLockStatusResponseProducer(
            @Value("${spring.kafka.producer.get-lock-status-response-client-id}") String producerClientId,
            @Value("${spring.kafka.producer.get-lock-status-response-topic}") String producerTopicName) {
        super(producerClientId, producerTopicName);
    }
}
package com.kpn.ndsal.sessionmanager.kafka.acquire;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.kafka.config.BaseProducer;
import com.kpn.ndsal.sessionmanager.model.ErrorDto;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;

@Service
public class AcquireResponseProducer extends BaseProducer<SessionAcquireResponseDto> {

    public AcquireResponseProducer(@Value("${spring.kafka.producer.acquire-response-client-id}") String producerClientId,
            @Value("${spring.kafka.producer.acquire-response-topic}") String producerTopicName) {
        super(producerClientId, producerTopicName);
    }

    public static SessionAcquireResponseDto createAcquireResponseFailDto(String errorMessage) {
        var acquireResponse = new SessionAcquireResponseDto();
        acquireResponse.setSessionAcquired(false);

        var errorDto = new ErrorDto();
        errorDto.setErrorMessage(errorMessage);
        acquireResponse.setErrorDto(errorDto);
        return acquireResponse;
    }
}
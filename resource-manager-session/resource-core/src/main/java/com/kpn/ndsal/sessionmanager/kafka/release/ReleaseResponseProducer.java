package com.kpn.ndsal.sessionmanager.kafka.release;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.kafka.config.BaseProducer;
import com.kpn.ndsal.sessionmanager.model.ErrorDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseResponseDto;

@Service
public class ReleaseResponseProducer extends BaseProducer<SessionReleaseResponseDto> {

    public ReleaseResponseProducer(@Value("${spring.kafka.producer.release-response-client-id}") String producerClientId,
            @Value("${spring.kafka.producer.release-response-topic}") String producerTopicName) {
        super(producerClientId, producerTopicName);
    }

    public static SessionReleaseResponseDto createReleaseResponseFailDto(String errorMessage) {
        var releaseResponse = new SessionReleaseResponseDto();

        var errorDto = new ErrorDto();
        errorDto.setErrorMessage(errorMessage);
        releaseResponse.setErrorDto(errorDto);
        return releaseResponse;
    }
}
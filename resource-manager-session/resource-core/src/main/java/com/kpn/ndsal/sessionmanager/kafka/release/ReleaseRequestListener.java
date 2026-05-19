package com.kpn.ndsal.sessionmanager.kafka.release;

import static com.kpn.ndsal.sessionmanager.kafka.release.ReleaseResponseProducer.createReleaseResponseFailDto;
import static java.util.stream.Collectors.joining;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
import com.kpn.ndsal.sessionmanager.task.ReleaseTask;
import com.networknt.schema.ValidationMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class ReleaseRequestListener extends BaseListener<SessionReleaseRequestDto> {
    private final ReleaseResponseProducer releaseResponseProducer;
    private final ReleaseTask releaseTask;

    @Override
    @KafkaListener(topics = "${spring.kafka.consumer.release-topic}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   clientIdPrefix = "${spring.kafka.consumer.release-client-id}")
    public void listener(String releaseRequestJson, @Header(RECEIVED_TOPIC) String topic,
            @Headers Map<String, byte[]> headers) {
        log.info("Received release message {}", releaseRequestJson);
        super.listener(releaseRequestJson, topic, headers);
    }

    @Override
    protected void consume(SessionReleaseRequestDto sessionsReleaseRequest, String topic, String correlationId,
            Map<String, byte[]> headers) {
        releaseTask.startRelease(sessionsReleaseRequest, correlationId);
    }

    @Override
    protected Class<SessionReleaseRequestDto> getRequestClass() {
        return SessionReleaseRequestDto.class;
    }

    @Override
    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var releaseResponse = createReleaseResponseFailDto(e.getMessage());
        releaseResponseProducer.sendMessage(releaseResponse, correlationId);
    }

    @Override
    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        String errorMessage = e.getErrors().stream()
                .map(ValidationMessage::getMessage)
                .collect(joining("\n"));
        var releaseResponse = createReleaseResponseFailDto(errorMessage);
        releaseResponseProducer.sendMessage(releaseResponse, correlationId);
    }
}

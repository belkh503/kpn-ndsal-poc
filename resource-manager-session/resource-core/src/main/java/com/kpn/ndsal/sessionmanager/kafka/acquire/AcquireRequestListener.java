package com.kpn.ndsal.sessionmanager.kafka.acquire;

import static com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer.createAcquireResponseFailDto;
import static java.util.stream.Collectors.joining;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;
import com.networknt.schema.ValidationMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class AcquireRequestListener extends BaseListener<SessionAcquireRequestDto> {
    private final AcquireResponseProducer acquireResponseProducer;
    private final AcquireExecutorService acquireExecutorService;
    private final HazelcastConfig hazelcastConfig;
    private final RequestValidationService validationService;

    @Override
    @KafkaListener(topics = "${spring.kafka.consumer.acquire-topic}", groupId = "${spring.kafka.consumer.group-id}",
                   clientIdPrefix = "${spring.kafka.consumer.acquire-client-id}")
    public void listener(String acquireRequestJson, @Header(RECEIVED_TOPIC) String topic, @Headers Map<String, byte[]> headers) {
        log.debug("Message received ({}): {}", topic, acquireRequestJson);
        super.listener(acquireRequestJson, topic, headers);
    }

    @Override
    protected void consume(SessionAcquireRequestDto sessionsAcquireRequest, String topic, String correlationId,
            Map<String, byte[]> headers) {
        if (validationService.isValidRequest(sessionsAcquireRequest, correlationId)) {
            addRequestToMemory(sessionsAcquireRequest, correlationId);
            acquireExecutorService.startAcquire();
        }
    }

    @Override
    protected Class<SessionAcquireRequestDto> getRequestClass() {
        return SessionAcquireRequestDto.class;
    }

    @Override
    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var acquireResponse = createAcquireResponseFailDto(e.getMessage());
        acquireResponseProducer.sendMessage(acquireResponse, correlationId);
    }

    @Override
    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var errorMessage = e.getErrors().stream().map(ValidationMessage::getMessage).collect(joining("\n"));
        var acquireResponse = createAcquireResponseFailDto(errorMessage);
        acquireResponseProducer.sendMessage(acquireResponse, correlationId);
    }

    private void addRequestToMemory(SessionAcquireRequestDto request, String correlationId) {
        log.info("request with correlationId = {} will be added to internal storage", correlationId);

        var internalRequest = new InternalRequest(request, correlationId);

        var hazelcastContext = hazelcastConfig.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            hazelcastConfig.getRequestsByUuid(hazelcastContext).put(internalRequest.getUuid(), internalRequest);

            hazelcastContext.commitTransaction();
        } catch (Throwable throwable) {
            hazelcastContext.rollbackTransaction();
            throw throwable;
        }
    }
}

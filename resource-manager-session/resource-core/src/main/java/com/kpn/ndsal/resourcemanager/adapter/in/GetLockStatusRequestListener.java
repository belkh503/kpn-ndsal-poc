package com.kpn.ndsal.resourcemanager.adapter.in;

import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.ACTIVE;
import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.ERROR;
import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.NOT_FOUND;
import static java.util.stream.Collectors.joining;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import java.util.List;
import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.GetLockStatusResponseProducer;
import com.kpn.ndsal.resourcemanager.application.port.in.StatusLockQuery;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusRequestDto;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto;
import com.networknt.schema.ValidationMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class GetLockStatusRequestListener extends BaseListener<GetLockStatusRequestDto> {
    private final GetLockStatusResponseProducer getLockStatusResponseProducer;
    private final StatusLockQuery statusLockQuery;

    @Override
    @KafkaListener(topics = "${spring.kafka.consumer.get-lock-status-topic}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   clientIdPrefix = "${spring.kafka.consumer.get-lock-status-client-id}")
    public void listener(String json, @Header(RECEIVED_TOPIC) String topic, @Headers Map<String, byte[]> headers) {
        log.debug("Message received ({}): {}", topic, json);
        super.listener(json, topic, headers);
    }

    @Override
    protected void consume(GetLockStatusRequestDto request, String topic, String correlationId,
            Map<String, byte[]> headers) {
        GetLockStatusResponseDto response = new GetLockStatusResponseDto();

        statusLockQuery.findLock(request.lockId).ifPresentOrElse(
            lock -> {
                response.lockId = lock.getId().toString();
                response.status = ACTIVE;
                response.domain = lock.getDomain();
                response.correlationId = lock.getCorrelationId();
                response.created = lock.getCreated();
                response.timesOutAt = lock.getTimesOutAt();
                response.lockObjects = mapLockObjects(lock.getLockObjectEntities());
            },
            () -> response.status = NOT_FOUND
        );

        getLockStatusResponseProducer.sendMessage(response, correlationId);
    }

    private List<GetLockStatusResponseDto.LockObjectDto> mapLockObjects(List<LockRequest.LockObject> lockObjects) {
        if (lockObjects == null) return List.of();
        return lockObjects.stream().map(lo -> {
            var dto = new GetLockStatusResponseDto.LockObjectDto();
            dto.name = lo.getName();
            dto.type = lo.getType();
            dto.lockType = lo.getLockType() != null ? lo.getLockType().name() : null;
            return dto;
        }).toList();
    }

    @Override
    protected Class<GetLockStatusRequestDto> getRequestClass() {
        return GetLockStatusRequestDto.class;
    }

    @Override
    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers, String requestJson) {
        GetLockStatusResponseDto response = new GetLockStatusResponseDto();
        response.status = ERROR;
        response.errorMessage = e.getMessage();
        getLockStatusResponseProducer.sendMessage(response, correlationId);
    }

    @Override
    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var errorMessage = e.getErrors().stream().map(ValidationMessage::getMessage).collect(joining("\n"));
        GetLockStatusResponseDto response = new GetLockStatusResponseDto();
        response.status = ERROR;
        response.errorMessage = errorMessage;
        getLockStatusResponseProducer.sendMessage(response, correlationId);
    }
}
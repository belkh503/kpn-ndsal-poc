package com.kpn.ndsal.resourcemanager.adapter.in;

import static com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto.DeleteStatus.ERROR;
import static com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto.DeleteStatus.LOCK_REMOVED;
import static java.util.stream.Collectors.joining;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.DeleteLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.port.in.ReleaseLockUseCase;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;
import com.kpn.ndsal.resourcemanager.model.DeleteLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto;
import com.networknt.schema.ValidationMessage;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@AllArgsConstructor
public class DeleteLockRequestListener extends BaseListener<DeleteLockRequestDto> {
    private final DeleteLockResponseProducer deleteLockResponseProducer;
    private final ReleaseLockUseCase releaseLockUseCase;
    private final ProcessQueueExecutorService processQueueExecutorService;

    @Override
    @KafkaListener(topics = "${spring.kafka.consumer.delete-lock-topic}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   clientIdPrefix = "${spring.kafka.consumer.delete-lock-client-id}")
    public void listener(String json, @Header(RECEIVED_TOPIC) String topic, @Headers Map<String, byte[]> headers) {
        log.debug("Message received ({}): {}", topic, json);
        super.listener(json, topic, headers);
    }

    @Override
    protected void consume(DeleteLockRequestDto request, String topic, String correlationId,
            Map<String, byte[]> headers) {
        releaseLockUseCase.release(request.lockId);

        DeleteLockResponseDto response = new DeleteLockResponseDto();
        response.deleteStatus = LOCK_REMOVED;
        deleteLockResponseProducer.sendMessage(response, correlationId);

        processQueueExecutorService.startProcessing();
    }

    @Override
    protected Class<DeleteLockRequestDto> getRequestClass() {
        return DeleteLockRequestDto.class;
    }

    @Override
    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        DeleteLockResponseDto response = new DeleteLockResponseDto();
        response.deleteStatus = ERROR;
        response.errorMessage = e.getMessage();
        deleteLockResponseProducer.sendMessage(response, correlationId);
    }

    @Override
    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var errorMessage = e.getErrors().stream().map(ValidationMessage::getMessage).collect(joining("\n"));
        DeleteLockResponseDto response = new DeleteLockResponseDto();
        response.deleteStatus = ERROR;
        response.errorMessage = errorMessage;
        deleteLockResponseProducer.sendMessage(response, correlationId);
    }
}

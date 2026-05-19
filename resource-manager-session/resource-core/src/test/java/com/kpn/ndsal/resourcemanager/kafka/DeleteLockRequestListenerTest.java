package com.kpn.ndsal.resourcemanager.kafka;

import static com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto.DeleteStatus.ERROR;
import static com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto.DeleteStatus.LOCK_REMOVED;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.resourcemanager.adapter.in.DeleteLockRequestListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.DeleteLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.resourcemanager.application.port.in.ReleaseLockUseCase;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;
import com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto;

@SpringBootTest(
        classes = {DeleteLockRequestListener.class, JsonSchemaValidationConfig.class, ObjectMapper.class, KafkaProperties.class})
@ActiveProfiles("test")
class DeleteLockRequestListenerTest {

    @MockitoBean
    private DeleteLockResponseProducer deleteLockResponseProducer;

    @MockitoBean
    private ReleaseLockUseCase releaseLockUseCase;

    @MockitoBean
    private ProcessQueueExecutorService processQueueExecutorService;

    @Autowired
    private DeleteLockRequestListener deleteLockRequestListener;

    @Test
    void listener_validRequest_resourcesLocked() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        UUID uuid = UUID.randomUUID();
        doReturn(null).when(processQueueExecutorService).startProcessing();

        String message = "{ " +
                "\"lockId\": \"" + uuid + "\"" +
                "}";

        deleteLockRequestListener.listener(message, "topic", headers);

        verify(releaseLockUseCase, times(1)).release(uuid);

        DeleteLockResponseDto response = new DeleteLockResponseDto();
        response.deleteStatus = LOCK_REMOVED;
        verify(deleteLockResponseProducer, times(1)).sendMessage(eq(response), any());
        verify(processQueueExecutorService, times(1)).startProcessing();
    }

    @Test
    void listener_invalidRequest() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        String message = "{}";
        deleteLockRequestListener.listener(message, "topic", headers);

        verify(releaseLockUseCase, never()).release(any());

        DeleteLockResponseDto response = new DeleteLockResponseDto();
        response.deleteStatus = ERROR;
        response.errorMessage = "$: required property 'lockId' not found";
        verify(deleteLockResponseProducer, times(1)).sendMessage(eq(response), any());
    }

    @Test
    void handleErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        RuntimeException e = new RuntimeException("uh oh");

        deleteLockRequestListener.handleErrorScenario(e, correlationId.toString(), null, "");

        verify(deleteLockResponseProducer).sendMessage(any(DeleteLockResponseDto.class), eq(correlationId.toString()));
    }

    @Test
    void handleValidationErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        ValidationException e = new ValidationException(new java.util.HashSet<com.networknt.schema.ValidationMessage>());

        deleteLockRequestListener.handleValidationErrorScenario(e, correlationId.toString(), null, "");

        verify(deleteLockResponseProducer).sendMessage(any(DeleteLockResponseDto.class), eq(correlationId.toString()));
    }
}
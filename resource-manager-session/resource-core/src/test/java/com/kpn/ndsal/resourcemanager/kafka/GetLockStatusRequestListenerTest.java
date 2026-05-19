package com.kpn.ndsal.resourcemanager.kafka;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;

import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.ERROR;
import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.LOCK_EXISTS;
import static com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus.LOCK_NOT_AVAILABLE;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.kpn.ndsal.resourcemanager.adapter.in.GetLockStatusRequestListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.GetLockStatusResponseProducer;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.resourcemanager.application.port.in.StatusLockQuery;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto;

@SpringBootTest(classes = {GetLockStatusRequestListener.class, JsonSchemaValidationConfig.class, ObjectMapper.class, KafkaProperties.class})
@ActiveProfiles("test")
class GetLockStatusRequestListenerTest {

    @MockitoBean
    private GetLockStatusResponseProducer getLockStatusResponseProducer;

    @MockitoBean
    private StatusLockQuery statusLockQuery;

    @Autowired
    private GetLockStatusRequestListener getLockStatusRequestListener;

    @Test
    void listener_validRequest_lockExists() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        UUID uuid = UUID.randomUUID();
        when(statusLockQuery.lockExist(uuid)).thenReturn(true);

        String message = "{ " +
                "\"lockId\": \"" + uuid + "\"" +
                "}";

        getLockStatusRequestListener.listener(message, "topic", headers);

        verify(statusLockQuery, times(1)).lockExist(uuid);

        GetLockStatusResponseDto response = new GetLockStatusResponseDto();
        response.lockStatus = LOCK_EXISTS;
        verify(getLockStatusResponseProducer, times(1)).sendMessage(eq(response), any());
    }

    @Test
    void listener_validRequest_lockNotAvailable() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        UUID uuid = UUID.randomUUID();
        when(statusLockQuery.lockExist(uuid)).thenReturn(false);

        String message = "{ " +
                "\"lockId\": \"" + uuid + "\"" +
                "}";

        getLockStatusRequestListener.listener(message, "topic", headers);

        verify(statusLockQuery, times(1)).lockExist(uuid);

        GetLockStatusResponseDto response = new GetLockStatusResponseDto();
        response.lockStatus = LOCK_NOT_AVAILABLE;
        verify(getLockStatusResponseProducer, times(1)).sendMessage(eq(response), any());
    }

    @Test
    void listener_invalidRequest() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        String message = "{}";
        getLockStatusRequestListener.listener(message, "topic", headers);

        verify(statusLockQuery, never()).lockExist(any());

        GetLockStatusResponseDto response = new GetLockStatusResponseDto();
        response.lockStatus = ERROR;
        response.errorMessage = "$: required property 'lockId' not found";
        verify(getLockStatusResponseProducer, times(1)).sendMessage(eq(response), any());
    }

    @Test
    void handleErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        RuntimeException e = new RuntimeException("uh oh");

        getLockStatusRequestListener.handleErrorScenario(e, correlationId.toString(), null, "");

        verify(getLockStatusResponseProducer).sendMessage(any(GetLockStatusResponseDto.class), eq(correlationId.toString()));
    }

    @Test
    void handleValidationErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        ValidationException e = new ValidationException(new java.util.HashSet<com.networknt.schema.ValidationMessage>());

        getLockStatusRequestListener.handleValidationErrorScenario(e, correlationId.toString(), null, "");

        verify(getLockStatusResponseProducer).sendMessage(any(GetLockStatusResponseDto.class), eq(correlationId.toString()));
    }
}
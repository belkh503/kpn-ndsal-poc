package com.kpn.ndsal.sessionmanager.unittests.kafka.acquire;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireRequestListener;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;

@SpringBootTest(classes = {AcquireRequestListener.class, JsonSchemaValidationConfig.class, ObjectMapper.class})
class AcquireRequestListenerTest {

    @MockitoBean
    private AcquireResponseProducer acquireResponseProducer;

    @MockitoBean
    private AcquireExecutorService acquireExecutorService;

    @MockitoBean
    private HazelcastConfig hazelcastConfig;

    @MockitoBean
    private RequestValidationService validationService;

    @Autowired
    private AcquireRequestListener acquireRequestListener;

    @Test
    void listener_invalidRequest() throws IOException {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());


        when(validationService.isValidRequest(any(), any())).thenReturn(false);

        String message = """
                {
                    "sessionsInfo":[
                       {
                          "domain":"bcpe",
                          "nodeName":"test1",
                          "numSessionsWanted":50,
                          "systemType":"ge104"
                       }
                    ],
                    "timeoutSec":10,
                    "priority":"HIGH"
                 }
                """;

        acquireRequestListener.listener(message, "topic", headers);

        verify(hazelcastConfig, never()).getHazelcastContext();
        verify(hazelcastConfig, never()).getRequestsByUuid(any());
        verify(acquireExecutorService, never()).startAcquire();
    }

    @Test
    void listener_validRequest() throws IOException {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        var transactionContext = mock(TransactionContext.class);
        var requestsByUuid = mock(TransactionalMap.class);

        when(validationService.isValidRequest(any(), any())).thenReturn(true);
        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenReturn(requestsByUuid);

        String message = """
                {
                    "sessionsInfo":[
                       {
                          "domain":"bcpe",
                          "nodeName":"test1",
                          "numSessionsWanted":3,
                          "systemType":"ge104"
                       }
                    ],
                    "timeoutSec":10,
                    "priority":"HIGH"
                 }
                """;

        acquireRequestListener.listener(message, "topic", headers);

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(hazelcastConfig, times(1)).getRequestsByUuid(any());
        verify(acquireExecutorService, times(1)).startAcquire();
    }

    @Test
    void listener_validRequest_hazelcastFails() throws IOException {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        var transactionContext = mock(TransactionContext.class);

        when(validationService.isValidRequest(any(), any())).thenReturn(true);
        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenThrow(RuntimeException.class);

        String message = """
                {
                    "sessionsInfo":[
                       {
                          "domain":"bcpe",
                          "nodeName":"test1",
                          "numSessionsWanted":3,
                          "systemType":"ge104"
                       }
                    ],
                    "timeoutSec":10,
                    "priority":"HIGH"
                 }
                """;

        acquireRequestListener.listener(message, "topic", headers);

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(hazelcastConfig, times(1)).getRequestsByUuid(any());
        verify(acquireExecutorService, never()).startAcquire();
    }

    @Test
    void handleErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        RuntimeException e = new RuntimeException("uh oh");

        acquireRequestListener.handleErrorScenario(e, correlationId.toString(), null, "");

        verify(acquireResponseProducer).sendMessage(any(SessionAcquireResponseDto.class), eq(correlationId.toString()));
    }

    @Test
    void handleValidationErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        ValidationException e = new ValidationException(new java.util.HashSet<com.networknt.schema.ValidationMessage>());

        acquireRequestListener.handleValidationErrorScenario(e, correlationId.toString(), null, "");

        verify(acquireResponseProducer).sendMessage(any(SessionAcquireResponseDto.class), eq(correlationId.toString()));
    }
}
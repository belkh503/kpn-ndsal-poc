package com.kpn.ndsal.resourcemanager.kafka;

import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.ERROR;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.resourcemanager.adapter.in.AcquireLockRequestListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.AcquireLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestQueueUseCase;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;
import com.kpn.ndsal.resourcemanager.application.service.RequestMapperImpl;
import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;

import lombok.SneakyThrows;

@SpringBootTest(
        classes = {AcquireLockRequestListener.class, JsonSchemaValidationConfig.class, ObjectMapper.class, KafkaProperties.class, RequestMapperImpl.class})
@ActiveProfiles("test")
class AcquireLockRequestListenerTest {

    @MockitoBean
    private AcquireLockResponseProducer acquireLockResponseProducer;

    @MockitoBean
    private RequestQueueUseCase requestQueueService;

    @MockitoBean
    private ProcessQueueExecutorService processQueueExecutorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AcquireLockRequestListener acquireLockRequestListener;

    @Test
    @SneakyThrows
    void listener_validRequest() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        String message = """
                {
                    "domain": "BCPE",
                    "lockGroups": [
                      {
                        "lockObjects":
                        [
                          {
                            "type": "NODE",
                            "id": "nl-pbl-cpe-01"
                          }
                        ]
                      }
                    ]
                  }
                """;
        var requestQueueEntity = messageToRequestQueueEntity(message, correlationId);

        doReturn(null).when(processQueueExecutorService).startProcessing();
        doNothing().when(requestQueueService).addRequestToQueue(requestQueueEntity);

        acquireLockRequestListener.listener(message, "topic", headers);

        verify(requestQueueService, times(1)).addRequestToQueue(any());
        verify(processQueueExecutorService, times(1)).startProcessing();
    }

    @Test
    void listener_invalidRequest() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());

        String message = """
                {
                    "lockGroups": [
                      {
                        "lockObjects":
                        [
                          {
                            "type": "NODE",
                            "id": "nl-pbl-cpe-01"
                          }
                        ]
                      }
                    ]
                  }
                """;

        acquireLockRequestListener.listener(message, "topic", headers);

        verify(requestQueueService, never()).addRequestToQueue(any());
        verify(processQueueExecutorService, never()).startProcessing();

        AcquireLockResponseDto response = new AcquireLockResponseDto();
        response.acquireStatus = ERROR;
        response.errorMessage = "$: required property 'domain' not found";
        verify(acquireLockResponseProducer, times(1)).sendMessage(eq(response), any());
    }

    @Test
    void handleErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        RuntimeException e = new RuntimeException("uh oh");

        acquireLockRequestListener.handleErrorScenario(e, correlationId.toString(), null, "");

        verify(acquireLockResponseProducer).sendMessage(any(AcquireLockResponseDto.class), eq(correlationId.toString()));
    }

    @Test
    void handleValidationErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        ValidationException e = new ValidationException(new java.util.HashSet<com.networknt.schema.ValidationMessage>());

        acquireLockRequestListener.handleValidationErrorScenario(e, correlationId.toString(), null, "");

        verify(acquireLockResponseProducer).sendMessage(any(AcquireLockResponseDto.class), eq(correlationId.toString()));
    }

    private RequestQueueEntity messageToRequestQueueEntity(String message,
            String correlationId) throws JsonProcessingException {
        var requestQueueEntity = objectMapper.readValue(message, RequestQueueEntity.class);
        requestQueueEntity.setCorrelationId(correlationId);
        return requestQueueEntity;
    }
}
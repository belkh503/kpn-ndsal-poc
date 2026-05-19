package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import lombok.SneakyThrows;

@SpringBootTest(classes = {RequestMapperImpl.class, ObjectMapper.class})
class RequestMapperTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestMapper requestMapper;

    @Test
    void toRequestQueueEntity() {
        var correlationId = UUID.randomUUID().toString();
        var acquireLockRequestDto = createAcquireLockRequest();
        SpanContext remoteContext = SpanContext.createFromRemoteParent("4586396b411fcc53071d8faf8b5399f5",
                "9da4c3275e703f02", TraceFlags.getSampled(), TraceState.getDefault());

        var context = Context.current().with(Span.wrap(remoteContext));

        var requestQueueEntity = requestMapper.toRequestQueueEntity(acquireLockRequestDto, correlationId, context);

        assertEquals(correlationId, requestQueueEntity.getCorrelationId());
        assertEquals(acquireLockRequestDto.getDomain(), requestQueueEntity.getDomain());
        assertEquals(acquireLockRequestDto.getPriority(), requestQueueEntity.getPriority());
        assertEquals(acquireLockRequestDto.getTimeout(), requestQueueEntity.getTimeout());
        assertEquals(acquireLockRequestDto.getLockGroups(), requestQueueEntity.getLockGroups());
        assertNotNull(requestQueueEntity.getCreationTime());
        assertNotNull(requestQueueEntity.getId());
        assertNotNull(requestQueueEntity.getContext());
        assertTrue(requestQueueEntity.getContext()
            .get("traceparent")
            .contains("4586396b411fcc53071d8faf8b5399f5-9da4c3275e703f02"));
    }

    @Test
    void toRequestQueueEntityWithoutContext() {
        var correlationId = UUID.randomUUID().toString();
        var acquireLockRequestDto = createAcquireLockRequest();

        var requestQueueEntity = requestMapper.toRequestQueueEntity(acquireLockRequestDto, correlationId, null);

        assertEquals(correlationId, requestQueueEntity.getCorrelationId());
        assertEquals(acquireLockRequestDto.getDomain(), requestQueueEntity.getDomain());
        assertEquals(acquireLockRequestDto.getPriority(), requestQueueEntity.getPriority());
        assertEquals(acquireLockRequestDto.getTimeout(), requestQueueEntity.getTimeout());
        assertEquals(acquireLockRequestDto.getLockGroups(), requestQueueEntity.getLockGroups());
        assertNotNull(requestQueueEntity.getCreationTime());
        assertNotNull(requestQueueEntity.getId());
        assertTrue(requestQueueEntity.getContext().isEmpty());
    }

    @Test
    void toAcquireLockRequestDto() {
        var requestQueueEntity = createRequestQueueEntity();

        var acquireLockRequestDto = requestMapper.toAcquireLockRequestDto(requestQueueEntity);

        assertEquals(requestQueueEntity.getDomain(), acquireLockRequestDto.getDomain());
        assertEquals(requestQueueEntity.getPriority(), acquireLockRequestDto.getPriority());
        assertEquals(requestQueueEntity.getTimeout(), acquireLockRequestDto.getTimeout());
        assertEquals(requestQueueEntity.getLockGroups(), acquireLockRequestDto.getLockGroups());
    }

    @SneakyThrows
    private AcquireLockRequestDto createAcquireLockRequest() {
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
        return objectMapper.readValue(message, AcquireLockRequestDto.class);
    }

    @SneakyThrows
    private RequestQueueEntity createRequestQueueEntity() {
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
        var requestQueueEntity = objectMapper.readValue(message, RequestQueueEntity.class);
        requestQueueEntity.setCreationTime(System.currentTimeMillis());
        requestQueueEntity.setId(UUID.randomUUID());
        requestQueueEntity.setCorrelationId(UUID.randomUUID().toString());
        return requestQueueEntity;
    }
}

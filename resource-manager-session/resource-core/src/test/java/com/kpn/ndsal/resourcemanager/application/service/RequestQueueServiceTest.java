package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.utils.TransactionalMapForRequestQueueUtils;

import lombok.SneakyThrows;

@SpringBootTest(classes = {RequestQueueService.class, ObjectMapper.class})
class RequestQueueServiceTest {

    @MockitoBean
    private HazelcastConfigurator hazelcastConfigurator;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestQueueService requestQueueService;

    @Test
    void addRequestToQueue_OK() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var requestQueue = TransactionalMapForRequestQueueUtils.create(List.of());
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenReturn(requestQueue);

        requestQueueService.addRequestToQueue(requestQueueEntity);

        assertEquals(1, requestQueue.size());
        assertEquals(requestQueueEntity, requestQueue.get(requestQueueEntity.getId()));
    }

    @Test
    void addRequestToQueue_exception() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenThrow(new RuntimeException("uh oh"));

        var exception = assertThrows(RuntimeException.class,
                () -> requestQueueService.addRequestToQueue(requestQueueEntity));
        assertTrue(exception.getMessage().contains("uh oh"));
    }

    @Test
    void deleteRequestFromQueue_OK() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var requestQueue = TransactionalMapForRequestQueueUtils.create(List.of(requestQueueEntity));
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenReturn(requestQueue);

        requestQueueService.deleteRequestFromQueue(requestQueueEntity);

        assertTrue(requestQueue.isEmpty());
    }

    @Test
    void deleteRequestFromQueue_exception() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenThrow(new RuntimeException("uh oh"));

        var exception = assertThrows(RuntimeException.class,
                () -> requestQueueService.deleteRequestFromQueue(requestQueueEntity));
        assertTrue(exception.getMessage().contains("uh oh"));
    }

    @SneakyThrows
    private RequestQueueEntity createRequestQueueEntity(String correlationId) {
        String message = """
                {
                    "domain": "BCPE",
                    "id": "a3489849-1276-a070-13c9-91f0ce52d429",
                    "creationTime": 1,
                    "correlationId": "e3abae16-80d4-48a9-86ae-aeb200e53b6e",
                    "priority": "MEDIUM",
                    "lockGroups": [
                      {
                        "lockObjects":
                        [
                          {
                            "type": "PORT",
                            "id": "nl-cpe-01:3",
                            "force": false
                          }
                        ]
                      }
                    ]
                  }
                """;
        var requestQueueEntity = objectMapper.readValue(message, RequestQueueEntity.class);
        requestQueueEntity.setCreationTime(System.currentTimeMillis());
        requestQueueEntity.setCorrelationId(correlationId);
        return requestQueueEntity;
    }
}
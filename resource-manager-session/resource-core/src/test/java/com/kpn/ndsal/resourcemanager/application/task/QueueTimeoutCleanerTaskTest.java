package com.kpn.ndsal.resourcemanager.application.task;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestTimeoutExceededException;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.application.service.RequestProcessorService;
import com.kpn.ndsal.resourcemanager.utils.TransactionalMapForRequestQueueUtils;

import lombok.SneakyThrows;

@SpringBootTest(classes = {ObjectMapper.class, QueueTimeoutCleanerTask.class})
@ActiveProfiles("test")
class QueueTimeoutCleanerTaskTest {

    @MockitoBean
    private HazelcastConfigurator hazelcastConfigurator;

    @MockitoBean
    private RequestProcessorService requestProcessorService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private QueueTimeoutCleanerTask queueTimeoutCleanerTask;

    @Test
    void startQueueCleaner_OK() {
        String correlationId = UUID.randomUUID().toString();

        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var requestQueue = TransactionalMapForRequestQueueUtils.create(List.of(requestQueueEntity));
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenReturn(requestQueue);
        doThrow(new RequestTimeoutExceededException()).when(requestProcessorService).processRequestTimeout(requestQueueEntity);

        queueTimeoutCleanerTask.startQueueCleaner();

        verify(hazelcastConfigurator, times(1)).getHazelcastContext();
        verify(hazelcastConfigurator, times(2)).getHazelcastRequestQueue(any());
        verify(requestProcessorService, times(1)).processRequestTimeout(any());
        assertTrue(requestQueue.isEmpty());
    }

    @Test
    void startQueueCleaner_nothingToDo() {
        String correlationId = UUID.randomUUID().toString();

        var requestQueueEntity = createRequestQueueEntity(correlationId);

        var requestQueue = TransactionalMapForRequestQueueUtils.create(List.of(requestQueueEntity));
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenReturn(requestQueue);
        doNothing().when(requestProcessorService).processRequestTimeout(requestQueueEntity);

        queueTimeoutCleanerTask.startQueueCleaner();

        verify(hazelcastConfigurator, times(1)).getHazelcastContext();
        verify(hazelcastConfigurator, times(1)).getHazelcastRequestQueue(any());
        verify(requestProcessorService, times(1)).processRequestTimeout(any());
        assertEquals(1, requestQueue.size());
    }

    @Test
    void startQueueCleaner_emptyQueue() {

        var requestQueue = TransactionalMapForRequestQueueUtils.create(List.of());
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenReturn(requestQueue);

        queueTimeoutCleanerTask.startQueueCleaner();

        verify(hazelcastConfigurator, times(1)).getHazelcastContext();
        verify(hazelcastConfigurator, times(1)).getHazelcastRequestQueue(any());
        verify(requestProcessorService, never()).processRequestTimeout(any());
        assertTrue(requestQueue.isEmpty());
    }

    @Test
    void processAllQueues_exception() {
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfigurator.getHazelcastRequestQueue(any())).thenThrow(new RuntimeException("uh oh"));

        var exception = assertThrows(RuntimeException.class,
                () -> queueTimeoutCleanerTask.startQueueCleaner());
        assertTrue(exception.getMessage().contains("uh oh"));
    }

    @SneakyThrows
    private RequestQueueEntity createRequestQueueEntity(String correlationId) {
        String message = """
                {
                    "domain": "BCPE",
                    "id": "bb18be82-a703-4272-94b5-072ae70cec16",
                    "creationTime": 1,
                    "correlationId": "e3abae16-80d4-48a9-86ae-aeb200e53b6e",
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
        requestQueueEntity.setCorrelationId(correlationId);
        return requestQueueEntity;
    }
}
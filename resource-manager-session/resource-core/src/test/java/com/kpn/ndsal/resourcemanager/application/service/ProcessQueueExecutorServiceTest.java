package com.kpn.ndsal.resourcemanager.application.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

import lombok.extern.slf4j.Slf4j;

@SpringBootTest(classes = { ProcessQueueExecutorService.class })
@Slf4j
class ProcessQueueExecutorServiceTest {

    @Autowired
    ProcessQueueExecutorService processQueueExecutorService;

    @MockitoBean
    RequestProcessorService requestProcessor;

    @MockitoBean
    HazelcastConfigurator hazelcastConfigurator;

    @Test
    void testProcessQueueExecutor_Ok() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);

        TransactionalMap<UUID, RequestQueueEntity> transactionalMap = mock(TransactionalMap.class);
        List<RequestQueueEntity> listRequestQueueEntity = new LinkedList<>();
        RequestQueueEntity requestQueueEntity1 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity1);
        RequestQueueEntity requestQueueEntity2 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity2);
        when(transactionalMap.values()).thenReturn(listRequestQueueEntity);

        processQueueExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getHazelcastRequestQueue(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            return transactionalMap;
        });

        ThreadPoolExecutor tpe = processQueueExecutorService.getThreadPoolExecutor();
        var f1 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f2 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f13 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f1, f2));
        // due to silent reject Future created but never process. At this step no active task and no more task in queue
        assertFalse(f13.isDone());

        assertFalse(processQueueExecutorService.getExceptionDuringProcess().get());
        assertFalse(tpe.isTerminated());

        var f3 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f4 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f14 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f3, f4));
        // due to silent reject Future created but never process. At this step no active task and no more task in queue
        assertFalse(f14.isDone());

        assertFalse(processQueueExecutorService.getExceptionDuringProcess().get());
    }

    private boolean checkFutureFinish(Future<?>... futures) {
        var allDone = true;
        for (var f : futures) {
            if (!f.isDone()) {
                allDone = false;
                break;
            }
        }
        ThreadPoolExecutor tpe = processQueueExecutorService.getThreadPoolExecutor();
        log.info("allDone:%s queue:%s active:%s ".formatted(allDone, tpe.getQueue().size(), tpe.getActiveCount()));
        return allDone && tpe.getQueue().size() == 0 && tpe.getActiveCount() == 0;
    }

    private boolean checkOneActive() {
        ThreadPoolExecutor tpe = processQueueExecutorService.getThreadPoolExecutor();
        return tpe.getActiveCount() != 0;
    }

    @Test
    void testProcessQueueExecutor_NOk() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);

        TransactionalMap<UUID, RequestQueueEntity> transactionalMap = mock(TransactionalMap.class);
        List<RequestQueueEntity> listRequestQueueEntity = new LinkedList<>();
        RequestQueueEntity requestQueueEntity1 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity1);
        RequestQueueEntity requestQueueEntity2 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity2);
        when(transactionalMap.values()).thenReturn(listRequestQueueEntity);

        processQueueExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getHazelcastRequestQueue(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            return transactionalMap;
        });

        doThrow(new RuntimeException("this is a custom error")).when(requestProcessor)
            .processRequest(requestQueueEntity1);

        ThreadPoolExecutor tpe = processQueueExecutorService.getThreadPoolExecutor();

        var f1 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f2 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f1, f2));

        assertTrue(processQueueExecutorService.getExceptionDuringProcess().get());
        assertFalse(tpe.isTerminated());

        var f3 = processQueueExecutorService.startProcessing();
        await().until(() -> checkOneActive());
        var f4 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f3, f4));

        assertTrue(processQueueExecutorService.getExceptionDuringProcess().get());

    }

    @Test
    void testProcessQueueExecutor_NOk_OK() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);

        TransactionalMap<UUID, RequestQueueEntity> transactionalMap = mock(TransactionalMap.class);
        List<RequestQueueEntity> listRequestQueueEntity = new LinkedList<>();
        RequestQueueEntity requestQueueEntity1 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity1);
        RequestQueueEntity requestQueueEntity2 = mock(RequestQueueEntity.class);
        listRequestQueueEntity.add(requestQueueEntity2);
        when(transactionalMap.values()).thenReturn(listRequestQueueEntity);

        processQueueExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getHazelcastRequestQueue(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            return transactionalMap;
        });

        doThrow(new RuntimeException("this is a custom error")).when(requestProcessor)
            .processRequest(requestQueueEntity1);

        var f1 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f1));

        assertTrue(processQueueExecutorService.getExceptionDuringProcess().get());

        doNothing().when(requestProcessor).processRequest(requestQueueEntity1);

        var f2 = processQueueExecutorService.startProcessing();

        await().until(() -> checkFutureFinish(f2));

        assertFalse(processQueueExecutorService.getExceptionDuringProcess().get());

    }

}
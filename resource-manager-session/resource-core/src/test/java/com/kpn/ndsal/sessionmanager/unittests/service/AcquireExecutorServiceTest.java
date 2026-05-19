package com.kpn.ndsal.sessionmanager.unittests.service;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
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
import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.entity.RequestStatus;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto.Priority;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerAcquireService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest(classes = { AcquireExecutorService.class })
class AcquireExecutorServiceTest {

    @Autowired
    AcquireExecutorService acquireExecutorService;

    @MockitoBean
    DomainsSessionConfig domains;

    @MockitoBean
    RequestValidationService validationService;

    @MockitoBean
    AcquireResponseProducer acquireResponseProducer;

    @MockitoBean
    HazelcastConfig hazelcastConfigurator;

    @MockitoBean
    SessionManagerAcquireService acquireService;

    @MockitoBean
    SessionManagerReleaseService releaseService;

    @Test
    void testProcessQueueExecutor_Ok() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);

        TransactionalMap<UUID, InternalRequest> transactionalMap = mock(TransactionalMap.class);

        List<InternalRequest> listRequestQueueEntity = new LinkedList<>();
        InternalRequest internalRequest1 = mock(InternalRequest.class);
        when(internalRequest1.getPriority()).thenReturn(Priority.HIGH);
        when(internalRequest1.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest1);
        InternalRequest internalRequest2 = mock(InternalRequest.class);
        when(internalRequest2.getPriority()).thenReturn(Priority.MEDIUM);
        when(internalRequest2.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest2);
        InternalRequest internalRequest3 = mock(InternalRequest.class);
        when(internalRequest3.getPriority()).thenReturn(Priority.LOW);
        when(internalRequest3.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest3);
        when(transactionalMap.values()).thenReturn(listRequestQueueEntity);

        acquireExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getRequestsByUuid(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            return transactionalMap;
        });

        ThreadPoolExecutor tpe = acquireExecutorService.getThreadPoolExecutor();
        var f1 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f2 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f13 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f1, f2));
        // due to silent reject Future created but never process. At this step no active task and no more task in queue
        assertFalse(f13.isDone());

        assertFalse(acquireExecutorService.getExceptionDuringProcess().get());
        assertFalse(tpe.isTerminated());

        var f3 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f4 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f14 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f3, f4));
        // due to silent reject Future created but never process. At this step no active task and no more task in queue
        assertFalse(f14.isDone());

        assertFalse(acquireExecutorService.getExceptionDuringProcess().get());
    }

    private boolean checkFutureFinish(Future<?>... futures) {
        var allDone = true;
        for (var f : futures) {
            if (!f.isDone()) {
                allDone = false;
                break;
            }
        }
        ThreadPoolExecutor tpe = acquireExecutorService.getThreadPoolExecutor();
        log.info("allDone:%s queue:%s active:%s ".formatted(allDone, tpe.getQueue().size(), tpe.getActiveCount()));
        return allDone && tpe.getQueue().size() == 0 && tpe.getActiveCount() == 0;
    }

    private boolean checkOneActive() {
        ThreadPoolExecutor tpe = acquireExecutorService.getThreadPoolExecutor();
        return tpe.getActiveCount() != 0;
    }

    @Test
    void testProcessQueueExecutor_NOk() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);
        acquireExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getRequestsByUuid(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            throw new RuntimeException("this is a custom error");
        });

        ThreadPoolExecutor tpe = acquireExecutorService.getThreadPoolExecutor();

        var f1 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f2 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f1, f2));

        assertTrue(acquireExecutorService.getExceptionDuringProcess().get());
        assertFalse(tpe.isTerminated());

        var f3 = acquireExecutorService.startAcquire();
        await().until(() -> checkOneActive());
        var f4 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f3, f4));

        assertTrue(acquireExecutorService.getExceptionDuringProcess().get());

    }

    @Test
    void testProcessQueueExecutor_NOk_OK() {
        TransactionContext transaction = mock(TransactionContext.class);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);

        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);
        acquireExecutorService.getExceptionDuringProcess().set(false);

        TransactionalMap<UUID, InternalRequest> transactionalMap = mock(TransactionalMap.class);

        List<InternalRequest> listRequestQueueEntity = new LinkedList<>();
        InternalRequest internalRequest1 = mock(InternalRequest.class);
        when(internalRequest1.getPriority()).thenReturn(Priority.HIGH);
        when(internalRequest1.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest1);
        InternalRequest internalRequest2 = mock(InternalRequest.class);
        when(internalRequest2.getPriority()).thenReturn(Priority.MEDIUM);
        when(internalRequest2.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest2);
        InternalRequest internalRequest3 = mock(InternalRequest.class);
        when(internalRequest3.getPriority()).thenReturn(Priority.LOW);
        when(internalRequest3.getRequestStatus()).thenReturn(RequestStatus.NEW);
        listRequestQueueEntity.add(internalRequest3);
        when(transactionalMap.values()).thenReturn(listRequestQueueEntity);

        acquireExecutorService.getExceptionDuringProcess().set(false);

        when(hazelcastConfigurator.getRequestsByUuid(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            throw new RuntimeException("this is a custom error");
        });

        var f1 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f1));

        assertTrue(acquireExecutorService.getExceptionDuringProcess().get());

        reset(hazelcastConfigurator);
        when(hazelcastConfigurator.getHazelcastContext()).thenReturn(transaction);
        when(hazelcastConfigurator.getRequestsByUuid(transaction)).thenAnswer(invocation -> {
            Awaitility.await().pollDelay(400, TimeUnit.MILLISECONDS).until(() -> true);
            return transactionalMap;
        });

        var f2 = acquireExecutorService.startAcquire();

        await().until(() -> checkFutureFinish(f2));

        assertFalse(acquireExecutorService.getExceptionDuringProcess().get());

    }
}

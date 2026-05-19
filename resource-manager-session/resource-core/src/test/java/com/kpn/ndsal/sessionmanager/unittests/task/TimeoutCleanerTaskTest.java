package com.kpn.ndsal.sessionmanager.unittests.task;

import static com.kpn.ndsal.sessionmanager.entity.RequestStatus.ACQUIRED;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;
import com.kpn.ndsal.sessionmanager.task.TimeoutCleanerTask;
import com.kpn.ndsal.sessionmanager.util.TransactionalMapForRequestsByUuidUtils;
import com.kpn.ndsal.sessionmanager.util.TransactionalMapForTripletUtils;

@SpringBootTest(classes = {TimeoutCleanerTask.class})
class TimeoutCleanerTaskTest {
    @MockitoBean
    private HazelcastConfig hazelcastConfig;
    @MockitoBean
    private SessionManagerReleaseService releaseService;
    @MockitoBean
    private AcquireExecutorService acquireExecutorService;

    @Autowired
    private TimeoutCleanerTask timeoutCleaner;


    @Test
    void testRelease_success_CleanNoExceptionDuringProcess() {
        var correlationId = UUID.randomUUID().toString();

        var sessionAcquireRequestDto1 = getSessionAcquireRequestDto();
        var sessionAcquireRequestDto2 = new SessionAcquireRequestDto();

        var internalRequests = TransactionalMapForRequestsByUuidUtils.create(List.of(sessionAcquireRequestDto1, sessionAcquireRequestDto2), correlationId);
        var uuid = internalRequests.keySet().iterator().next();
        internalRequests.get(uuid).setRequestStatus(ACQUIRED);

        var tripletEntity = new TripletEntity("bcpe", "ge104", "test2");
        var triplets = TransactionalMapForTripletUtils.create(tripletEntity, uuid, 3, true);

        var transactionContext = mock(TransactionContext.class);
        var uuids = List.of(uuid);

        when(acquireExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(false));
        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenReturn(internalRequests);
        when(hazelcastConfig.getSessionsByTriplet(transactionContext)).thenReturn(triplets);
        when(releaseService.releaseSessions(
                (List<UUID>) argThat(t -> (((List<UUID>) t).containsAll(uuids) && uuids.containsAll((List<UUID>) t))),
                eq(transactionContext))).thenReturn(uuids);

        timeoutCleaner.release();

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(hazelcastConfig, times(2)).getRequestsByUuid(transactionContext);
        verify(hazelcastConfig, times(1)).getSessionsByTriplet(transactionContext);
        verify(releaseService, times(1)).releaseSessions(anyList(), eq(transactionContext));
        verify(acquireExecutorService, times(1)).startAcquire();
    }

    @Test
    void startLockCleaner_OK_NoCleanButExceptionDuringProcess() {

        var correlationId = UUID.randomUUID().toString();

        var sessionAcquireRequestDto1 = getSessionAcquireRequestDto();

        var internalRequests = TransactionalMapForRequestsByUuidUtils
            .create(List.of(sessionAcquireRequestDto1), correlationId);
        var uuid = internalRequests.keySet().iterator().next();
        internalRequests.get(uuid).setRequestStatus(ACQUIRED);

        var tripletEntity = new TripletEntity("bcpe", "ge104", "test2");
        var triplets = TransactionalMapForTripletUtils.create(tripletEntity, uuid, 3, false);

        var transactionContext = mock(TransactionContext.class);
        var uuids = List.of(uuid);


        when(acquireExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(true));
        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenReturn(internalRequests);
        when(hazelcastConfig.getSessionsByTriplet(transactionContext)).thenReturn(triplets);
        when(releaseService.releaseSessions(uuids, transactionContext)).thenReturn(List.of());


        timeoutCleaner.release();

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(hazelcastConfig, times(2)).getRequestsByUuid(transactionContext);
        verify(hazelcastConfig, times(1)).getSessionsByTriplet(transactionContext);
        verify(releaseService, times(1)).releaseSessions(new ArrayList<>(), transactionContext);
        verify(acquireExecutorService, times(1)).startAcquire();
    }


    @Test
    void startLockCleaner_OK_NoCleanNoExceptionDuringProcess() {

        var correlationId = UUID.randomUUID().toString();

        var sessionAcquireRequestDto1 = getSessionAcquireRequestDto();

        var internalRequests = TransactionalMapForRequestsByUuidUtils.create(List.of(sessionAcquireRequestDto1),
                correlationId);
        var uuid = internalRequests.keySet().iterator().next();
        internalRequests.get(uuid).setRequestStatus(ACQUIRED);

        var tripletEntity = new TripletEntity("bcpe", "ge104", "test2");
        var triplets = TransactionalMapForTripletUtils.create(tripletEntity, uuid, 3, false);

        var transactionContext = mock(TransactionContext.class);
        var uuids = List.of(uuid);

        when(acquireExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(false));
        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenReturn(internalRequests);
        when(hazelcastConfig.getSessionsByTriplet(transactionContext)).thenReturn(triplets);
        when(releaseService.releaseSessions(uuids, transactionContext)).thenReturn(List.of());

        timeoutCleaner.release();

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(hazelcastConfig, times(2)).getRequestsByUuid(transactionContext);
        verify(hazelcastConfig, times(1)).getSessionsByTriplet(transactionContext);
        verify(releaseService, times(1)).releaseSessions(new ArrayList<>(), transactionContext);
        verify(acquireExecutorService, times(0)).startAcquire();
    }

    @NotNull
    private static SessionAcquireRequestDto getSessionAcquireRequestDto() {
        var sessionsWanted = 3;
        var sessionInfo = new SessionInfo();
        sessionInfo.setDomain("bcpe");
        sessionInfo.setSystemType("ge104");
        sessionInfo.setNodeName("test2");
        sessionInfo.setNumSessionsWanted(sessionsWanted);
        var sessionAcquireRequestDto1 = new SessionAcquireRequestDto();
        sessionAcquireRequestDto1.setPriority(SessionAcquireRequestDto.Priority.HIGH);
        sessionAcquireRequestDto1.setSessionsInfo(List.of(sessionInfo));
        sessionAcquireRequestDto1.setTimeoutSec(15);
        return sessionAcquireRequestDto1;
    }
}
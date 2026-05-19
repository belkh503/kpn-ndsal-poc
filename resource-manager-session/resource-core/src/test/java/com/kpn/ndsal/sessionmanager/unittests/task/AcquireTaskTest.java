package com.kpn.ndsal.sessionmanager.unittests.task;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionException;
import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.RequestStatus;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerAcquireService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;
import com.kpn.ndsal.sessionmanager.task.AcquireTask;
import com.kpn.ndsal.sessionmanager.util.TransactionalMapForRequestsByUuidUtils;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class AcquireTaskTest {

    @Mock
    private HazelcastConfig hazelcastConfig;

    @Mock
    private RequestValidationService validationService;

    @Mock
    private SessionManagerAcquireService acquireService;

    @Mock
    private SessionManagerReleaseService releaseService;

    @Mock
    private AcquireResponseProducer acquireResponseProducer;

    @Mock
    private DomainsSessionConfig domains;

    @Mock
    private AtomicBoolean exceptionDuringProcess;

    @InjectMocks
    private AcquireTask acquireTask;

    @Test
    void givenNoRequest_whenAcquireCycle_thenNothingHappens() {

        // given
        var requestsByUuid = TransactionalMapForRequestsByUuidUtils.create(List.of(), null);
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(any())).thenReturn(requestsByUuid);

        // when
        acquireTask.run();

        // then
        verify(transactionContext, times(SessionAcquireRequestDto.Priority.values().length)).beginTransaction();
        verify(transactionContext, times(SessionAcquireRequestDto.Priority.values().length)).commitTransaction();
        verify(validationService, never()).getSessionsRequestInfo(any());
        verify(acquireResponseProducer, never()).sendMessage(any(), any());
    }

    @Test
    void givenNoRequest_whenCommitTransactionFails_thenRollback() {

        // given
        var requestsByUuid = TransactionalMapForRequestsByUuidUtils.create(List.of(), null);
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(any())).thenReturn(requestsByUuid);

        when(hazelcastConfig.getRequestsByUuid(any())).thenReturn(requestsByUuid);

        doThrow(TransactionException.class).when(transactionContext).commitTransaction();

        // when
        assertThatThrownBy(() -> {
            acquireTask.run();
        }).isInstanceOf(TransactionException.class);

        // then
        verify(transactionContext, times(1)).beginTransaction();
        verify(transactionContext, times(1)).commitTransaction();
        verify(transactionContext, times(1)).rollbackTransaction();
        verify(exceptionDuringProcess, times(1)).set(true);
        verify(validationService, never()).getSessionsRequestInfo(any());
        verify(acquireResponseProducer, never()).sendMessage(any(), any());
    }

    @Test
    void givenSingleRequest_whenAcquireCycle_thenAcquired() {

        // given
        var sessionsWanted = 3;
        var sessionsMax = 5;
        var correlationId = UUID.randomUUID().toString();

        var sessionInfo = new SessionInfo();
        sessionInfo.setDomain("bcpe");
        sessionInfo.setSystemType("ge104");
        sessionInfo.setNodeName("test2");
        sessionInfo.setNumSessionsWanted(sessionsWanted);
        var sessionAcquireRequestDto = new SessionAcquireRequestDto();
        sessionAcquireRequestDto.setPriority(SessionAcquireRequestDto.Priority.HIGH);
        sessionAcquireRequestDto.setSessionsInfo(List.of(
                sessionInfo
        ));
        sessionAcquireRequestDto.setTimeoutSec(15);

        var requestsByUuid = TransactionalMapForRequestsByUuidUtils.create(List.of(sessionAcquireRequestDto), correlationId);
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(hazelcastConfig.getRequestsByUuid(transactionContext)).thenReturn(requestsByUuid);

        doNothing().when(acquireResponseProducer).sendMessage(any(), any());

        when(domains.getMaxSessions(any())).thenReturn(sessionsMax);
        doNothing().when(releaseService).releaseExpiredSessionsByTriplet(any(), anyInt(), any());
        when(acquireService.acquireSessionsForTriplet(any(), anyInt(), anyLong(), any(), any())).thenReturn(true);

        when(validationService.getSessionsRequestInfo(sessionAcquireRequestDto.getSessionsInfo())).thenAnswer(invocation -> {

            List<SessionInfo> sessionsInfo = invocation.getArgument(0);
            var rvs = new RequestValidationService(domains, acquireResponseProducer);
            return rvs.getSessionsRequestInfo(sessionsInfo);
        });

        // when
        acquireTask.run();

        // then
        verify(transactionContext, times(SessionAcquireRequestDto.Priority.values().length + 1)).beginTransaction();
        verify(transactionContext, times(SessionAcquireRequestDto.Priority.values().length + 1)).commitTransaction();
        verify(validationService, times(1)).getSessionsRequestInfo(any());
        verify(acquireResponseProducer, times(1)).sendMessage(any(), any());

        var requestsByUuidModified = requestsByUuid.values().stream().toList();
        assertEquals(1, requestsByUuidModified.size());
        assertThat(requestsByUuidModified.get(0).getRequestStatus()).isEqualTo(RequestStatus.ACQUIRED);
    }
}

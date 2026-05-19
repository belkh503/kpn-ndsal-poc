package com.kpn.ndsal.sessionmanager.unittests.task;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.sessionmanager.kafka.release.ReleaseResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseResponseDto;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;
import com.kpn.ndsal.sessionmanager.task.ReleaseTask;

@SpringBootTest(classes = {ReleaseTask.class, JsonSchemaValidationConfig.class, ObjectMapper.class})
 class ReleaseTaskTest {

    @MockitoBean
    private HazelcastConfig hazelcastConfig;
    @MockitoBean
    private SessionManagerReleaseService releaseService;
    @MockitoBean
    private ReleaseResponseProducer releaseResponseProducer;
    @MockitoBean
    private AcquireExecutorService acquireExecutorService;

    @Autowired
    private ReleaseTask releaseTask;


    @Test
    void testRelease_success_atLeastOne() {
        var uuids = List.of(UUID.randomUUID(), UUID.randomUUID());

        var sessionsReleaseRequest = new SessionReleaseRequestDto();
        sessionsReleaseRequest.setUuids(uuids);

        var sessionReleasedResponse = new SessionReleaseResponseDto();
        sessionReleasedResponse.setUuids(uuids);

        var correlationId = UUID.randomUUID().toString();
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(releaseService.releaseSessions(uuids, transactionContext)).thenReturn(uuids);

        releaseTask.startRelease(sessionsReleaseRequest, correlationId);

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(releaseService, times(1)).releaseSessions(uuids, transactionContext);
        verify(releaseResponseProducer, times(1)).sendMessage(sessionReleasedResponse, correlationId);
        verify(acquireExecutorService, times(1)).startAcquire();
    }

    @Test
    void testRelease_success_zero() {
        var uuids = List.of(UUID.randomUUID(), UUID.randomUUID());
        var releasedUuids = new ArrayList<UUID>();

        var sessionsReleaseRequest = new SessionReleaseRequestDto();
        sessionsReleaseRequest.setUuids(uuids);

        var sessionReleasedResponse = new SessionReleaseResponseDto();
        sessionReleasedResponse.setUuids(releasedUuids);

        var correlationId = UUID.randomUUID().toString();
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(releaseService.releaseSessions(uuids, transactionContext)).thenReturn(releasedUuids);

        releaseTask.startRelease(sessionsReleaseRequest, correlationId);

        verify(hazelcastConfig, times(1)).getHazelcastContext();
        verify(releaseService, times(1)).releaseSessions(uuids, transactionContext);
        verify(releaseResponseProducer, times(1)).sendMessage(sessionReleasedResponse, correlationId);
        verify(acquireExecutorService, never()).startAcquire();
    }

    @Test
    void testRelease_fail() {
        var uuids = List.of(UUID.randomUUID());

        var sessionsReleaseRequest = new SessionReleaseRequestDto();
        sessionsReleaseRequest.setUuids(uuids);

        var correlationId = UUID.randomUUID().toString();
        var transactionContext = mock(TransactionContext.class);

        when(hazelcastConfig.getHazelcastContext()).thenReturn(transactionContext);
        when(releaseService.releaseSessions(uuids, transactionContext)).thenThrow(RuntimeException.class);

        try {
            releaseTask.startRelease(sessionsReleaseRequest, correlationId);
        } catch (Throwable e) {
            verify(hazelcastConfig, times(1)).getHazelcastContext();
            verify(releaseService, times(1)).releaseSessions(uuids, transactionContext);
            verify(releaseResponseProducer, never()).sendMessage(any(), any());
            verify(acquireExecutorService, never()).startAcquire();
        }
    }
}

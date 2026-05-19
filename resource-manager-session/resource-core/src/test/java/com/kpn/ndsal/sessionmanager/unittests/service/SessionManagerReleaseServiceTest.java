package com.kpn.ndsal.sessionmanager.unittests.service;

import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;
import com.kpn.ndsal.sessionmanager.util.TransactionalMapForTripletUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class SessionManagerReleaseServiceTest {

    @Mock
    private HazelcastConfig hazelcastConfig;

    @InjectMocks
    private SessionManagerReleaseService sessionManagerReleaseService;

    @Test
    void givenSession_whenExpired_thenRelease() {
        // GIVEN
        var uuid = UUID.randomUUID();
        var maximumAllowedSessions = 3;
        var triplet = new TripletEntity("bcpe", "ge104", "node-1234");
        var transactionContext = mock(TransactionContext.class);
        var sessionsByTriplet = TransactionalMapForTripletUtils.create(triplet, uuid, 3, true);

        when(hazelcastConfig.getSessionsByTriplet(any())).thenReturn(sessionsByTriplet);

        // WHEN
        sessionManagerReleaseService.releaseExpiredSessionsByTriplet(triplet, maximumAllowedSessions, transactionContext);

        assertThat(sessionsByTriplet.size()).isZero();
    }

    @Test
    void givenSession_whenNotExpired_thenDoNotTouch() {
        // GIVEN
        var uuid = UUID.randomUUID();
        var maximumAllowedSessions = 3;
        var triplet = new TripletEntity("bcpe", "ge104", "node-1234");
        var transactionContext = mock(TransactionContext.class);
        var sessionsByTriplet = TransactionalMapForTripletUtils.create(triplet, uuid, 3, false);

        when(hazelcastConfig.getSessionsByTriplet(any())).thenReturn(sessionsByTriplet);

        // WHEN
        sessionManagerReleaseService.releaseExpiredSessionsByTriplet(triplet, maximumAllowedSessions, transactionContext);

        assertThat(sessionsByTriplet.size()).isEqualTo(sessionsByTriplet.size());
    }

    @Test
    void givenNonExpiredSession_whenForceReleasing_thenSuccess() {
        // GIVEN
        var uuid = UUID.randomUUID();
        var amountOfSessions = 3;
        var triplet = new TripletEntity("bcpe", "ge104", "node-1234");
        var transactionContext = mock(TransactionContext.class);
        var sessionsByTriplet = TransactionalMapForTripletUtils.create(triplet, uuid, amountOfSessions, false);

        when(hazelcastConfig.getSessionsByTriplet(any())).thenReturn(sessionsByTriplet);

        // WHEN
        var result = sessionManagerReleaseService.releaseSessions(List.of(uuid), transactionContext);

        assertThat(sessionsByTriplet.size()).isZero();
        assertEquals(1, result.size());
    }
}
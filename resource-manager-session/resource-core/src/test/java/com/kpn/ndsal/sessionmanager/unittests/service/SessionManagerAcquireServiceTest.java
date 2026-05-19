package com.kpn.ndsal.sessionmanager.unittests.service;

import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.model.SessionRequestInfo;
import com.kpn.ndsal.sessionmanager.service.SessionManagerAcquireService;
import com.kpn.ndsal.sessionmanager.util.TransactionalMapForTripletUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKey;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerAcquireServiceTest {

    @Mock
    private HazelcastConfig hazelcastConfig;
    @Mock
    private IMap<String, Boolean> locksByTriplet;

    @InjectMocks
    private SessionManagerAcquireService sessionManagerAcquireService;

    @Captor
    private ArgumentCaptor<String> locksByTripletCaptor;

    /**
     * GIVEN:
     *  - no session locked for given triplet
     *  - all slots are available for given triplet
     *  WHEN:
     *  - request to lock less sessions than max allowed per domain
     * THEN:
     *  - success
     * */
    @Test
    void givenNoSessionLocked_whenAcquireSessionsForTriplet_thenAllow() {

        // GIVEN
        //
        var uuid = UUID.randomUUID();

        var transactionContext = mock(TransactionContext.class);

        var triplet = new TripletEntity("bcpe", "ge104", "node-1234");

        var sessionsWanted = 2;
        var maximumAllowedSessions = 3;
        var sessionRequestInfo = new SessionRequestInfo(triplet, sessionsWanted, maximumAllowedSessions);

        var sessionsByTriplet = TransactionalMapForTripletUtils.create(triplet, uuid, 0, false);

        when(hazelcastConfig.getSessionsByTriplet(any())).thenReturn(sessionsByTriplet);
        doNothing().when(locksByTriplet).lock(locksByTripletCaptor.capture());
        when(locksByTriplet.isLocked(locksByTripletCaptor.capture())).thenReturn(true);
        doNothing().when(locksByTriplet).unlock(locksByTripletCaptor.capture());

        // WHEN
        sessionManagerAcquireService.acquireSessionsForTriplet(sessionRequestInfo, maximumAllowedSessions, 15000L, uuid, transactionContext);

        assertThat(locksByTripletCaptor.getAllValues()).asList()
                .hasSize(3)
                .containsExactly(getTripletAsKey(triplet), getTripletAsKey(triplet), getTripletAsKey(triplet));

        verify(locksByTriplet, times(1)).lock(any());
        verify(locksByTriplet, times(1)).isLocked(any());
        verify(locksByTriplet, times(1)).unlock(any());

        assertThat(sessionsByTriplet.size()).isEqualTo(sessionsWanted);
    }
}

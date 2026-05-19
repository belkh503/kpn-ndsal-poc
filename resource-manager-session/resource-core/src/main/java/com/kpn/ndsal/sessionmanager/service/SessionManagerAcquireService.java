package com.kpn.ndsal.sessionmanager.service;

import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKey;
import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKeyWithIndex;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.model.SessionRequestInfo;
import com.kpn.ndsal.sessionmanager.util.HazelcastServiceUtility;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagerAcquireService {

    private final HazelcastConfig hazelcastConfig;
    private final IMap<String, Boolean> locksByTriplet;

    /**
     *
     * @return              true, if the number of acquired sessions is equal to the number of requested sessions.
     *
     * */
    public boolean acquireSessionsForTriplet(SessionRequestInfo requestInfo, int maximumAllowedSessions, long timeoutMilliSec, UUID uuid, TransactionContext hazelcastContext) {
        var sessionsAcquired = 0;
        var triplet = requestInfo.getTriplet();

        try {
            log.debug("Going to acquire lock for triplet {}", triplet);

            locksByTriplet.lock(getTripletAsKey(triplet));

            int availableSessions = getAvailableSessions(maximumAllowedSessions, hazelcastConfig.getSessionsByTriplet(hazelcastContext), triplet);
            if (availableSessions >= requestInfo.getSessionsWanted()) {
                for (int index = 0; index < maximumAllowedSessions && sessionsAcquired < requestInfo.getSessionsWanted(); index++) {
                    var sessionKey = getTripletAsKeyWithIndex(triplet, index);
                    if (!hazelcastConfig.getSessionsByTriplet(hazelcastContext).containsKey(sessionKey)) {
                        // save by triplet only
                        saveSession(triplet, timeoutMilliSec, uuid, hazelcastContext, index, sessionKey);
                        sessionsAcquired++;
                    }
                }
            }
        } finally {
            HazelcastServiceUtility.unlockIfLocked(locksByTriplet, getTripletAsKey(triplet));
        }

        return sessionsAcquired == requestInfo.getSessionsWanted();
    }

    private void saveSession(TripletEntity triplet, long timeoutMilliSec, UUID uuid, TransactionContext hazelcastContext, int index, String sessionKey) {
        SessionEntity session = new SessionEntity(uuid, index, timeoutMilliSec, System.currentTimeMillis(), triplet);
        hazelcastConfig.getSessionsByTriplet(hazelcastContext).put(sessionKey, session);
    }

    /**
     * Calculate amount of sessions slots that are available for acquiring. If amount of available session slot is less than requested, acquisition is not possible.
     *
     * @param maximumAllowedSessions    the maximum amount of sessions that can be acquired for given domain and system type
     * @param sessions                  sessions
     * @param triplet                   triplet
     *
     * @return                          amount of sessions slots that are available for acquiring
     * */
    private static int getAvailableSessions(int maximumAllowedSessions, TransactionalMap<String, SessionEntity> sessions, TripletEntity triplet) {
        int sessionsInUse = 0;

        for (int index = 0; index < maximumAllowedSessions; index++) {
            if (sessions.containsKey(getTripletAsKeyWithIndex(triplet, index)))
                sessionsInUse++;
        }

        return maximumAllowedSessions - sessionsInUse;
    }
}

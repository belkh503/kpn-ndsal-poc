package com.kpn.ndsal.sessionmanager.service;

import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKeyWithIndex;
import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.isSessionTimedOut;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class SessionManagerReleaseService {

    private final HazelcastConfig hazelcastConfig;

    public void releaseExpiredSessionsByTriplet(TripletEntity triplet, int maximumAllowedSessions,
            TransactionContext hazelcastContext) {
        var sessionsByTriplet = hazelcastConfig.getSessionsByTriplet(hazelcastContext);
        for (int index = 0; index < maximumAllowedSessions; index++) {
            var session = sessionsByTriplet.get(getTripletAsKeyWithIndex(triplet, index));
            if (isSessionTimedOut(session)) {
                releaseSessions(session.uuid(), hazelcastContext);
            }
        }
    }

    public List<UUID> releaseSessions(List<UUID> uuids, TransactionContext hazelcastContext) {
        var releasedUuids = new ArrayList<UUID>();

        uuids.forEach(uuid -> {
            if (releaseSessions(uuid, hazelcastContext)) {
                log.info("uuid '{}' has been released", uuid);
                releasedUuids.add(uuid);
            } else {
                log.debug("Session with uuid '{}' has already been released or doesn't exist", uuid);
            }
        });

        return releasedUuids;
    }

    public boolean releaseSessions(UUID uuid, TransactionContext hazelcastContext) {
        var sessions = hazelcastConfig.getSessionsByTriplet(hazelcastContext)
                                       .values(entry -> entry.getValue().uuid().equals(uuid));

        for (var session : sessions) {
            String sessionKey = getTripletAsKeyWithIndex(session.triplet(), session.index());
            hazelcastConfig.getSessionsByTriplet(hazelcastContext).remove(sessionKey);
        }

        return !sessions.isEmpty();
    }
}

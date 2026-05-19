package com.kpn.ndsal.sessionmanager.task;

import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.isSessionTimedOut;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.entity.RequestStatus;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
public class TimeoutCleanerTask {

    private final HazelcastConfig hazelcastConfig;
    private final SessionManagerReleaseService releaseService;
    private final AcquireExecutorService acquireExecutorService;

    @Scheduled(cron = "${cronExpression}")
    public void release() {

        boolean hasReleasedUuids;

        var hazelcastContext = hazelcastConfig.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            var requests = hazelcastConfig.getRequestsByUuid(hazelcastContext).values(
                    entry -> entry.getValue().getRequestStatus() == RequestStatus.ACQUIRED
            );

            var acquiredUuids = requests.stream().map(InternalRequest::getUuid).toList();
            var uuidToRelease = computeUuidsToRelease(acquiredUuids, hazelcastContext);
            var releasedUuids = releaseService.releaseSessions(uuidToRelease, hazelcastContext);

            releasedUuids.forEach(hazelcastConfig.getRequestsByUuid(hazelcastContext)::remove);

            hazelcastContext.commitTransaction();

            hasReleasedUuids = !releasedUuids.isEmpty();

            log.info("TimeoutCleaner Task released '{}' uuids at {}", releasedUuids.size(), LocalTime.now());
        } catch (Throwable throwable) {
            hazelcastContext.rollbackTransaction();

            throw throwable;
        }

        if (hasReleasedUuids || acquireExecutorService.getExceptionDuringProcess().get()) {
            acquireExecutorService.startAcquire();
        }
    }

    private List<UUID> computeUuidsToRelease(List<UUID> acquiredUuids, TransactionContext hazelcastContext) {
        var uuidToRelease = new ArrayList<UUID>();

        for (var uuid : acquiredUuids) {
            var sessions = hazelcastConfig.getSessionsByTriplet(hazelcastContext)
                    .values(entry -> entry.getValue().uuid().equals(uuid));

            log.info("computeUuidsToRelease: {} sessions found for id = {}. These sessions will be released.", sessions.size(), uuid);

            sessions.stream().findFirst().ifPresent(sessionEntity -> {
                if (isSessionTimedOut(sessionEntity)) {
                    uuidToRelease.add(sessionEntity.uuid());
                }
            });
        }

        return uuidToRelease;
    }
}

package com.kpn.ndsal.sessionmanager.task;

import org.springframework.stereotype.Component;

import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.kafka.release.ReleaseResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseResponseDto;
import com.kpn.ndsal.sessionmanager.service.AcquireExecutorService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class ReleaseTask {

    private final HazelcastConfig hazelcastConfig;
    private final SessionManagerReleaseService releaseService;
    private final ReleaseResponseProducer releaseResponseProducer;
    private final AcquireExecutorService acquireExecutorService;

    public void startRelease(SessionReleaseRequestDto sessionsReleaseRequest, String correlationId) {
        var hazelcastContext = hazelcastConfig.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            var uuidsReleased = releaseService.releaseSessions(sessionsReleaseRequest.getUuids(), hazelcastContext);

            var sessionReleasedResponse = new SessionReleaseResponseDto();
            sessionReleasedResponse.setUuids(uuidsReleased);

            releaseResponseProducer.sendMessage(sessionReleasedResponse, correlationId);

            if (!uuidsReleased.isEmpty()) {
                acquireExecutorService.startAcquire();
            }

            hazelcastContext.commitTransaction();
        } catch (Throwable throwable) {
            hazelcastContext.rollbackTransaction();
            throw throwable;
        }
    }
}

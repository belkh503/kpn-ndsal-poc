package com.kpn.ndsal.sessionmanager.task;

import static java.util.EnumSet.allOf;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.MDC;

import com.hazelcast.transaction.TransactionContext;
import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.entity.RequestStatus;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto.Priority;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;
import com.kpn.ndsal.sessionmanager.model.SessionRequestInfo;
import com.kpn.ndsal.sessionmanager.model.SessionResponseInfo;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerAcquireService;
import com.kpn.ndsal.sessionmanager.service.SessionManagerReleaseService;
import com.kpn.ndsal.sessionmanager.util.MDCConstants;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class AcquireTask implements Runnable {

    private final HazelcastConfig hazelcastConfig;
    private final RequestValidationService validationService;
    private final SessionManagerAcquireService acquireService;
    private final SessionManagerReleaseService releaseService;
    private final AcquireResponseProducer acquireResponseProducer;
    private final DomainsSessionConfig domains;
    private AtomicBoolean exceptionDuringProcess;

    @Override
    public void run() {
        log.info("started scanning internal storage for acquire");

        allOf(Priority.class).forEach(this::tryAcquire);
    }

    private void tryAcquire(Priority priority) {
        log.trace("tryAcquire: {}", priority);

        var hazelcastContext = hazelcastConfig.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            var requests = hazelcastConfig.getRequestsByUuid(hazelcastContext)
                                           .values(entry -> entry.getValue().getPriority() == priority
                                                   && entry.getValue().getRequestStatus() == RequestStatus.NEW);

            for (var request : requests) {
                acquireRequest(hazelcastContext, request);
            }

            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            log.error("A technical exception happen during Process queue", e);
            exceptionDuringProcess.set(true);
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Acquire request and update status from NEW to ACQUIRED
     *
     * @param hazelcastContext      Hazelcast transactional context
     * @param request               Internal Request structure
     * */
    private void acquireRequest(TransactionContext hazelcastContext, InternalRequest request) {
        try {
            MDC.put(MDCConstants.MCD_CORRELATION_ID_KEY, request.getCorrelationId());
            if (acquireRequest(request)) {
                log.debug("acquire task with correlationId '{}' has been processed", request.getCorrelationId());

                request.setRequestStatus(RequestStatus.ACQUIRED);
                hazelcastConfig.getRequestsByUuid(hazelcastContext).replace(request.getUuid(), request);
            }
        } catch (InterruptedException e) {
            log.error("error locking/unlocking", e);
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("error locking/unlocking", e);
        } finally {
            MDC.remove(MDCConstants.MCD_CORRELATION_ID_KEY);
        }
    }

    private boolean acquireRequest(InternalRequest request) throws ExecutionException, InterruptedException {
        var sessionsRequestInfo = validationService.getSessionsRequestInfo(request.getSessionsInfo());

        var sessionResponseInfoDto = acquireSessions(sessionsRequestInfo, request.getTimeoutSec() * 1000L,
                request.getUuid());

        if (sessionResponseInfoDto.isSessionAcquired()) {
            acquireResponseProducer.sendMessage(buildAcquireResponseForKafka(sessionResponseInfoDto),
                    request.getCorrelationId());
        }

        return sessionResponseInfoDto.isSessionAcquired();
    }

    private SessionResponseInfo acquireSessions(Collection<SessionRequestInfo> sessionRequestInfoList,
            long timeoutMilliSec, UUID uuid) throws ExecutionException, InterruptedException {
        log.debug("started the actual acquiry process: {}", uuid);

        var completableFuture = CompletableFuture.supplyAsync(
                () -> acquireSessionFuture(sessionRequestInfoList, timeoutMilliSec, uuid));

        var allSessionsAcquired = completableFuture.get();

        return handleResponse(uuid, allSessionsAcquired);
    }

    private boolean acquireSessionFuture(Collection<SessionRequestInfo> sessionRequestInfoList, long timeoutMilliSec,
            UUID uuid) {

        var hazelcastContext = hazelcastConfig.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            var allSessionsAcquired = true;
            for (SessionRequestInfo sessionRequestInfo : sessionRequestInfoList) {
                var maxSessions = domains.getMaxSessions(sessionRequestInfo.getTriplet());

                releaseService.releaseExpiredSessionsByTriplet(sessionRequestInfo.getTriplet(), maxSessions,
                        hazelcastContext);

                allSessionsAcquired &= acquireService.acquireSessionsForTriplet(sessionRequestInfo, maxSessions,
                        timeoutMilliSec, uuid, hazelcastContext);
            }

            if (allSessionsAcquired) {
                hazelcastContext.commitTransaction();
            } else {
                hazelcastContext.rollbackTransaction();
            }

            return allSessionsAcquired;
        } catch (Throwable throwable) {
            hazelcastContext.rollbackTransaction();
            throw throwable;
        }
    }

    private SessionAcquireResponseDto buildAcquireResponseForKafka(SessionResponseInfo sessionResponseInfoDto) {
        var acquireResponse = new SessionAcquireResponseDto();
        acquireResponse.setSessionAcquired(sessionResponseInfoDto.isSessionAcquired());
        acquireResponse.setUuid(sessionResponseInfoDto.getUuid());

        return acquireResponse;
    }

    private SessionResponseInfo handleResponse(UUID uuid, boolean allSessionsAcquired) {
        var sessionResponseInfo = new SessionResponseInfo();
        sessionResponseInfo.setUuid(uuid);
        sessionResponseInfo.setSessionAcquired(allSessionsAcquired);

        return sessionResponseInfo;
    }
}

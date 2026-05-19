package com.kpn.ndsal.resourcemanager.application.service;

import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.RESOURCES_LOCKED;
import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.RESOURCES_ALREADY_LOCKED;
import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.RESOURCES_LOCK_FAILED;
import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.ERROR;
import static java.util.Objects.nonNull;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import com.kpn.ndsal.resourcemanager.application.port.in.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.adapter.out.kafka.AcquireLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;
import com.kpn.ndsal.resourcemanager.model.Timeout;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class RequestProcessorService implements RequestProcessorUseCase {

    private final AcquireLockUseCase acquireLockUseCase;
    private final AcquireLockResponseProducer acquireLockResponseProducer;
    private final RequestMapper requestMapper;
    private final long defaultQueueItemTimeout;
    private final LocalDateTime defaultLockTimeout;
    private final ContextService contextService;

    public RequestProcessorService(AcquireLockUseCase acquireLockUseCase,
            AcquireLockResponseProducer acquireLockResponseProducer,
            RequestMapper requestMapper, ContextService contextService,
            @Value("${requestQueue.timeout.value:1}") int requestTimeoutValue,
            @Value("${requestQueue.timeout.unit:hour}") String requestTimeoutUnit,
            @Value("${lock.defaultTimeout.value:1}") int defaultLockTimeoutValue,
            @Value("${lock.defaultTimeout.unit:day}") String defaultLockTimeoutUnit) {
        this.acquireLockUseCase = acquireLockUseCase;
        this.acquireLockResponseProducer = acquireLockResponseProducer;
        this.requestMapper = requestMapper;
        this.contextService = contextService;
        this.defaultQueueItemTimeout = toMilliSeconds(requestTimeoutValue, requestTimeoutUnit);
        this.defaultLockTimeout = toLocalDateTime(defaultLockTimeoutValue, defaultLockTimeoutUnit);
    }

    @Override
    public void processRequest(RequestQueueEntity requestQueueEntity) {
        contextService.setContext(requestQueueEntity);
        var acquireLockRequestDto = requestMapper.toAcquireLockRequestDto(requestQueueEntity);
        var timesOutAt = nonNull(requestQueueEntity.getTimeout()) ? toLocalDateTime(requestQueueEntity.getTimeout())
                : defaultLockTimeout;

        var command = LockRequestCommandMapper.map(acquireLockRequestDto);

        AcquireLockResponseDto response = new AcquireLockResponseDto();

        try {
            response.lockId = acquireLockUseCase.acquireLock(command, acquireLockRequestDto.getDomain(), requestQueueEntity.getCorrelationId(),
                    timesOutAt).toString();
            response.acquireStatus = RESOURCES_LOCKED;

        }catch (RuntimeException e) {
            if (e instanceof AcquireLockNotPossibleException) {
                throw new AcquireLockNotPossibleException();
            }else if(e instanceof AcquireLockInvalidRequestException ||
                e instanceof  AcquireLockWrongDomainException){
                response.acquireStatus = ERROR;
                log.warn("Acquire lock request validation/domain error. correlationId={}", requestQueueEntity.getCorrelationId(), e);
            }else if(e instanceof DatabaseConnectionException){
                response.acquireStatus = RESOURCES_LOCK_FAILED;
                log.error("Database connection error while acquiring/removing lock. correlationId={}", requestQueueEntity.getCorrelationId(), e);
            }else {
                response.acquireStatus = ERROR;
                log.error("Unexpected error while acquiring/removing lock. correlationId={}", requestQueueEntity.getCorrelationId(), e);
            }
        }
        acquireLockResponseProducer.sendMessage(response, requestQueueEntity.getCorrelationId());
    }

    @Override
    public void processRequestTimeout(RequestQueueEntity requestQueueEntity) {
        contextService.setContext(requestQueueEntity);
        if (!requestTimeoutExceeded(requestQueueEntity)) {
            return;
        }

        AcquireLockResponseDto response = new AcquireLockResponseDto();
        response.acquireStatus = RESOURCES_ALREADY_LOCKED;
        acquireLockResponseProducer.sendMessage(response, requestQueueEntity.getCorrelationId());

        throw new RequestTimeoutExceededException();
    }

    private boolean requestTimeoutExceeded(RequestQueueEntity requestQueueEntity) {
        return System.currentTimeMillis() - requestQueueEntity.getCreationTime() > defaultQueueItemTimeout;
    }

    private LocalDateTime toLocalDateTime(Timeout timeout) {
        return toLocalDateTime(timeout.getValue(), timeout.getUnit().value());
    }

    private LocalDateTime toLocalDateTime(int timeoutValue, String timeoutUnit) {
        var timeoutMillis = toMilliSeconds(timeoutValue, timeoutUnit);
        var timeoutInstant = Instant.ofEpochMilli(System.currentTimeMillis() + timeoutMillis);
        var zoneId = ZoneId.systemDefault();
        return timeoutInstant.atZone(zoneId).toLocalDateTime();
    }

    private long toMilliSeconds(int timeoutValue, String timeoutUnit) {
        var multiplicationFactor = switch (timeoutUnit) {
            case "day" -> 86400;
            case "hour" -> 3600;
            case "min" -> 60;
            default -> 1;
        };
        return timeoutValue * multiplicationFactor * 1000L;
    }
}

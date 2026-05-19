package com.kpn.ndsal.resourcemanager.application.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.ReleaseLockUseCase;
import com.kpn.ndsal.resourcemanager.application.service.LoadLocksService;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@Service
public class LockTimeoutCleanerTask {
    private final ReleaseLockUseCase releaseLockUseCase;
    private final ProcessQueueExecutorService processQueueExecutorService;
    private final LoadLocksService loadLocksService;

    @Scheduled(cron = "${lock.cleanerCronExpression}")
    public void startLockCleaner() {
        log.info("Scheduled lock cleaner");
        var expiredLocks = loadLocksService.loadExpired();
        expiredLocks.forEach(lockRequest -> {
            log.error("LockCleaner: timed out lock for correlationId: {}", lockRequest.getCorrelationId());
            releaseLockUseCase.release(lockRequest.getId());
        });
        if (!expiredLocks.isEmpty() || processQueueExecutorService.getExceptionDuringProcess().get()) {
            processQueueExecutorService.startProcessing();
        }
    }
}

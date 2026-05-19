package com.kpn.ndsal.resourcemanager.application.task;

import java.util.concurrent.atomic.AtomicBoolean;

import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockNotPossibleException;
import com.kpn.ndsal.resourcemanager.application.service.RequestProcessorService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@AllArgsConstructor
public class ProcessQueueTask implements Runnable {

    private final HazelcastConfigurator hazelcastConfigurator;
    private final RequestProcessorService requestProcessorService;
    private AtomicBoolean exceptionDuringProcess;

    @Override
    public void run() {
        log.info("Process queue");
        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();

        hazelcastContext.beginTransaction();

        try {
            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext)
                    .values()
                    .stream()
                    .sorted()
                    .forEach(requestQueueEntity -> {
                        try {
                            requestProcessorService.processRequest(requestQueueEntity);
                            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).remove(requestQueueEntity.getId());
                        } catch (AcquireLockNotPossibleException ignored) {
                            log.debug("Lock not possible for request queue item {}", requestQueueEntity.getCorrelationId());
                        }
                    });
            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            log.error("A technical exception happen during Process queue", e);
            exceptionDuringProcess.set(true);
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }
}

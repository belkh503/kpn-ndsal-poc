package com.kpn.ndsal.resourcemanager.application.task;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestTimeoutExceededException;
import com.kpn.ndsal.resourcemanager.application.service.RequestProcessorService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueTimeoutCleanerTask {

    private final HazelcastConfigurator hazelcastConfigurator;
    private final RequestProcessorService requestProcessorService;

    @Scheduled(cron = "${requestQueue.cleanerCronExpression}")
    public void startQueueCleaner() {
        log.info("Scheduled queue cleaner");
        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();

        hazelcastContext.beginTransaction();

        try {
            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext)
                    .values()
                    .stream()
                    .sorted()
                    .forEach(requestQueueEntity -> {
                        try {
                            requestProcessorService.processRequestTimeout(requestQueueEntity);
                        } catch (RequestTimeoutExceededException e) {
                            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).remove(requestQueueEntity.getId());
                        }
                    });
            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }
}

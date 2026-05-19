package com.kpn.ndsal.resourcemanager.application.service;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestQueueUseCase;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestQueueService implements RequestQueueUseCase {

    private final HazelcastConfigurator hazelcastConfigurator;

    @Override
    public void addRequestToQueue(RequestQueueEntity requestQueueEntity) {
        log.info("request will be added to queue");

        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).put(requestQueueEntity.getId(), requestQueueEntity);

            hazelcastContext.commitTransaction();
        } catch (Exception exception) {
            hazelcastContext.rollbackTransaction();
            throw exception;
        }
    }

    @Override
    public void deleteRequestFromQueue(RequestQueueEntity requestQueueEntity) {
        log.info("request will be removed to queue");

        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();
        hazelcastContext.beginTransaction();

        try {
            hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).remove(requestQueueEntity.getId());

            hazelcastContext.commitTransaction();
        } catch (Exception exception) {
            hazelcastContext.rollbackTransaction();
            throw exception;
        }
    }
}

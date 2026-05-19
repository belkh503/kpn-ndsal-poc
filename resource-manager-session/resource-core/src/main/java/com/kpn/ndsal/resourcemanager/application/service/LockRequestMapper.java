package com.kpn.ndsal.resourcemanager.application.service;

import static java.lang.Boolean.TRUE;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

interface LockRequestMapper {

    static LockRequest map(AcquireLockCommand command, String domain, String correlationId, LocalDateTime timesOutAt) {

        return LockRequest.builder()
                .request(null)
                .correlationId(correlationId)
                .created(LocalDateTime.now())
                .timesOutAt(timesOutAt)
                .domain(domain)
                .lockObjectEntities(command.getLockGroups().stream()
                        .map(p -> p.getLockObjects().stream())
                        .reduce(Stream::concat)
                        .orElseGet(Stream::empty)
                        .map(p -> LockRequest.LockObject.builder()
                                .name(p.getType())
                                .lockType(TRUE.equals(p.getForce()) ? LockRequest.LockType.EXCLUSIVE : LockRequest.LockType.SHARED)
                                .build())
                        .toList())
                .build();
    }

    static LockRequest map(List<AcquireLockCommand.LockObject> lockObjects, String domain, String correlationId,
            LocalDateTime timesOutAt) {

        return LockRequest.builder()
                .request(null)
                .correlationId(correlationId)
                .created(LocalDateTime.now())
                .timesOutAt(timesOutAt)
                .domain(domain)
                .lockObjectEntities(lockObjects.stream()
                        .map(p -> LockRequest.LockObject.builder()
                                .name(p.getId())
                                .type(p.getType())
                                .lockType(TRUE.equals(p.getForce()) ? LockRequest.LockType.EXCLUSIVE : LockRequest.LockType.SHARED)
                                .build())
                        .toList())
                .build();
    }

}

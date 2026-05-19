package com.kpn.ndsal.resourcemanager.application.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.StatusLockQuery;
import com.kpn.ndsal.resourcemanager.application.port.out.LoadLockRequestPort;
import com.kpn.ndsal.resourcemanager.common.UseCase;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Service
@AllArgsConstructor
class StatusLockService implements StatusLockQuery {

    private LoadLockRequestPort loadLockRequestPort;

    @Override
    public Optional<LockRequest> findLock(UUID lockId) {
        log.debug("StatusLockService::findLock: {}", lockId);

        return loadLockRequestPort.findLockRequest(lockId);
    }
}

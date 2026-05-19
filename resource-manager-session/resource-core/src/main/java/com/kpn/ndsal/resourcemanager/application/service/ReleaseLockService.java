package com.kpn.ndsal.resourcemanager.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.ReleaseLockUseCase;
import com.kpn.ndsal.resourcemanager.application.port.out.DeleteLockPort;
import com.kpn.ndsal.resourcemanager.common.UseCase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Service
@AllArgsConstructor
public class ReleaseLockService implements ReleaseLockUseCase {

    private DeleteLockPort deleteLockPort;

    @Override
    public void release(UUID lockId) {
        log.trace("ReleaseLockService::release: {}", lockId);

        deleteLockPort.deleteLockRequest(lockId);
    }
}

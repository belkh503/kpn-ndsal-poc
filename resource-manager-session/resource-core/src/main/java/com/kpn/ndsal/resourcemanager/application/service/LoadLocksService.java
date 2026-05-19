package com.kpn.ndsal.resourcemanager.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.LoadLocksUseCase;
import com.kpn.ndsal.resourcemanager.application.port.out.LoadLocksPort;
import com.kpn.ndsal.resourcemanager.common.UseCase;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Service
@AllArgsConstructor
public class LoadLocksService implements LoadLocksUseCase {

    private LoadLocksPort loadLocksPort;

    @Override
    public List<LockRequest> loadExpired() {
        log.trace("LoadLocksService::loadExpired");

        return loadLocksPort.findAllExpiredLocks();
    }
}

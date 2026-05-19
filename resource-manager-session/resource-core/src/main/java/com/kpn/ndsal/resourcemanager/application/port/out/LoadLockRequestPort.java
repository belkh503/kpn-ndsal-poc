package com.kpn.ndsal.resourcemanager.application.port.out;

import java.util.Optional;
import java.util.UUID;

import org.springframework.lang.NonNull;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@FunctionalInterface
public interface LoadLockRequestPort {

    @NonNull
    Optional<LockRequest> findLockRequest(UUID lockId);

}

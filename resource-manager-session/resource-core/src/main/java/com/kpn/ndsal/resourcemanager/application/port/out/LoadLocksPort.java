package com.kpn.ndsal.resourcemanager.application.port.out;

import java.util.List;

import org.springframework.lang.NonNull;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@FunctionalInterface
public interface LoadLocksPort {

    @NonNull
    List<LockRequest> findAllExpiredLocks();
}

package com.kpn.ndsal.resourcemanager.application.port.out;

import java.util.UUID;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@FunctionalInterface
public interface SaveLockRequestPort {

    UUID saveLockRequest(LockRequest lockRequest);

}

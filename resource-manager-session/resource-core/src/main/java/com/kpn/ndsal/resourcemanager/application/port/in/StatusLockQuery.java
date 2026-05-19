package com.kpn.ndsal.resourcemanager.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

public interface StatusLockQuery {

    Optional<LockRequest> findLock(UUID lockId);

}

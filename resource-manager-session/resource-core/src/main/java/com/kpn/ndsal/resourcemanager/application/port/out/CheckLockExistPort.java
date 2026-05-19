package com.kpn.ndsal.resourcemanager.application.port.out;

import java.util.List;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@FunctionalInterface
public interface CheckLockExistPort {

    boolean checkLockRequest(String domain, List<LockRequest.LockObject> resources);

}

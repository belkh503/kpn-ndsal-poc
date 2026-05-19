package com.kpn.ndsal.resourcemanager.application.port.out;

import java.util.UUID;

@FunctionalInterface
public interface DeleteLockPort {

    void deleteLockRequest(UUID lockId);

}

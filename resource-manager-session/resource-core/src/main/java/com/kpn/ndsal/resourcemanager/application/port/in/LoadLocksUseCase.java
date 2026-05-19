package com.kpn.ndsal.resourcemanager.application.port.in;

import java.util.List;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

public interface LoadLocksUseCase {

    List<LockRequest> loadExpired();
}

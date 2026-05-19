package com.kpn.ndsal.resourcemanager.application.port.in;

import java.util.UUID;

public interface ReleaseLockUseCase {

    void release(UUID lockId);

}

package com.kpn.ndsal.resourcemanager.model;

import java.util.UUID;

public class AcquireLockResponseDto {
    public enum AcquireStatus {
        RESOURCES_LOCKED,
        RESOURCES_ALREADY_LOCKED,
        RESOURCES_LOCK_FAILED,
        ERROR
    }
    public AcquireStatus acquireStatus;
    public String lockId;
    public String errorMessage;

    public AcquireStatus getAcquireStatus() { return acquireStatus; }
    public void setAcquireStatus(AcquireStatus acquireStatus) { this.acquireStatus = acquireStatus; }
    public String getLockId() { return lockId; }
    public void setLockId(UUID lockId) { this.lockId = lockId != null ? lockId.toString() : null; }
    public void setLockId(String lockId) { this.lockId = lockId; }
    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
}

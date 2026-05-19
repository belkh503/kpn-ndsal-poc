package com.kpn.ndsal.resourcemanager.model;

public class DeleteLockResponseDto {
    public enum DeleteStatus {
        ERROR,
        LOCK_REMOVED
    }
    public DeleteStatus deleteStatus;
    public String errorMessage;
}

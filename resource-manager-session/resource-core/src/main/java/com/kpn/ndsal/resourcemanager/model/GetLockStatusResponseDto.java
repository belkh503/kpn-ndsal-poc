package com.kpn.ndsal.resourcemanager.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class GetLockStatusResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public String lockId;
    public LockStatus status;
    public String domain;
    public String correlationId;
    public LocalDateTime created;
    public LocalDateTime timesOutAt;
    public List<LockObjectDto> lockObjects;
    public String errorMessage;

    public enum LockStatus {
        ACTIVE,
        NOT_FOUND,
        ERROR
    }

    public static class LockObjectDto implements Serializable {
        private static final long serialVersionUID = 1L;

        public String name;
        public String type;
        public String lockType;
    }
}

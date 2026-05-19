package com.kpn.ndsal.sessionmanager.model;

import java.io.Serializable;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionAcquireRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private Priority priority;
    private int timeoutSec;
    private List<SessionInfo> sessionsInfo;

    public enum Priority {
        LOW, MEDIUM, HIGH
    }
}

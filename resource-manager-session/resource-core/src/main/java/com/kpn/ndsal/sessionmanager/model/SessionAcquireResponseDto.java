package com.kpn.ndsal.sessionmanager.model;

import java.io.Serializable;
import java.util.UUID;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionAcquireResponseDto implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean sessionAcquired;
    private UUID uuid;
    private ErrorDto errorDto;

    /** Explicit getter to support both getSessionAcquired() and isSessionAcquired() naming. */
    public boolean getSessionAcquired() { return sessionAcquired; }
}

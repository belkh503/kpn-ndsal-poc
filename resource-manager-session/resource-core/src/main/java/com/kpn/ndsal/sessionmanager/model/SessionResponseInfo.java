package com.kpn.ndsal.sessionmanager.model;

import lombok.Data;

import java.util.UUID;

@Data
public class SessionResponseInfo {
    private boolean sessionAcquired;
    private UUID uuid;
}

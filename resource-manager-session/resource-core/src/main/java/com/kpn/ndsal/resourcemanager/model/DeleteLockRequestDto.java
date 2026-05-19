package com.kpn.ndsal.resourcemanager.model;

import java.io.Serializable;
import java.util.UUID;

public class DeleteLockRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public UUID lockId;
    public String sessionId;
}

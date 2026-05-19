package com.kpn.ndsal.sessionmanager.entity;

import java.io.Serializable;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record SessionEntity(UUID uuid, int index, long timeoutMilliSec, long lockedEpoch,
                            TripletEntity triplet) implements Serializable, Comparable<SessionEntity> {
    @Override
    public int compareTo(@NotNull SessionEntity o) {
        return uuid.compareTo(o.uuid);
    }
}

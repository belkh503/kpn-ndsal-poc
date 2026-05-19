package com.kpn.ndsal.resourcemanager.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Builder;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class LockRequest {

    UUID id;

    String domain;

    String correlationId;

    LocalDateTime created;

    LocalDateTime timesOutAt;

    boolean released;

    String request;

    List<LockObject> lockObjectEntities;

    @Value
    @Builder(toBuilder = true)
    public static class LockObject {

        UUID id;

        String name;

        String type;

        LockRequest.LockType lockType;
    }

    public enum LockType {

        SHARED,

        EXCLUSIVE

    }

}

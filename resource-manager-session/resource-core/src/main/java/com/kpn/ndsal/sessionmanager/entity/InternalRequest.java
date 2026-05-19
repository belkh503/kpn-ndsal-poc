package com.kpn.ndsal.sessionmanager.entity;

import java.io.Serializable;
import java.util.UUID;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = false)
public class InternalRequest extends SessionAcquireRequestDto implements Serializable, Comparable<InternalRequest> {
    private UUID uuid;
    private Long creationTime;
    private String correlationId;
    private RequestStatus requestStatus;

    public InternalRequest() {
        super();
    }

    public InternalRequest(SessionAcquireRequestDto request, String correlationId) {
        this.uuid = UUID.randomUUID();
        this.creationTime = System.currentTimeMillis();
        this.correlationId = correlationId;
        this.requestStatus = RequestStatus.NEW;

        setPriority(request.getPriority());
        setSessionsInfo(request.getSessionsInfo());
        setTimeoutSec(request.getTimeoutSec());
    }

    @Override
    public int compareTo(@NotNull InternalRequest o) {
        return creationTime.compareTo(o.creationTime);
    }

}

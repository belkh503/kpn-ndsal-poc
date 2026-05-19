package com.kpn.ndsal.resourcemanager.application.queue.out.persistence;

import java.io.Serializable;
import java.util.HashMap;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RequestQueueEntity extends AcquireLockRequestDto implements Serializable, Comparable<RequestQueueEntity> {

    @NotNull
    private UUID id;

    @NotNull
    private Long creationTime;

    @NotNull
    public String correlationId;

    @Null
    public HashMap<String, String> context;

    @Override
    public int compareTo(@NotNull RequestQueueEntity requestQueueEntity) {
        AcquireLockRequestDto.Priority thisPriority = getPriority() != null ? getPriority() : AcquireLockRequestDto.Priority.LOW;
        AcquireLockRequestDto.Priority otherPriority = requestQueueEntity.getPriority() != null ? requestQueueEntity.getPriority() : AcquireLockRequestDto.Priority.LOW;

        // Reversed so that HIGH priority is processed first
        int priorityOrder = otherPriority.compareTo(thisPriority);

        if (priorityOrder != 0) {
            return priorityOrder;
        }

        return getCreationTime().compareTo(requestQueueEntity.getCreationTime());
    }

}

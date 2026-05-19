package com.kpn.ndsal.resourcemanager.application.service;

import static java.util.Objects.nonNull;

import java.util.HashMap;
import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;

import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.TextMapSetter;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR,
        unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface RequestMapper {

    AcquireLockRequestDto toAcquireLockRequestDto(RequestQueueEntity requestQueueEntity);

    @Mapping(target = "id", expression = "java(java.util.UUID.randomUUID())")
    @Mapping(target = "creationTime", expression = "java(System.currentTimeMillis())")
    @Mapping(target = "correlationId", source = "correlationId")
    @Mapping(target = "context", source = "context", qualifiedByName = "contextToBeMapped")
    RequestQueueEntity toRequestQueueEntity(AcquireLockRequestDto acquireLockRequestDto, String correlationId,
            Context context);

    @org.mapstruct.Named("contextToBeMapped")
    default HashMap<String, String> contextToBeMapped(Context context) {
        var carrier = new HashMap<String, String>();
        if (nonNull(context)) {
            TextMapSetter<Map<String, String>> setter = (c, k, v) -> c.put(k, v);
            W3CTraceContextPropagator.getInstance().inject(context, carrier, setter);
        }
        return carrier;
    }
}
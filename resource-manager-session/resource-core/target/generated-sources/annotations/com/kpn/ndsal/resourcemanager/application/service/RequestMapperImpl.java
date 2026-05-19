package com.kpn.ndsal.resourcemanager.application.service;

import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.LockGroup;
import io.opentelemetry.context.Context;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-19T12:26:06+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class RequestMapperImpl implements RequestMapper {

    @Override
    public AcquireLockRequestDto toAcquireLockRequestDto(RequestQueueEntity requestQueueEntity) {
        if ( requestQueueEntity == null ) {
            return null;
        }

        AcquireLockRequestDto acquireLockRequestDto = new AcquireLockRequestDto();

        acquireLockRequestDto.setDomain( requestQueueEntity.getDomain() );
        acquireLockRequestDto.setPriority( requestQueueEntity.getPriority() );
        acquireLockRequestDto.setTimeout( requestQueueEntity.getTimeout() );
        Set<LockGroup> set = requestQueueEntity.getLockGroups();
        if ( set != null ) {
            acquireLockRequestDto.setLockGroups( new LinkedHashSet<LockGroup>( set ) );
        }
        Map<String, Object> map = requestQueueEntity.getAdditionalProperties();
        if ( map != null ) {
            acquireLockRequestDto.setAdditionalProperties( new LinkedHashMap<String, Object>( map ) );
        }

        return acquireLockRequestDto;
    }

    @Override
    public RequestQueueEntity toRequestQueueEntity(AcquireLockRequestDto acquireLockRequestDto, String correlationId, Context context) {
        if ( acquireLockRequestDto == null && correlationId == null && context == null ) {
            return null;
        }

        RequestQueueEntity requestQueueEntity = new RequestQueueEntity();

        if ( acquireLockRequestDto != null ) {
            requestQueueEntity.setDomain( acquireLockRequestDto.getDomain() );
            requestQueueEntity.setPriority( acquireLockRequestDto.getPriority() );
            requestQueueEntity.setTimeout( acquireLockRequestDto.getTimeout() );
            Set<LockGroup> set = acquireLockRequestDto.getLockGroups();
            if ( set != null ) {
                requestQueueEntity.setLockGroups( new LinkedHashSet<LockGroup>( set ) );
            }
            Map<String, Object> map = acquireLockRequestDto.getAdditionalProperties();
            if ( map != null ) {
                requestQueueEntity.setAdditionalProperties( new LinkedHashMap<String, Object>( map ) );
            }
        }
        requestQueueEntity.setCorrelationId( correlationId );
        requestQueueEntity.setContext( contextToBeMapped( context ) );
        requestQueueEntity.setId( java.util.UUID.randomUUID() );
        requestQueueEntity.setCreationTime( System.currentTimeMillis() );

        return requestQueueEntity;
    }
}

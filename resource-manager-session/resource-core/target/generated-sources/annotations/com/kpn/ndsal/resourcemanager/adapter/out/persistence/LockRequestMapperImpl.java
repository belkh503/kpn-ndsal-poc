package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-05-19T12:26:06+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
class LockRequestMapperImpl implements LockRequestMapper {

    @Override
    public LockRequest map(LockRequestEntity entity) {
        if ( entity == null ) {
            return null;
        }

        LockRequest.LockRequestBuilder lockRequest = LockRequest.builder();

        lockRequest.correlationId( entity.getCorrelationId() );
        lockRequest.created( entity.getCreated() );
        lockRequest.domain( entity.getDomain() );
        lockRequest.id( entity.getId() );
        lockRequest.lockObjectEntities( lockObjectEntityListToLockObjectList( entity.getLockObjectEntities() ) );
        lockRequest.released( entity.isReleased() );
        lockRequest.request( entity.getRequest() );
        lockRequest.timesOutAt( entity.getTimesOutAt() );

        return lockRequest.build();
    }

    @Override
    public LockRequestEntity map(LockRequest lockRequest) {
        if ( lockRequest == null ) {
            return null;
        }

        LockRequestEntity lockRequestEntity = new LockRequestEntity();

        lockRequestEntity.setCorrelationId( lockRequest.getCorrelationId() );
        lockRequestEntity.setCreated( lockRequest.getCreated() );
        lockRequestEntity.setDomain( lockRequest.getDomain() );
        lockRequestEntity.setId( lockRequest.getId() );
        lockRequestEntity.setLockObjectEntities( lockObjectListToLockObjectEntityList( lockRequest.getLockObjectEntities() ) );
        lockRequestEntity.setReleased( lockRequest.isReleased() );
        lockRequestEntity.setRequest( lockRequest.getRequest() );
        lockRequestEntity.setTimesOutAt( lockRequest.getTimesOutAt() );

        return lockRequestEntity;
    }

    @Override
    public LockObjectEntity map(LockRequest.LockObject lockObject) {
        if ( lockObject == null ) {
            return null;
        }

        LockObjectEntity lockObjectEntity = new LockObjectEntity();

        lockObjectEntity.setId( lockObject.getId() );
        lockObjectEntity.setLockType( lockTypeToLockType1( lockObject.getLockType() ) );
        lockObjectEntity.setName( lockObject.getName() );
        lockObjectEntity.setType( lockObject.getType() );

        return lockObjectEntity;
    }

    protected LockRequest.LockType lockTypeToLockType(LockType lockType) {
        if ( lockType == null ) {
            return null;
        }

        LockRequest.LockType lockType1;

        switch ( lockType ) {
            case SHARED: lockType1 = LockRequest.LockType.SHARED;
            break;
            case EXCLUSIVE: lockType1 = LockRequest.LockType.EXCLUSIVE;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + lockType );
        }

        return lockType1;
    }

    protected LockRequest.LockObject lockObjectEntityToLockObject(LockObjectEntity lockObjectEntity) {
        if ( lockObjectEntity == null ) {
            return null;
        }

        LockRequest.LockObject.LockObjectBuilder lockObject = LockRequest.LockObject.builder();

        lockObject.id( lockObjectEntity.getId() );
        lockObject.lockType( lockTypeToLockType( lockObjectEntity.getLockType() ) );
        lockObject.name( lockObjectEntity.getName() );
        lockObject.type( lockObjectEntity.getType() );

        return lockObject.build();
    }

    protected List<LockRequest.LockObject> lockObjectEntityListToLockObjectList(List<LockObjectEntity> list) {
        if ( list == null ) {
            return null;
        }

        List<LockRequest.LockObject> list1 = new ArrayList<LockRequest.LockObject>( list.size() );
        for ( LockObjectEntity lockObjectEntity : list ) {
            list1.add( lockObjectEntityToLockObject( lockObjectEntity ) );
        }

        return list1;
    }

    protected List<LockObjectEntity> lockObjectListToLockObjectEntityList(List<LockRequest.LockObject> list) {
        if ( list == null ) {
            return null;
        }

        List<LockObjectEntity> list1 = new ArrayList<LockObjectEntity>( list.size() );
        for ( LockRequest.LockObject lockObject : list ) {
            list1.add( map( lockObject ) );
        }

        return list1;
    }

    protected LockType lockTypeToLockType1(LockRequest.LockType lockType) {
        if ( lockType == null ) {
            return null;
        }

        LockType lockType1;

        switch ( lockType ) {
            case SHARED: lockType1 = LockType.SHARED;
            break;
            case EXCLUSIVE: lockType1 = LockType.EXCLUSIVE;
            break;
            default: throw new IllegalArgumentException( "Unexpected enum constant: " + lockType );
        }

        return lockType1;
    }
}

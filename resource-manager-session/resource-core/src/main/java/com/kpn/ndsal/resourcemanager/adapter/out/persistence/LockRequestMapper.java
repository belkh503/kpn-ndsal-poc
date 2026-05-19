package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import org.mapstruct.Mapper;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@Mapper(componentModel = "spring")
interface LockRequestMapper {

    LockRequest map(LockRequestEntity entity);

    LockRequestEntity map(LockRequest lockRequest);

    LockObjectEntity map(LockRequest.LockObject lockObject);

}

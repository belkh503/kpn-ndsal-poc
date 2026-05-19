package com.kpn.ndsal.resourcemanager.application.service;

import java.util.List;
import java.util.function.Function;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.LockGroup;

public interface LockRequestCommandMapper {

    Function<LockGroup, List<AcquireLockCommand.LockObject>> converter = lockGroup -> lockGroup.lockObjects.stream()
            .map(p -> new AcquireLockCommand.LockObject(p.type, p.id, p.force))
            .toList();

    static AcquireLockCommand map(AcquireLockRequestDto request) {

        var lockResources = request.lockGroups.stream()
                .map(p -> new AcquireLockCommand.LockGroup(converter.apply(p)))
                .toList();

        return new AcquireLockCommand(lockResources);
    }

}


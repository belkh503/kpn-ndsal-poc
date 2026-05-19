package com.kpn.ndsal.resourcemanager.application.service;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.List;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;

class LockRequestMapperTest {

    @Test
    void givenAcquireLockCommand_whenMappedToDomainObject_thenEqual() {

        var command = new AcquireLockCommand(List.of(
                new AcquireLockCommand.LockGroup(
                        List.of(
                                new AcquireLockCommand.LockObject("NODE", "nl-pbl-cpe-01", false),
                                new AcquireLockCommand.LockObject("PORT", "nl-pbl-cpe-01:1/1/2", false),
                                new AcquireLockCommand.LockObject("EAS", "EAS000002", true),
                                new AcquireLockCommand.LockObject("EVA", "EVA000001", false),
                                new AcquireLockCommand.LockObject("EVS", "EVS000001", false)
                        )
                )
        ));

        var request = LockRequestMapper.map(command, "CORE", "correlation_id", LocalDateTime.now());

        assertThat(request)
                .has(new Condition<>(p -> p.getLockObjectEntities().size() == 5 && p.getDomain().equalsIgnoreCase("CORE"), "request with 5 resources for `CORE` domain"));
    }
}

package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.kpn.ndsal.resourcemanager.application.port.in.DatabaseConnectionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand.LockGroup;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand.LockObject;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockNotPossibleException;
import com.kpn.ndsal.resourcemanager.application.port.out.CheckLockExistPort;
import com.kpn.ndsal.resourcemanager.application.port.out.SaveLockRequestPort;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;
import org.springframework.transaction.TransactionSystemException;

@SpringBootTest(classes = {AcquireLockService.class})
class AcquireLockServiceTest {
    @Autowired
    AcquireLockService acquireLockService;

    @MockitoBean
    CheckLockExistPort checkLockExistPort;

    @MockitoBean
    SaveLockRequestPort saveLockRequestPort;

    @Test
    void testAcquireLock_Ok() {
        var lockObject1 = new LockObject("type", "id");
        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(lockObject1);
        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);

        var acquireLockCommand = new AcquireLockCommand(lockGroups);
        var domain = "domain";
        var correlationId = UUID.randomUUID().toString();
        var timesOutAt = LocalDateTime.now().plusMinutes(10);

        var lockObject2 = LockRequest.LockObject.builder()
                .name("id")
                .type("type")
                .lockType(LockRequest.LockType.SHARED).build();

        var expectedUuid = UUID.randomUUID();
        when(checkLockExistPort.checkLockRequest(domain.toUpperCase(), List.of(lockObject2))).thenReturn(true);
        when(saveLockRequestPort.saveLockRequest(any())).thenReturn(expectedUuid);
        var actualUuid = acquireLockService.acquireLock(acquireLockCommand, domain, correlationId, timesOutAt);

        assertEquals(expectedUuid, actualUuid);
        verify(checkLockExistPort, times(1)).checkLockRequest(domain.toUpperCase(), List.of(lockObject2));
        verify(saveLockRequestPort, times(1)).saveLockRequest(any());

    }

    @Test
    void testAcquireLock_Exception() {
        var lockObject1 = new LockObject("type", "id");
        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(lockObject1);
        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);

        var acquireLockCommand = new AcquireLockCommand(lockGroups);
        var domain = "domain";
        var correlationId = UUID.randomUUID().toString();
        var timesOutAt = LocalDateTime.now().plusMinutes(10);

        var lockObject2 = LockRequest.LockObject.builder()
                .name("id")
                .type("type")
                .lockType(LockRequest.LockType.SHARED).build();

        when(checkLockExistPort.checkLockRequest(domain.toUpperCase(), List.of(lockObject2))).thenReturn(false);
        assertThrows(AcquireLockNotPossibleException.class, () -> acquireLockService.acquireLock(acquireLockCommand, domain, correlationId, timesOutAt));

        verify(checkLockExistPort, times(1)).checkLockRequest(domain.toUpperCase(), List.of(lockObject2));
        verify(saveLockRequestPort, never()).saveLockRequest(any());
    }

    @Test
    void testAcquireLock_UnexpectedRuntimeException() {
        var lockObject1 = new LockObject("type", "id");
        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(lockObject1);
        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);

        var acquireLockCommand = new AcquireLockCommand(lockGroups);
        var domain = "domain";
        var correlationId = UUID.randomUUID().toString();
        var timesOutAt = LocalDateTime.now().plusMinutes(10);

        var lockObject2 = LockRequest.LockObject.builder()
                .name("id")
                .type("type")
                .lockType(LockRequest.LockType.SHARED).build();

        when(checkLockExistPort.checkLockRequest(domain.toUpperCase(), List.of(lockObject2))).thenThrow(new RuntimeException("Unexpected error"));
        assertThrows(RuntimeException.class, () -> acquireLockService.acquireLock(acquireLockCommand, domain, correlationId, timesOutAt));

        verify(checkLockExistPort, times(1)).checkLockRequest(domain.toUpperCase(), List.of(lockObject2));
        verify(saveLockRequestPort, never()).saveLockRequest(any());
    }

    @Test
    void testAcquireLock_DatabaseConnectionException_WhenCheckLockRequestFails() {
        var lockObject1 = new LockObject("type", "id");
        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(lockObject1);
        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);

        var acquireLockCommand = new AcquireLockCommand(lockGroups);
        var domain = "NGDC";
        var correlationId = UUID.randomUUID().toString();
        var timesOutAt = LocalDateTime.now().plusMinutes(10);

        when(checkLockExistPort.checkLockRequest(any(), any()))
                .thenThrow(new DataAccessResourceFailureException("Neo4j unavailable"));

        var ex = assertThrows(
                DatabaseConnectionException.class,
                () -> acquireLockService.acquireLock(acquireLockCommand, domain, correlationId, timesOutAt)
        );

        assertTrue(ex.getMessage().contains("Error while accessing Database"));
        verify(checkLockExistPort, times(1)).checkLockRequest(any(), any());
        verify(saveLockRequestPort, never()).saveLockRequest(any());
    }

    @Test
    void testAcquireLock_TransactionException_WhenCheckLockRequestFails() {
        var lockObject1 = new LockObject("type", "id");
        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(lockObject1);
        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);

        var acquireLockCommand = new AcquireLockCommand(lockGroups);
        var domain = "NGDC";
        var correlationId = UUID.randomUUID().toString();
        var timesOutAt = LocalDateTime.now().plusMinutes(10);

        when(checkLockExistPort.checkLockRequest(any(), any()))
                .thenThrow(new TransactionSystemException("Neo4j unavailable"));

        var ex = assertThrows(
                DatabaseConnectionException.class,
                () -> acquireLockService.acquireLock(acquireLockCommand, domain, correlationId, timesOutAt)
        );

        assertTrue(ex.getMessage().contains("Error while accessing Database"));
        verify(checkLockExistPort, times(1)).checkLockRequest(any(), any());
        verify(saveLockRequestPort, never()).saveLockRequest(any());
    }
}
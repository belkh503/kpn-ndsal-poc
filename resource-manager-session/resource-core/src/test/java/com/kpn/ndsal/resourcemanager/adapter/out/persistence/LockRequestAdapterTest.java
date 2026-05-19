package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@SpringBootTest(classes = {LockRequestAdapter.class})
class LockRequestAdapterTest {
    @Autowired
    LockRequestAdapter lockRequestAdapter;

    @MockitoBean
    LockRequestMapper mapper;

    @MockitoBean
    LockRequestRepository lockRequestRepository;

    @MockitoBean
    LockObjectRepository lockObjectRepository;

    @Test
    void testCheckLockRequest_trueCompatibilityMatrix() {
        var lockObjectEntity = new LockObjectEntity();
        lockObjectEntity.setLockType(LockType.SHARED);
        lockObjectEntity.setName("name");

        var objectEntities = new ArrayList<LockObjectEntity>();
        objectEntities.add(lockObjectEntity);

        when(lockObjectRepository.findByDomainAndLockObjectEntitiesNameIn("domain", List.of("name"))).thenReturn(objectEntities);

        var lockObject = LockRequest.LockObject.builder()
                .lockType(LockRequest.LockType.SHARED)
                .name("name")
                .build();
        var resources = new ArrayList<LockRequest.LockObject>();
        resources.add(lockObject);

        var response = lockRequestAdapter.checkLockRequest("domain", resources);

        assertTrue(response);
        verify(lockObjectRepository, times(1)).findByDomainAndLockObjectEntitiesNameIn("domain", List.of("name"));
    }

    @Test
    void testCheckLockRequest_falseCompatibilityMatrix() {
        var lockObjectEntity = new LockObjectEntity();
        lockObjectEntity.setLockType(LockType.SHARED);
        lockObjectEntity.setName("name");

        var objectEntities = new ArrayList<LockObjectEntity>();
        objectEntities.add(lockObjectEntity);

        when(lockObjectRepository.findByDomainAndLockObjectEntitiesNameIn("domain", List.of("name"))).thenReturn(objectEntities);

        var lockObject = LockRequest.LockObject.builder()
                .lockType(LockRequest.LockType.EXCLUSIVE)
                .name("name")
                .build();
        var resources = new ArrayList<LockRequest.LockObject>();
        resources.add(lockObject);

        var response = lockRequestAdapter.checkLockRequest("domain", resources);

        assertFalse(response);
        verify(lockObjectRepository, times(1)).findByDomainAndLockObjectEntitiesNameIn("domain", List.of("name"));
    }

    @Test
    void testSaveLock() {
        var expectedUuid = UUID.randomUUID();

        var lockRequestEntity = new LockRequestEntity();
        lockRequestEntity.setDomain("domain");
        lockRequestEntity.setId(expectedUuid);

        var lockRequest = LockRequest.builder()
                .domain("domain")
                .created(LocalDateTime.now())
                .build();

        when(mapper.map(lockRequest)).thenReturn(lockRequestEntity);
        when(lockRequestRepository.save(lockRequestEntity)).thenReturn(lockRequestEntity);

        var actualUuid = lockRequestAdapter.saveLockRequest(lockRequest);

        assertEquals(expectedUuid, actualUuid);
        verify(mapper, times(1)).map(lockRequest);
        verify(lockRequestRepository, times(1)).save(lockRequestEntity);
    }

    @Test
    void testFindLockRequest() {
        var uuid = UUID.randomUUID();

        var lockRequestEntity = new LockRequestEntity();
        lockRequestEntity.setDomain("domain");
        lockRequestEntity.setId(uuid);

        var expectedLockRequest = LockRequest.builder()
                .domain("domain")
                .created(LocalDateTime.now())
                .build();

        when(mapper.map(lockRequestEntity)).thenReturn(expectedLockRequest);
        when(lockRequestRepository.findById(uuid)).thenReturn(Optional.of(lockRequestEntity));

        var actualLockRequest = lockRequestAdapter.findLockRequest(uuid);

        assertTrue(actualLockRequest.isPresent());
        assertEquals(expectedLockRequest, actualLockRequest.get());
        verify(mapper, times(1)).map(lockRequestEntity);
        verify(lockRequestRepository, times(1)).findById(uuid);
    }

    @Test
    void testFindAllExpiredLocks() {
        var uuid = UUID.randomUUID();

        var lockRequestEntity = new LockRequestEntity();
        lockRequestEntity.setDomain("domain");
        lockRequestEntity.setId(uuid);

        var expectedLockRequest = LockRequest.builder()
                .domain("domain")
                .created(LocalDateTime.now())
                .build();

        when(mapper.map(lockRequestEntity)).thenReturn(expectedLockRequest);
        when(lockRequestRepository.findAllExpired()).thenReturn(List.of(lockRequestEntity));

        var expiredLockRequests = lockRequestAdapter.findAllExpiredLocks();

        assertFalse(expiredLockRequests.isEmpty());
        assertEquals(expectedLockRequest, expiredLockRequests.get(0));
        verify(mapper, times(1)).map(lockRequestEntity);
        verify(lockRequestRepository, times(1)).findAllExpired();
    }

    @Test
    void testDeleteRequest() {
        var uuid = UUID.randomUUID();

        lockRequestAdapter.deleteLockRequest(uuid);
        verify(lockRequestRepository, times(1)).deleteLockRequestEntity(uuid);
    }

    @Test
    void testGetLocksCount() {
        when(lockRequestRepository.count()).thenReturn(2L);

        var locksCount = lockRequestAdapter.getLocksCount();
        assertEquals(2L, locksCount);
        verify(lockRequestRepository, times(1)).count();
    }
}
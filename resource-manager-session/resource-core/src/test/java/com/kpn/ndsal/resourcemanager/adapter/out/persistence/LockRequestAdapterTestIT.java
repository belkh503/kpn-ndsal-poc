package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.kpn.ndsal.resourcemanager.domain.LockRequest;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@SpringBootTest
@ActiveProfiles("test")
class LockRequestAdapterTestIT extends BaseNeo4jTestConfig {

    @Autowired
    LockRequestAdapter lockRequestAdapter;

    @Autowired
    LockRequestRepository lockRequestRepository;

    @BeforeEach
    void clearDatabase() {
        cleanupNeo4jDb();
    }

    private static final Supplier<LockRequest> LOCKED_RESOURCES = () -> LockRequest.builder().request(null).created(
            LocalDateTime.now()).domain("BCPE").lockObjectEntities(List.of(
                    LockRequest.LockObject.builder().name("NODE1").lockType(LockRequest.LockType.SHARED).build(),
                    LockRequest.LockObject.builder().name("NODE2").lockType(LockRequest.LockType.EXCLUSIVE).build(),
                    LockRequest.LockObject.builder().name("NODE3").lockType(LockRequest.LockType.EXCLUSIVE).build()))
                                                                                   .build();

    @Test
    void givenRequestAdapter_thenCreateNewLockRequest_thenPresentAndSearchable() {

        // Given

        // When
        var result = lockRequestAdapter.saveLockRequest(LOCKED_RESOURCES.get());

        // And
        var lockResultSaved = lockRequestAdapter.findLockRequest(result);

        // Then
        assertThat(result).isNotNull();
        assertThat(lockResultSaved).isPresent().isNotNull();
        assertThat(lockResultSaved.get()).usingRecursiveComparison().ignoringFields("id", "created")
                                         .ignoringFieldsMatchingRegexes(".*\\.id$").ignoringCollectionOrder()
                                         .isEqualTo(LOCKED_RESOURCES.get());
    }

    @Test
    void givenRequestAdapter_whenSearchForNonExistingLockRequest_thenNotPresent() {
        assertThat(lockRequestAdapter.findLockRequest(UUID.randomUUID())).isNotPresent();
    }

    @Test
    void givenLockedResourcesExist_whenNonOverlappingRequest_thenLockingAllowed() {

        // Given
        var resourcesToLock = LockRequest.builder().request("{}").created(
                LocalDateTime.now()).domain("BCPE").lockObjectEntities(List.of(
                        LockRequest.LockObject.builder().name("NODE9").lockType(LockRequest.LockType.SHARED).build(),
                        LockRequest.LockObject.builder().name("NODE8").lockType(LockRequest.LockType.EXCLUSIVE).build(),
                        LockRequest.LockObject.builder().name("NODE7").lockType(LockRequest.LockType.EXCLUSIVE)
                                              .build()))
                                         .build();

        var resourcesToLockShared = resourcesToLock.getLockObjectEntities().stream()
                                                   .filter(p -> p.getLockType().equals(LockRequest.LockType.EXCLUSIVE))
                                                   .toList();

        var lockedResourcesResult = lockRequestAdapter.saveLockRequest(LOCKED_RESOURCES.get());
        assertThat(lockedResourcesResult).isNotNull();

        // When
        var result = lockRequestAdapter.checkLockRequest(resourcesToLock.getDomain(), resourcesToLockShared);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void givenLockedResourcesExist_whenOverlappingSharedRequest_thenLockingAllowed() {

        // Given
        var resourcesToLock = LockRequest.builder().request(null).created(
                LocalDateTime.now()).domain("BCPE").lockObjectEntities(List.of(
                        LockRequest.LockObject.builder().name("NODE9").lockType(LockRequest.LockType.SHARED).build(),
                        LockRequest.LockObject.builder().name("NODE1").lockType(LockRequest.LockType.EXCLUSIVE).build(),
                        LockRequest.LockObject.builder().name("NODE7").lockType(LockRequest.LockType.EXCLUSIVE)
                                              .build()))
                                         .build();

        var resourcesToLockShared = resourcesToLock.getLockObjectEntities().stream()
                                                   .filter(p -> p.getLockType().equals(LockRequest.LockType.EXCLUSIVE))
                                                   .toList();

        var lockedResourcesResult = lockRequestAdapter.saveLockRequest(LOCKED_RESOURCES.get());
        assertThat(lockedResourcesResult).isNotNull();

        lockRequestRepository.findAll();

        // When
        var result = lockRequestAdapter.checkLockRequest(resourcesToLock.getDomain(), resourcesToLockShared);

        // Then
        assertThat(result).isFalse();
    }

    @Test
    void givenLockedResourcesExist_whenOverlappingOnDifferentDomains_thenLockingAllowed() {

        // Given
        var resourcesToLock = LockRequest.builder().request("{}").created(
                LocalDateTime.now()).domain("CORE").lockObjectEntities(List.of(
                        LockRequest.LockObject.builder().name("NODE1").lockType(LockRequest.LockType.SHARED).build(),
                        LockRequest.LockObject.builder().name("NODE2").lockType(LockRequest.LockType.EXCLUSIVE).build(),
                        LockRequest.LockObject.builder().name("NODE3").lockType(LockRequest.LockType.EXCLUSIVE)
                                              .build()))
                                         .build();

        var resourcesToLockShared = resourcesToLock.getLockObjectEntities().stream()
                                                   .filter(p -> p.getLockType().equals(LockRequest.LockType.EXCLUSIVE))
                                                   .toList();

        var lockedResourcesResult = lockRequestAdapter.saveLockRequest(LOCKED_RESOURCES.get());
        assertThat(lockedResourcesResult).isNotNull();

        // When
        var result = lockRequestAdapter.checkLockRequest(resourcesToLock.getDomain(), resourcesToLockShared);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    void givenTwoLockedResourcesExist_whenGetCount_thenReturnCorrectAmount() {

        // Given
        var lockedResourcesResult = lockRequestAdapter.saveLockRequest(LOCKED_RESOURCES.get());
        var lockedResourcesResult2 = lockRequestAdapter.saveLockRequest(
                LockRequest.builder().request("{}").created(LocalDateTime.now()).domain("CORE")
                           .lockObjectEntities(List.of(
                                   LockRequest.LockObject.builder().name("NODE5").lockType(LockRequest.LockType.SHARED)
                                                         .build(),
                                   LockRequest.LockObject.builder().name("NODE6")
                                                         .lockType(LockRequest.LockType.EXCLUSIVE).build(),
                                   LockRequest.LockObject.builder().name("NODE7")
                                                         .lockType(LockRequest.LockType.EXCLUSIVE).build()))
                           .build());
        assertThat(lockedResourcesResult).isNotNull();
        assertThat(lockedResourcesResult2).isNotNull();

        // When
        var locksCount = lockRequestAdapter.getLocksCount();

        // Then
        assertThat(locksCount).isEqualTo(2);
    }

    @Override
    public LockRequestRepository getLockRequestRepository() {
        return this.lockRequestRepository;
    }
}

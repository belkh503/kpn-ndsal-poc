package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import com.kpn.ndsal.resourcemanager.application.port.out.CheckLockExistPort;
import com.kpn.ndsal.resourcemanager.application.port.out.DeleteLockPort;
import com.kpn.ndsal.resourcemanager.application.port.out.LoadLockRequestPort;
import com.kpn.ndsal.resourcemanager.application.port.out.LoadLocksPort;
import com.kpn.ndsal.resourcemanager.application.port.out.RunningLocksCounterPort;
import com.kpn.ndsal.resourcemanager.application.port.out.SaveLockRequestPort;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@AllArgsConstructor
class LockRequestAdapter implements CheckLockExistPort, SaveLockRequestPort, LoadLockRequestPort, LoadLocksPort, DeleteLockPort, RunningLocksCounterPort {

    private final LockRequestMapper mapper;
    private final LockRequestRepository lockRequestRepository;
    private final LockObjectRepository lockObjectRepository;

    private static final Map<Pair<LockType, LockRequest.LockType>, Boolean> compatibilityMatrix = new HashMap<>();

    static {
        compatibilityMatrix.put(Pair.of(LockType.SHARED, LockRequest.LockType.SHARED), true);
        compatibilityMatrix.put(Pair.of(LockType.SHARED, LockRequest.LockType.EXCLUSIVE), false);
        compatibilityMatrix.put(Pair.of(LockType.EXCLUSIVE, LockRequest.LockType.SHARED), false);
        compatibilityMatrix.put(Pair.of(LockType.EXCLUSIVE, LockRequest.LockType.EXCLUSIVE), false);
    }

    /**
     * Given: list of resources to be EXCLUSIVELY locked
     * Then: check in DB if SHARED or EXCLUSIVE lock exist for `any` of these resources
     *
     * @param domain
     *         -
     * @param resources
     *         -
     */
    @Override
    public boolean checkLockRequest(String domain, List<LockRequest.LockObject> resources) {
        log.trace("checkLockRequest:: domain: {}", domain);
        List<String> requestedLockedObjectNames = resources.stream().map(LockRequest.LockObject::getName).toList();
        var lockedResources = lockObjectRepository.findByDomainAndLockObjectEntitiesNameIn(domain, requestedLockedObjectNames);

        // Step 1. loop thru lockedResources
        return lockedResources.stream()
                .map(p -> {
                    // Step 2. compare locked resource with request using CM
                    var resourceToLockOpt = resources.stream()
                            .filter(d -> d.getName().equalsIgnoreCase(p.getName()))
                            .findFirst();
                    if (resourceToLockOpt.isPresent()) {
                        var resourceToLock = resourceToLockOpt.get();

                        var p1 = p.getLockType();
                        var p2 = resourceToLock.getLockType();

                        return compatibilityMatrix.get(Pair.of(p1, p2));
                    }

                    return true;
                })
                .filter(p -> !p)
                .findFirst()
                .orElse(true);
    }

    /**
     * Given:
     * Then:
     *
     * @param lockRequest
     *         -
     */
    @NonNull
    @Override
    //@Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.DEFAULT)
    public UUID saveLockRequest(LockRequest lockRequest) {
        log.trace("saveLockRequest: {}", lockRequest);

        var lockRequestEntity = lockRequestRepository.save(mapper.map(lockRequest));

        return lockRequestEntity.getId();
    }

    /**
     * Given: lockId - lock request unique identifier
     * Then:
     *
     * @param lockId
     *         - lock request unique identifier
     */
    @NonNull
    @Override
    public Optional<LockRequest> findLockRequest(UUID lockId) {
        log.trace("findLockRequest: {}", lockId);

        return lockRequestRepository.findById(lockId)
                .map(mapper::map);
    }

    /**
     * Return the list of all existing lock requests in the database
     *
     * @return List of expired LockRequests
     */
    @NonNull
    @Override
    public List<LockRequest> findAllExpiredLocks() {
        return lockRequestRepository.findAllExpired().stream().map(mapper::map).toList();
    }

    /**
     * Given: lockId - lock request unique identifier
     * Then: delete existing lock
     *
     * @param lockId
     *         - lock request unique identifier
     */
    @Override
    public void deleteLockRequest(UUID lockId) {
        log.trace("deleteLockRequest: {}", lockId);

        lockRequestRepository.deleteLockRequestEntity(lockId);
    }

    /**
     * Given:
     * Then: return total amount of locked resources
     *
     * @return the number of locks
     */
    @Override
    public long getLocksCount() {
        log.trace("getLocksCount called");

        return lockRequestRepository.count();
    }
}

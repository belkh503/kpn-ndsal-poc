package com.kpn.ndsal.resourcemanager.application.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.kpn.ndsal.resourcemanager.application.port.in.DatabaseConnectionException;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockNotPossibleException;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockUseCase;
import com.kpn.ndsal.resourcemanager.application.port.out.CheckLockExistPort;
import com.kpn.ndsal.resourcemanager.application.port.out.SaveLockRequestPort;
import com.kpn.ndsal.resourcemanager.common.UseCase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.TransactionException;

@Slf4j
@UseCase
@Service
@AllArgsConstructor
class AcquireLockService implements AcquireLockUseCase {

    private final SaveLockRequestPort saveLockRequestPort;
    private final CheckLockExistPort checkLockExistPort;

    @Override
    public UUID acquireLock(AcquireLockCommand command, String domain, String correlationId, LocalDateTime timesOutAt) {
        log.debug("AcquireLockService::acquireLock: {}", command);

        var allLockObjects = getAllLockObjects(command);

        var lockRequest = LockRequestMapper.map(allLockObjects, domain.toUpperCase(), correlationId, timesOutAt);

        try {
                if (!checkLockExistPort.checkLockRequest(lockRequest.getDomain(), lockRequest.getLockObjectEntities())) {
                    throw new AcquireLockNotPossibleException();
                }
                return saveLockRequestPort.saveLockRequest(lockRequest);
        }catch (DataAccessException | TransactionException ne){
            throw new DatabaseConnectionException("Error while accessing Database, please check the connection and try again", ne);
        }catch (AcquireLockNotPossibleException ae){
            throw ae;
        }catch (RuntimeException e) {
            throw e;
        }
    }

    /**
     * If  same entity(NODE, PORT, EAS etc...) is having two graph with both lock as Shared and Exclusive then Exclusive will take precedence over shared. Therefore, this method will update all the graph of the same entity with exclusive lock
     *
     * @param command
     *         as an Object to hold all the LockGroup and LockObject
     * @return List of LockObject with one type lock for each entity
     */
    private List<AcquireLockCommand.LockObject> getAllLockObjects(AcquireLockCommand command) {
        var exclusiveLockObjects = command.getAllExclusiveLockedObject();

        command.getLockGroups().stream()
                .flatMap(lockGroup -> lockGroup.getLockObjects().stream())
                .filter(exclusiveLockObjects::contains)
                .forEach(lockObject -> lockObject.setForce(true));
        return command.getLockGroups().stream()
                .flatMap(g -> g.getLockObjects().stream())
                .distinct()
                .toList();
    }
}
package com.kpn.ndsal.resourcemanager.application.port.in;

import java.time.LocalDateTime;
import java.util.UUID;

public interface AcquireLockUseCase {

    /**
     * Acquire lock for resources.
     *
     * @param command
     *         Command to acquire lock. See {@link AcquireLockCommand}
     * @param domain
     *         Name of the domain. CORE or BCPE are the most common name for supported domain.
     * @param correlationId
     *         request correlationId
     * @param timesOutAt
     *         Lock times out at local datetime
     * @return UUID of the lock.
     * @throws AcquireLockNotPossibleException
     *         Impossible to acquire the lock.
     * @throws AcquireLockInvalidRequestException
     *         Request doesn't match resource schema.
     * @throws AcquireLockWrongDomainException
     */
    UUID acquireLock(AcquireLockCommand command, String domain, String correlationId, LocalDateTime timesOutAt);

}

package com.kpn.ndsal.resourcemanager.application.port.in;

import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

public interface RequestProcessorUseCase {

    /**
     * Process a RequestQueueEntity (wrapper around AcquireLockRequestDto).
     * If a lock can be acquired:
     * - lock the LockObjects
     * - send the "resources locked" response message
     * - return true
     * If a lock cannot be acquired:
     * if the RequestQueueEntity has timed out:
     * - send the "already locked" response
     * - return true
     * else:
     * - return false
     * If an exception occurs:
     * - send error response
     * - return true
     *
     * @param requestQueueEntity
     *         the RequestQueueEntity to process
     */
    void processRequest(RequestQueueEntity requestQueueEntity);

    /**
     * Check if a queue item's createTime has exceeded the global timeout
     *
     * @param requestQueueEntity
     *         the RequestQueueEntity to check
     */
    void processRequestTimeout(RequestQueueEntity requestQueueEntity);
}

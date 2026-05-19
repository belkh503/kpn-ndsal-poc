package com.kpn.ndsal.resourcemanager.application.port.in;

import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

public interface RequestQueueUseCase {
    /**
     * Add RequestQueueEntity to the queue transactional
     *
     * @param requestQueueEntity
     *         the item to add to the queue
     */
    void addRequestToQueue(RequestQueueEntity requestQueueEntity);

    /**
     * Delete a RequestQueueEntity from the queue transactional
     *
     * @param requestQueueEntity
     *         the item to delete from the queue
     */
    void deleteRequestFromQueue(RequestQueueEntity requestQueueEntity);
}

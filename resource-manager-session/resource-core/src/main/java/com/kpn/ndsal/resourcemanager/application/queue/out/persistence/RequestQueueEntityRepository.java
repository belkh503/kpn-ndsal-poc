package com.kpn.ndsal.resourcemanager.application.queue.out.persistence;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RequestQueueEntityRepository extends Neo4jRepository<RequestQueueDatabaseEntity, UUID> {

    Optional<RequestQueueDatabaseEntity> findByPid(UUID pid);

}

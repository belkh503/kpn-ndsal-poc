package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.util.List;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface LockRequestRepository extends Neo4jRepository<LockRequestEntity, UUID> {

    @Query("""
            MATCH (e:LockRequestEntity)-[HAS_LOCKED_ENTITY]-(LockObjectEntity)
            WHERE e.id = $id
            DELETE e,HAS_LOCKED_ENTITY,LockObjectEntity
            """)
    void deleteLockRequestEntity(@Param("id") UUID id);

    @Query("""
            MATCH (lockRequest:LockRequestEntity)-[r:HAS_LOCKED_ENTITY]->(lockObject:LockObjectEntity)
            WHERE duration.between(lockRequest.timesOutAt, localdatetime()).milliseconds > 0
            RETURN *
            """)
    List<LockRequestEntity> findAllExpired();
}

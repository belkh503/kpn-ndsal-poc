package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.util.List;

import com.kpn.neo4j.test.condition.context.EmbeddedNeo4j;

@EmbeddedNeo4j(databaseDirectoryPathEnv = "EmbeddedNeo4j", databaseDirectoryPath = "target/tmpNeo4j", databaseName = "Neo4j", boltEnable = true, boltPort = -1)
public abstract class BaseNeo4jTestConfig {

    public abstract LockRequestRepository getLockRequestRepository();

    protected void cleanupNeo4jDb() {
        List<LockRequestEntity> requestEntities = this.getLockRequestRepository().findAll();
        requestEntities.stream().forEach((requestEntity) -> this.getLockRequestRepository().deleteLockRequestEntity(requestEntity.getId()));
    }
}

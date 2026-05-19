package com.kpn.ndsal.resourcemanager.application.queue.out.persistence;

import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Node("RequestEntity")
public class RequestQueueDatabaseEntity implements CacheableMapStoreBaseEntity<UUID> {

    @Id
    private UUID pid;

    private String payload;

    @Override
    public UUID getPid() {
        return this.pid;
    }

    @Override
    public String getPayload() {
        return this.payload;
    }
}

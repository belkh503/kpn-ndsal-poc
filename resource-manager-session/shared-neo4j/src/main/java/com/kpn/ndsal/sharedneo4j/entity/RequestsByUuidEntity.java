package com.kpn.ndsal.sharedneo4j.entity;

import java.util.UUID;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Node("RequestsByUuid")
public class RequestsByUuidEntity implements CacheableMapStoreBaseEntity<UUID> {

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

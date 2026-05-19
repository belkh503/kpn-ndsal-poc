package com.kpn.ndsal.sessionmanager.persistence.entity;

import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Node("SessionByTriplet")
public class SessionByTripletEntity implements CacheableMapStoreBaseEntity<String> {

    @Id
    private String pid;

    private String payload;

    @Override
    public String getPid() {
        return this.pid;
    }

    @Override
    public String getPayload() {
        return this.payload;
    }
}

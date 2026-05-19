package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Lob;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Relationship;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

//@Entity
//@Table(name = "lock_request")
//@Data
@AllArgsConstructor
@NoArgsConstructor
@Node
@Getter
@Setter
class LockRequestEntity {

    /**
     * Column, also primary key, keeps Unique Lock ID.
     */

    @GeneratedValue(generatorClass = GeneratedValue.UUIDGenerator.class)
    @Id
    private UUID id;

    @NotNull
    private String domain;

    @NotNull
    private String correlationId;

    @NotNull
    @Column(name = "created", columnDefinition = "TIMESTAMP")
    private LocalDateTime created;

    @NotNull
    private LocalDateTime timesOutAt;

    @NotNull
    private boolean released;

    @Lob
    private String request;

    @Relationship(type = "HAS_LOCKED_ENTITY", direction = Relationship.Direction.OUTGOING)
    private List<LockObjectEntity> lockObjectEntities;
}

package com.kpn.ndsal.resourcemanager.adapter.out.persistence;

import java.util.UUID;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotNull;

import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Node
@Getter
@Setter
class LockObjectEntity {

    @GeneratedValue(generatorClass = GeneratedValue.UUIDGenerator.class)
    @Id
    private UUID id;

    @NotNull
    private String name;

    @NotNull
    private String type;

    @Enumerated(EnumType.ORDINAL)
    @Property("lock_type")
    private LockType lockType;
}
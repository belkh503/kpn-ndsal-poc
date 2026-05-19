package com.kpn.ndsal.sharedneo4j.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.kpn.ndsal.sharedneo4j.entity.RequestsByUuidEntity;

public interface RequestsByUuidRepository extends Neo4jRepository<RequestsByUuidEntity, UUID>  {

    Optional<RequestsByUuidEntity> findByPid(UUID key);

}

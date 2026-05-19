package com.kpn.ndsal.sharedneo4j.repository;

import java.util.Optional;

import org.springframework.data.neo4j.repository.Neo4jRepository;

import com.kpn.ndsal.sharedneo4j.entity.SessionByTripletEntity;

public interface SessionByTripletRepository extends Neo4jRepository<SessionByTripletEntity, String> {

    Optional<SessionByTripletEntity> findByPid(String key);

}

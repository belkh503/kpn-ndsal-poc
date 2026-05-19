package com.kpn.ndsal.sessionmanager.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.map.MapStore;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.persistence.entity.RequestsByUuidEntity;
import com.kpn.ndsal.sessionmanager.persistence.repository.RequestsByUuidRepository;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class RequestsByUuidMapStoreForNeo4J implements MapStore<UUID, InternalRequest> {

    private final ObjectMapper objectMapper;
    private final RequestsByUuidRepository repository;

    @SneakyThrows
    @Override
    public void store(UUID key, InternalRequest value) {
        log.debug("RequestsByUuidMapStoreForNeo4J - storing key: {}", key);

        var payload = objectMapper.writeValueAsString(value);

        var cacheEntity = new RequestsByUuidEntity(key, payload);

        repository.save(cacheEntity);
    }

    @Override
    public void storeAll(Map<UUID, InternalRequest> map) {
        log.debug("RequestsByUuidMapStoreForNeo4J - storing all keys");

        map.forEach(this::store);
    }

    @Override
    public void delete(UUID key) {
        log.debug("RequestsByUuidMapStoreForNeo4J - deleting key: {}", key);

        repository.deleteById(key);
    }

    @Override
    public void deleteAll(Collection<UUID> keys) {
        log.debug("RequestsByUuidMapStoreForNeo4J - deleting all keys");

        keys.forEach(this::delete);
    }

    @SneakyThrows
    @Override
    public InternalRequest load(UUID key) {
        log.debug("RequestsByUuidMapStoreForNeo4J - loading value for key: {}", key);

        var payload = repository.findByPid(key);

        if (!payload.isPresent())
            return null;

        return objectMapper.readValue(payload.get().getPayload(), new TypeReference<>() {
        });
    }

    @Override
    public Map<UUID, InternalRequest> loadAll(Collection<UUID> keys) {
        log.debug("RequestsByUuidMapStoreForNeo4J - loading all");

        return keys.stream().reduce(new HashMap<>(), (hashMap, key) -> {
            hashMap.put(key, this.load(key));
            return hashMap;
        }, (m, m2) -> {
            m.putAll(m2);
            return m;
        });
    }

    @Override
    public Iterable<UUID> loadAllKeys() {
        log.debug("RequestsByUuidMapStoreForNeo4J - loading all keys");

        return repository.findAll().stream().map(RequestsByUuidEntity::getPid).toList();
    }
}

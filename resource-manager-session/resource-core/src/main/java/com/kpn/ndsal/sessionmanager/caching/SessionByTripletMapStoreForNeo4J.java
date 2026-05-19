package com.kpn.ndsal.sessionmanager.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.map.MapStore;
import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.persistence.entity.SessionByTripletEntity;
import com.kpn.ndsal.sessionmanager.persistence.repository.SessionByTripletRepository;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class SessionByTripletMapStoreForNeo4J implements MapStore<String, SessionEntity> {

    private final ObjectMapper objectMapper;
    private final SessionByTripletRepository repository;

    @SneakyThrows
    @Override
    public void store(String key, SessionEntity value) {
        log.debug("SessionByTripletMapStoreForNeo4J - storing key: {}", key);

        var payload = objectMapper.writeValueAsString(value);

        var cacheEntity = new SessionByTripletEntity(key, payload);

        repository.save(cacheEntity);
    }

    @Override
    public void storeAll(Map<String, SessionEntity> map) {
        log.debug("SessionByTripletMapStoreForNeo4J - storing all keys");

        map.forEach(this::store);
    }

    @Override
    public void delete(String key) {
        log.debug("SessionByTripletMapStoreForNeo4J - deleting key: {}", key);

        repository.deleteById(key);
    }

    @Override
    public void deleteAll(Collection<String> keys) {
        log.debug("SessionByTripletMapStoreForNeo4J - deleting all keys");

        keys.forEach(this::delete);
    }

    @SneakyThrows
    @Override
    public SessionEntity load(String key) {
        log.debug("SessionByTripletMapStoreForNeo4J - loading value for key: {}", key);

        var payload = repository.findByPid(key);

        if (!payload.isPresent())
            return null;

        return objectMapper.readValue(payload.get().getPayload(), new TypeReference<>() {
        });
    }

    @Override
    public Map<String, SessionEntity> loadAll(Collection<String> keys) {
        log.debug("SessionByTripletMapStoreForNeo4J - loading all");

        return keys.stream().reduce(new HashMap<>(), (hashMap, key) -> {
            hashMap.put(key, this.load(key));
            return hashMap;
        }, (m, m2) -> {
            m.putAll(m2);
            return m;
        });
    }

    @Override
    public Iterable<String> loadAllKeys() {
        log.debug("SessionByTripletMapStoreForNeo4J - loading all keys");

        return repository.findAll().stream().map(SessionByTripletEntity::getPid).toList();
    }
}

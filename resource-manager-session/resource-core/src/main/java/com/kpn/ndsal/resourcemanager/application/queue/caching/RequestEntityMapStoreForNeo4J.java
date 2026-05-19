package com.kpn.ndsal.resourcemanager.application.queue.caching;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hazelcast.map.MapStore;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueDatabaseEntity;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntityRepository;

import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@AllArgsConstructor
public class RequestEntityMapStoreForNeo4J implements MapStore<UUID, RequestQueueEntity> {

    private final ObjectMapper objectMapper;
    private final RequestQueueEntityRepository repository;

    @SneakyThrows
    @Override
    public synchronized void store(UUID key, RequestQueueEntity value) {
        log.debug("MapStoreForNeo4J (resourcemanager) - storing key: {}", key);

        var payload = objectMapper.writeValueAsString(value);

        var cacheEntity = new RequestQueueDatabaseEntity(key, payload);

        repository.save(cacheEntity);
    }

    @Override
    public synchronized void storeAll(Map<UUID, RequestQueueEntity> map) {
        log.debug("MapStoreForNeo4J (resourcemanager) - storing all keys");

        map.forEach(this::store);
    }

    @Override
    public synchronized void delete(UUID key) {
        log.debug("MapStoreForNeo4J (resourcemanager) - deleting key: {}", key);

        repository.deleteById(key);
    }

    @Override
    public synchronized void deleteAll(Collection<UUID> keys) {
        log.debug("MapStoreForNeo4J (resourcemanager) - deleting all keys");

        keys.forEach(this::delete);
    }

    @SneakyThrows
    @Override
    public synchronized RequestQueueEntity load(UUID key) {
        log.debug("MapStoreForNeo4J (resourcemanager) - loading value for key: {}", key);

        var payload = repository.findByPid(key);

        if (payload.isEmpty())
            return null;

        return objectMapper.readValue(payload.get().getPayload(), new TypeReference<>() {
        });
    }

    @Override
    public synchronized Map<UUID, RequestQueueEntity> loadAll(Collection<UUID> keys) {
        log.debug("MapStoreForNeo4J (resourcemanager) - loading all");

        return keys.stream()
                .reduce(new HashMap<>(), (hashMap, key) -> {
                    hashMap.put(key, this.load(key));
                    return hashMap;
                }, (m, m2) -> {
                    m.putAll(m2);
                    return m;
                });
    }

    @Override
    public Iterable<UUID> loadAllKeys() {
        log.debug("MapStoreForNeo4J (resourcemanager) - loading all keys");

        return repository.findAll()
                .stream()
                .map(RequestQueueDatabaseEntity::getPid)
                .toList();
    }
}
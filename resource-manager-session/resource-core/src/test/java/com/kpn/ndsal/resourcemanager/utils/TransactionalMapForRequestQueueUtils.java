package com.kpn.ndsal.resourcemanager.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.hazelcast.query.Predicate;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;

public class TransactionalMapForRequestQueueUtils {

    public static TransactionalMap<UUID, RequestQueueEntity> create(List<RequestQueueEntity> requestQueueEntities) {

        var map = new HashMap<UUID, RequestQueueEntity>();

        requestQueueEntities.forEach(request -> map.put(request.getId(), request));

        return new TransactionalMap<>() {

            @Override
            public boolean containsKey(Object o) {
                return map.containsKey(o);
            }

            @Override
            public RequestQueueEntity get(Object o) {
                return map.get(o);
            }

            @Override
            public RequestQueueEntity getForUpdate(Object o) {
                return map.get(o);
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public RequestQueueEntity put(UUID s, RequestQueueEntity internalRequest) {
                return map.put(s, internalRequest);
            }

            @Override
            public RequestQueueEntity put(UUID s, RequestQueueEntity internalRequest, long l, TimeUnit timeUnit) {
                return internalRequest;
            }

            @Override
            public void set(UUID s, RequestQueueEntity internalRequest) {
            }

            @Override
            public RequestQueueEntity putIfAbsent(UUID s, RequestQueueEntity internalRequest) {
                return null;
            }

            @Override
            public RequestQueueEntity replace(UUID s, RequestQueueEntity internalRequest) {
                return null;
            }

            @Override
            public boolean replace(UUID s, RequestQueueEntity internalRequest, RequestQueueEntity v1) {
                return false;
            }

            @Override
            public RequestQueueEntity remove(Object o) {
                return map.remove(o);
            }

            @Override
            public void delete(Object o) {
            }

            @Override
            public boolean remove(Object o, Object o1) {
                return false;
            }

            @Override
            public Set<UUID> keySet() {
                return map.keySet();
            }

            @Override
            public Set<UUID> keySet(Predicate<UUID, RequestQueueEntity> predicate) {
                return null;
            }

            @Override
            public Collection<RequestQueueEntity> values() {
                return map.values();
            }

            @Override
            public Collection<RequestQueueEntity> values(Predicate<UUID, RequestQueueEntity> predicate) {

                return map.entrySet().stream()
                        .filter(predicate::apply)
                        .map(Map.Entry::getValue)
                        .collect(Collectors.toSet());
            }

            @Override
            public String getPartitionKey() {
                return null;
            }

            @Override
            public String getName() {
                return HazelcastConfigurator.HAZELCAST_REQUEST_QUEUE;
            }

            @Override
            public String getServiceName() {
                return null;
            }

            @Override
            public void destroy() {

            }
        };
    }

}

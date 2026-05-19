package com.kpn.ndsal.sessionmanager.util;

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
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;

public class TransactionalMapForRequestsByUuidUtils {

    public static TransactionalMap<UUID, InternalRequest> create(List<SessionAcquireRequestDto> sards, String correlationId) {

        var map = new HashMap<UUID, InternalRequest>();

        sards.forEach(request -> {
                    var internalRequest = new InternalRequest(request, correlationId);
                    map.put(internalRequest.getUuid(), internalRequest);
                });

        return new TransactionalMap<>() {

            @Override
            public boolean containsKey(Object o) {
                return map.containsKey(o);
            }

            @Override
            public InternalRequest get(Object o) {
                return map.get(o);
            }

            @Override
            public InternalRequest getForUpdate(Object o) {
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
            public InternalRequest put(UUID s, InternalRequest internalRequest) {
                return map.put(s, internalRequest);
            }

            @Override
            public InternalRequest put(UUID s, InternalRequest internalRequest, long l, TimeUnit timeUnit) {
                return internalRequest;
            }

            @Override
            public void set(UUID s, InternalRequest internalRequest) {
            }

            @Override
            public InternalRequest putIfAbsent(UUID s, InternalRequest internalRequest) {
                return null;
            }

            @Override
            public InternalRequest replace(UUID s, InternalRequest internalRequest) {
                return null;
            }

            @Override
            public boolean replace(UUID s, InternalRequest internalRequest, InternalRequest v1) {
                return false;
            }

            @Override
            public InternalRequest remove(Object o) {
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
            public Set<UUID> keySet(Predicate<UUID, InternalRequest> predicate) {
                return null;
            }

            @Override
            public Collection<InternalRequest> values() {
                return map.values();
            }

            @Override
            public Collection<InternalRequest> values(Predicate<UUID, InternalRequest> predicate) {

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
                return HazelcastConfig.SESSIONS_BY_TRIPLET;
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

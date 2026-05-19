package com.kpn.ndsal.sessionmanager.util;

import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKeyWithIndex;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.hazelcast.query.Predicate;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;

public class TransactionalMapForTripletUtils {

    public static TransactionalMap<String, SessionEntity> create(TripletEntity triplet, UUID uuid, int amount, boolean expired) {

        var map = new HashMap<String, SessionEntity>();
        IntStream.range(0, amount).boxed()
                .forEach(index -> {
                    var name = getTripletAsKeyWithIndex(triplet, index);
                    var se = new SessionEntity(uuid,
                            index,
                            15000L,
                            System.currentTimeMillis() - (expired ? 25000L : 2000L),
                            triplet);
                    map.put(name, se);
                });

        return new TransactionalMap<>() {

            @Override
            public boolean containsKey(Object o) {
                return map.containsKey(o);
            }

            @Override
            public SessionEntity get(Object o) {
                return map.get(o);
            }

            @Override
            public SessionEntity getForUpdate(Object o) {
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
            public SessionEntity put(String s, SessionEntity sessionEntity) {
                return map.put(s, sessionEntity);
            }

            @Override
            public SessionEntity put(String s, SessionEntity sessionEntity, long l, TimeUnit timeUnit) {
                return sessionEntity;
            }

            @Override
            public void set(String s, SessionEntity sessionEntity) {
            }

            @Override
            public SessionEntity putIfAbsent(String s, SessionEntity sessionEntity) {
                return null;
            }

            @Override
            public SessionEntity replace(String s, SessionEntity sessionEntity) {
                return null;
            }

            @Override
            public boolean replace(String s, SessionEntity sessionEntity, SessionEntity v1) {
                return false;
            }

            @Override
            public SessionEntity remove(Object o) {
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
            public Set<String> keySet() {
                return map.keySet();
            }

            @Override
            public Set<String> keySet(Predicate<String, SessionEntity> predicate) {
                return null;
            }

            @Override
            public Collection<SessionEntity> values() {
                return map.values();
            }

            @Override
            public Collection<SessionEntity> values(Predicate<String, SessionEntity> predicate) {

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

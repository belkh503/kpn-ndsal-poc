package com.kpn.ndsal.sessionmanager.config;

import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.config.MapStoreConfig.InitialLoadMode;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import com.hazelcast.map.IMap;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.sessionmanager.caching.RequestsByUuidMapStoreForNeo4J;
import com.kpn.ndsal.sessionmanager.caching.SessionByTripletMapStoreForNeo4J;
import com.kpn.ndsal.sessionmanager.entity.InternalRequest;
import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.util.List;
import java.util.UUID;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class HazelcastConfig {

    private static final String LOCK_BY_TRIPLET = "lockByTriplet";

    public static final String SESSIONS_BY_TRIPLET = "sessionsByTriplet";

    public static final String REQUESTS_BY_UUID = "requestsByUuid";

    private final ApplicationContext applicationContext;

    @Bean(name = "sessionManagerHazelcastInstance")
    public HazelcastInstance hazelcastInstance(
            @Value("${cache.writethrough.enabled:false}") boolean cacheWriteThroughEnabled,
            SessionByTripletMapStoreForNeo4J sessionByTripletMapStore,
            RequestsByUuidMapStoreForNeo4J requestsByUuidMapStore) {

        var mapStoreConfigForSessionByTripletMapStore = new MapStoreConfig();
        mapStoreConfigForSessionByTripletMapStore.setEnabled(cacheWriteThroughEnabled);
        mapStoreConfigForSessionByTripletMapStore.setImplementation(sessionByTripletMapStore);
        mapStoreConfigForSessionByTripletMapStore.setWriteDelaySeconds(0);
        mapStoreConfigForSessionByTripletMapStore.setInitialLoadMode(InitialLoadMode.EAGER);

        var mapStoreConfigForRequestsByUuidMapStore = new MapStoreConfig();
        mapStoreConfigForRequestsByUuidMapStore.setEnabled(cacheWriteThroughEnabled);
        mapStoreConfigForRequestsByUuidMapStore.setImplementation(requestsByUuidMapStore);
        mapStoreConfigForRequestsByUuidMapStore.setWriteDelaySeconds(0);
        mapStoreConfigForRequestsByUuidMapStore.setInitialLoadMode(InitialLoadMode.EAGER);

        var config = Config.loadDefault();
        config.setInstanceName("sessionManagerHazelcastInstance");
        config.getMapConfig(SESSIONS_BY_TRIPLET).setIndexConfigs(List.of(new IndexConfig(IndexType.HASH, "uuid")))
              .setMapStoreConfig(mapStoreConfigForSessionByTripletMapStore);
        config.getMapConfig(REQUESTS_BY_UUID)
              .setIndexConfigs(List.of(new IndexConfig(IndexType.SORTED, "creationTime"),
                      new IndexConfig(IndexType.HASH, "priority")))
              .setMapStoreConfig(mapStoreConfigForRequestsByUuidMapStore);

        return Hazelcast.newHazelcastInstance(config);
    }

    @Bean(name = "locksByTriplet")
    public IMap<String, Boolean> getLocksByTriplet(@Qualifier("sessionManagerHazelcastInstance") HazelcastInstance sessionManagerHazelcastInstance) {
        return sessionManagerHazelcastInstance.getMap(LOCK_BY_TRIPLET);
    }

    public TransactionContext getHazelcastContext() {
        return applicationContext.getBean(TransactionContext.class);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TransactionContext getHazelcastContext(@Qualifier("sessionManagerHazelcastInstance") HazelcastInstance sessionManagerHazelcastInstance) {
        return sessionManagerHazelcastInstance.newTransactionContext(new TransactionOptions().setTransactionType(TransactionOptions.TransactionType.TWO_PHASE));
    }

    /**
     * Transactional IMap contains Session Locking Requests that are coming via Kafka.
     * */
    public TransactionalMap<UUID, InternalRequest> getRequestsByUuid(TransactionContext transactionContext) {
        return transactionContext.getMap(HazelcastConfig.REQUESTS_BY_UUID);
    }

    /**
     * Transactional IMap contains locked resources by triplet. After requested timeout, resources will be released.
     * */
    public TransactionalMap<String, SessionEntity> getSessionsByTriplet(TransactionContext transactionContext) {
        return transactionContext.getMap(HazelcastConfig.SESSIONS_BY_TRIPLET);
    }

    public static <T, U> void unlockIfLocked(IMap<T, U> imap, T key) {
        if (imap.isLocked(key)) {
            imap.unlock(key);
        }
    }

}

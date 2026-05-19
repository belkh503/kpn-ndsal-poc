package com.kpn.ndsal.resourcemanager.application.configuration;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.hazelcast.config.Config;
import com.hazelcast.config.IndexConfig;
import com.hazelcast.config.IndexType;
import com.hazelcast.config.MapStoreConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.transaction.TransactionContext;
import com.hazelcast.transaction.TransactionOptions;
import com.hazelcast.transaction.TransactionalMap;
import com.kpn.ndsal.resourcemanager.application.queue.caching.RequestEntityMapStoreForNeo4J;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

@EnableCaching
@Configuration
@RequiredArgsConstructor
public class HazelcastConfigurator {

    public static final String HAZELCAST_REQUEST_QUEUE = "hazelcastRequestQueue";
    private static final Logger logger = LoggerFactory.getLogger(HazelcastConfigurator.class);

    private final ApplicationContext applicationContext;

    @Bean(name = "resourceManagerHazelcastInstance")
    public HazelcastInstance hazelcastInstance(
            RequestEntityMapStoreForNeo4J requestQueueEntityMapStore,
            @Value("${cache.writethrough.enabled:false}") boolean cacheWriteThroughEnabled
    ) {

        var mapStoreConfigForRequests = new MapStoreConfig();
        mapStoreConfigForRequests.setEnabled(cacheWriteThroughEnabled);
        mapStoreConfigForRequests.setImplementation(requestQueueEntityMapStore);
        mapStoreConfigForRequests.setWriteDelaySeconds(0);
        mapStoreConfigForRequests.setInitialLoadMode(MapStoreConfig.InitialLoadMode.EAGER);

        var config = Config.loadDefault();
        config.setInstanceName("resourceManagerHazelcastInstance");
        config.getMapConfig(HAZELCAST_REQUEST_QUEUE)
                .setIndexConfigs(List.of(new IndexConfig(IndexType.SORTED, "creationTime"),
                        new IndexConfig(IndexType.HASH, "priority")
                ))
                .setMapStoreConfig(mapStoreConfigForRequests);

        return Hazelcast.newHazelcastInstance(config);
    }

    public TransactionalMap<UUID, RequestQueueEntity> getHazelcastRequestQueue(TransactionContext transactionContext) {
        try {
            return transactionContext.getMap(HAZELCAST_REQUEST_QUEUE);
        } catch (RuntimeException e) {
            logger.error("Error while getting hazelcast request queue", e);
            throw e;
        }
    }

    public TransactionContext getHazelcastContext() {
        return applicationContext.getBean(TransactionContext.class);
    }

    @Bean
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
    public TransactionContext getHazelcastContext(HazelcastInstance resourceManagerHazelcastInstance) {
        return resourceManagerHazelcastInstance.newTransactionContext(new TransactionOptions().setTransactionType(TransactionOptions.TransactionType.TWO_PHASE));
    }
}

package com.kpn.ndsal.sessionmanager.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@Configuration
public class SessionManagerConfig {

    @Value("${spring.kafka.consumer.acquire-topic}")
    private String acquireTopic;

    @Value("${spring.kafka.consumer.release-topic}")
    private String releaseTopic;

    @Value("${spring.kafka.producer.acquire-response-topic}")
    private String acquireResponseTopic;

    @Value("${spring.kafka.producer.release-response-topic}")
    private String releaseResponseTopic;

    @Bean
    public NewTopic sessionAcquireRequestTopic() {
        return TopicBuilder.name(acquireTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sessionReleaseRequestTopic() {
        return TopicBuilder.name(releaseTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sessionAcquireResponseTopic() {
        return TopicBuilder.name(acquireResponseTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic sessionReleaseResponseTopic() {
        return TopicBuilder.name(releaseResponseTopic).partitions(1).replicas(1).build();
    }
}

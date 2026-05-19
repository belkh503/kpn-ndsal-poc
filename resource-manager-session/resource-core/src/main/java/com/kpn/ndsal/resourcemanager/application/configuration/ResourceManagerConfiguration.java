package com.kpn.ndsal.resourcemanager.application.configuration;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.neo4j.config.EnableNeo4jAuditing;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.kpn.ndsal.resourcemanager.adapter.out.persistence.LockRequestRepository;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntityRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@EnableAsync(proxyTargetClass = true)
@EnableNeo4jRepositories(basePackageClasses = {LockRequestRepository.class, RequestQueueEntityRepository.class})
@EnableNeo4jAuditing
@EnableScheduling
@Import({ com.kpn.ndsal.kafkacommon.config.KafkaCommonConfig.class })
class ResourceManagerConfiguration {

    @Value("${spring.kafka.consumer.acquire-lock-topic}")
    private String acquireLockTopic;

    @Value("${spring.kafka.consumer.delete-lock-topic}")
    private String deleteLockTopic;

    @Value("${spring.kafka.consumer.get-lock-status-topic}")
    private String getLockStatusTopic;

    @Value("${spring.kafka.producer.acquire-lock-response-topic}")
    private String acquireLockResponseTopic;

    @Value("${spring.kafka.producer.delete-lock-response-topic}")
    private String deleteLockResponseTopic;

    @Value("${spring.kafka.producer.get-lock-status-response-topic}")
    private String getLockStatusResponseTopic;

    @Bean
    public NewTopic acquireLockRequestTopic() {
        return TopicBuilder.name(acquireLockTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic deleteLockRequestTopic() {
        return TopicBuilder.name(deleteLockTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic getLockStatusRequestTopic() {
        return TopicBuilder.name(getLockStatusTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic acquireLockResponseTopic() {
        return TopicBuilder.name(acquireLockResponseTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic deleteLockResponseTopic() {
        return TopicBuilder.name(deleteLockResponseTopic).partitions(1).replicas(1).build();
    }

    @Bean
    public NewTopic getLockStatusResponseTopic() {
        return TopicBuilder.name(getLockStatusResponseTopic).partitions(1).replicas(1).build();
    }

    @Bean
    ObjectMapper objectMapper() {
        return new ObjectMapper()
                .findAndRegisterModules()
                .setSerializationInclusion(JsonInclude.Include.NON_NULL)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .disable(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    @Bean
    ObjectWriter objectWriter() {
        return objectMapper().writerWithDefaultPrettyPrinter();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}

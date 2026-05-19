package com.kpn.ndsal.kafkacommon.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaCommonConfig {
    // Kafka common configuration provided by the kafka-common library.
    // Spring Boot auto-configures Kafka producer via spring.kafka.* properties in application.yml.
    // Ensure application.yml includes:
    //   spring.kafka.producer.value-serializer: org.springframework.kafka.support.serializer.JsonSerializer
}

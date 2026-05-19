package com.kpn.ndsal.kafkacommon.kafka.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;

public abstract class BaseProducer<T> {

    private final String producerClientId;
    private final String producerTopicName;

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;

    protected BaseProducer(String producerClientId, String producerTopicName) {
        this.producerClientId = producerClientId;
        this.producerTopicName = producerTopicName;
    }

    public void sendMessage(T message, String correlationId) {
        kafkaTemplate.send(producerTopicName, message);
    }

    public String getProducerClientId() {
        return producerClientId;
    }

    public String getProducerTopicName() {
        return producerTopicName;
    }
}

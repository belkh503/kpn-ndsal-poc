package com.kpn.ndsal.sessionmanager.cucumber.context;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.springframework.stereotype.Component;

import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;

import lombok.Data;

@Data
@Component
public class ExecuteContext {
    private KafkaProducer<String, String> producer;
    private BlockingQueue<ConsumerRecord<String, String>> acquireConsumer;
    private BlockingQueue<ConsumerRecord<String, String>> releaseConsumer;

    private final SessionAcquireRequestDto acquireRequest = new SessionAcquireRequestDto();
    private final SessionReleaseRequestDto releaseRequest = new SessionReleaseRequestDto();

    private ConsumerRecord<String, String> acquireResponse;
    private ConsumerRecord<String, String> releaseResponse;

    private final List<UUID> acquiredUuids = new ArrayList<>();
}

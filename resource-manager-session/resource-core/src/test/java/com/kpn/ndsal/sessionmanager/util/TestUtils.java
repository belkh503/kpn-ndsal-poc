package com.kpn.ndsal.sessionmanager.util;
import static com.kpn.ndsal.kafkacommon.kafka.config.BaseListener.CORRELATION_ID_HEADER;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.sessionmanager.model.*;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto.Priority;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

// import static com.kpn.ndsal.kafkacommon.util.BaseListener.CORRELATION_ID_HEADER;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

public class TestUtils {

    public static final String DLT_SUFFIX = "-dlt";
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> Consumer<String, T> configureConsumer(EmbeddedKafkaBroker embeddedKafkaBroker, String topic,
            String groupId, Class<T> consumedClass) {
        Map<String, Object> consumerProps = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        consumerProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "10000");

        Consumer<String, T> consumer = new DefaultKafkaConsumerFactory<>(consumerProps, new StringDeserializer(),
                new JsonDeserializer<>(consumedClass)).createConsumer();

        consumer.subscribe(Collections.singleton(topic));

        await().atMost(30, SECONDS).until(topicIsListed(consumer, topic));

        return consumer;
    }

    private static Callable<Boolean> topicIsListed(Consumer<String, ?> consumer, String topic) {
        return new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                return nonNull(consumer.listTopics().get(topic));
            }
        };
    }

    public static <T> Producer<String, T> configureProducer(EmbeddedKafkaBroker embeddedKafkaBroker) {
        Map<String, Object> producerProps = new HashMap<>(KafkaTestUtils.producerProps(embeddedKafkaBroker));
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<String, T>(producerProps).createProducer();
    }

    public static BlockingQueue<ConsumerRecord<String, String>> createConsumer(EmbeddedKafkaBroker kafkaBroker,
            String topic) {
        var configs = new HashMap<>(KafkaTestUtils.consumerProps("consumer", "false", kafkaBroker));
        var ncoConsumerFactory = new DefaultKafkaConsumerFactory<>(configs, new StringDeserializer(),
                new StringDeserializer());

        var ncoContainerProperties = new ContainerProperties(new TopicPartitionOffset(topic, 0));
        var container = new KafkaMessageListenerContainer<>(ncoConsumerFactory, ncoContainerProperties);

        BlockingQueue<ConsumerRecord<String, String>> records = new LinkedBlockingQueue<>();
        container.setupMessageListener((MessageListener<String, String>) records::add);
        container.start();
        waitForAssignment(container, kafkaBroker.getPartitionsPerTopic());
        return records;
    }

    public static KafkaProducer<String, String> createProducer(KafkaProperties kafkaProperties) {
        Map<String, Object> customProducerProperties = kafkaProperties.buildProducerProperties();
        customProducerProperties.put("value.serializer", StringSerializer.class);
        return new KafkaProducer<>(customProducerProperties);
    }

    public static void putJsonOnTopic(KafkaProducer<String, String> jsonProducer, String json, String correlationId,
            String topic) {
        ProducerRecord<String, String> record = new ProducerRecord<>(topic, "name", json);
        if (nonNull(correlationId)) {
            record.headers().add(CORRELATION_ID_HEADER, correlationId.getBytes());
        }
        jsonProducer.send(record);
    }

    public static ConsumerRecord<String, String> retrieveRecordFromResponseTopic(
            BlockingQueue<ConsumerRecord<String, String>> records, long timeOut) throws InterruptedException {
        ConsumerRecord<String, String> singleRecord = records.poll(timeOut, MILLISECONDS);
        assertNotNull(singleRecord);
        return singleRecord;
    }

    public static ConsumerRecord<String, String> retrieveRecordFromErrorTopic(
            BlockingQueue<ConsumerRecord<String, String>> records) throws InterruptedException {
        ConsumerRecord<String, String> singleRecord = records.poll(1000, MILLISECONDS);
        assertNotNull(singleRecord);
        return singleRecord;
    }

    public static boolean checkCorrelationIdHeader(String correlationId, ConsumerRecord<String, String> singleRecord) {
        return correlationId.equals(getCorrelationIdHeader(singleRecord.headers()));
    }

    public static String getCorrelationIdHeader(Headers headers) {
        for (Header header : headers) {
            if (CORRELATION_ID_HEADER.equals(header.key())) {
                return new String(header.value());
            }
        }
        return null;
    }

    public static SessionAcquireRequestDto createAcquireRequest(Priority priority, int timeout, String domain,
            String systemType, String nodeName, int numSessions) {
        final SessionAcquireRequestDto sessionAcquireRequest = new SessionAcquireRequestDto();
        sessionAcquireRequest.setPriority(priority);
        sessionAcquireRequest.setTimeoutSec(timeout);

        SessionInfo sessionsInfo = new SessionInfo();
        sessionsInfo.setDomain(domain);
        sessionsInfo.setSystemType(systemType);
        sessionsInfo.setNodeName(nodeName);
        sessionsInfo.setNumSessionsWanted(numSessions);
        sessionAcquireRequest.setSessionsInfo(Collections.singletonList(sessionsInfo));

        return sessionAcquireRequest;
    }

    public static SessionReleaseRequestDto createReleaseRequest(UUID uuid) {
        List<UUID> uuids = Collections.singletonList(uuid);

        final SessionReleaseRequestDto sessionReleaseRequest = new SessionReleaseRequestDto();
        sessionReleaseRequest.setUuids(uuids);
        return sessionReleaseRequest;
    }

    public static SessionAcquireResponseDto verifyAcquireResponse(ConsumerRecord<String, String> response)
            throws JsonProcessingException {
        SessionAcquireResponseDto acquireResponse = objectMapper.readValue(response.value(),
                SessionAcquireResponseDto.class);

        assertThat(acquireResponse.getSessionAcquired()).isTrue();
        assertThat(acquireResponse.getUuid()).isNotNull();

        return acquireResponse;
    }

    public static void verifyReleaseResponse(ConsumerRecord<String, String> response, boolean isReleased)
            throws JsonProcessingException {
        SessionReleaseResponseDto releaseResponse = objectMapper.readValue(response.value(),
                SessionReleaseResponseDto.class);

        List<UUID> listUuids = releaseResponse.getUuids();
        if (isReleased) {
            assertThat(listUuids).isNotEmpty();
        } else {
            assertThat(listUuids).isEmpty();
        }
    }
}

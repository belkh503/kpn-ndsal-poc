package com.kpn.ndsal.resourcemanager;

import static com.kpn.ndsal.kafkacommon.kafka.config.BaseListener.CORRELATION_ID_HEADER;
import static com.kpn.ndsal.resourcemanager.cucumber.BcpeLockingStepsDefinition.currentCorrelationId;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.kafka.test.utils.ContainerTestUtils.waitForAssignment;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collector;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.TopicPartitionOffset;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.utils.KafkaTestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.resourcemanager.model.DeleteLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusRequestDto;
import com.kpn.ndsal.resourcemanager.model.Timeout;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestHelpers {
    public static final ObjectMapper objectMapper = new ObjectMapper();

    public static <T> List<T> getFieldsAnnotatedWithGraphGraphLockingRequest(
            ArgumentsProvider object) throws IllegalAccessException {
        List<T> result = new ArrayList<>();
        for (Field field : object.getClass().getDeclaredFields()) {
            if (field.isAnnotationPresent(GraphLockingRequestSimplified.class)) {
                result.add((T) field.get(object));
            }
        }

        return result;
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
        var customProducerProperties = kafkaProperties.buildProducerProperties();
        customProducerProperties.put("value.serializer", StringSerializer.class);
        return new KafkaProducer<>(customProducerProperties);
    }

    public static void putJsonOnTopic(KafkaProducer<String, String> jsonProducer, String json, String topic) {
        currentCorrelationId = UUID.randomUUID().toString();

        var record = new ProducerRecord<>(topic, "name", json);
        if (nonNull(currentCorrelationId)) {
            record.headers().add(CORRELATION_ID_HEADER, currentCorrelationId.getBytes());
        }
        jsonProducer.send(record, (recordMetadata, e) -> {
            if (e == null) {
                log.info("message pushed successfully to topic {}, offset {}, partition {}, timestamp {}",
                        topic, recordMetadata.offset(), recordMetadata.partition(), recordMetadata.timestamp()
                );
            } else {
                log.error(e.getMessage());
            }
        });
        jsonProducer.flush();
    }

    public static String prepareAcquireLockRequest(List<String> graphs) throws JSONException {
        var jsonPayload = new JSONObject();
        var lockGroups = new JSONArray();

        for (String graph : graphs) {
            jsonPayload.put("domain", "BCPE");
            lockGroups.put(createGroups(List.of(graph.split(","))));

        }
        jsonPayload.put("lockGroups", lockGroups);

        System.out.println(jsonPayload);
        return jsonPayload.toString();
    }

    public static String prepareAcquireLockRequest(List<String> graphs, String priority) throws JSONException {
        var jsonPayload = new JSONObject();
        var lockGroups = new JSONArray();

        for (String graph : graphs) {
            jsonPayload.put("domain", "BCPE");

            if (priority != null) {jsonPayload.put("priority", priority);}

            lockGroups.put(createGroups(List.of(graph.split(","))));

        }
        jsonPayload.put("lockGroups", lockGroups);

        System.out.println(jsonPayload);
        return jsonPayload.toString();
    }

    public static String prepareAcquireLockRequest(List<String> graphs, int timeout, String unit) throws JSONException {
        var jsonPayload = new JSONObject();
        var lockGroups = new JSONArray();

        var requestTimeout = new JSONObject();
        requestTimeout.put("value", timeout);
        requestTimeout.put("unit", Timeout.Unit.fromValue(unit));

        for (String graph : graphs) {
            jsonPayload.put("domain", "BCPE");

            jsonPayload.put("timeout", requestTimeout);

            lockGroups.put(createGroups(List.of(graph.split(","))));

        }
        jsonPayload.put("lockGroups", lockGroups);

        System.out.println(jsonPayload);
        return jsonPayload.toString();
    }


    public static JSONObject createGroups(List<String> graph) throws JSONException {
        var lockedGroups = new JSONArray();

        var lockedObjectList = graph.stream().map(item -> {
            JSONObject lockedObject = new JSONObject();
            try {
                StringTokenizer stringTokenizer = new StringTokenizer(item, "#");
                lockedObject.put("type", stringTokenizer.nextToken().trim());
                lockedObject.put("id", stringTokenizer.nextToken());
                lockedObject.put("force", Boolean.valueOf(stringTokenizer.nextToken()));
                return lockedObject;
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collector.of(JSONArray::new, JSONArray::put, JSONArray::put));

        var lockedObjects = new JSONObject().put("lockObjects", lockedObjectList);
        lockedGroups.put(lockedObjects);

        return lockedObjects;
    }

    public static String prepareDeleteLockRequest(UUID lockId) throws JsonProcessingException {
        DeleteLockRequestDto request = new DeleteLockRequestDto();
        request.lockId = lockId;
        return objectMapper.writeValueAsString(request);
    }

    public static String prepareGetLockStatusRequest(UUID lockId) throws JsonProcessingException {
        GetLockStatusRequestDto request = new GetLockStatusRequestDto();
        request.lockId = lockId;
        return objectMapper.writeValueAsString(request);
    }

    public static String retrieveResponseFromKafka(BlockingQueue<ConsumerRecord<String, String>> consumer)
            throws InterruptedException {
        var lockResponse = retrieveRecordFromResponseTopic(consumer, 20000);
        var lastRecord = lockResponse;
        while (nonNull(lockResponse)) {
            String actualCorrelationId = "";
            for (Header header : lockResponse.headers()) {
                if (CORRELATION_ID_HEADER.equals(header.key())) {
                    actualCorrelationId = new String(header.value());
                }
            }
            if (currentCorrelationId != actualCorrelationId) {
                lastRecord = lockResponse;
                lockResponse = consumer.poll(1000, MILLISECONDS);
            }
        }
        checkCorrelationIdHeader(currentCorrelationId, lastRecord);
        return lastRecord.value();
    }

    public static ConsumerRecord<String, String> retrieveRecordFromResponseTopic(
            BlockingQueue<ConsumerRecord<String, String>> records, long timeOut) throws InterruptedException {
        var record = records.poll(timeOut, MILLISECONDS);
        assertNotNull(record);
        return record;
    }

    public static void checkCorrelationIdHeader(String correlationId, ConsumerRecord<String, String> singleRecord) {
        String actualCorrelationId = "";
        for (Header header : singleRecord.headers()) {
            if (CORRELATION_ID_HEADER.equals(header.key())) {
                actualCorrelationId = new String(header.value());
            }
        }

        assertEquals(correlationId, actualCorrelationId);
    }
}
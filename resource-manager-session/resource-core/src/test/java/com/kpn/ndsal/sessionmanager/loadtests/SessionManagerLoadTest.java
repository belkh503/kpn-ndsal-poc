//package com.kpn.ndsal.sessionmanager.loadtests;
//
//import static com.kpn.ndsal.sessionmanager.util.TestUtils.configureConsumer;
//import static com.kpn.ndsal.sessionmanager.util.TestUtils.configureProducer;
//import static com.kpn.ndsal.sessionmanager.util.TestUtils.createAcquireRequest;
//import static com.kpn.ndsal.sessionmanager.util.TestUtils.createReleaseRequest;
//import static com.kpn.ndsal.sessionmanager.util.TestUtils.getCorrelationIdHeader;
//import static com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto.Priority.HIGH;
//import static com.kpn.ndsal.sessionmanager.util.Constants.KAFKA_CORRELATION_ID_HEADER;
//import static org.assertj.core.api.Assertions.assertThat;
//import static org.junit.jupiter.api.Assertions.assertEquals;
//
//import java.text.MessageFormat;
//import java.util.HashSet;
//import java.util.LinkedList;
//import java.util.List;
//import java.util.Set;
//import java.util.UUID;
//import java.util.concurrent.Future;
//
//import org.apache.kafka.clients.consumer.Consumer;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.clients.consumer.ConsumerRecords;
//import org.apache.kafka.clients.producer.Producer;
//import org.apache.kafka.clients.producer.ProducerRecord;
//import org.apache.kafka.clients.producer.RecordMetadata;
//import org.apache.kafka.common.errors.WakeupException;
//import org.apache.kafka.common.header.Headers;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.kafka.test.EmbeddedKafkaBroker;
//import org.springframework.kafka.test.context.EmbeddedKafka;
//import org.springframework.kafka.test.utils.KafkaTestUtils;
//import org.springframework.test.context.ActiveProfiles;
//
//import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
//import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;
//import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
//import com.kpn.ndsal.sessionmanager.model.SessionReleaseResponseDto;
//
//import lombok.Data;
//import lombok.NonNull;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//
//@SpringBootTest
//@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
//@ActiveProfiles("test")
//@Slf4j
//class SessionManagerLoadTest {
//
//    @Autowired
//    EmbeddedKafkaBroker embeddedKafkaBroker;
//
//    @Value("${spring.kafka.consumer.acquire-topic}")
//    private String acquireRequestTopic;
//
//    @Value("${spring.kafka.producer.acquire-response-topic}")
//    private String acquireResponseTopic;
//
//    @Value("${spring.kafka.consumer.release-topic}")
//    private String releaseRequestTopic;
//
//    @Value("${spring.kafka.producer.release-response-topic}")
//    private String releaseResponseTopic;
//
//    @Test
//    void acquireAndReleaseLoadTest() throws Exception {
//        embeddedKafkaBroker.addTopicsWithResults(acquireResponseTopic, releaseResponseTopic);
//
//        try (Consumer<String, SessionAcquireResponseDto> acquireConsumer = configureConsumer(embeddedKafkaBroker,
//                acquireResponseTopic, "groupId1", SessionAcquireResponseDto.class);
//             Consumer<String, SessionReleaseResponseDto> releaseConsumer = configureConsumer(embeddedKafkaBroker,
//                     releaseResponseTopic, "groupId2", SessionReleaseResponseDto.class);
//             Producer<String, SessionAcquireRequestDto> acquireProducer = configureProducer(embeddedKafkaBroker);
//             Producer<String, SessionReleaseRequestDto> releaseProducer = configureProducer(embeddedKafkaBroker)) {
//
//            int size = 10000;
//
//            Set<String> correlationIds = new HashSet<>();
//
//            List<ConsumerRecord<String, SessionAcquireResponseDto>> acquireResponses = sendAcquireRequests(
//                    acquireConsumer, acquireProducer, size, correlationIds);
//
//            log.debug(MessageFormat.format("acquireResponses size {0}", acquireResponses.size()));
//            List<UUID> acquiredUuids = getAcquiredUuids(size, correlationIds, acquireResponses);
//
//            correlationIds = new HashSet<>();
//            List<ConsumerRecord<String, SessionReleaseResponseDto>> releaseResponses = sendReleaseRequests(
//                    releaseConsumer, releaseProducer, size, acquiredUuids, correlationIds);
//
//            checkReleaseResponses(size, releaseResponses, correlationIds);
//        }
//
//        // put a breakpoint here to find out the time taken for acquire and release
//        String breakpointLocation;
//    }
//
//    private List<ConsumerRecord<String, SessionAcquireResponseDto>> sendAcquireRequests(
//            Consumer<String, SessionAcquireResponseDto> consumer, Producer<String, SessionAcquireRequestDto> producer,
//            int size, Set<String> correlationIds) throws Exception {
//        ProducerRunnable<String, SessionAcquireRequestDto> producerRunnable = new ProducerRunnable<>(size, producer,
//                correlationIds, i -> {
//                    SessionAcquireRequestDto acquireRequest = createAcquireRequest(HIGH, 180, "bcpe", "ge104",
//                            "test" + i, 3);
//                    ProducerRecord<String, SessionAcquireRequestDto> producerRecord = new ProducerRecord<>(
//                            acquireRequestTopic, acquireRequest);
//                    return producerRecord;
//                });
//        Thread tproducer = new Thread(producerRunnable);
//        tproducer.start();
//
//        ConsumerRunnable<String, SessionAcquireResponseDto> consumerRunnable = new ConsumerRunnable<>(size, 80,
//                consumer);
//        Thread tconsumer = new Thread(consumerRunnable);
//        tconsumer.start();
//
//        tproducer.join();
//        tconsumer.join();
//
//        log.info(MessageFormat.format("end of acquire records wait get {0} records",
//                consumerRunnable.getRecords().size()));
//        return consumerRunnable.getRecords();
//    }
//
//    public void addCorrelationIdHeader(Headers headers, String correlationId) {
//        headers.add(KAFKA_CORRELATION_ID_HEADER, correlationId.getBytes());
//    }
//
//    private List<ConsumerRecord<String, SessionReleaseResponseDto>> sendReleaseRequests(
//            Consumer<String, SessionReleaseResponseDto> consumer, Producer<String, SessionReleaseRequestDto> producer,
//            int size, List<UUID> uuids, Set<String> correlationIds) throws InterruptedException {
//
//        ProducerRunnable<String, SessionReleaseRequestDto> producerRunnable = new ProducerRunnable<>(size, producer,
//                correlationIds, i -> {
//                    SessionReleaseRequestDto releaseRequest = createReleaseRequest(uuids.get(i));
//                    ProducerRecord<String, SessionReleaseRequestDto> producerRecord = new ProducerRecord<>(
//                            releaseRequestTopic, releaseRequest);
//                    return producerRecord;
//                });
//
//        Thread tproducer = new Thread(producerRunnable);
//        tproducer.start();
//
//        ConsumerRunnable<String, SessionReleaseResponseDto> consumerRunnable = new ConsumerRunnable<>(size, 80,
//                consumer);
//        Thread tconsumer = new Thread(consumerRunnable);
//        tconsumer.start();
//
//        tproducer.join();
//        tconsumer.join();
//
//        log.info(MessageFormat.format("end of release records wait get {0} records", consumerRunnable.records.size()));
//
//        return consumerRunnable.records;
//    }
//
//    private List<UUID> getAcquiredUuids(int size, Set<String> correlationIds,
//            List<ConsumerRecord<String, SessionAcquireResponseDto>> acquireResponses) {
//
//        List<ConsumerRecord<String, SessionAcquireResponseDto>> filteredAcquireResponses = checkAndFilterByCorrelationIds(
//                size, correlationIds, acquireResponses);
//
//        List<UUID> acquiredUuids = filteredAcquireResponses.stream().map(record -> record.value().getUuid()).toList();
//
//        return acquiredUuids;
//    }
//
//    private void checkReleaseResponses(int size,
//            List<ConsumerRecord<String, SessionReleaseResponseDto>> releaseResponses, Set<String> correlationIds) {
//
//        List<ConsumerRecord<String, SessionReleaseResponseDto>> filteredReleaseResponses = checkAndFilterByCorrelationIds(size,
//                releaseResponses, correlationIds);
//
//        List<UUID> releasedUuids = filteredReleaseResponses.stream().filter(
//                record -> !record.value().getUuids().isEmpty()).map(
//                record -> record.value().getUuids().get(0)).toList();
//        int numReleasedSessions = releasedUuids.size();
//        assertThat(numReleasedSessions).isEqualTo(size);
//    }
//
//    private List<ConsumerRecord<String, SessionAcquireResponseDto>> checkAndFilterByCorrelationIds(int size,
//            Set<String> correlationIds, List<ConsumerRecord<String, SessionAcquireResponseDto>> acquireResponses) {
//
//        List<ConsumerRecord<String, SessionAcquireResponseDto>> filteredAcquireResponses = acquireResponses.stream()
//                .filter(response -> correlationIds.contains(getCorrelationIdHeader(response.headers()))).toList();
//
//        assertEquals(size, correlationIds.size());
//        assertEquals(size, filteredAcquireResponses.size());
//
//        return filteredAcquireResponses;
//    }
//
//    private List<ConsumerRecord<String, SessionReleaseResponseDto>> checkAndFilterByCorrelationIds(int size,
//            List<ConsumerRecord<String, SessionReleaseResponseDto>> releaseResponses, Set<String> correlationIds) {
//
//        List<ConsumerRecord<String, SessionReleaseResponseDto>> filteredReleaseResponses = releaseResponses.stream()
//                .filter(response -> correlationIds.contains(getCorrelationIdHeader(response.headers()))).toList();
//
//        assertEquals(size, correlationIds.size());
//        assertEquals(size, filteredReleaseResponses.size());
//        return filteredReleaseResponses;
//    }
//
//    @Data
//    @RequiredArgsConstructor
//    public class ConsumerRunnable<K, V> implements Runnable {
//
//        List<ConsumerRecord<K, V>> records = new LinkedList<>();
//        @NonNull
//        Integer size;
//        @NonNull
//        Integer timeoutInS;
//        @NonNull
//        Consumer<K, V> consumer;
//
//        @Override
//        public void run() {
//
//            for (int i = 0; i <= timeoutInS; i++) {
//                try {
//                    ConsumerRecords<K, V> tmpRecords = KafkaTestUtils.getRecords(consumer, 1000, size);
//                    tmpRecords.forEach(record -> records.add(record));
//                    if (records.size() >= size) {
//                        break;
//                    }
//                } catch (WakeupException e) {
//                    log.info("a wake up exception was thrown", e);
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e1) {
//                        break;
//                    }
//                }
//            }
//        }
//
//    }
//
//    @FunctionalInterface
//    public interface GenerateRecords<T> {
//        T generate(int i);
//    }
//
//    @Data
//    @RequiredArgsConstructor
//    public class ProducerRunnable<K, V> implements Runnable {
//
//        @NonNull
//        Integer size;
//        @NonNull
//        Producer<K, V> producer;
//
//        @NonNull
//        Set<String> correlationIds;
//
//        @NonNull
//        GenerateRecords<ProducerRecord<K, V>> generateRecords;
//
//        @Override
//        public void run() {
//            List<Future<RecordMetadata>> futures = new LinkedList<>();
//            for (int i = 0; i < size; i++) {
//                ProducerRecord<K, V> producerRecord = generateRecords.generate(i);
//
//                String correlationId = UUID.randomUUID().toString();
//                correlationIds.add(correlationId);
//
//                addCorrelationIdHeader(producerRecord.headers(), correlationId);
//
//                futures.add(producer.send(producerRecord));
//            }
//
//            boolean oneCancelled = false;
//            while (!oneCancelled && !futures.stream().allMatch(Future::isDone)) {
//                oneCancelled = futures.stream().anyMatch(Future::isCancelled);
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e) {
//                    oneCancelled = true;
//                }
//            }
//            if (oneCancelled) {
//                throw new AssertionError("not all acquire request send successfully some were cancelled");
//            }
//        }
//    }
//}
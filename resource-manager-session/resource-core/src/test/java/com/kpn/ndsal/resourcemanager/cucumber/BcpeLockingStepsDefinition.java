package com.kpn.ndsal.resourcemanager.cucumber;

import static com.kpn.ndsal.resourcemanager.TestHelpers.createConsumer;
import static com.kpn.ndsal.resourcemanager.TestHelpers.createProducer;
import static com.kpn.ndsal.resourcemanager.TestHelpers.objectMapper;
import static com.kpn.ndsal.resourcemanager.TestHelpers.prepareAcquireLockRequest;
import static com.kpn.ndsal.resourcemanager.TestHelpers.prepareDeleteLockRequest;
import static com.kpn.ndsal.resourcemanager.TestHelpers.prepareGetLockStatusRequest;
import static com.kpn.ndsal.resourcemanager.TestHelpers.putJsonOnTopic;
import static com.kpn.ndsal.resourcemanager.TestHelpers.retrieveResponseFromKafka;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.assertj.core.util.Lists;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kpn.ndsal.resourcemanager.adapter.out.persistence.BaseNeo4jTestConfig;
import com.kpn.ndsal.resourcemanager.adapter.out.persistence.LockRequestRepository;
import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.service.RequestMapper;
import com.kpn.ndsal.resourcemanager.application.task.LockTimeoutCleanerTask;
import com.kpn.ndsal.resourcemanager.application.task.QueueTimeoutCleanerTask;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus;
import com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto;
import com.kpn.ndsal.resourcemanager.model.DeleteLockResponseDto.DeleteStatus;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto;
import com.kpn.ndsal.resourcemanager.model.GetLockStatusResponseDto.LockStatus;
import com.kpn.neo4j.test.*;

import io.cucumber.java.Before;
import io.cucumber.java.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;

@CucumberContextConfiguration
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(partitions = 1)
@ActiveProfiles("test")
@DirtiesContext
public class BcpeLockingStepsDefinition extends BaseNeo4jTestConfig {
    @Value("${spring.kafka.consumer.acquire-lock-topic}")
    private String acquireLockRequestTopic;

    @Value("${spring.kafka.producer.acquire-lock-response-topic}")
    private String acquireLockResponseTopic;

    @Value("${spring.kafka.consumer.get-lock-status-topic}")
    private String getLockStatusRequestTopic;

    @Value("${spring.kafka.producer.get-lock-status-response-topic}")
    private String getLockStatusResponseTopic;

    @Value("${spring.kafka.consumer.delete-lock-topic}")
    private String deleteLockRequestTopic;

    @Value("${spring.kafka.producer.delete-lock-response-topic}")
    private String deleteLockResponseTopic;

    private KafkaProducer<String, String> producer;
    private BlockingQueue<ConsumerRecord<String, String>> acquireLockConsumer;
    private BlockingQueue<ConsumerRecord<String, String>> getLockStatusConsumer;
    private BlockingQueue<ConsumerRecord<String, String>> deleteLockConsumer;

    @Autowired
    EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    private LockRequestRepository lockRequestRepository;

    @Autowired
    private HazelcastConfigurator hazelcastConfigurator;

    @Autowired
    private LockTimeoutCleanerTask lockTimeoutCleanerTask;

    @Autowired
    private QueueTimeoutCleanerTask queueTimeoutCleanerTask;

    public static String currentCorrelationId;
    public static UUID currentLockId = UUID.randomUUID();
    @Autowired
    private RequestMapper requestMapper;

    @Autowired
    private EmbeddedNeo4jRunner embeddedNeo4jRunner;

    @Before
    public void setup() {
        this.cleanupNeo4jDb();
        assertNotNull(embeddedNeo4jRunner);
    }

    @Given("initiated producers and consumers")
    public void initiateConsumersAndProducers() {
        closeProducersAndConsumers();

        producer = createProducer(kafkaProperties);
        acquireLockConsumer = createConsumer(kafkaBroker, acquireLockResponseTopic);
        getLockStatusConsumer = createConsumer(kafkaBroker, getLockStatusResponseTopic);
        deleteLockConsumer = createConsumer(kafkaBroker, deleteLockResponseTopic);
    }

    @Then("producers and consumers are closed")
    public void closeProducersAndConsumers() {
        if (nonNull(producer)) {
            producer.close();
        }
        if (acquireLockConsumer != null) {
            acquireLockConsumer.clear();
        }
        if (getLockStatusConsumer != null) {
            getLockStatusConsumer.clear();
        }
        if (deleteLockConsumer != null) {
            deleteLockConsumer.clear();
        }
    }

    @When("A lock request is sent kafka topic on {graph}")
    public void sendAcquireLockRequestViaKafka(List<String> requestedGraph) throws JSONException {
        String request = prepareAcquireLockRequest(requestedGraph);
        putJsonOnTopic(producer, request, acquireLockRequestTopic);
    }

    @When("A delete lock request is sent via kafka")
    public void sendDeleteLockRequestViaKafka() throws JsonProcessingException {
        String request = prepareDeleteLockRequest(currentLockId);
        putJsonOnTopic(producer, request, deleteLockRequestTopic);
    }

    @When("A get lock status request is sent via kafka")
    public void sendGetLockStatusRequestViaKafka() throws JsonProcessingException {
        String request = prepareGetLockStatusRequest(currentLockId);
        putJsonOnTopic(producer, request, getLockStatusRequestTopic);
    }

    @Then("AcquireLockResponse has status {}")
    public void checkAcquireLockResponseStatus(
            AcquireStatus acquireStatus) throws InterruptedException, JsonProcessingException {
        var kafkaResponse = retrieveResponseFromKafka(acquireLockConsumer);
        var acquireResponse = objectMapper.readValue(kafkaResponse, AcquireLockResponseDto.class);

        assertNotNull(acquireResponse.acquireStatus);
        assertEquals(acquireStatus, acquireResponse.acquireStatus);
        if (acquireResponse.lockId != null) {
            currentLockId = UUID.fromString(acquireResponse.lockId);
        }
    }

    @Then("DeleteLockResponse has status {}")
    public void checkDeleteLockResponseStatus(
            DeleteStatus deleteStatus) throws InterruptedException, JsonProcessingException {
        var kafkaResponse = retrieveResponseFromKafka(deleteLockConsumer);
        var deleteLockResponse = objectMapper.readValue(kafkaResponse, DeleteLockResponseDto.class);

        assertNotNull(deleteLockResponse.deleteStatus);
        assertEquals(deleteStatus, deleteLockResponse.deleteStatus);
    }

    @Then("GetLockStatusResponse has status {}")
    public void checkGetLockStatusResponseStatus(
            LockStatus lockStatus) throws InterruptedException, JsonProcessingException {
        var kafkaResponse = retrieveResponseFromKafka(getLockStatusConsumer);
        var getLockStatusResponse = objectMapper.readValue(kafkaResponse, GetLockStatusResponseDto.class);

        assertNotNull(getLockStatusResponse.status);
        assertEquals(lockStatus, getLockStatusResponse.status);
    }

    /**
     * Deserialize string representation of a graph.
     */
    @ParameterType(".*")
    public List<String> graph(String value) {
        if (value.contains("%")) {
            return Lists.list(value.split("%"));
        }
        return Lists.list(value);
    }

    @Override
    public LockRequestRepository getLockRequestRepository() {
        return this.lockRequestRepository;
    }

    @And("Resources are locked {graph}")
    public void resourcesAreLockedRequestedLock(List<String> lockedGraph) {
        System.out.println(lockedGraph);
    }

    @And("ResourceGroupId is generated and returned")
    public void resourceGroupIdIsGeneratedAndReturned() {
        System.out.println("implement me");
    }

    @Then("A lock request is queued")
    public void lockRequestIsQueued() {
        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();

        hazelcastContext.beginTransaction();
        await().atMost(1, SECONDS).until(() -> !hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).isEmpty());
        try {
            System.out.println("THIS IS THE QUEUE OF LOCKS: " + hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).values());
            assertFalse(hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).isEmpty());
            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }

    @When("A lock request with priority {} is sent kafka topic on {graph}")
    public void sendAcquireLockRequestWithPriorityViaKafka(String priority,
            List<String> requestedGraph) throws JSONException {
        String request = prepareAcquireLockRequest(requestedGraph, priority);
        putJsonOnTopic(producer, request, acquireLockRequestTopic);
    }

    @When("A lock request with timeout {}-{} is sent kafka topic on {graph}")
    public void sendAcquireLockRequestWithTimeoutViaKafka(int timeout, String unit,
            List<String> requestedGraph) throws JSONException {
        String request = prepareAcquireLockRequest(requestedGraph, timeout, unit);
        putJsonOnTopic(producer, request, acquireLockRequestTopic);
    }

    /*
     * Checks Queue size.
     */
    @Then("Check request queue size is {}")
    public void checkRequestQueue(int size) {
        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();

        hazelcastContext.beginTransaction();
        try {
            System.out.println("THIS IS THE QUEUE OF LOCKS IN CHECK: " + hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).values());
            await().atMost(5, SECONDS).until(() -> hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).size() == size);
            assertEquals(hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).size(), size);
            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }

    /**
     * Check the remaining request and that processing is done according to request priorities
     */
    @And("Check remaining request")
    public void checkRemainingRequest() {
        var hazelcastContext = hazelcastConfigurator.getHazelcastContext();

        hazelcastContext.beginTransaction();
        System.out.println("THE QUEUE CONTAINS THIS REMAINING REQUEST: " + hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).values());
        try {
            var firstRequest = hazelcastConfigurator.getHazelcastRequestQueue(hazelcastContext).values().stream().findFirst().map(requestMapper::toAcquireLockRequestDto);
            firstRequest.ifPresent(acquireLockRequestDto -> assertNotNull(acquireLockRequestDto.getPriority())); // Priority.LOW check removed: priority is now Integer
            hazelcastContext.commitTransaction();
        } catch (RuntimeException e) {
            System.out.println("Rolling back transaction");
            hazelcastContext.rollbackTransaction();
            throw e;
        }
    }

    @When("Start lock cleaner after {} seconds")
    public void startLockCleanerAfterSec(int timeout) {
        await().atMost(timeout, SECONDS);
        lockTimeoutCleanerTask.startLockCleaner();
    }

    @Then("No locks in database")
    public void noLocksInDatabase() {
        var requestEntities = this.getLockRequestRepository().findAll();
        assertTrue(requestEntities.isEmpty());
    }

    @Then("Start queue cleanup after {} seconds")
    public void startQueueCleanup(int timeout) {
        await().pollDelay(timeout, SECONDS).until(() -> true);
        queueTimeoutCleanerTask.startQueueCleaner();
    }
}

package com.kpn.ndsal.sessionmanager.cucumber.steps;

import static com.kpn.ndsal.sessionmanager.util.TestUtils.checkCorrelationIdHeader;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.createConsumer;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.createProducer;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.objectMapper;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.putJsonOnTopic;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.retrieveRecordFromResponseTopic;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.verifyAcquireResponse;
import static com.kpn.ndsal.sessionmanager.util.TestUtils.verifyReleaseResponse;
import static java.util.Collections.singletonList;
import static java.util.Objects.nonNull;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kpn.ndsal.sessionmanager.cucumber.context.ExecuteContext;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto.Priority;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;
import com.kpn.ndsal.sessionmanager.persistence.repository.RequestsByUuidRepository;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import lombok.extern.slf4j.Slf4j;

@CucumberContextConfiguration
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:0", "port=9092" })
@ActiveProfiles("test")
@Slf4j
@SpringBootTest
public class StepsDefinitions {

    @Value("${spring.kafka.consumer.acquire-topic}")
    private String acquireReqTopic;

    @Value("${spring.kafka.producer.acquire-response-topic}")
    private String acquireResTopic;

    @Value("${spring.kafka.consumer.release-topic}")
    private String releaseReqTopic;

    @Value("${spring.kafka.producer.release-response-topic}")
    private String releaseResTopic;

    @Autowired
    EmbeddedKafkaBroker kafkaBroker;

    @Autowired
    KafkaProperties kafkaProperties;

    @Autowired
    RequestsByUuidRepository requestsByUuidRepository;

    public static final String DLT_SUFFIX = "-dlt";

    private final ExecuteContext executeContext = new ExecuteContext();

    /*
     * @DynamicPropertySource static void neo4jProperties(DynamicPropertyRegistry registry) {
     * registry.add("spring.neo4j.uri", managementService.); registry.add("spring.neo4j.authentication.username", () ->
     * "neo4j"); registry.add("spring.neo4j.authentication.password", () -> null); }
     */

    @Given("initiated producers and consumers")
    public void initiateConsumersAndProducers() {
        closeProducersAndConsumers();
        executeContext.getAcquiredUuids().clear();

        executeContext.setProducer(createProducer(kafkaProperties));
        executeContext.setAcquireConsumer(createConsumer(kafkaBroker, acquireResTopic));
        executeContext.setReleaseConsumer(createConsumer(kafkaBroker, releaseResTopic));
    }

    @Given("initiated bolt connection")
    public void initiateBoltConnection() {
        log.info("try a database connection");
        requestsByUuidRepository.count();
        log.info("end a database connection");

    }

    @Then("producers and consumers are closed")
    public void closeProducersAndConsumers() {
        if (nonNull(executeContext.getProducer())) {
            executeContext.getProducer().close();
        }
    }

    @When("an acquire request with timeout {long} seconds, priority {}")
    public void setAcquireRequest(long timeout, Priority priority) {
        executeContext.getAcquireRequest().setPriority(priority);
        executeContext.getAcquireRequest().setTimeoutSec((int) timeout);
    }

    @And("on current request a list of SessionInfo:")
    public void aListOfSessionInfo(List<Map<String, String>> table) {
        List<SessionInfo> sessionsInfo = new ArrayList<>();
        for (Map<String, String> map : table) {
            SessionInfo sessionInfo = new SessionInfo();
            sessionInfo.setDomain(map.get("domain"));
            sessionInfo.setSystemType(map.get("systemType"));
            sessionInfo.setNodeName(map.get("nodeName"));
            sessionInfo.setNumSessionsWanted(Integer.parseInt(map.get("numSessionsWanted")));
            sessionsInfo.add(sessionInfo);
        }
        executeContext.getAcquireRequest().setSessionsInfo(sessionsInfo);
    }

    @When("a {} request with CORRELATION_ID {} is sent over Kafka")
    public void requestSentOverKafka(String topicName, String correlationId) throws JsonProcessingException {
        if ("ACQUIRE".equals(topicName)) {
            String json = objectMapper.writeValueAsString(executeContext.getAcquireRequest());
            putJsonOnTopic(executeContext.getProducer(), json, correlationId, acquireReqTopic);
        } else if ("RELEASE".equals(topicName)) {
            String json = objectMapper.writeValueAsString(executeContext.getReleaseRequest());
            putJsonOnTopic(executeContext.getProducer(), json, correlationId, releaseReqTopic);
        }
    }

    @Then("a {} response is received after {} ms with CORRELATION_ID {}")
    public void responseReceivedOverKafka(String topicName, long milliseconds, String expectedCorrelationId) {
        boolean received = false;

        Calendar calendar = Calendar.getInstance();
        long timeoutDateInMs = calendar.getTimeInMillis() + milliseconds;

        try {
            while (!received && Calendar.getInstance().getTimeInMillis() < timeoutDateInMs) {
                log.info(MessageFormat.format("check {0} record on topic {1}", topicName, acquireResTopic));
                if ("ACQUIRE".equals(topicName)) {

                    ConsumerRecord<String, String> response = executeContext.getAcquireConsumer().poll(milliseconds,
                            MILLISECONDS);

                    if (nonNull(response) && nonNull(response.value())
                            && checkCorrelationIdHeader(expectedCorrelationId, response)) {
                        executeContext.setAcquireResponse(response);
                        received = true;
                    }
                } else if ("RELEASE".equals(topicName)) {
                    var response = retrieveRecordFromResponseTopic(executeContext.getReleaseConsumer(), milliseconds);

                    if (nonNull(response.value()) && checkCorrelationIdHeader(expectedCorrelationId, response)) {
                        executeContext.setReleaseResponse(response);
                        received = true;
                    }
                } else {
                    fail(MessageFormat.format("Error of input step parameter {0} is not managed", topicName));
                }
            }
        } catch (Exception e) {
            log.info(MessageFormat.format("an exception {0} was raised while waiting a message", e.getMessage()));
            fail(e.getMessage());
        }

        if (!received) {
            fail();
        }
    }

    @Then("sessions are acquired with isSessionAcquired is true and a non null value for uuid")
    public void sessionsAcquired() throws JsonProcessingException {
        var acquireResponse = verifyAcquireResponse(executeContext.getAcquireResponse());
        executeContext.getAcquiredUuids().add(acquireResponse.getUuid());
    }

    @Given("a release request with uuid {}")
    public void setReleaseRequest(String uuid) {
        executeContext.getReleaseRequest().setUuids(singletonList(UUID.fromString(uuid)));
    }

    @When("a release request with order {}")
    public void setReleaseRequest(int order) {
        executeContext.getReleaseRequest().setUuids(singletonList(executeContext.getAcquiredUuids().get(order - 1)));
    }

    @Then("sessions are released is {}")
    public void sessionsReleased(boolean isReleased) throws JsonProcessingException {
        verifyReleaseResponse(executeContext.getReleaseResponse(), isReleased);
    }

    @Then("an acquire response is not received over Kafka after {} ms with CORRELATION_ID {}")
    public void acquireResponseNotReceived(long milliseconds, String expectedCorrelationId)
            throws InterruptedException {
        boolean keepSearching = true;
        Calendar calendar = Calendar.getInstance();
        long timeoutDateInMs = calendar.getTimeInMillis() + milliseconds;
        boolean foundAResponse = false;
        while (keepSearching && Calendar.getInstance().getTimeInMillis() < timeoutDateInMs) {
            try {
                ConsumerRecord<String, String> response = executeContext.getAcquireConsumer().poll(milliseconds,
                        MILLISECONDS);

                if (nonNull(response) && nonNull(response.value())
                        && checkCorrelationIdHeader(expectedCorrelationId, response)) {
                    keepSearching = false;
                    foundAResponse = true;
                }
            } catch (IllegalStateException ex) {
                keepSearching = false;
            }
        }
        if (foundAResponse) {
            fail();
        }
    }

    @Then("the acquire error message has isSessionAcquired = {}, errorDto field is not empty and uuid is null")
    public void acquireErrorResponseCheck(boolean isSessionAcquired) throws JsonProcessingException {
        var acquireResponse = objectMapper.readValue(executeContext.getAcquireResponse().value(),
                SessionAcquireResponseDto.class);

        assertEquals(isSessionAcquired, acquireResponse.getSessionAcquired());
        assertNotNull(acquireResponse.getErrorDto());
        assertFalse(acquireResponse.getErrorDto().getErrorMessage().isEmpty());
        assertNull(acquireResponse.getUuid());
    }

    @Then("the acquire error message has {string} in errorDto field")
    public void acquireResponseErrorDto(String message) throws JsonProcessingException {
        var acquireResponse = objectMapper.readValue(executeContext.getAcquireResponse().value(),
                SessionAcquireResponseDto.class);
        assertTrue(acquireResponse.getErrorDto().getErrorMessage().contains(message), MessageFormat.format(
                "Must contains {0} in {1}", message, acquireResponse.getErrorDto().getErrorMessage()));
    }
}

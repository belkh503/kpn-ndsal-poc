package com.kpn.ndsal.sessionmanager.unittests.kafka.release;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.resourcemanager.application.configuration.JsonSchemaValidationConfig;
import com.kpn.ndsal.sessionmanager.kafka.release.ReleaseRequestListener;
import com.kpn.ndsal.sessionmanager.kafka.release.ReleaseResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionReleaseResponseDto;
import com.kpn.ndsal.sessionmanager.task.ReleaseTask;

@SpringBootTest(classes = {ReleaseRequestListener.class, JsonSchemaValidationConfig.class, ObjectMapper.class})
 class ReleaseRequestListenerTest {

    @MockitoBean
    private  ReleaseResponseProducer releaseResponseProducer;
    @MockitoBean
    private  ReleaseTask releaseTask;

    @Autowired
    private ReleaseRequestListener releaseRequestListener;

    @Test
    void listener_success() {
        Map<String, byte[]> headers = new HashMap<>();
        String correlationId = UUID.randomUUID().toString();
        headers.put(BaseListener.CORRELATION_ID_HEADER, correlationId.getBytes());


        String message = """
                {
                   "uuids":[
                      "f90a73ee-32e5-4d3b-8c1f-bd31d4898a5f",
                      "efadb320-dcad-4baf-bfef-87495f1386bf"
                   ]
                }
                """;

        releaseRequestListener.listener(message, "topic", headers);

        verify(releaseTask, times(1)).startRelease(any(SessionReleaseRequestDto.class), eq(correlationId));
    }

    @Test
    void handleErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        RuntimeException e = new RuntimeException("uh oh");

        releaseRequestListener.handleErrorScenario(e, correlationId.toString(), null, "");

        verify(releaseResponseProducer).sendMessage(any(SessionReleaseResponseDto.class), eq(correlationId.toString()));
    }

    @Test
    void handleValidationErrorScenario() {
        UUID correlationId = UUID.randomUUID();
        ValidationException e = new ValidationException(new java.util.HashSet<com.networknt.schema.ValidationMessage>());

        releaseRequestListener.handleValidationErrorScenario(e, correlationId.toString(), null, "");

        verify(releaseResponseProducer).sendMessage(any(SessionReleaseResponseDto.class), eq(correlationId.toString()));
    }
}
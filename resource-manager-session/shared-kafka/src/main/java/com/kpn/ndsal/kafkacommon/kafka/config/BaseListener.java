package com.kpn.ndsal.kafkacommon.kafka.config;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

public abstract class BaseListener<T> {

    private static final Logger log = LoggerFactory.getLogger(BaseListener.class);
    public static final String CORRELATION_ID_HEADER = "correlationId";

    @Autowired
    private ObjectMapper objectMapper;

    public void listener(String json, String topic, Map<String, byte[]> headers) {
        String correlationId = extractCorrelationId(headers);
        try {
            T request = objectMapper.readValue(json, getRequestClass());
            consume(request, topic, correlationId, headers);
        } catch (ValidationException e) {
            log.warn("Validation error for topic={} correlationId={}: {}", topic, correlationId, e.getMessage());
            handleValidationErrorScenario(e, correlationId, headers, json);
        } catch (Exception e) {
            log.error("Error processing message from topic={} correlationId={}", topic, correlationId, e);
            handleErrorScenario(e, correlationId, headers, json);
        }
    }

    private String extractCorrelationId(Map<String, byte[]> headers) {
        if (headers != null && headers.containsKey(CORRELATION_ID_HEADER)) {
            byte[] value = headers.get(CORRELATION_ID_HEADER);
            if (value != null) {
                return new String(value, StandardCharsets.UTF_8);
            }
        }
        return java.util.UUID.randomUUID().toString();
    }

    protected abstract void consume(T request, String topic, String correlationId, Map<String, byte[]> headers);

    protected abstract Class<T> getRequestClass();

    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers, String requestJson) {
        // Default error handler — override in subclass
    }

    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers, String requestJson) {
        // Default validation error handler — override in subclass
    }
}

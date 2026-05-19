package com.kpn.ndsal.resourcemanager.adapter.in;

import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.ERROR;
import static java.util.stream.Collectors.joining;
import static org.springframework.kafka.support.KafkaHeaders.RECEIVED_TOPIC;

import java.util.Map;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;

import com.kpn.ndsal.kafkacommon.exception.ValidationException;
import com.kpn.ndsal.kafkacommon.kafka.config.BaseListener;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.AcquireLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestQueueUseCase;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;
import com.kpn.ndsal.resourcemanager.application.service.RequestMapper;
import com.kpn.ndsal.resourcemanager.model.AcquireLockRequestDto;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;
import com.networknt.schema.ValidationMessage;

import io.opentelemetry.context.Context;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcquireLockRequestListener extends BaseListener<AcquireLockRequestDto> {

    private final AcquireLockResponseProducer acquireLockResponseProducer;
    private final RequestQueueUseCase requestQueueService;
    private final RequestMapper requestMapper;
    private final ProcessQueueExecutorService processQueueExecutorService;

    @Override
    @KafkaListener(topics = "${spring.kafka.consumer.acquire-lock-topic}",
                   groupId = "${spring.kafka.consumer.group-id}",
                   clientIdPrefix = "${spring.kafka.consumer.acquire-lock-client-id}")
    public void listener(String json, @Header(RECEIVED_TOPIC) String topic, @Headers Map<String, byte[]> headers) {
        log.info("Message received ({}): {}", topic, json);
        super.listener(json, topic, headers);
    }

    @Override
    protected void consume(AcquireLockRequestDto acquireLockRequestDto, String topic, String correlationId,
            Map<String, byte[]> headers) {
        Context context = Context.current();
        var requestQueueEntity = requestMapper.toRequestQueueEntity(acquireLockRequestDto, correlationId, context);
        requestQueueService.addRequestToQueue(requestQueueEntity);
        processQueueExecutorService.startProcessing();
    }

    @Override
    protected Class<AcquireLockRequestDto> getRequestClass() {
        return AcquireLockRequestDto.class;
    }

    @Override
    public void handleErrorScenario(Exception e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        AcquireLockResponseDto response = new AcquireLockResponseDto();
        response.acquireStatus = ERROR;
        response.errorMessage = e.getMessage();
        acquireLockResponseProducer.sendMessage(response, correlationId);
    }

    @Override
    public void handleValidationErrorScenario(ValidationException e, String correlationId, Map<String, byte[]> headers,
            String requestJson) {
        var errorMessage = e.getErrors().stream().map(ValidationMessage::getMessage).collect(joining("\n"));
        AcquireLockResponseDto response = new AcquireLockResponseDto();
        response.acquireStatus = ERROR;
        response.errorMessage = errorMessage;
        acquireLockResponseProducer.sendMessage(response, correlationId);
    }
}

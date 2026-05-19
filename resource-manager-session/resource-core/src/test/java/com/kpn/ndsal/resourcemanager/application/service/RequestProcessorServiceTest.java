package com.kpn.ndsal.resourcemanager.application.service;

import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.ERROR;
import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.RESOURCES_ALREADY_LOCKED;
import static com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto.AcquireStatus.RESOURCES_LOCKED;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kpn.ndsal.resourcemanager.adapter.out.kafka.AcquireLockResponseProducer;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockInvalidRequestException;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockNotPossibleException;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockUseCase;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockWrongDomainException;
import com.kpn.ndsal.resourcemanager.application.port.in.RequestTimeoutExceededException;
import com.kpn.ndsal.resourcemanager.application.queue.out.persistence.RequestQueueEntity;
import com.kpn.ndsal.resourcemanager.model.AcquireLockResponseDto;
import com.kpn.ndsal.resourcemanager.model.Timeout;

import lombok.SneakyThrows;

@SpringBootTest(classes = { ObjectMapper.class, RequestProcessorService.class, RequestMapperImpl.class,
        ContextService.class })
@ActiveProfiles("test")
class RequestProcessorServiceTest {

    @MockitoBean
    private AcquireLockResponseProducer acquireLockResponseProducer;

    @MockitoBean
    private AcquireLockUseCase acquireLockUseCase;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RequestProcessorService requestProcessorService;

    @MockitoBean
    private ContextService contextService;

    @Test
    void processRequest_OK() {
        String correlationId = UUID.randomUUID().toString();
        UUID lockId = UUID.randomUUID();
        var requestQueueEntity = createRequestQueueEntity(correlationId);
        var lockTimeout = new Timeout();
        lockTimeout.setUnit(Timeout.Unit.MIN);
        lockTimeout.setValue(1);
        requestQueueEntity.setTimeout(lockTimeout);
        var acquireLockResponseDto = new AcquireLockResponseDto();
        acquireLockResponseDto.setAcquireStatus(RESOURCES_LOCKED);
        acquireLockResponseDto.setLockId(lockId);

        when(acquireLockUseCase.acquireLock(any(), eq("BCPE"), eq(correlationId), any())).thenReturn(lockId);
        doNothing().when(acquireLockResponseProducer).sendMessage(acquireLockResponseDto, correlationId);

        requestProcessorService.processRequest(requestQueueEntity);

        verify(acquireLockUseCase, times(1)).acquireLock(any(), eq("BCPE"), eq(correlationId), any());
        verify(acquireLockResponseProducer, times(1)).sendMessage(any(), eq(correlationId));
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @Test
    void processRequest_alreadyLocked() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);

        when(acquireLockUseCase.acquireLock(any(), eq("BCPE"), eq(correlationId), any())).thenThrow(new AcquireLockNotPossibleException());

        var exception = assertThrows(AcquireLockNotPossibleException.class, () -> requestProcessorService.processRequest(requestQueueEntity));

        assertTrue(exception.getMessage().contains("Locking rejected because some resources are already locked."));
        verify(acquireLockUseCase, times(1)).acquireLock(any(), eq("BCPE"), eq(correlationId), any());
        verify(acquireLockResponseProducer, never()).sendMessage(any(), any());
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @Test
    void processRequest_error_invalidRequestException() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);
        var lockTimeout = new Timeout();
        lockTimeout.setUnit(Timeout.Unit.SECOND);
        lockTimeout.setValue(60);
        requestQueueEntity.setTimeout(lockTimeout);
        requestQueueEntity.setCreationTime(1L);
        var acquireLockResponseDto = new AcquireLockResponseDto();
        acquireLockResponseDto.setAcquireStatus(ERROR);

        when(acquireLockUseCase.acquireLock(any(), eq("BCPE"), eq(correlationId), any())).thenThrow(new AcquireLockInvalidRequestException());
        doNothing().when(acquireLockResponseProducer).sendMessage(acquireLockResponseDto, correlationId);

        requestProcessorService.processRequest(requestQueueEntity);

        verify(acquireLockUseCase, times(1)).acquireLock(any(), eq("BCPE"), eq(correlationId), any());
        verify(acquireLockResponseProducer, times(1)).sendMessage(any(), eq(correlationId));
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @Test
    void processRequest_error_wrongDomainException() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);
        requestQueueEntity.setCreationTime(1L);
        var acquireLockResponseDto = new AcquireLockResponseDto();
        acquireLockResponseDto.setAcquireStatus(ERROR);

        when(acquireLockUseCase.acquireLock(any(), eq("BCPE"), eq(correlationId), any())).thenThrow(new AcquireLockWrongDomainException());
        doNothing().when(acquireLockResponseProducer).sendMessage(acquireLockResponseDto, correlationId);

        requestProcessorService.processRequest(requestQueueEntity);

        verify(acquireLockUseCase, times(1)).acquireLock(any(), eq("BCPE"), eq(correlationId), any());
        verify(acquireLockResponseProducer, times(1)).sendMessage(any(), eq(correlationId));
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @Test
    void checkRequestTimeout_timeoutTrue() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);
        requestQueueEntity.setCreationTime(1L);
        var acquireLockResponseDto = new AcquireLockResponseDto();
        acquireLockResponseDto.setAcquireStatus(RESOURCES_ALREADY_LOCKED);

        doNothing().when(acquireLockResponseProducer).sendMessage(acquireLockResponseDto, correlationId);

        var exception = assertThrows(RequestTimeoutExceededException.class, () -> requestProcessorService.processRequestTimeout(requestQueueEntity));

        assertTrue(exception.getMessage().contains("Request Queue timeout exceeded."));
        verify(acquireLockResponseProducer, times(1)).sendMessage(any(), eq(correlationId));
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @Test
    void checkRequestTimeout_timeoutFalse() {
        String correlationId = UUID.randomUUID().toString();
        var requestQueueEntity = createRequestQueueEntity(correlationId);
        requestQueueEntity.setCreationTime(System.currentTimeMillis());

        requestProcessorService.processRequestTimeout(requestQueueEntity);

        verify(acquireLockResponseProducer, never()).sendMessage(any(), eq(correlationId));
        verify(contextService, times(1)).setContext(requestQueueEntity);
    }

    @SneakyThrows
    private RequestQueueEntity createRequestQueueEntity(String correlationId) {
        String message = """
                {
                    "domain": "BCPE",
                    "id": "bb18be82-a703-4272-94b5-072ae70cec16",
                    "creationTime": 1,
                    "correlationId": "e3abae16-80d4-48a9-86ae-aeb200e53b6e",
                    "lockGroups": [
                      {
                        "lockObjects":
                        [
                          {
                            "type": "NODE",
                            "id": "nl-pbl-cpe-01"
                          }
                        ]
                      }
                    ]
                  }
                """;
        var requestQueueEntity = objectMapper.readValue(message, RequestQueueEntity.class);
        requestQueueEntity.setCorrelationId(correlationId);
        requestQueueEntity.setCreationTime(System.currentTimeMillis());
        return requestQueueEntity;
    }

}
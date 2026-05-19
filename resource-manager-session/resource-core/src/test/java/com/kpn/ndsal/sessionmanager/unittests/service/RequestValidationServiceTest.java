package com.kpn.ndsal.sessionmanager.unittests.service;

import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireResponseDto;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;
import com.kpn.ndsal.sessionmanager.service.RequestValidationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
class RequestValidationServiceTest {

    @Mock
    private DomainsSessionConfig domains;

    @Mock
    private AcquireResponseProducer acquireResponseProducer;

    @InjectMocks
    private RequestValidationService requestValidationService;

    @Captor
    private ArgumentCaptor<String> correlationIdCaptor;

    @Captor
    private ArgumentCaptor<SessionAcquireResponseDto> sessionAcquireResponseDtoCaptor;

    @Test
    void givenInvalidDomain_whenValidateRequest_thenError() {

        // GIVEN
        var correlationId = UUID.randomUUID().toString();
        var sessionInfo = new SessionInfo();
        sessionInfo.setDomain("bcpe");
        sessionInfo.setSystemType("ge104");
        sessionInfo.setNodeName("test2");
        sessionInfo.setNumSessionsWanted(2);
        var sessionAcquireRequestDto = new SessionAcquireRequestDto();
        sessionAcquireRequestDto.setPriority(SessionAcquireRequestDto.Priority.HIGH);
        sessionAcquireRequestDto.setTimeoutSec(10);
        sessionAcquireRequestDto.setSessionsInfo(List.of(
                sessionInfo
        ));

        when(domains.isValidDomainAndSystemType(any())).thenReturn(false);
        doNothing().when(acquireResponseProducer).sendMessage(sessionAcquireResponseDtoCaptor.capture(), correlationIdCaptor.capture());

        // WHEN
        var result = requestValidationService.isValidRequest(sessionAcquireRequestDto, correlationId);

        // THEN
        assertThat(result).isFalse();

        verify(domains, times(1)).isValidDomainAndSystemType(any());
        verify(domains, never()).getMaxSessions(any());
        verify(acquireResponseProducer, times(1)).sendMessage(any(), any());
        assertThat(correlationIdCaptor.getValue()).isEqualTo(correlationId);

        assertThat(sessionAcquireResponseDtoCaptor.getValue().getErrorDto().errorMessage)
                .isEqualTo(String.format("Invalid domain and/or system type: %s-%s", sessionInfo.getDomain(), sessionInfo.getSystemType()));
    }

    @Test
    void givenInvalidSessionsRequest_whenValidateRequest_thenError() {

        // GIVEN
        var maxSessionsAllowed = 5;
        var correlationId = UUID.randomUUID().toString();
        var sessionInfo = new SessionInfo();
        sessionInfo.setDomain("bcpe");
        sessionInfo.setSystemType("ge104");
        sessionInfo.setNodeName("test2");
        sessionInfo.setNumSessionsWanted(6);
        var sessionAcquireRequestDto = new SessionAcquireRequestDto();
        sessionAcquireRequestDto.setPriority(SessionAcquireRequestDto.Priority.HIGH);
        sessionAcquireRequestDto.setTimeoutSec(10);
        sessionAcquireRequestDto.setSessionsInfo(List.of(
                sessionInfo
        ));

        when(domains.isValidDomainAndSystemType(any())).thenReturn(true);
        when(domains.getMaxSessions(any())).thenReturn(maxSessionsAllowed);
        doNothing().when(acquireResponseProducer).sendMessage(sessionAcquireResponseDtoCaptor.capture(), correlationIdCaptor.capture());

        // WHEN
        var result = requestValidationService.isValidRequest(sessionAcquireRequestDto, correlationId);

        // THEN
        assertThat(result).isFalse();

        verify(domains, times(1)).isValidDomainAndSystemType(any());
        verify(domains, times(2)).getMaxSessions(any());
        verify(acquireResponseProducer, times(1)).sendMessage(any(), any());
        assertThat(correlationIdCaptor.getValue()).isEqualTo(correlationId);

        assertThat(sessionAcquireResponseDtoCaptor.getValue().getErrorDto().errorMessage)
                .isEqualTo(String.format("Invalid numSessionsWanted: You are requesting 6 sessions for domain %s and systemType %s, but we cannot assign more than %d",
                        sessionInfo.getDomain(),
                        sessionInfo.getSystemType(),
                        maxSessionsAllowed));
    }

    @Test
    void givenValidSessionsRequest_whenValidateRequest_thenSuccess() {

        // GIVEN
        var maxSessionsAllowed = 10;
        var correlationId = UUID.randomUUID().toString();
        var sessionInfo = new SessionInfo();
        sessionInfo.setDomain("bcpe");
        sessionInfo.setSystemType("ge104");
        sessionInfo.setNodeName("test2");
        sessionInfo.setNumSessionsWanted(3);
        var sessionAcquireRequestDto = new SessionAcquireRequestDto();
        sessionAcquireRequestDto.setPriority(SessionAcquireRequestDto.Priority.HIGH);
        sessionAcquireRequestDto.setTimeoutSec(10);
        sessionAcquireRequestDto.setSessionsInfo(List.of(
                sessionInfo
        ));

        when(domains.isValidDomainAndSystemType(any())).thenReturn(true);
        when(domains.getMaxSessions(any())).thenReturn(maxSessionsAllowed);

        // WHEN
        var result = requestValidationService.isValidRequest(sessionAcquireRequestDto, correlationId);

        // THEN

        verify(domains, times(1)).isValidDomainAndSystemType(any());
        verify(domains, times(2)).getMaxSessions(any());
        verify(acquireResponseProducer, never()).sendMessage(any(), any());

        assertThat(result).isTrue();
    }
}

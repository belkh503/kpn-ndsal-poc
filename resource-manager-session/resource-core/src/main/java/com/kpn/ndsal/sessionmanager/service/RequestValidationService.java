package com.kpn.ndsal.sessionmanager.service;

import static com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer.createAcquireResponseFailDto;
import static com.kpn.ndsal.sessionmanager.util.Constants.INVALID_DOMAIN_OR_SYSTEM_TYPE;
import static com.kpn.ndsal.sessionmanager.util.Constants.INVALID_NUM_LOCKS;
import static com.kpn.ndsal.sessionmanager.util.SessionEntityUtil.getTripletAsKey;
import static java.lang.String.format;
import static java.lang.String.join;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.model.SessionAcquireRequestDto;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;
import com.kpn.ndsal.sessionmanager.model.SessionRequestInfo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class RequestValidationService {

    private final DomainsSessionConfig domains;
    private final AcquireResponseProducer acquireResponseProducer;

    public boolean isValidRequest(SessionAcquireRequestDto acquireRequest, String correlationId) {
        var sessionsInfo = acquireRequest.getSessionsInfo();
        var invalidDomainSysType = getInvalidDomainSysType(sessionsInfo);

        if (!invalidDomainSysType.isEmpty()) {
            log.debug("Got invalid domain & system type pair");

            var errorMessage = format(INVALID_DOMAIN_OR_SYSTEM_TYPE, join(",", invalidDomainSysType));
            var acquireResponse = createAcquireResponseFailDto(errorMessage);
            acquireResponseProducer.sendMessage(acquireResponse, correlationId);

            return false;
        }

        var sessionsRequestInfo = getSessionsRequestInfo(sessionsInfo);
        var invalidNumSessionsWanted = getInvalidNumSessionsWanted(sessionsRequestInfo);

        if (!invalidNumSessionsWanted.isEmpty()) {
            log.debug("Got invalid num sessions");

            var errorMessage = join("\n", invalidNumSessionsWanted);
            var acquireResponse = createAcquireResponseFailDto(errorMessage);
            acquireResponseProducer.sendMessage(acquireResponse, correlationId);

            return false;
        }

        return true;
    }

    public Collection<SessionRequestInfo> getSessionsRequestInfo(List<SessionInfo> sessionsInfo) {
        var sessionRequestInfoMap = new HashMap<String, SessionRequestInfo>();

        for (var sessionInfo : sessionsInfo) {
            var triplet = new TripletEntity(sessionInfo.getDomain(), sessionInfo.getSystemType(), sessionInfo.getNodeName());
            var maxAllowedSessions = domains.getMaxSessions(triplet);
            var sessionsWanted = sessionInfo.getNumSessionsWanted();

            var tripletKey = getTripletAsKey(sessionInfo.getDomain(), sessionInfo.getSystemType(), sessionInfo.getSystemType());
            if (sessionRequestInfoMap.containsKey(tripletKey)) {
                SessionRequestInfo sessionRequestInfoDto = sessionRequestInfoMap.get(getTripletAsKey(triplet));
                sessionsWanted += sessionRequestInfoDto.getSessionsWanted();
            }
            sessionRequestInfoMap.put(getTripletAsKey(triplet), new SessionRequestInfo(triplet, sessionsWanted, maxAllowedSessions));
        }
        return sessionRequestInfoMap.values();
    }

    private List<String> getInvalidDomainSysType(List<SessionInfo> sessionsInfo) {
        return sessionsInfo.stream()
                .filter(sessionInfo -> !domains.isValidDomainAndSystemType(sessionInfo))
                .map(sessionInfo -> format("%s-%s", sessionInfo.getDomain(), sessionInfo.getSystemType()))
                .toList();
    }

    private List<String> getInvalidNumSessionsWanted(Collection<SessionRequestInfo> sessionRequestInfoList) {
        return sessionRequestInfoList.stream()
                .filter(sessionRequestInfo -> sessionRequestInfo.getSessionsWanted() > domains.getMaxSessions(sessionRequestInfo.getTriplet()))
                .map(this::formatErrorMessage)
                .toList();
    }

    private String formatErrorMessage(SessionRequestInfo sessionRequestInfo) {
        return format(INVALID_NUM_LOCKS, sessionRequestInfo.getSessionsWanted(), sessionRequestInfo.getTriplet().domain(),
                sessionRequestInfo.getTriplet().systemType(), sessionRequestInfo.getMaximumAllowedSessions());
    }
}

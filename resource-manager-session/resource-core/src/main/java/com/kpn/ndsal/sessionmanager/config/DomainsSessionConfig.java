package com.kpn.ndsal.sessionmanager.config;

import static java.util.Objects.isNull;

import javax.annotation.PostConstruct;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import com.kpn.ndsal.sessionmanager.config.data.DomainConfig;
import com.kpn.ndsal.sessionmanager.config.data.SystemTypeConfig;
import com.kpn.ndsal.sessionmanager.config.factory.YamlPropertySourceFactory;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import com.kpn.ndsal.sessionmanager.model.SessionInfo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Configuration
@ConfigurationProperties(prefix = "domains", ignoreUnknownFields = false)
@PropertySource(value = "${domains-config-path}", factory = YamlPropertySourceFactory.class)
@Data
@Slf4j
public class DomainsSessionConfig {

    private Map<String, DomainConfig> domainsConfig = new HashMap<>();

    /**
     * This map is for accessing maximum allowed sessions for a given system type fast. domainsConfig should be fine to
     * use if we have small number of system types for each domain, but if we will have thousands of system types for
     * each domain then getting maximum allowed sessions for a given system type would take O(n), instead of O(1)
     */
    private Map<String, Map<String, SystemTypeConfig>> domainsConfigMap = new HashMap<>();

    private static final String MISSING_MAX_ALLOWED_SESSIONS = "Domain %s is missing maximumAllowedSessions";
    private static final String INVALID_MAX_ALLOWED_SESSIONS = "Domain %s has %d maximumAllowedSessions, but it should be a positive integer";

    @PostConstruct
    private void init() {
        log.info("in init");
        for (Map.Entry<String, DomainConfig> entry : domainsConfig.entrySet()) {
            String domain = entry.getKey();
            DomainConfig domainConfig = entry.getValue();
            if (isNull(domainConfig.getMaximumAllowedSessions())) {
                throw new IllegalArgumentException(String.format(MISSING_MAX_ALLOWED_SESSIONS, domain));
            } else if (domainConfig.getMaximumAllowedSessions() <= 0) {
                throw new IllegalArgumentException(
                        String.format(INVALID_MAX_ALLOWED_SESSIONS, domain, domainConfig.getMaximumAllowedSessions()));
            } else {
                List<SystemTypeConfig> systemTypes = domainConfig.getSystemTypes();
                HashMap<String, SystemTypeConfig> systemTypeConfigHashMap = new HashMap<>();
                for (SystemTypeConfig systemTypeConfig : systemTypes) {
                    systemTypeConfigHashMap.put(systemTypeConfig.getSystemType(), systemTypeConfig);
                }
                domainsConfigMap.put(domain, systemTypeConfigHashMap);
            }
        }
    }

    public boolean isValidDomainAndSystemType(SessionInfo sessionInfo) {
        if (domainsConfigMap.containsKey(sessionInfo.getDomain())) {
            Map<String, SystemTypeConfig> systemTypeConfigMap = domainsConfigMap.get(sessionInfo.getDomain());
            return systemTypeConfigMap.containsKey(sessionInfo.getSystemType());
        } else {
            return false;
        }
    }

    public int getMaxSessions(TripletEntity triplet) {
        Map<String, SystemTypeConfig> domainSystemTypeConfig = domainsConfigMap.get(triplet.domain());
        SystemTypeConfig systemTypeConfig = domainSystemTypeConfig.get(triplet.systemType());
        if (systemTypeConfig.getMaximumAllowedSessions() == null) {
            return domainsConfig.get(triplet.domain()).getMaximumAllowedSessions();
        } else {
            return systemTypeConfig.getMaximumAllowedSessions();
        }
    }
}

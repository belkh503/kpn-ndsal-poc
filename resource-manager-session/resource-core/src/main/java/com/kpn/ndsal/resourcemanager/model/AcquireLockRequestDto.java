package com.kpn.ndsal.resourcemanager.model;

import java.io.Serializable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class AcquireLockRequestDto implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Priority {
        LOW, MEDIUM, HIGH
    }

    private String domain;
    private Priority priority;
    private Timeout timeout;
    public Set<LockGroup> lockGroups = new LinkedHashSet<>();
    private Map<String, Object> additionalProperties;

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public Priority getPriority() {
        return priority;
    }

    public void setPriority(Priority priority) {
        this.priority = priority;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public void setTimeout(Timeout timeout) {
        this.timeout = timeout;
    }

    public Set<LockGroup> getLockGroups() {
        return lockGroups;
    }

    public void setLockGroups(Set<LockGroup> lockGroups) {
        this.lockGroups = lockGroups;
    }

    public Map<String, Object> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}


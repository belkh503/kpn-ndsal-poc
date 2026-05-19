package com.kpn.ndsal.sessionmanager.config.data;

import lombok.Data;

@Data
public class SystemTypeConfig {
    private String systemType;
    private Integer maximumAllowedSessions;
}

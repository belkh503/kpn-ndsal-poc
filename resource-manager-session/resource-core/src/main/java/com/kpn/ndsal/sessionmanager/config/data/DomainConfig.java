package com.kpn.ndsal.sessionmanager.config.data;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DomainConfig {
    private List<SystemTypeConfig> systemTypes = new ArrayList<>();
    private Integer maximumAllowedSessions;
}

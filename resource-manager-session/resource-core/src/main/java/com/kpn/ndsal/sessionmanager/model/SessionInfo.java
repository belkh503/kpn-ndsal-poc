package com.kpn.ndsal.sessionmanager.model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SessionInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private String domain;
    private String systemType;
    private String nodeName;
    private int numSessionsWanted;
}

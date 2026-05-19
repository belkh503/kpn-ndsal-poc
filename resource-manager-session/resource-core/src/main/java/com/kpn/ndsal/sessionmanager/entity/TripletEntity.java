package com.kpn.ndsal.sessionmanager.entity;

import java.io.Serializable;

public record TripletEntity(String domain, String systemType, String nodeName) implements Serializable {
}

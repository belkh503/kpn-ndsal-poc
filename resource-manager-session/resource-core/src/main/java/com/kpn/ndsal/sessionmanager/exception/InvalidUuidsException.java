package com.kpn.ndsal.sessionmanager.exception;

import lombok.Getter;

import java.util.List;

public class InvalidUuidsException extends Exception {
    @Getter
    private final List<String> invalidUuidsStrings;
    private static final String INVALID_UUIDS_FORMAT = "You have passed invalid UUIDs: %s";

    public InvalidUuidsException(List<String> invalidUuidsStrings) {
        super(String.format(INVALID_UUIDS_FORMAT, String.join(",", invalidUuidsStrings)));
        this.invalidUuidsStrings = invalidUuidsStrings;
    }
}

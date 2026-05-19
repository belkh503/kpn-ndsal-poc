package com.kpn.ndsal.resourcemanager.application.port.in;

public class RequestTimeoutExceededException extends RuntimeException {

    public RequestTimeoutExceededException() {

        super("Request Queue timeout exceeded.");
    }

}

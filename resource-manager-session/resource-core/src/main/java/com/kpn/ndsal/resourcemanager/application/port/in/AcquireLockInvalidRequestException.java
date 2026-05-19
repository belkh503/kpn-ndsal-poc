package com.kpn.ndsal.resourcemanager.application.port.in;

public class AcquireLockInvalidRequestException extends RuntimeException {

    public AcquireLockInvalidRequestException() {

        super("Service can not or will not process the request because it doesn’t correspond validation schema or domain requirements.");
    }

}

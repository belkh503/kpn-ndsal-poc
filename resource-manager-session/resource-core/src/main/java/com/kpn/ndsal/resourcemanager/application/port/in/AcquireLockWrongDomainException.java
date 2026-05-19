package com.kpn.ndsal.resourcemanager.application.port.in;

public class AcquireLockWrongDomainException extends RuntimeException {

    public AcquireLockWrongDomainException() {

        super("Service can not process request because domain does not match.");

    }

}

package com.kpn.ndsal.resourcemanager.application.port.in;

public class AcquireLockNotPossibleException extends RuntimeException {

    public AcquireLockNotPossibleException() {

        super("Locking rejected because some resources are already locked.");
    }

}

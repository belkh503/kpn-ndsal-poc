package com.kpn.ndsal.resourcemanager.application.port.out;

@FunctionalInterface
public interface RunningLocksCounterPort {

    long getLocksCount();

}

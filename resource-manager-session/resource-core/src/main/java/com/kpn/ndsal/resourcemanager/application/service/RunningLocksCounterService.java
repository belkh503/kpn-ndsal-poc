package com.kpn.ndsal.resourcemanager.application.service;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.port.in.RunningLocksCounterQuery;
import com.kpn.ndsal.resourcemanager.application.port.out.RunningLocksCounterPort;
import com.kpn.ndsal.resourcemanager.common.UseCase;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@Service
@AllArgsConstructor
public class RunningLocksCounterService implements RunningLocksCounterQuery {

    private final RunningLocksCounterPort runningLocksCounterPort;

    @Override
    public long getCount() {
        log.trace("RunningLocksCounterService::getCount");

        return runningLocksCounterPort.getLocksCount();
    }

}

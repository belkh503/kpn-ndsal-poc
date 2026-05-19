package com.kpn.ndsal.sessionmanager.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.sessionmanager.config.DomainsSessionConfig;
import com.kpn.ndsal.sessionmanager.config.HazelcastConfig;
import com.kpn.ndsal.sessionmanager.kafka.acquire.AcquireResponseProducer;
import com.kpn.ndsal.sessionmanager.task.AcquireTask;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AcquireExecutorService {

    private final DomainsSessionConfig domains;

    private final RequestValidationService validationService;
    private final AcquireResponseProducer acquireResponseProducer;
    private final HazelcastConfig hazelcastConfig;
    private final SessionManagerAcquireService acquireService;
    private final SessionManagerReleaseService releaseService;
    @Getter
    private AtomicBoolean exceptionDuringProcess = new AtomicBoolean(false);

    @Getter
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 60, SECONDS,
            new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());

    public Future<?> startAcquire() {
        exceptionDuringProcess.set(false);
        var acquireTask = new AcquireTask(hazelcastConfig, validationService, acquireService, releaseService,
                acquireResponseProducer, domains, exceptionDuringProcess);
        return threadPoolExecutor.submit(acquireTask);
    }

    @PostConstruct
    public void afterPropertySet() {
        threadPoolExecutor.prestartAllCoreThreads();
    }
}

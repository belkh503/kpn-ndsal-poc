package com.kpn.ndsal.resourcemanager.application.service;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;

import org.springframework.stereotype.Service;

import com.kpn.ndsal.resourcemanager.application.configuration.HazelcastConfigurator;
import com.kpn.ndsal.resourcemanager.application.task.ProcessQueueTask;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ProcessQueueExecutorService {

    private final RequestProcessorService requestProcessor;
    private final HazelcastConfigurator hazelcastConfigurator;
    @Getter
    private AtomicBoolean exceptionDuringProcess = new AtomicBoolean(false);
    @Getter
    private final ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(1, 1, 60, SECONDS,
            new ArrayBlockingQueue<>(1), new ThreadPoolExecutor.DiscardPolicy());

    public Future<?> startProcessing() {
        exceptionDuringProcess.set(false);
        var processQueueTask = new ProcessQueueTask(hazelcastConfigurator, requestProcessor, exceptionDuringProcess);
        return threadPoolExecutor.submit(processQueueTask);
    }

    @PostConstruct
    public void afterPropertySet() {
        threadPoolExecutor.prestartAllCoreThreads();
    }
}

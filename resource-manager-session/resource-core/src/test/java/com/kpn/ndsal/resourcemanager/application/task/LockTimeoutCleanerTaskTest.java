package com.kpn.ndsal.resourcemanager.application.task;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.in.ReleaseLockUseCase;
import com.kpn.ndsal.resourcemanager.application.service.LoadLocksService;
import com.kpn.ndsal.resourcemanager.application.service.ProcessQueueExecutorService;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@SpringBootTest(classes = {LockTimeoutCleanerTask.class})
@ActiveProfiles("test")
class LockTimeoutCleanerTaskTest {

    @MockitoBean
    private ReleaseLockUseCase releaseLockUseCase;

    @MockitoBean
    private LoadLocksService loadLocksService;

    @MockitoBean
    private ProcessQueueExecutorService processQueueExecutorService;

    @Autowired
    private LockTimeoutCleanerTask lockTimeoutCleanerTask;

    @Test
    void startLockCleaner_OK() {
        var lockId = UUID.randomUUID();

        var lockObject = LockRequest.LockObject.builder()
                .id(UUID.randomUUID())
                .type("NODE")
                .name("nl-cpe-01")
                .lockType(LockRequest.LockType.EXCLUSIVE)
                .build();

        var lockRequest = LockRequest.builder()
                .id(lockId)
                .correlationId(UUID.randomUUID().toString())
                .timesOutAt(LocalDateTime.now())
                .domain("BCPE")
                .lockObjectEntities(List.of(lockObject)).build();

        when(loadLocksService.loadExpired()).thenReturn(List.of(lockRequest));

        when(processQueueExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(false));
        doNothing().when(releaseLockUseCase).release(lockId);
        doReturn(null).when(processQueueExecutorService).startProcessing();

        lockTimeoutCleanerTask.startLockCleaner();

        verify(loadLocksService, times(1)).loadExpired();
        verify(releaseLockUseCase, times(1)).release(any());
        verify(processQueueExecutorService, times(1)).startProcessing();
    }

    @Test
    void startLockCleaner_OK_NoCleanButExceptionDuringProcess() {
        when(processQueueExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(true));
        when(loadLocksService.loadExpired()).thenReturn(List.of());
        doReturn(null).when(processQueueExecutorService).startProcessing();

        lockTimeoutCleanerTask.startLockCleaner();

        verify(loadLocksService, times(1)).loadExpired();
        verify(releaseLockUseCase, never()).release(any());
        verify(processQueueExecutorService, times(1)).startProcessing();
    }

    @Test
    void startLockCleaner_NothingFound() {
        when(processQueueExecutorService.getExceptionDuringProcess()).thenReturn(new AtomicBoolean(false));
        when(loadLocksService.loadExpired()).thenReturn(List.of());

        lockTimeoutCleanerTask.startLockCleaner();

        verify(loadLocksService, times(1)).loadExpired();
        verify(releaseLockUseCase, never()).release(any());
        verify(processQueueExecutorService, never()).startProcessing();
    }
}

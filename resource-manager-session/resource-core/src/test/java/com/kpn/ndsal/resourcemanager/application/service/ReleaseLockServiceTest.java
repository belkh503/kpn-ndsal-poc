package com.kpn.ndsal.resourcemanager.application.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.out.DeleteLockPort;


@SpringBootTest(classes = {ReleaseLockService.class})
class ReleaseLockServiceTest {
    @Autowired
    ReleaseLockService releaseLockService;

    @MockitoBean
    DeleteLockPort deleteLockPort;

    @Test
    void testReleaseLock() {
        var uuid = UUID.randomUUID();
        releaseLockService.release(uuid);

        verify(deleteLockPort, times(1)).deleteLockRequest(uuid);
    }
}
package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.out.LoadLockRequestPort;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;


@SpringBootTest(classes = {StatusLockService.class})
class StatusLockServiceTest {
    @Autowired
    StatusLockService statusLockService;

    @MockitoBean
    LoadLockRequestPort loadLockRequestPort;

    @Test
    void testFindLockEmpty() {
        var uuid = UUID.randomUUID();
        when(loadLockRequestPort.findLockRequest(uuid)).thenReturn(Optional.empty());
        var response = statusLockService.findLock(uuid);

        assertFalse(response.isPresent());
        verify(loadLockRequestPort, times(1)).findLockRequest(uuid);
    }

    @Test
    void testFindLockPresent() {
        var uuid = UUID.randomUUID();
        var lock = LockRequest.builder().build();
        when(loadLockRequestPort.findLockRequest(uuid)).thenReturn(Optional.of(lock));
        var response = statusLockService.findLock(uuid);

        assertTrue(response.isPresent());
        verify(loadLockRequestPort, times(1)).findLockRequest(uuid);
    }
}
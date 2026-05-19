package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.out.LoadLocksPort;
import com.kpn.ndsal.resourcemanager.domain.LockRequest;

@SpringBootTest(classes = {LoadLocksService.class})
class LoadLocksServiceTest {
    @Autowired
    private LoadLocksService loadLocksService;

    @MockitoBean
    private LoadLocksPort loadLocksPort;

    @Test
    void testLoadExpiredOK() {
        var uuid = UUID.randomUUID();

        when(loadLocksPort.findAllExpiredLocks()).thenReturn(List.of(LockRequest.builder().id(uuid).build()));
        var response = loadLocksService.loadExpired();

        assertFalse(response.isEmpty());
        assertEquals(uuid, response.get(0).getId());
        verify(loadLocksPort, times(1)).findAllExpiredLocks();
    }

    @Test
    void testLoadExpiredNotFound() {

        when(loadLocksPort.findAllExpiredLocks()).thenReturn(List.of());
        var response = loadLocksService.loadExpired();

        assertTrue(response.isEmpty());
        verify(loadLocksPort, times(1)).findAllExpiredLocks();
    }
}
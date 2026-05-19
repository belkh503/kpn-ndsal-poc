package com.kpn.ndsal.resourcemanager.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.kpn.ndsal.resourcemanager.application.port.out.RunningLocksCounterPort;


@SpringBootTest(classes = {RunningLocksCounterService.class})
class RunningLocksCounterServiceTest {
    @Autowired
    RunningLocksCounterService runningLocksCounterService;

    @MockitoBean
    RunningLocksCounterPort runningLocksCounterPort;

    @Test
    void testGetCount() {
        when(runningLocksCounterPort.getLocksCount()).thenReturn(2L);
        long response = runningLocksCounterService.getCount();

        assertEquals(2L, response);
        verify(runningLocksCounterPort, times(1)).getLocksCount();
    }
}
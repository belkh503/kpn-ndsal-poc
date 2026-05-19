package com.kpn.ndsal.sessionmanager.util;

import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class SessionEntityUtilTest {
    String domain = "domain";
    String systemType = "systemType";
    String nodeName = "nodeName";

    @Test
    void testUtilityClass() {
        assertThrows(InvocationTargetException.class, () -> {
            var constructor = SessionEntityUtil.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        });
    }

    @Test
    void getTripletAsKeyWithIndex() {
        var triplet = new TripletEntity(domain, systemType, nodeName);
        var index = 4;

        var expectedResult = domain + "-" + systemType + "-" + nodeName + "-" + index;
        var result = SessionEntityUtil.getTripletAsKeyWithIndex(triplet, index);
        assertEquals(expectedResult, result);
    }

    @Test
    void getTripletAsKey_tripletEntity() {
        var triplet = new TripletEntity(domain, systemType, nodeName);

        var expectedResult = domain + "-" + systemType + "-" + nodeName;
        var result = SessionEntityUtil.getTripletAsKey(triplet);
        assertEquals(expectedResult, result);
    }

    @Test
    void getTripletAsKey_separateValues() {
        var expectedResult = domain + "-" + systemType + "-" + nodeName;
        var result = SessionEntityUtil.getTripletAsKey(domain, systemType, nodeName);
        assertEquals(expectedResult, result);
    }

    @Test
    void isSessionTimedOut_null() {
        assertFalse(SessionEntityUtil.isSessionTimedOut(null));
    }

    @Test
    void isSessionTimedOut_falseNotNull() {
        var triplet = new TripletEntity(domain, systemType, nodeName);
        var timeout = 10000;
        var lockedEpoch = System.currentTimeMillis() - 1000;

        var sessionEntity = new SessionEntity(UUID.randomUUID(), 3, timeout, lockedEpoch, triplet);

        var result = SessionEntityUtil.isSessionTimedOut(sessionEntity);
        assertFalse(result);
    }

    @Test
    void isSessionTimedOut_trueNotNull() {
        var triplet = new TripletEntity(domain, systemType, nodeName);
        var timeout = 10000;
        var lockedEpoch = System.currentTimeMillis() - 20000;

        var sessionEntity = new SessionEntity(UUID.randomUUID(), 3, timeout, lockedEpoch, triplet);

        var result = SessionEntityUtil.isSessionTimedOut(sessionEntity);
        assertTrue(result);
    }
}

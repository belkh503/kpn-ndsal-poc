package com.kpn.ndsal.resourcemanager.application.port.in;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand.LockGroup;
import com.kpn.ndsal.resourcemanager.application.port.in.AcquireLockCommand.LockObject;


@SpringBootTest(classes = {AcquireLockCommand.class})
class AcquireLockCommandTest {

    @Test
    void testGetAllExclusiveLockedObject_NotEmpty() {
        var acquireLockCommand = createAcquireLockCommand(true);
        var response = acquireLockCommand.getAllExclusiveLockedObject();
        assertFalse(response.isEmpty());
        LockObject actualLockObject = response.iterator().next();
        assertEquals(acquireLockCommand.getLockGroups().get(0).getLockObjects().get(0), actualLockObject);
    }

    @Test
    void testGetAllExclusiveLockedObject_Empty() {
        var acquireLockCommand = createAcquireLockCommand(false);
        var response = acquireLockCommand.getAllExclusiveLockedObject();
        assertTrue(response.isEmpty());
    }

    @Test
    void testEquals() {
        var acquireLockCommand1 = createAcquireLockCommand(true);

        var lockObject2 = new LockObject(acquireLockCommand1.getLockGroups().get(0).getLockObjects().get(0));

        var lockObjects2 = new ArrayList<LockObject>();
        lockObjects2.add(lockObject2);

        var lockGroup2 = new LockGroup(lockObjects2);
        var lockGroups2 = new ArrayList<LockGroup>();
        lockGroups2.add(lockGroup2);
        var acquireLockCommand2 = new AcquireLockCommand(lockGroups2);

        boolean result = acquireLockCommand2.equals(acquireLockCommand1);
        assertTrue(result);
    }

    @Test
    void testEqualsDifferentObject() {
        var acquireLockCommand = createAcquireLockCommand(true);
        var response = acquireLockCommand.equals("");
        assertFalse(response);
    }

    @Test
    void testEqualsSameObject() {
        var acquireLockCommand = createAcquireLockCommand(true);
        var response = acquireLockCommand.equals(acquireLockCommand);
        assertTrue(response);
    }

    private AcquireLockCommand createAcquireLockCommand(boolean force) {
        var expectedLockObject = new LockObject("type", "id", force);

        var lockObjects = new ArrayList<LockObject>();
        lockObjects.add(expectedLockObject);

        var lockGroup = new LockGroup(lockObjects);
        var lockGroups = new ArrayList<LockGroup>();
        lockGroups.add(lockGroup);
        return new AcquireLockCommand(lockGroups);
    }
}
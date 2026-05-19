package com.kpn.ndsal.commonutils;

import com.hazelcast.map.IMap;

public class HazelcastServiceUtility {

    /**
     * private ctor.
     */
    private HazelcastServiceUtility() {
        throw new IllegalStateException("Hazelcast Utility class");
    }

    public static <T, U> void unlockIfLocked(IMap<T, U> imap, T key) {
        if (imap.isLocked(key)) {
            imap.unlock(key);
        }
    }
}

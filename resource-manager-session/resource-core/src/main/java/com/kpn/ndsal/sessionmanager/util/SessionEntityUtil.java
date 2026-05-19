package com.kpn.ndsal.sessionmanager.util;

import com.kpn.ndsal.sessionmanager.entity.SessionEntity;
import com.kpn.ndsal.sessionmanager.entity.TripletEntity;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SessionEntityUtil {
    public static String getTripletAsKeyWithIndex(TripletEntity triplet, int index) {
        return getTripletAsKey(triplet) + "-" + index;
    }

    public static String getTripletAsKey(TripletEntity triplet) {
        return String.format("%s-%s-%s", triplet.domain(), triplet.systemType(), triplet.nodeName());
    }

    public static String getTripletAsKey(String domain, String systemType, String nodeName) {
        return String.format("%s-%s-%s", domain, systemType, nodeName);
    }

    public static boolean isSessionTimedOut(SessionEntity session) {
        if (session == null) {
            return false;
        }

        long now = System.currentTimeMillis();
        return now - session.lockedEpoch() >= session.timeoutMilliSec();
    }
}

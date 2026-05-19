package com.kpn.ndsal.resourcemanager;

import java.util.List;

import org.apache.http.HttpStatus;

public class LockingScenarios {

    static LockRequest lockRequest1 = LockRequest.builder()
            .domainName("BCPE")
            .lockGroups(
                    List.of(LockGroup.builder().lockObjects(
                            List.of(
                                    new LockObject("NODE", "nl-pbl-cpe-01", false),
                                    new LockObject("PORT", "nl-pbl-cpe-01:1/1/1", false),
                                    new LockObject("PORT", "nl-pbl-cpe-01:1/1/2", false),
                                    new LockObject("PORT", "nl-pbl-cpe-01:1/1/3", false)
                            )
                    ).build())
            )
            .build();
    static LockRequest lockRequest2 = LockRequest.builder()
            .domainName("BCPE")
            .lockGroups(
                    List.of(LockGroup.builder().lockObjects(
                            List.of(
                                    new LockObject("NODE", "nl-pbl-cpe-01", false)
                            )
                    ).build())
            )
            .build();
    static LockRequest  lockRequest3 = LockRequest.builder()
            .domainName("BCPE")
            .lockGroups(
                    List.of(LockGroup.builder().lockObjects(
                            List.of(
                                    new LockObject("NODE", "nl-pbl-cpe-02", false),
                                    new LockObject("PORT", "nl-pbl-cpe-01:1/1/2", false)
                            )
                    ).build())
            )
            .build();
    static LockRequest lockRequest4 = LockRequest.builder()
            .domainName("BCPE")
            .lockGroups(
                    List.of(LockGroup.builder().lockObjects(
                            List.of(
                                    new LockObject("NODE", "nl-pbl-cpe-02", false),
                                    new LockObject("PORT", "nl-pbl-cpe-02:1/1/2", false)
                            )
                    ).build())
            )
            .build();

    public static List<Pair<LockRequest, Integer>> lockingScenario1() {
        return List.of(
                Pair.of(lockRequest1, HttpStatus.SC_CREATED),
                Pair.of(lockRequest2, HttpStatus.SC_CONFLICT),
                Pair.of(lockRequest3, HttpStatus.SC_CONFLICT),
                Pair.of(lockRequest4, HttpStatus.SC_CREATED)
        );
    }
}

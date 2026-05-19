package com.kpn.ndsal.resourcemanager.application.queue.out.persistence;

public interface CacheableMapStoreBaseEntity<K extends Comparable<K>> {

    K getPid();

    String getPayload();

}

package com.kpn.ndsal.sharedneo4j.entity;

public interface CacheableMapStoreBaseEntity<K extends Comparable<K>> {

    K getPid();

    String getPayload();

}

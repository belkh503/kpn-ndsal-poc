package com.kpn.ndsal.sessionmanager.persistence.entity;

public interface CacheableMapStoreBaseEntity<K extends Comparable<K>> {

    K getPid();

    String getPayload();

}

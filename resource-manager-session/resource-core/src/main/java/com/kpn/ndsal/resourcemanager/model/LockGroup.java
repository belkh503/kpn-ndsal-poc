package com.kpn.ndsal.resourcemanager.model;

import java.io.Serializable;
import java.util.List;

public class LockGroup implements Serializable {
    private static final long serialVersionUID = 1L;

    public List<LockObject> lockObjects;

    public static class LockObject implements Serializable {
        private static final long serialVersionUID = 1L;

        public String type;
        public String id;
        public Boolean force;

        public LockObject() {}

        public LockObject(String type, String id) {
            this.type = type;
            this.id = id;
            this.force = false;
        }

        public LockObject(String type, String id, Boolean force) {
            this.type = type;
            this.id = id;
            this.force = force;
        }
    }
}

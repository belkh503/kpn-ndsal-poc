package com.kpn.ndsal.resourcemanager.application.port.in;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.kpn.ndsal.resourcemanager.common.SelfValidating;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Value;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@SuppressWarnings({"squid:S2975", "squid:S1182"})
public class AcquireLockCommand extends SelfValidating<AcquireLockCommand> {

    private List<LockGroup> lockGroups;

    public Set<LockObject> getAllExclusiveLockedObject() {
        return this.lockGroups.stream().map(lockGroup -> lockGroup.lockObjects.stream()
                .filter(lockObject -> lockObject.force)
                .collect(Collectors.toSet())).flatMap(Collection::stream).collect(Collectors.toSet());
    }

    @Value
    public static class LockGroup {
        protected List<LockObject> lockObjects;
    }

    @Data
    public static class LockObject implements Cloneable {
        private String type;
        private String id;
        private Boolean force;

        public LockObject(String type, String id) {
            this.type = type;
            this.id = id;
            this.force = false;
        }

        public LockObject(LockObject lockObject) {
            this.type = lockObject.type;
            this.id = lockObject.id;
            this.force = lockObject.force;
        }

        public LockObject(String type, String id, Boolean force) {
            this.type = type;
            this.id = id;
            this.force = force;
        }

        @Override
        public Object clone() {
            return new LockObject(this.type, this.id, this.force);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            LockObject that = (LockObject) o;
            return type.equals(that.type) && id.equals(that.id);
        }

        @Override
        public int hashCode() {
            return Objects.hash(type, id);
        }
    }
}


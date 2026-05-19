package com.kpn.ndsal.resourcemanager.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public class Timeout implements Serializable {
    private static final long serialVersionUID = 1L;

    private int value;
    private Unit unit;

    public Timeout() {}
    public Timeout(int value, Unit unit) {
        this.value = value;
        this.unit = unit;
    }
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    public Unit getUnit() { return unit; }
    public void setUnit(Unit unit) { this.unit = unit; }

    public enum Unit {
        DAY("day"), HOUR("hour"), MIN("min"), SECOND("second");
        private final String value;
        Unit(String value) { this.value = value; }

        @JsonValue
        public String value() { return value; }

        @JsonCreator
        public static Unit fromValue(String v) {
            for (Unit u : values()) {
                if (u.value.equalsIgnoreCase(v) || u.name().equalsIgnoreCase(v)) return u;
            }
            throw new IllegalArgumentException("Unknown Timeout.Unit: " + v);
        }
    }
}

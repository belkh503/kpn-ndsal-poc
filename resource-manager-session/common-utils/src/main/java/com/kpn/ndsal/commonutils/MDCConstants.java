package com.kpn.ndsal.commonutils;

/**
 * Constant class describing common field use in console logs
 */
public class MDCConstants {

    public static final String DEPTH = "depth";
    public static final String MCD_CORRELATION_ID_KEY = "correlationId";

    private MDCConstants() {
        throw new IllegalAccessError("Constants class");
    }

}

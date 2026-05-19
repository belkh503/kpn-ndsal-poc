package com.kpn.ndsal.commonutils;

public class Constants {
    public static final String INVALID_NUM_LOCKS = "Invalid numSessionsWanted: You are requesting %d sessions for domain %s "
            + "and systemType %s, but we cannot assign more than %d";
    public static final String INVALID_DOMAIN_OR_SYSTEM_TYPE = "Invalid domain and/or system type: %s";
    public static final String INVALID_CORRELATION_ID = "correlationId header cannot be blank";

    public static final String KAFKA_CORRELATION_ID_HEADER = "correlationId";
    public static final String KAFKA_CORRELATION_ID_NOT_PROVIDED_HEADER = "CORRELATION_ID_NOT_PROVIDED";

    private Constants() {
        throw new IllegalStateException("Constants class");
    }

}

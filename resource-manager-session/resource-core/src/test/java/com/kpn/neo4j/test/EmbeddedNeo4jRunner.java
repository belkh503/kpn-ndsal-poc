package com.kpn.neo4j.test;

import org.springframework.stereotype.Component;

/**
 * Stub class for the internal com.kpn.ndsal:embedded-neo4j library.
 * The original library is not available; this stub allows test code to compile.
 * The original EmbeddedNeo4jRunner was a Spring-managed bean that controlled
 * the embedded Neo4j instance lifecycle during integration tests.
 */
@Component
public class EmbeddedNeo4jRunner {

    public void start() {
        // stub - no-op
    }

    public void stop() {
        // stub - no-op
    }
}

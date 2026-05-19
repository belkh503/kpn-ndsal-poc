package com.kpn.neo4j.test.condition.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Stub annotation for the internal com.kpn.ndsal:embedded-neo4j library.
 * The original library is not available; this stub allows test code to compile.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface EmbeddedNeo4j {

    String databaseDirectoryPathEnv() default "";

    String databaseDirectoryPath() default "target/tmpNeo4j";

    String databaseName() default "Neo4j";

    boolean boltEnable() default true;

    int boltPort() default -1;
}

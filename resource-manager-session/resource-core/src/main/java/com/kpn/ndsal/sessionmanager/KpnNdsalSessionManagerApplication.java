package com.kpn.ndsal.sessionmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.neo4j.repository.config.EnableNeo4jRepositories;

@SpringBootApplication(exclude = {org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration.class})
@ComponentScan(basePackages = "com.kpn")
@EnableNeo4jRepositories(basePackages = {"com.kpn.ndsal.resourcemanager.adapter.out.persistence", "com.kpn.ndsal.sessionmanager.persistence.repository"})
public class KpnNdsalSessionManagerApplication {
    public static void main(String[] args) {
        SpringApplication.run(KpnNdsalSessionManagerApplication.class, args);
    }
}

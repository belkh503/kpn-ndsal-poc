package com.kpn.ndsal.resourcemanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

@SpringBootApplication(
        scanBasePackages = "com.kpn.ndsal",
        exclude = DataSourceAutoConfiguration.class
)
public class ResourceManagerEntryPoint {

    public static void main(String[] args) {
        SpringApplication.run(ResourceManagerEntryPoint.class, args);
    }

}

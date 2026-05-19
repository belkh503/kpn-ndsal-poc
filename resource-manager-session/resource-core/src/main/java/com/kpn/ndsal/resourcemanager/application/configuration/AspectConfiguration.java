package com.kpn.ndsal.resourcemanager.application.configuration;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@Configuration
@EnableAspectJAutoProxy
@ComponentScan(basePackages = {"com.kpn.ndsal.resourcemanager"})
public class AspectConfiguration {
}

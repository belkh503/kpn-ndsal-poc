package com.kpn.ndsal.sessionmanager.config;

import javax.annotation.PostConstruct;

import java.util.Locale;

import org.apache.commons.lang3.LocaleUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LocaleConfig {

    @Value("${spring.mvc.locale}")
    String locale;

    @PostConstruct
    public void changeDefaultLocale() {
        Locale.setDefault(LocaleUtils.toLocale(locale));
    }

}

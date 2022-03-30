package com.example.springdemo.integration;

import cloud.pyrgus.framework.core.configuration.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Component
public class SpringEnvironmentConfiguration implements Configuration {

    private final Environment environment;

    public SpringEnvironmentConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Set<String> keySet() {
        return Collections.emptySet();
    }

    @Override
    public boolean containsKey(String key) {
        return environment.containsProperty(key);
    }

    @Override
    public String get(String key) {
        return environment.getProperty(key);
    }
}

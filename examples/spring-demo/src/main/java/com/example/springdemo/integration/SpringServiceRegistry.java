package com.example.springdemo.integration;

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceInitiator;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.cqrs.domain.DomainService;
import io.vavr.control.Option;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Component
public class SpringServiceRegistry implements ServiceRegistry, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> getAllServices() {
        return Collections.emptyMap();
    }

    @Override
    public <S extends Service> Option<S> loadService(Class<S> serviceType) {
        return Option.of(applicationContext.getBeanProvider(serviceType).getIfAvailable());
    }

    @Override
    public <S extends Service> List<S> loadAllServices(Class<S> serviceType) {
        return new ArrayList<>(applicationContext.getBeansOfType(serviceType).values());
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}

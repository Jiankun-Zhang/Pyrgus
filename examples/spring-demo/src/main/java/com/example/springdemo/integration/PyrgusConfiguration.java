package com.example.springdemo.integration;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.cqrs.CQRSServiceRegistry;
import cloud.pyrgus.framework.cqrs.aggregate.Aggregate;
import cloud.pyrgus.framework.cqrs.domain.DomainService;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionRouter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.persistence.metamodel.Type;
import java.util.List;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Service
public class PyrgusConfiguration {

    private final SpringServiceRegistry springServiceRegistry;

    private final List<Configuration> configurations;

    private final List<DomainService> domainServices;

    @PersistenceUnit
    private EntityManagerFactory entityManagerFactory;

    @Autowired
    public PyrgusConfiguration(SpringServiceRegistry springServiceRegistry, List<Configuration> configurations, List<DomainService> domainServices) {
        this.springServiceRegistry = springServiceRegistry;
        this.configurations = configurations;
        this.domainServices = domainServices;
    }

    @PostConstruct
    void configure() {
        Pyrgus.Builder builder = Pyrgus.builder();
        builder.withServiceRegistry(CQRSServiceRegistry.builder()
                .build())
                .withServiceRegistry(springServiceRegistry);
        configurations.forEach(builder::withConfiguration);
        builder.build();

        MessageRouter messageRouter = Pyrgus.getServiceRegistry().loadServiceOrThrown(MessageRouter.class);
        SimpleActionRouter actionRouter = (SimpleActionRouter) messageRouter;
        domainServices.forEach(actionRouter::scanActionHandler);
        entityManagerFactory.getMetamodel()
                .getEntities()
                .stream()
                .map(Type::getJavaType)
                .filter(Aggregate.class::isAssignableFrom)
                .forEach(actionRouter::scanActionHandler);
    }

}

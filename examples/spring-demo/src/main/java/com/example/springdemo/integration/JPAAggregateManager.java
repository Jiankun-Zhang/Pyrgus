package com.example.springdemo.integration;

import cloud.pyrgus.framework.cqrs.aggregate.Aggregate;
import cloud.pyrgus.framework.cqrs.aggregate.AggregateManager;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Component
public class JPAAggregateManager implements AggregateManager<Long> {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <E extends Aggregate<Long>> E load(Long aLong, Class<E> aggregateType) {
        return entityManager.find(aggregateType, aLong);
    }

    @Override
    public <E extends Aggregate<Long>> E save(E aggregate) {
        entityManager.persist(aggregate);
        return aggregate;
    }

    @Override
    public <E extends Aggregate<Long>> void remove(E aggregate) {
        entityManager.remove(aggregate);
    }

    @Override
    public <E extends Aggregate<Long>> E update(E aggregate) {
        return entityManager.merge(aggregate);
    }
}

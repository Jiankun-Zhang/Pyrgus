/*
 * Copyright (c) 2022. Zhang Jiankun
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package cloud.pyrgus.framework.cqrs;

import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.message.MessageFactory;
import cloud.pyrgus.framework.core.message.MessageInterceptorChainFactory;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.core.service.AbstractServiceRegistry;
import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceInitiator;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import cloud.pyrgus.framework.cqrs.event.EventStore;
import cloud.pyrgus.framework.cqrs.interceptor.arguments.ArgumentResolveInterceptor;
import cloud.pyrgus.framework.cqrs.interceptor.arguments.ArgumentResolver;
import cloud.pyrgus.framework.cqrs.interceptor.arguments.state.StateResolver;
import cloud.pyrgus.framework.cqrs.interceptor.arguments.task.TaskResolver;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import cloud.pyrgus.framework.internal.simple.SimpleServiceInitiator;
import cloud.pyrgus.framework.internal.simple.core.message.SimpleMessageInterceptorChainFactory;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleThreadPoolTaskExecutor;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleUnitOfWork;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionMessageFactory;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionRouter;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleEventStore;
import lombok.AllArgsConstructor;

import java.util.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
public class CQRSServiceRegistry extends AbstractServiceRegistry {

    protected CQRSServiceRegistry(Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators) {
        super(serviceInitiators);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators = new HashMap<>();


        private Builder() {
            applyDefault();
        }

        private void applyDefault() {
            applyActionMessageFactory(new SimpleActionMessageFactory())
                    .applyService(new ArgumentResolveInterceptor.Initiator())
                    .applyArgumentResolver(new StateResolver())
                    .applyArgumentResolver(new TaskResolver())
                    .applyEventStore(new SimpleServiceInitiator<>(EventStore.class, new SimpleEventStore()))
                    .applyService(new SimpleServiceInitiator<>(MessageRouter.class, new SimpleActionRouter()))
                    .applyService(new SimpleServiceInitiator<>(TaskExecutor.class, new SimpleThreadPoolTaskExecutor()))
                    .applyService(new SimpleServiceInitiator<>(MessageInterceptorChainFactory.class, new SimpleMessageInterceptorChainFactory()))
                    .applyService(new SimpleServiceInitiator<>(UnitOfWork.class, new SimpleUnitOfWork()));
        }

        public Builder applyArgumentResolver(ArgumentResolver argumentResolver) {
            return this.applyArgumentResolver(new SimpleServiceInitiator<>(ArgumentResolver.class, argumentResolver));
        }

        public Builder applyArgumentResolver(ServiceInitiator<ArgumentResolver> argumentResolverInitiator) {
            return applyService(argumentResolverInitiator);
        }

        public Builder applyActionMessageFactory(ActionMessageFactory actionMessageFactory) {
            return applyService(new SimpleServiceInitiator<>(MessageFactory.class, actionMessageFactory));
        }

        public Builder applyActionMessageFactory(ServiceInitiator<ActionMessageFactory> actionMessageFactoryInitiator) {
            return applyService(new ServiceInitiatorProxy<>(actionMessageFactoryInitiator, MessageFactory.class));
        }

        public Builder applyEventStore(EventStore eventStore) {
            return applyEventStore(new SimpleServiceInitiator<>(EventStore.class, eventStore));
        }

        public Builder applyEventStore(ServiceInitiator<EventStore> eventStoreServiceInitiator) {
            return applyService(eventStoreServiceInitiator);
        }

        public <S extends Service> Builder applyService(Class<S> serviceType, S service) {
            return applyService(new SimpleServiceInitiator<>(serviceType, service));
        }

        public <S extends Service> Builder applyService(ServiceInitiator<S> serviceInitiator) {
            Class<S> serviceType = serviceInitiator.serviceInitiated();
            List<ServiceInitiator<? extends Service>> initiators = serviceInitiators.getOrDefault(serviceType, new LinkedList<>());
            if (!initiators.isEmpty()) {
                initiators.removeIf(initiator -> SimpleImplementation.class.isAssignableFrom(initiator.implementationType()));
            }
            initiators.add(serviceInitiator);
            serviceInitiators.put(serviceType, initiators);
            return this;
        }

        public ServiceRegistry build() {
            return new CQRSServiceRegistry(Collections.unmodifiableMap(serviceInitiators));
        }

        @AllArgsConstructor
        static class ServiceInitiatorProxy<S extends Service, T extends S> implements ServiceInitiator<S> {

            private final ServiceInitiator<T> source;
            private final Class<S> realServiceType;

            @Override
            public Class<S> serviceInitiated() {
                return realServiceType;
            }

            @Override
            public Class<? extends S> implementationType() {
                return source.implementationType();
            }

            @Override
            public S initiateService(ServiceRegistry serviceRegistry, Configuration configuration) {
                return source.initiateService(serviceRegistry, configuration);
            }
        }

    }

}

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

package cloud.pyrgus.framework.internal.simple.core;

import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.message.MessageAdapterRegistry;
import cloud.pyrgus.framework.core.message.MessageFactory;
import cloud.pyrgus.framework.core.message.MessageInterceptorChainFactory;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.core.service.AbstractServiceRegistry;
import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceInitiator;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.service.exception.ServiceRegistryAlreadyConfiguredException;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import cloud.pyrgus.framework.internal.simple.SimpleServiceInitiator;
import cloud.pyrgus.framework.internal.simple.core.message.*;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleThreadPoolTaskExecutor;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleUnitOfWork;

import java.util.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class SimpleCoreServiceRegistry extends AbstractServiceRegistry {

    private static SimpleCoreServiceRegistry instance = null;

    protected SimpleCoreServiceRegistry(Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators) {
        super(serviceInitiators);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators = new HashMap<>();

        private final List<Configuration> configurations = new LinkedList<>();

        private Builder() {
            addSimpleImplementations();
        }

        private void addSimpleImplementations() {
            applyService(MessageAdapterRegistry.class, new SimpleMessageAdapterRegistry(new SimpleMessageAdapter()))
                    .applyService(MessageFactory.class, new SimpleMessageFactory())
                    .applyService(UnitOfWork.class, new SimpleUnitOfWork())
                    .applyService(MessageRouter.class, new SimpleMessageRouter())
                    .applyService(MessageInterceptorChainFactory.class, new SimpleMessageInterceptorChainFactory())
                    .applyService(TaskExecutor.class, new SimpleThreadPoolTaskExecutor());
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
            if (instance != null) {
                throw new ServiceRegistryAlreadyConfiguredException(SimpleCoreServiceRegistry.class);
            }
            return instance = new SimpleCoreServiceRegistry(Collections.unmodifiableMap(serviceInitiators));
        }

    }

}

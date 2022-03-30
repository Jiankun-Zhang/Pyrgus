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

package cloud.pyrgus.framework.core.service;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.service.exception.MultipleServiceImplementationFoundException;
import io.vavr.control.Option;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
public abstract class AbstractServiceRegistry implements ServiceRegistry {

    protected final Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators;

    protected AbstractServiceRegistry(Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators) {
        this.serviceInitiators = serviceInitiators;
    }

    @Override
    public Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> getAllServices() {
        return serviceInitiators;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> Option<S> loadService(Class<S> serviceType) {
        List<ServiceInitiator<? extends Service>> initiators = serviceInitiators.getOrDefault(serviceType, Collections.emptyList());
        if (initiators.isEmpty()) {
            return Option.none();
        }
        Configuration configuration = Pyrgus.getConfiguration();
        if (initiators.size() == 1) {
            return Option.of((S) initiators.get(0).initiateService(this, configuration));
        }
        String implementationClassName = configuration.get(serviceType.getName());
        if (implementationClassName == null) {
            throw new MultipleServiceImplementationFoundException(serviceType, initiators.stream()
                    .map(ServiceInitiator::implementationType)
                    .collect(Collectors.toList()));
        }
        return Option.ofOptional(initiators.stream()
                .filter(serviceInitiator -> Objects.equals(implementationClassName, serviceInitiator.implementationType().getName()))
                .map(serviceInitiator -> (S) serviceInitiator.initiateService(this, configuration))
                .findAny());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> List<S> loadAllServices(Class<S> serviceType) {
        List<S> services = serviceInitiators.getOrDefault(serviceType, Collections.emptyList())
                .stream()
                .map(serviceInitiator -> (S) serviceInitiator.initiateService(this, Pyrgus.getConfiguration()))
                .collect(Collectors.toList());
        if (OrderedService.class.isAssignableFrom(serviceType)) {
            ((List<OrderedService>) services).sort(Comparator.comparingInt(OrderedService::getOrder));
        }
        return services;
    }

}

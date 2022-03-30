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

package cloud.pyrgus.framework;

import cloud.pyrgus.framework.core.Ordered;
import cloud.pyrgus.framework.core.configuration.Configuration;
import cloud.pyrgus.framework.core.configuration.PropertiesConfiguration;
import cloud.pyrgus.framework.core.configuration.RefreshableConfiguration;
import cloud.pyrgus.framework.core.configuration.StringMapConfiguration;
import cloud.pyrgus.framework.core.message.MessageFactory;
import cloud.pyrgus.framework.core.message.MessageInterceptorChainFactory;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.core.service.OrderedService;
import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceInitiator;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.service.exception.MultipleServiceImplementationFoundException;
import cloud.pyrgus.framework.core.service.exception.ServiceImplementationNotFoundException;
import cloud.pyrgus.framework.core.service.exception.ServiceRegistryNotInitializedException;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import cloud.pyrgus.framework.cqrs.CQRSServiceRegistry;
import cloud.pyrgus.framework.exception.PyrgusAlreadyInitializedException;
import cloud.pyrgus.framework.internal.simple.core.message.SimpleMessageInterceptorChainFactory;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleThreadPoolTaskExecutor;
import cloud.pyrgus.framework.internal.simple.core.task.SimpleUnitOfWork;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionMessageFactory;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionRouter;
import io.vavr.control.Option;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Pyrgus 的主要入口, 可以获取配置好的
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
@Slf4j
public class Pyrgus implements ServiceRegistry, Configuration {

    private Pyrgus(Set<ServiceRegistry> serviceRegistries, Set<Configuration> configurations, BiFunction<Map.Entry<String, String>, Configuration, String> mergeFunction) {
        mergeAllFromConfigurations(configurations, mergeFunction);
        mergeAllFromRegistries(serviceRegistries);
        log.info("======================================== Pyrgus configured ========================================");
        log.info("Num of configurations: {}", configurations.size());
        log.info("Num of generic configurations: {}", genericConfigurations.size());
        log.info("Num of service registries: {}", serviceRegistries.size());
        log.info("Num of generic service registries: {}", genericServiceRegistries.size());
        log.info("Num of service: {}", serviceInitiators.keySet().size());
        log.info("Num of service implementation: {}", serviceInitiators.values().stream().mapToInt(List::size).sum());
        if (log.isDebugEnabled()) {
            log.debug("---------------------------------------------------------------------------------------------------");
            log.debug("Configurations:");
            configurations.stream().sorted(Comparator.comparingInt(Ordered::getOrder)).forEach(configuration -> {
                log.debug("\t> configuration: [ {} ] | refreshable: {} | generic: {} | order: {}", configuration.getClass().getName(), configuration.isRefreshable(), configuration.isGenericConfiguration(), configuration.getOrder());
                for (String key : Option.of(configuration.keySet()).getOrElse(Collections.emptySet())) {
                    log.debug("\t\t>> key: ( {} ) => value: ( {} )", key, configuration.get(key));
                }
            });
            log.debug("---------------------------------------------------------------------------------------------------");
            log.debug("Services:");
            for (ServiceRegistry serviceRegistry : serviceRegistries) {
                log.debug("\t> service registry: [ {} ] | generic: {}", serviceRegistry.getClass().getName(), serviceRegistry.isGenericServiceRegistry());
                for (Map.Entry<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> entry : serviceRegistry.getAllServices().entrySet()) {
                    for (ServiceInitiator<? extends Service> initiator : entry.getValue()) {
                        log.debug("\t\t>> service: [ {} ] | implementation: [ {} ]", entry.getKey().getName(), initiator.implementationType().getName());
                    }
                }
            }
        }
        log.info("===================================================================================================");
    }

    private static Pyrgus instance = null;

    private final Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> serviceInitiators = new HashMap<>();
    private final Set<ServiceRegistry> genericServiceRegistries = new HashSet<>();
    private final Map<String, Object> configurations = new HashMap<>();
    private final Set<Configuration> genericConfigurations = new HashSet<>();

    @Override
    public String get(String key) {
        String genericValue = genericConfigurations
                .stream()
                .filter(configuration -> configuration.containsKey(key))
                .map(configuration -> configuration.get(key))
                .findFirst()
                .orElse(null);
        if (genericValue != null) {
            return genericValue;
        }
        if (configurations.containsKey(key)) {
            Object value = configurations.get(key);
            if (value instanceof RefreshableValue) {
                return ((RefreshableValue) value).getValue();
            }
            return (String) value;
        } else {
            return null;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static ServiceRegistry getServiceRegistry() {
        if (instance == null) {
            throw new ServiceRegistryNotInitializedException();
        }
        return instance;
    }

    public static Configuration getConfiguration() {
        if (instance == null) {
            throw new ServiceRegistryNotInitializedException();
        }
        return instance;
    }

    @Override
    public Set<String> keySet() {
        return configurations.keySet();
    }

    @Override
    public boolean containsKey(String key) {
        return configurations.containsKey(key);
    }

    private void mergeAllFromConfigurations(Set<Configuration> configurations, BiFunction<Map.Entry<String, String>, Configuration, String> mergeFunction) {
        Map<String, Object> map = new HashMap<>();
        configurations.stream()
                .sorted(Comparator.comparingInt(Ordered::getOrder))
                .forEachOrdered(configuration -> {
                    if (configuration.isGenericConfiguration()) {
                        genericConfigurations.add(configuration);
                        return;
                    }
                    final boolean isRefreshable = configuration instanceof RefreshableConfiguration;
                    for (String key : configuration.keySet()) {
                        Object valueProxy = isRefreshable
                                ? new RefreshableValue((RefreshableConfiguration) configuration, key)
                                : configuration.get(key);
                        if (map.containsKey(key)) {
                            Object oldValue = map.get(key);
                            if (oldValue instanceof RefreshableValue) {
                                oldValue = ((RefreshableValue) oldValue).getValue();
                            }
                            map.put(key, isRefreshable
                                    ? valueProxy
                                    : mergeFunction.apply(new AbstractMap.SimpleEntry<>(key, (String) oldValue), configuration));
                        } else {
                            map.put(key, valueProxy);
                        }
                    }
                });
        this.configurations.putAll(map);
    }

    private void mergeAllFromRegistries(Set<ServiceRegistry> serviceRegistries) {
        Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> allServiceInitiators = new HashMap<>();
        for (ServiceRegistry serviceRegistry : serviceRegistries) {
            if (serviceRegistry.isGenericServiceRegistry()) {
                genericServiceRegistries.add(serviceRegistry);
                continue;
            }
            Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> services = serviceRegistry.getAllServices();
            for (Class<? extends Service> serviceType : services.keySet()) {
                List<ServiceInitiator<? extends Service>> list = allServiceInitiators.getOrDefault(serviceType, new LinkedList<>());
                list.addAll(services.get(serviceType));
                allServiceInitiators.put(serviceType, list);
            }
        }
        for (Map.Entry<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> entry : allServiceInitiators.entrySet()) {
            Class<? extends Service> serviceType = entry.getKey();
            String serviceName = serviceType.getName();
            if (containsKey(serviceName)) {
                String targetType = get(serviceName);
                entry.getValue().removeIf(serviceInitiator -> !targetType.equals(serviceInitiator.implementationType().getName()));
                if (entry.getValue().isEmpty()) {
                    throw new ServiceImplementationNotFoundException(serviceType, targetType);
                }
            } else if (genericServiceRegistries.stream().anyMatch(serviceRegistry -> serviceRegistry.loadService(serviceType).isDefined())) {
                entry.getValue().clear();
            }
        }
        this.serviceInitiators.putAll(allServiceInitiators);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> Option<S> loadService(Class<S> serviceType) {
        List<S> services = genericServiceRegistries.stream()
                .map(serviceRegistry -> serviceRegistry.loadService(serviceType))
                .filter(Option::isDefined)
                .map(Option::get)
                .collect(Collectors.toList());
        if (services.size() == 1) {
            return Option.of(services.get(0));
        }
        if (services.size() > 1) {
            throw new MultipleServiceImplementationFoundException(serviceType, services.stream().map(service -> service.getClass()).collect(Collectors.toList()));
        }
        if (!serviceInitiators.containsKey(serviceType)) {
            return Option.none();
        }
        List<ServiceInitiator<? extends Service>> initiators = this.serviceInitiators.getOrDefault(serviceType, Collections.emptyList());
        if (initiators.isEmpty()) {
            return Option.none();
        }
        if (initiators.size() == 1) {
            return Option.of((S) initiators.get(0).initiateService(this, this));
        }
        throw new MultipleServiceImplementationFoundException(serviceType, initiators.stream().map(ServiceInitiator::implementationType).collect(Collectors.toList()));
    }

    @Override
    public Map<Class<? extends Service>, List<ServiceInitiator<? extends Service>>> getAllServices() {
        return Collections.emptyMap();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> List<S> loadAllServices(Class<S> serviceType) {
        List<S> services = genericServiceRegistries.stream()
                .map(serviceRegistry -> serviceRegistry.loadAllServices(serviceType))
                .filter(s -> !s.isEmpty())
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        if (!services.isEmpty()) {
            return services;
        }
        services = serviceInitiators.getOrDefault(serviceType, Collections.emptyList())
                .stream()
                .map(serviceInitiator -> (S) serviceInitiator.initiateService(this, this))
                .collect(Collectors.toList());
        if (OrderedService.class.isAssignableFrom(serviceType)) {
            List<OrderedService> orderedServices = (List<OrderedService>) services;
            orderedServices.sort((o1, o2) -> {
                int o1Order = o1.getOrder();
                int o2Order = o2.getOrder();
                String o1Key = o1.getClass().getName() + ".order";
                String o2Key = o2.getClass().getName() + ".order";
                if (containsKey(o1Key)) {
                    o1Order = Integer.parseInt(get(o1Key));
                }
                if (containsKey(o2Key)) {
                    o2Order = Integer.parseInt(get(o2Key));
                }
                return Integer.compare(o1Order, o2Order);
            });
        }
        return services;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @AllArgsConstructor
    static class RefreshableValue {

        private final RefreshableConfiguration configuration;

        private final String key;

        public String getValue() {
            return configuration.get(key);
        }


    }

    public static class Builder {


        private final Set<ServiceRegistry> serviceRegistries = new HashSet<>();

        private final Set<Configuration> configurations = new HashSet<>();

        private BiFunction<Map.Entry<String, String>, Configuration, String> mergeFunction;

        private Builder() {
            withConfiguration(System.getProperties())
                    .withConfiguration(System.getenv());
        }

        public Builder withDefaultServiceRegistries() {
            Properties properties = new Properties();
            properties.put(MessageFactory.class.getName(), SimpleActionMessageFactory.class.getName());
            properties.put(MessageRouter.class.getName(), SimpleActionRouter.class.getName());
            return withServiceRegistry(CQRSServiceRegistry.builder()
                    .build())
                    .withConfiguration(System.getProperties())
                    .withConfiguration(properties);
        }

        public Builder withServiceRegistry(ServiceRegistry serviceRegistry) {
            this.serviceRegistries.add(serviceRegistry);
            if (serviceRegistry instanceof Configuration) {
                return withConfiguration((Configuration) serviceRegistry);
            }
            return this;
        }

        public Builder withConfiguration(Configuration configuration) {
            this.configurations.add(configuration);
            return this;
        }

        public Builder withConfiguration(Properties properties) {
            return withConfiguration(new PropertiesConfiguration(properties));
        }

        public Builder withConfiguration(Map<String, String> map) {
            return withConfiguration(new StringMapConfiguration(map));
        }

        public Builder withConfigurationCompute(BiFunction<Map.Entry<String, String>, Configuration, String> mergeFunction) {
            this.mergeFunction = mergeFunction;
            return this;
        }

        public void build() {
            if (instance != null) {
                throw new PyrgusAlreadyInitializedException();
            }
            if (serviceRegistries.isEmpty()) {
                throw new NullPointerException("Pyrgus#serviceRegistry");
            }
            if (mergeFunction == null) {
                mergeFunction = (entry, configuration) -> configuration.get(entry.getKey());
            }
            instance = new Pyrgus(serviceRegistries, configurations, mergeFunction);
        }

    }
}

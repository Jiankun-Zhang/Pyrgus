/*
 * MIT License
 *
 * Copyright (c) 2022 Zhang Jiankun
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
 */

package cloud.pyrgus.framework.impl.core.service;

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.service.exception.PyrgusServiceException;
import cloud.pyrgus.framework.core.service.exception.ServiceNotRegisteredException;
import cloud.pyrgus.framework.core.service.exception.ServiceNotUniqueException;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 一个简单的 {@link ServiceRegistry} 实现, 要求所有服务实例都只应该只有一个无餐构造器.
 * 此注册表可以识别出单个服务实例继承关系上的所有 {@link Service} 声明, 因此支持以下情景:
 * <br/><br/>
 * <pre>
 * interface Foo {}
 * interface Foo1 extends Foo {}
 * class FooImpl implements Foo1 {}
 *
 * SimpleServiceRegistry registry = new SimpleServiceReg(Collections.singletonList(FooImpl.class));
 * Foo foo = register.loadService(Foo.class);
 * Foo1 foo1 = register.loadService(Foo1.class);
 * FooImpl fooImpl = register.loadService(FooImpl.class);
 * </pre>
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/13
 */
@Slf4j
public class SimpleServiceRegistry implements ServiceRegistry {

    private final Map<Class<? extends Service>, List<Service>> services = new HashMap<>();

    public SimpleServiceRegistry(@NotNull Set<Class<? extends Service>> classes) {
        for (Class<? extends Service> type : classes) {
            if (!Modifier.isPublic(type.getModifiers()) || type.isInterface()) {
                continue;
            }
            try {
                Constructor<?>[] constructors = type.getConstructors();
                if (constructors.length > 1) {
                    skip(type);
                } else if (constructors.length == 1) {
                    Constructor<?> constructor = constructors[0];
                    constructor.setAccessible(true);
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("register service: [ {} ]. ", constructor.getDeclaringClass().getName());
                        }
                        appendComponent(type.newInstance());
                    } else {
                        skip(type);
                    }
                } else {
                    constructors = type.getDeclaredConstructors();
                    if (constructors.length != 1) {
                        skip(type);
                    } else if (constructors[0].getParameterCount() == 0) {
                        appendComponent(type.newInstance());
                    } else {
                        skip(type);
                    }
                }
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("initiate service [ " + type.getName() + " ] failed.", e);
            }
        }
    }

    private void skip(Class<? extends Service> type) {
        log.warn("skip service [ {} ], make sure the service only have one public constructor with no parameters.", type);
    }

    private void appendComponent(@NotNull Service service) {
        findComponentTypeThenRegister(service.getClass(), service);
    }

    @SuppressWarnings("unchecked")
    private void findComponentTypeThenRegister(@Nullable Class<?> aClass, @NotNull Service service) {
        if (aClass == null || Object.class.equals(aClass)) {
            return;
        }
        if (Service.class.isAssignableFrom(aClass)) {
            registerComponent((Class<? extends Service>) aClass, service);
        }
        for (Class<?> anInterface : aClass.getInterfaces()) {
            findComponentTypeThenRegister(anInterface, service);
        }
        findComponentTypeThenRegister(aClass.getSuperclass(), service);
    }

    private void registerComponent(@NotNull Class<? extends Service> serviceType, @NotNull Service service) {
        List<Service> list = Option.of(services.get(serviceType)).getOrElse(ArrayList::new);
        list.add(service);
        services.put(serviceType, list);
    }

    /**
     * 加载指定服务类型的实例.
     *
     * @param serviceType 希望加载的服务类型.
     * @return 注册表中该服务类型的实例.
     * @throws ServiceNotRegisteredException 请参阅此异常的注释.
     * @throws ServiceNotUniqueException     请参阅此异常的注释.
     */
    @Override
    @SuppressWarnings("unchecked")
    public <S extends Service> S loadService(@NotNull Class<S> serviceType) throws PyrgusServiceException {
        List<S> services = loadServices(serviceType);
        if (services.isEmpty()) {
            throw new ServiceNotRegisteredException(serviceType);
        }
        if (services.size() > 1) {
            throw new ServiceNotUniqueException(serviceType, services.stream()
                    .map(s -> (Class<? extends S>) s.getClass())
                    .collect(Collectors.toList()));
        }
        return services.get(0);
    }

    /**
     * 加载指定服务类型的所有实例.
     *
     * @param serviceType 希望加载的服务类型.
     * @return 注册表中该服务类型的所有实例, 如果没有至少一个实例则返回空集合: {@link Collections#EMPTY_LIST}.
     */
    @Override
    @SuppressWarnings("unchecked")
    public @NotNull <S extends Service> List<S> loadServices(@NotNull Class<S> serviceType) {
        return (List<S>) services.getOrDefault(serviceType, Collections.emptyList());
    }
}

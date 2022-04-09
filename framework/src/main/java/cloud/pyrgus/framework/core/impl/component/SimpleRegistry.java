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

package cloud.pyrgus.framework.core.impl.component;

import cloud.pyrgus.framework.core.component.Component;
import cloud.pyrgus.framework.core.component.Registry;
import cloud.pyrgus.framework.core.component.exception.NotSuchComponentException;
import cloud.pyrgus.framework.core.component.exception.NotUniqueComponentException;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 简单注册表
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
@Slf4j
public class SimpleRegistry implements Registry {

    /**
     * 组件
     */
    private final Map<Class<? extends Component>, List<Component>> components = new HashMap<>();

    /**
     * 简单注册表
     *
     * @param classes 类
     */
    public SimpleRegistry(Set<Class<? extends Component>> classes) {
        for (Class<? extends Component> type : classes) {
            try {
                Constructor<?>[] constructors = type.getConstructors();
                if (constructors.length > 1) {
                    log.warn("Skip component [ {} ], make sure that only have one constructor.", type);
                    continue;
                }
                if (constructors.length == 1) {
                    Constructor<?> constructor = constructors[0];
                    constructor.setAccessible(true);
                    Class<?>[] parameterTypes = constructor.getParameterTypes();
                    if (parameterTypes.length == 0) {
                        if (log.isDebugEnabled()) {
                            log.debug("register component: [ {} ]", constructor.getDeclaringClass().getName());
                        }
                        appendComponent(type.newInstance());
                    } else {
                        log.warn("Skip component [ {} ], make sure that constructor does not have any parameters", type);
                    }
                    continue;
                }
                appendComponent(type.newInstance());
            } catch (InstantiationException | IllegalAccessException e) {
                log.error("initiate component [ " + type.getName() + " ] failed.", e);
            }
        }
    }

    /**
     * 追加组件
     *
     * @param component 组件
     */
    private void appendComponent(Component component) {
        findComponentTypeThenRegister(component.getClass(), component);
    }

    /**
     * 注册组件
     *
     * @param componentType 组件类型
     * @param component     组件
     */
    private void registerComponent(Class<? extends Component> componentType, Component component) {
        List<Component> list = Option.of(components.get(componentType)).getOrElse(ArrayList::new);
        list.add(component);
        components.put(componentType, list);
    }

    /**
     * 找到组件类型然后注册
     *
     * @param aClass    组件类型
     * @param component 组件
     */
    @SuppressWarnings("unchecked")
    private void findComponentTypeThenRegister(Class<?> aClass, Component component) {
        if (aClass == null || Object.class.equals(aClass)) {
            return;
        }
        if (Component.class.isAssignableFrom(aClass)) {
            registerComponent((Class<? extends Component>) aClass, component);
        }
        for (Class<?> anInterface : aClass.getInterfaces()) {
            findComponentTypeThenRegister(anInterface, component);
        }
        findComponentTypeThenRegister(aClass.getSuperclass(), component);
    }

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return {@link C}
     */
    @Override
    public <C extends Component> C getComponent(Class<C> componentType) {
        List<C> components = getComponents(componentType);
        if (components.isEmpty()) {
            throw new NotSuchComponentException(componentType);
        }
        if (components.size() > 1) {
            throw new NotUniqueComponentException(componentType, components.stream().map(Object::getClass).collect(Collectors.toList()));
        }
        return components.get(0);
    }

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return {@link List}<{@link C}>
     */
    @Override
    @SuppressWarnings("unchecked")
    public <C extends Component> List<C> getComponents(Class<C> componentType) {
        return (List<C>) components.getOrDefault(componentType, Collections.emptyList());
    }
}

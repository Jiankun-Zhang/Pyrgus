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
import cloud.pyrgus.framework.core.component.RegistryBuilder;
import lombok.extern.slf4j.Slf4j;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 基于 Reflections 库的注册表构建器
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
@Slf4j
public class ReflectionsRegistryBuilder implements RegistryBuilder {

    /**
     * Reflections 实例
     */
    private final Reflections reflections;

    /**
     * 基于 Reflections 库的注册表构建器
     *
     * @param configurationBuilder 配置构建器
     */
    public ReflectionsRegistryBuilder(ConfigurationBuilder configurationBuilder) {
        this.reflections = new Reflections(configurationBuilder);
    }

    /**
     * 构建
     *
     * @return {@link Registry}
     */
    @Override
    public Registry build() {
        Set<Class<? extends Component>> classes = reflections.getSubTypesOf(Component.class)
                .stream()
                .filter(aClass -> !aClass.isInterface())
                .collect(Collectors.toSet());
        if (log.isDebugEnabled()) {
            classes.forEach(aClass -> log.debug("found component type: [ {} ]", reflections.toName(aClass)));
        }
        return new SimpleRegistry(classes);
    }

}

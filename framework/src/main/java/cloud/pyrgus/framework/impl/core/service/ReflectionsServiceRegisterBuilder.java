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
import cloud.pyrgus.framework.core.service.ServiceRegisterBuilder;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reflections.Configuration;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * 使用 {@link Reflections} 扫描 {@link Service} 的实现类并注册至 {@link SimpleServiceRegistry}.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
@Slf4j
public class ReflectionsServiceRegisterBuilder implements ServiceRegisterBuilder {

    private final Reflections reflections;

    /**
     * 创建一个新的注册表构造器.
     *
     * @param rootClass 希望从哪个包路径开始扫描.
     */
    public ReflectionsServiceRegisterBuilder(@NotNull Class<?> rootClass) {
        this(new ConfigurationBuilder()
                .forPackages(rootClass.getPackage().getName())
                .setScanners(Scanners.SubTypes)
        );
    }

    /**
     * 创建一个新的注册表构造器.
     *
     * @param configuration 构造 {@link Reflections} 实例的配置内容.
     */
    public ReflectionsServiceRegisterBuilder(@NotNull Configuration configuration) {
        this(new Reflections(configuration));
    }

    /**
     * 创建一个新的注册表构造器.
     *
     * @param reflections 已配置好的 {@link Reflections} 实例.
     */
    public ReflectionsServiceRegisterBuilder(@NotNull Reflections reflections) {
        this.reflections = reflections;
    }

    /**
     * 构建 {@link ServiceRegistry} 实例.
     *
     * @return 服务注册表实例.
     */
    @Override
    public ServiceRegistry build() {
        Set<Class<? extends Service>> types = reflections.getSubTypesOf(Service.class)
                .stream()
                .filter(aClass -> !aClass.isInterface())
                .collect(Collectors.toSet());
        if (log.isDebugEnabled()) {
            types.forEach(aClass -> log.debug("found component type: [ {} ]", reflections.toName(aClass)));
        }
        return new SimpleServiceRegistry(types);
    }

}

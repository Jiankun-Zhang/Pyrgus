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

package cloud.pyrgus.framework.core;

import cloud.pyrgus.framework.core.component.Component;
import cloud.pyrgus.framework.core.component.PropertyProvider;
import cloud.pyrgus.framework.core.component.Registry;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

/**
 * pyrgus
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
@Slf4j
public class Pyrgus implements Registry, PropertyProvider {

    /**
     * 实例
     */
    private static Pyrgus instance;
    /**
     * 属性提供器
     */
    private final PropertyProvider propertyProvider;
    /**
     * 注册表
     */
    private final Registry registry;

    /**
     * pyrgus
     *
     * @param configuration 配置
     */
    public Pyrgus(Configuration configuration) {
        this.propertyProvider = Objects.requireNonNull(configuration.getPropertyProvider());
        this.registry = Objects.requireNonNull(configuration.getRegistry());
    }

    /**
     * 获取已配置实例
     *
     * @return {@link Pyrgus}
     */
    public static Pyrgus instance() {
        return instance;
    }

    /**
     * 配置
     *
     * @param configuration 配置
     * @return {@link Pyrgus}
     */
    public static Pyrgus configure(Configuration configuration) {
        return instance = new Pyrgus(configuration);
    }

    /**
     * 查询此属性提供器是否含有指定的键.
     *
     * @param key 属性键
     * @return 当此属性提供者含有指定的键时返回 {@code true}, 否则返回 {@code false}.
     */
    @Override
    public boolean containsKey(String key) {
        return propertyProvider.containsKey(key);
    }

    /**
     * 读取指定键的属性值.
     *
     * @param key 属性键
     * @return {@link Option}<{@link String}>
     */
    @Override
    public Option<String> getProperty(String key) {
        return propertyProvider.getProperty(key);
    }

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return {@link C}
     */
    @Override
    public <C extends Component> C getComponent(Class<C> componentType) {
        return registry.getComponent(componentType);
    }

    /**
     * 获得组件
     *
     * @param componentType 组件类型
     * @return {@link List}<{@link C}>
     */
    @Override
    public <C extends Component> List<C> getComponents(Class<C> componentType) {
        return registry.getComponents(componentType);
    }
}

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

package cloud.pyrgus.framework;

import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.service.contract.PropertyProvider;
import cloud.pyrgus.framework.exception.PyrgusAlreadyConfiguredException;
import cloud.pyrgus.framework.exception.PyrgusNotConfiguredException;
import cloud.pyrgus.framework.impl.core.service.ReflectionsServiceRegisterBuilder;
import cloud.pyrgus.framework.impl.core.service.contract.PropertiesProvider;
import lombok.experimental.Delegate;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * 外界与 Pyrgus 交互的主要入口.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/13
 */
public class Pyrgus {

    @Delegate
    private PropertyProvider propertyProvider;
    @Delegate
    private ServiceRegistry serviceRegistry;
    private boolean configured = false;

    /**
     * 使用 {@link PropertiesProvider} 与 {@link ReflectionsServiceRegisterBuilder} 作为最小配置.
     *
     * @param rootClass 请参阅 {@link ReflectionsServiceRegisterBuilder#ReflectionsServiceRegisterBuilder(Class)}.
     * @return 完成配置的 {@link Pyrgus} 实例.
     */
    public static Pyrgus configure(Class<?> rootClass) {
        return configure(Configuration.builder()
                .propertyProvider(new PropertiesProvider())
                .serviceRegisterBuilder(new ReflectionsServiceRegisterBuilder(rootClass))
                .build());
    }

    /**
     * 配置 {@link Pyrgus}.
     *
     * @param configuration 配置内容
     * @return 完成配置的 {@link Pyrgus} 实例.
     * @throws PyrgusAlreadyConfiguredException 请参阅此异常的注释.
     * @see Pyrgus#getInstance()
     */
    public static synchronized Pyrgus configure(@NotNull Configuration configuration) {
        Pyrgus instance = Holder.instance;
        if (instance.configured) {
            throw new PyrgusAlreadyConfiguredException();
        }
        instance.propertyProvider = Objects.requireNonNull(configuration.propertyProvider);
        instance.serviceRegistry = Objects.requireNonNull(configuration.serviceRegisterBuilder).build();
        instance.configured = true;
        return instance;
    }

    /**
     * 获取已配置的 {@link Pyrgus} 实例.
     *
     * @return 已完成配置的 {@link Pyrgus} 实例.
     * @throws PyrgusNotConfiguredException 请参阅此异常的注释.
     * @see Pyrgus#configure(Configuration)
     */
    public static synchronized Pyrgus getInstance() {
        Pyrgus instance = Holder.instance;
        if (instance.configured) {
            return instance;
        }
        throw new PyrgusNotConfiguredException();
    }

    private static class Holder {

        private static final Pyrgus instance = new Pyrgus();

    }
}

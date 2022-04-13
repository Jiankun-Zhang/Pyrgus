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

package cloud.pyrgus.framework.core.service;

import cloud.pyrgus.framework.core.service.exception.PyrgusServiceException;
import cloud.pyrgus.framework.core.service.exception.ServiceNotRegisteredException;
import cloud.pyrgus.framework.core.service.exception.ServiceNotUniqueException;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

/**
 * 服务注册表, 服务可以注册到此注册表以供 Pyrgus 内部或外部(不推荐)使用.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/13
 */
public interface ServiceRegistry {

    /**
     * 加载指定服务类型的实例.
     *
     * @param serviceType 希望加载的服务类型.
     * @param <S>         服务类型.
     * @return 注册表中该服务类型的实例.
     * @throws ServiceNotRegisteredException 请参阅此异常的注释.
     * @throws ServiceNotUniqueException     请参阅此异常的注释.
     */
    <S extends Service> S loadService(@NotNull Class<S> serviceType) throws PyrgusServiceException;

    /**
     * 加载指定服务类型的所有实例.
     *
     * @param serviceType 希望加载的服务类型.
     * @param <S>         服务类型.
     * @return 注册表中该服务类型的所有实例, 如果没有至少一个实例则返回空集合: {@link Collections#EMPTY_LIST}.
     */
    @NotNull <S extends Service> List<S> loadServices(@NotNull Class<S> serviceType);

}

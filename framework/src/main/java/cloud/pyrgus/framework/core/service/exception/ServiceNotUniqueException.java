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

package cloud.pyrgus.framework.core.service.exception;

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.ServiceRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 当 {@link ServiceRegistry} 中同类型的 Service 存在多个注册实例时抛出此异常.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/13
 */
public class ServiceNotUniqueException extends PyrgusServiceException {

    public <S extends Service> ServiceNotUniqueException(Class<S> serviceType, List<Class<? extends S>> registeredTypes) {
        super(format(serviceType, registeredTypes));
    }

    private static <S extends Service> String format(Class<S> serviceType, List<Class<? extends S>> registeredTypes) {
        String separator = System.getProperty("line.separator") + "\t > ";
        return serviceType.getName() + separator
                + registeredTypes.stream()
                .map(Class::getName)
                .collect(Collectors.joining(separator));
    }

}

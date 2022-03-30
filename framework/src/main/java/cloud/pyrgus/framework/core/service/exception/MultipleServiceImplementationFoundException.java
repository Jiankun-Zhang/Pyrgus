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

package cloud.pyrgus.framework.core.service.exception;

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.exception.PyrgusRuntimeException;

import java.util.List;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class MultipleServiceImplementationFoundException extends PyrgusRuntimeException {

    private final Class<? extends Service> serviceType;
    private final List<Class<?>> implementations;

    public <S extends Service> MultipleServiceImplementationFoundException(Class<S> serviceType, List<Class<?>> implementations) {
        this.serviceType = serviceType;
        this.implementations = implementations;
    }

    @Override
    public String getMessage() {
        StringBuilder message = new StringBuilder(String.format("serviceType: ( %s )\n", serviceType.getName()));
        for (int i = 0, num = implementations.size(); i < num; i++) {
            Class<?> implementation = implementations.get(i);
            message.append(String.format("\t+ [#%d] [ %s ]\n", i, implementation.getName()));
        }
        return message.toString();
    }
}

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

package cloud.pyrgus.framework.core.component.exception;

import cloud.pyrgus.framework.core.component.Component;
import cloud.pyrgus.framework.core.exception.PyrgusRuntimeException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
public class NotUniqueComponentException extends PyrgusRuntimeException {

    /**
     * @param componentType 组件类型
     */
    public NotUniqueComponentException(Class<? extends Component> componentType, List<Class<?>> components) {
        super(String.format("type: [ %s ]\n%s", componentType.getName(),
                components.stream()
                        .map(Class::getName)
                        .map(s -> "\t #" + s)
                        .collect(Collectors.joining("\n"))));
    }

}

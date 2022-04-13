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

package cloud.pyrgus.framework.core.service.contract;


import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * 属性供应者, 向 Pyrgus 应用提供所需的属性.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/13
 */
public interface PropertyProvider {

    /**
     * 获取指定的属性的值.
     *
     * @param key 属性键.
     * @return 当键存在时返回属性值, 否则返回 {@code null}.
     */
    @Nullable
    String getProperty(@NotNull String key);

    /**
     * 获取指定的属性的值.
     *
     * @param key          属性键.
     * @param defaultValue 当键不存在时使用此参数所产生的值作为返回内容.
     * @return 当键存在时返回属性值, 否则返回 {@literal defaultValue} 所产生的值.
     */
    @NotNull
    default String getProperty(@NotNull String key, @NotNull Supplier<String> defaultValue) {
        return Option.of(getProperty(key)).getOrElse(defaultValue);
    }

}

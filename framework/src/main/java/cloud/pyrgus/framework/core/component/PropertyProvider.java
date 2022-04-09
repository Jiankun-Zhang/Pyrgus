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

package cloud.pyrgus.framework.core.component;

import io.vavr.control.Option;

import java.util.function.Supplier;

/**
 * 属性提供器
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
public interface PropertyProvider {

    /**
     * 查询此属性提供器是否含有指定的键.
     *
     * @param key 属性键
     * @return 当此属性提供者含有指定的键时返回 {@code true}, 否则返回 {@code false}.
     */
    boolean containsKey(String key);

    /**
     * 读取指定键的属性值.
     *
     * @param key 属性键
     * @return {@link Option}<{@link String}> 当键不存在时返回 {@link  Option#none()}, 否则将属性值包裹在 {@link Option} 后返回.<br/>
     * 请注意, 当键对应的值就是 {@literal null} 时, 应该返回 {@code Option.some(null)}, 这与 {@code Option.none()} 是不一样的.
     */
    Option<String> getProperty(String key);

    /**
     * 读取指定键的属性值, 当键不存在时将返回指定的默认值.
     *
     * @param key                  属性键
     * @param defaultValueSupplier 默认值供应器
     * @return {@link String} 当键存在时将返回对应的值, 若键不存在则返回指定的默认值.
     */
    default String getProperty(String key, Supplier<String> defaultValueSupplier) {
        return getProperty(key).getOrElse(defaultValueSupplier);
    }

}

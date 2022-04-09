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

import cloud.pyrgus.framework.core.component.PropertyProvider;
import io.vavr.control.Option;

/**
 * 系统属性提供者
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
public class SystemPropertiesProvider implements PropertyProvider {

    /**
     * 查询此属性提供器是否含有指定的键.
     *
     * @param key 属性键
     * @return 当此属性提供者含有指定的键时返回 {@code true}, 否则返回 {@code false}.
     */
    @Override
    public boolean containsKey(String key) {
        return System.getProperties().containsKey(key)
                || System.getenv().containsKey(key);
    }

    /**
     * 读取指定键的属性值.
     *
     * @param key 属性键
     * @return {@link Option}<{@link String}> 当键不存在时返回 {@link  Option#none()}, 否则将属性值包裹在 {@link Option} 后返回.<br/>
     * 请注意, 当键对应的值就是 {@literal null} 时, 应该返回 {@code Option.some(null)}, 这与 {@code Option.none()} 是不一样的.
     */
    @Override
    public Option<String> getProperty(String key) {
        if (!containsKey(key)) {
            return Option.none();
        }
        if (System.getProperties().containsKey(key)) {
            return Option.some(System.getProperty(key));
        }
        return Option.of(System.getenv(key));
    }
}

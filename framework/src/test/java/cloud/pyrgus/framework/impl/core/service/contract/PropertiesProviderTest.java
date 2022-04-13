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

package cloud.pyrgus.framework.impl.core.service.contract;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class PropertiesProviderTest {

    @Test
    void should_throws_when_use_null_key() {
        PropertiesProvider provider = getPropertiesProviderByDefault();
        assertThatThrownBy(() -> provider.getProperty(null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void should_get_null_from_un_exists_key() {
        PropertiesProvider provider = getPropertiesProviderByDefault();
        assertThat(provider.getProperty("foo")).isNull();
    }

    @Test
    void should_get_value_from_system_properties() {
        Properties properties = System.getProperties();
        PropertiesProvider provider = getPropertiesProviderByDefault();
        properties.forEach((key, val) -> assertThat(provider.getProperty((String) key)).isEqualTo(val));
    }

    @Test
    void should_get_value_from_system_env() {
        Map<String, String> env = System.getenv();
        PropertiesProvider provider = getPropertiesProviderByDefault();
        env.forEach((key, val) -> assertThat(provider.getProperty(key)).isEqualTo(val));
    }

    @Test
    void should_get_value_from_default_value_supplier() {
        PropertiesProvider provider = getPropertiesProviderByDefault();
        String foo = "foo";
        String bar = "bar";
        assertThat(provider.getProperty(foo)).isNull();
        assertThat(provider.getProperty(foo, () -> bar)).isEqualTo(bar);
    }

    private PropertiesProvider getPropertiesProviderByDefault() {
        return new PropertiesProvider();
    }

}
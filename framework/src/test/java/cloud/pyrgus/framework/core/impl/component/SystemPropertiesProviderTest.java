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

import org.junit.jupiter.api.Test;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/9
 */
class SystemPropertiesProviderTest {

    private final SystemPropertiesProvider propertiesProvider = new SystemPropertiesProvider();

    @Test
    void containsKey() {
        Properties properties = System.getProperties();
        for (Object propKey : properties.keySet()) {
            if (propKey instanceof String) {
                assertThat(propertiesProvider.containsKey((String) propKey)).isTrue();
            }
        }
        assertThat(propertiesProvider.containsKey("foo")).isFalse();
        properties.setProperty("foo", "bar");
        assertThat(propertiesProvider.containsKey("foo")).isTrue();
        properties.remove("foo");
        for (String envKey : System.getenv().keySet()) {
            assertThat(propertiesProvider.containsKey(envKey)).isTrue();
        }
    }

    @Test
    void getProperty() {
        Properties properties = System.getProperties();
        for (Object propKey : properties.keySet()) {
            if (propKey instanceof String) {
                assertThat(propertiesProvider.getProperty((String) propKey).get())
                        .isEqualTo(properties.getProperty((String) propKey));
            }
        }
        assertThat(propertiesProvider.getProperty("foo").isDefined()).isFalse();
        properties.setProperty("foo", "bar");
        assertThat(propertiesProvider.getProperty("foo").isDefined()).isTrue();
        properties.remove("foo");
        for (String envKey : System.getenv().keySet()) {
            assertThat(propertiesProvider.getProperty(envKey).get())
                    .isEqualTo(System.getenv(envKey));
        }

    }
}
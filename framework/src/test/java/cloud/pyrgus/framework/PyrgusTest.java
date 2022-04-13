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

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.exception.PyrgusAlreadyConfiguredException;
import cloud.pyrgus.framework.impl.core.service.contract.PropertiesProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Properties;

import static org.assertj.core.api.Assertions.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class PyrgusTest {

    static Field field;

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    static {
        try {
            field = Pyrgus.class.getDeclaredField("configured");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        field.setAccessible(true);
    }

    @BeforeEach
    void clean_up_pyrgus() {
        try {
            Pyrgus pyrgus = Pyrgus.getInstance();
            field.set(pyrgus, false);
        } catch (Exception ignore) {
        }
    }

    @Test
    void should_throws_when_configuring_nothing() {
        assertThatThrownBy(() -> Pyrgus.configure(Configuration.builder().build()))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> Pyrgus.configure(Configuration.builder()
                .propertyProvider(new PropertiesProvider())
                .build()))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void should_throws_when_configured_twice() {
        Pyrgus.configure(this.getClass());
        assertThatThrownBy(() -> Pyrgus.configure(this.getClass()))
                .isInstanceOf(PyrgusAlreadyConfiguredException.class);
    }

    @Test
    void should_be_fine_with_default_configuration() {
        assertThatCode(() -> Pyrgus.configure(this.getClass()))
                .doesNotThrowAnyException();

        Pyrgus pyrgus = Pyrgus.getInstance();

        Properties properties = System.getProperties();
        properties.forEach((key, val) -> assertThat(pyrgus.getProperty((String) key)).isEqualTo(val));

        Map<String, String> env = System.getenv();
        env.forEach((key, val) -> assertThat(pyrgus.getProperty(key)).isEqualTo(val));

        assertThatCode(() -> pyrgus.loadService(Foo.class))
                .doesNotThrowAnyException();
    }

    interface Foo extends Service {
    }

    public static class Foo1 implements Foo {
    }

}
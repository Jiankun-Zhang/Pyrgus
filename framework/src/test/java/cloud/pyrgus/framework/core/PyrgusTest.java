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

package cloud.pyrgus.framework.core;

import cloud.pyrgus.framework.core.component.Component;
import cloud.pyrgus.framework.core.component.exception.NotUniqueComponentException;
import cloud.pyrgus.framework.core.impl.component.ReflectionsRegistryBuilder;
import cloud.pyrgus.framework.core.impl.component.SystemPropertiesProvider;
import org.junit.jupiter.api.Test;
import org.reflections.scanners.Scanners;
import org.reflections.util.ConfigurationBuilder;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/9
 */
class PyrgusTest {

    @Test
    void it_should_be_fine_with_mini_settings() {
        Pyrgus pyrgus = Pyrgus.configure(Configuration.builder()
                .propertyProvider(new SystemPropertiesProvider())
                .registry(new ReflectionsRegistryBuilder(
                                new ConfigurationBuilder()
                                        .forPackage(PyrgusTest.class.getPackage().getName())
                                        .setScanners(Scanners.SubTypes)
                        )
                                .build()
                )
                .build());
        assertThat(pyrgus).isEqualTo(Pyrgus.instance());

        assertThatThrownBy(() -> pyrgus.getComponent(Foo.class))
                .isInstanceOf(NotUniqueComponentException.class);
        assertThat(pyrgus.getComponent(Foo1.class)).isNotNull();
        assertThat(pyrgus.getComponents(Foo2.class)).hasSize(2);
        assertThat(pyrgus.getComponents(Foo.class)).hasSize(3);

        for (Map.Entry<String, String> entry : System.getenv().entrySet()) {
            assertThat(pyrgus.getProperty(entry.getKey()).get()).isEqualTo(entry.getValue());
        }
    }

    interface Foo extends Component {
    }

    public static class Foo1 implements Foo {
    }

    public static class Foo2 implements Foo {
    }

    public static class Foo23 extends Foo2 {
    }

}
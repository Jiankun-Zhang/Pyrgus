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

package cloud.pyrgus.framework.impl.core.service;

import cloud.pyrgus.framework.core.service.Service;
import cloud.pyrgus.framework.core.service.exception.ServiceNotRegisteredException;
import cloud.pyrgus.framework.core.service.exception.ServiceNotUniqueException;
import lombok.SneakyThrows;
import org.assertj.core.util.Sets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class SimpleServiceRegistryTest {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @Test
    void should_throws_if_service_not_registered() {
        SimpleServiceRegistry registry = new SimpleServiceRegistry(Collections.emptySet());
        assertThatThrownBy(() -> registry.loadService(Foo.class))
                .isInstanceOf(ServiceNotRegisteredException.class);
    }

    @Test
    void should_throws_if_service_registered_multiple_implementations() {
        SimpleServiceRegistry registry = new SimpleServiceRegistry(Sets.set(Foo1.class, Foo2.class));
        assertThatThrownBy(() -> registry.loadService(Foo.class))
                .isInstanceOf(ServiceNotUniqueException.class);
    }

    @SneakyThrows
    @Test
    void should_load_foo1_when_load_service_foo() {
        SimpleServiceRegistry registry = new SimpleServiceRegistry(Sets.set(Foo1.class));
        assertThat(registry.loadService(Foo.class)).isInstanceOf(Foo1.class);
    }

    @SneakyThrows
    @Test
    void should_load_foo2_when_load_service_foo() {
        System.setProperty(Foo.class.getName(), Foo2.class.getName());
        SimpleServiceRegistry registry = new SimpleServiceRegistry(Sets.set(Foo1.class, Foo2.class));
        assertThat(registry.loadService(Foo.class)).isInstanceOf(Foo2.class);
        System.getProperties().remove(Foo.class.getName());
    }

    @ParameterizedTest
    @ValueSource(classes = {
            Foo3.class, Foo4.class, Foo5.class
    })
    void should_skip_illegal_types(Class<? extends Foo> illegalType) {
        SimpleServiceRegistry registry = new SimpleServiceRegistry(Sets.set(illegalType));
        assertThat(registry.loadServices(Foo.class)).isEmpty();
    }

    interface Foo extends Service {
    }

    public static class Foo1 implements Foo {
    }

    public static class Foo2 implements Foo {
        public Foo2() {
        }
    }

    public static class Foo3 implements Foo {
        Foo3(String arg) {
        }
    }

    public static class Foo4 implements Foo {
        public Foo4(String arg) {
        }
    }

    public static class Foo5 implements Foo {
        Foo5(String arg1) {
        }

        Foo5(String arg1, String arg2) {
        }
    }

}
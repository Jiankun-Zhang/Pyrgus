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

import cloud.pyrgus.framework.core.component.Component;
import cloud.pyrgus.framework.core.component.exception.NotSuchComponentException;
import cloud.pyrgus.framework.core.component.exception.NotUniqueComponentException;
import org.junit.jupiter.api.Test;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/9
 */
class SimpleRegistryTest {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    private final SimpleRegistry simpleRegistry = new SimpleRegistry(new HashSet<Class<? extends Component>>() {{
        add(Foo1.class);
        add(Foo2.class);
        add(Foo3.class);
    }});

    @Test
    void getComponent() {
        assertThat(simpleRegistry.getComponent(Foo1.class)).isNotNull();
        assertThat(simpleRegistry.getComponent(Foo3.class)).isNotNull();
        assertThatThrownBy(() -> simpleRegistry.getComponent(Foo.class))
                .isInstanceOf(NotUniqueComponentException.class);
        assertThatThrownBy(() -> simpleRegistry.getComponent(Foo2.class))
                .isInstanceOf(NotUniqueComponentException.class);
        assertThatThrownBy(() -> simpleRegistry.getComponent(Foo4.class))
                .isInstanceOf(NotSuchComponentException.class);
    }

    @Test
    void getComponents() {
        assertThat(simpleRegistry.getComponents(Foo.class)).hasSize(3);
        assertThat(simpleRegistry.getComponents(Foo1.class)).hasSize(1);
        assertThat(simpleRegistry.getComponents(Foo2.class)).hasSize(2);
        assertThat(simpleRegistry.getComponents(Foo4.class)).isEmpty();
    }

    interface Foo extends Component {
    }

    static class Foo1 implements Foo {
    }

    static class Foo2 implements Foo {
    }

    static class Foo3 extends Foo2 {
    }

    static class Foo4 implements Foo {
    }
}
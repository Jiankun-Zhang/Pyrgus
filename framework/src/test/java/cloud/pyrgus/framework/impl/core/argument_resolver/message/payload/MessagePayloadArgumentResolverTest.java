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

package cloud.pyrgus.framework.impl.core.argument_resolver.message.payload;

import cloud.pyrgus.framework.cqrs.Action;
import cloud.pyrgus.framework.cqrs.ActionMessage;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class MessagePayloadArgumentResolverTest {

    @NotNull
    private ActionMessage getActionMessage() {
        return new ActionMessage(Collections.emptyMap(), new Foo());
    }

    @SneakyThrows
    @Test
    void should_skip() {
        MessagePayloadArgumentResolver resolver = new MessagePayloadArgumentResolver();

        Parameter shouldSkip = Bar.class.getDeclaredMethod("shouldSkip", Object.class).getParameters()[0];

        assertThat(resolver.resolve(getActionMessage(), Collections.emptyMap(), shouldSkip)).isEmpty();
    }

    @SneakyThrows
    @Test
    void should_resolve() {
        MessagePayloadArgumentResolver resolver = new MessagePayloadArgumentResolver();

        Parameter shouldSkip = Bar.class.getDeclaredMethod("shouldResolve", Foo.class).getParameters()[0];

        Option<Object> resolve = resolver.resolve(getActionMessage(), Collections.emptyMap(), shouldSkip);
        assertThat(resolve).isNotEmpty();
        assertThat(resolve.get()).isInstanceOf(Foo.class);
    }

    static class Foo implements Action<String> {
    }

    static class Bar {
        void shouldSkip(Object object) {
        }

        void shouldResolve(Foo foo) {
        }
    }

}
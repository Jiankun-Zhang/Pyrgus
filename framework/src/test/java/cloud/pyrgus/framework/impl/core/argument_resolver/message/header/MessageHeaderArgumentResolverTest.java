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

package cloud.pyrgus.framework.impl.core.argument_resolver.message.header;

import cloud.pyrgus.framework.cqrs.ActionMessage;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.assertj.core.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public class MessageHeaderArgumentResolverTest {

    @NotNull
    private ActionMessage getActionMessage(Map<String, Object> headers) {
        return new ActionMessage(Option.of(headers).getOrElse(HashMap::new), "");
    }

    @SneakyThrows
    @Test
    void should_throw_when_illegal_usage() {
        MessageHeaderArgumentResolver resolver = new MessageHeaderArgumentResolver();

        Parameter stringParam = Foo.class.getDeclaredMethod("shouldUseMap", String.class).getParameters()[0];

        assertThatThrownBy(() -> resolver.resolve(getActionMessage(null), Collections.emptyMap(), stringParam))
                .isInstanceOf(IllegalArgumentException.class);

        Parameter mapParam = Foo.class.getDeclaredMethod("shouldUseString", Integer.class).getParameters()[0];

        assertThatThrownBy(() -> resolver.resolve(getActionMessage(null), Collections.emptyMap(), mapParam))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @SneakyThrows
    @Test
    void should_skip_without_annotation() {
        MessageHeaderArgumentResolver resolver = new MessageHeaderArgumentResolver();

        Parameter skipParam = Foo.class.getDeclaredMethod("shouldSkip", String.class).getParameters()[0];

        assertThat(resolver.resolve(getActionMessage(null), Collections.emptyMap(), skipParam)).isEmpty();
    }

    @SneakyThrows
    @SuppressWarnings("unchecked")
    @Test
    void should_get_all_headers() {
        MessageHeaderArgumentResolver resolver = new MessageHeaderArgumentResolver();

        Map<String, Object> headers = Maps.newHashMap("foo", "bar");

        Parameter allHeaders = Foo.class.getDeclaredMethod("useMap", Map.class).getParameters()[0];

        Option<Object> resolve = resolver.resolve(getActionMessage(headers), Collections.emptyMap(), allHeaders);
        assertThat(resolve).isNotEmpty();

        assertThat(((Map<String, Object>) resolve.get())).containsEntry("foo", "bar");
    }

    @SneakyThrows
    @Test
    void should_get_foo_from_headers() {
        MessageHeaderArgumentResolver resolver = new MessageHeaderArgumentResolver();

        Map<String, Object> headers = Maps.newHashMap("foo", "bar");

        Parameter allHeaders = Foo.class.getDeclaredMethod("useString", String.class).getParameters()[0];

        Option<Object> resolve = resolver.resolve(getActionMessage(headers), Collections.emptyMap(), allHeaders);
        assertThat(resolve).isNotEmpty();

        assertThat(((String) resolve.get())).isEqualTo("bar");
    }

    static class Foo {
        void shouldSkip(String header) {
        }

        void shouldUseMap(@MessageHeader String header) {
        }

        void shouldUseString(@MessageHeader(name = "foo") Integer header) {
        }

        void useMap(@MessageHeader Map<String, Object> headers) {
        }

        void useString(@MessageHeader(name = "foo") String headers) {
        }
    }

}
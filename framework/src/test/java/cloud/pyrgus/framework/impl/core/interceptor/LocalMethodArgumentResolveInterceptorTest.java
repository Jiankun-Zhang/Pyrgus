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

package cloud.pyrgus.framework.impl.core.interceptor;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.consumer.LocalMethodMessageConsumer;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.TaskInterceptorChain;
import cloud.pyrgus.framework.cqrs.Action;
import cloud.pyrgus.framework.cqrs.ActionMessage;
import cloud.pyrgus.framework.cqrs.ActionTask;
import cloud.pyrgus.framework.impl.core.argument_resolver.message.payload.MessagePayloadArgumentResolver;
import cloud.pyrgus.framework.impl.core.argument_resolver.task.state.TaskState;
import cloud.pyrgus.framework.impl.core.argument_resolver.task.state.TaskStateArgumentResolver;
import lombok.SneakyThrows;
import org.assertj.core.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class LocalMethodArgumentResolveInterceptorTest {

    @Test
    void should_skip_when_type_of_consumer_is_not_local_method_consumer() {
        LocalMethodArgumentResolveInterceptor interceptor = new LocalMethodArgumentResolveInterceptor();

        TaskInterceptorChain chain = mock(TaskInterceptorChain.class);
        doNothing().when(chain).next();

        ActionTask task = getActionTask(false);
        interceptor.intercept(task, chain);

        verify(chain, times(1)).next();
        assertThat(task.getContext()).isEmpty();
    }

    @Test
    void should_resolve_arguments() {
        LocalMethodArgumentResolveInterceptor interceptor = new LocalMethodArgumentResolveInterceptor();

        TaskInterceptorChain chain = mock(TaskInterceptorChain.class);
        doNothing().when(chain).next();

        ServiceRegistry registry = mock(ServiceRegistry.class);
        when(registry.loadServices(any())).thenReturn(Arrays.asList(new MessagePayloadArgumentResolver(), new TaskStateArgumentResolver()));

        interceptor.configure(registry, mock(PropertyProvider.class));

        ActionTask task = getActionTask(true);
        interceptor.intercept(task, chain);

        verify(chain, times(1)).next();
        assertThat(task.getContext()).isNotEmpty();

        Object[] args = (Object[]) task.getContext().get(LocalMethodArgumentResolveInterceptor.CTX_KEY_ARGS);
        assertThat(args[0]).isInstanceOf(Foo.class);
        assertThat(args[1]).isInstanceOf(String.class).isEqualTo("bar");
    }

    @NotNull
    private ActionTask getActionTask(boolean useLocalMethodMessageConsumer) {
        Foo foo = new Foo();
        if (useLocalMethodMessageConsumer) {
            LocalMethodMessageConsumer consumer = new LocalMethodMessageConsumer() {

                @Override
                public @NotNull Object getInvokeTarget() {
                    return foo;
                }

                @SneakyThrows
                @Override
                public @NotNull Method matchMethod(@NotNull Message message) {
                    return Bar.class.getDeclaredMethod("handle", Foo.class, String.class);
                }
            };
            return new ActionTask(getActionMessage(), consumer, new HashMap<>(), Maps.newHashMap("foo", "bar"), new CompletableFuture<>());
        }
        return new ActionTask(getActionMessage(), (message, future) -> future.complete(((ActionMessage) message).getAction()), new HashMap<>(), new HashMap<>(), new CompletableFuture<>());
    }

    private ActionMessage getActionMessage() {
        return new ActionMessage(Collections.emptyMap(), new Foo());
    }

    static class Foo implements Action<String> {
    }

    static class Bar {
        void handle(Foo foo, @TaskState(name = "foo") String state) {
        }
    }
}
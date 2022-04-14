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

package cloud.pyrgus.framework.impl.core.message;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageConsumer;
import cloud.pyrgus.framework.core.message.MessageFilter;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class NamedMessageDispatcherTest {

    static NamedMessageDispatcher dispatcher = new NamedMessageDispatcher();

    @BeforeAll
    static void configureDispatcher() {
        dispatcher.configure(mockServiceRegistry(), mockPropertyProvider());
    }

    private static ServiceRegistry mockServiceRegistry() {
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.loadServices(ArgumentMatchers.eq(MessageFilter.class))).thenReturn(Collections.emptyList());
        return serviceRegistry;
    }

    private static PropertyProvider mockPropertyProvider() {
        return mock(PropertyProvider.class);
    }

    private static Message prepareArgument(String name) {
        return new Message() {
            @Override
            public @NotNull Map<String, Object> getHeaders() {
                Map<String, Object> map = new HashMap<>();
                if (name != null) {
                    map.put("name", name);
                }
                return map;
            }

            @Override
            public @NotNull Object getPayload() {
                return name;
            }
        };
    }

    @SneakyThrows
    @BeforeEach
    void cleanUpConsumers() {
        Field field = NamedMessageDispatcher.class.getDeclaredField("consumerMap");
        field.setAccessible(true);
        ((Map<String, MessageConsumer>) field.get(dispatcher)).clear();
    }

    @Test
    void should_failed_without_name_in_message_headers() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        MessageConsumer consumer = dispatcher.dispatch(prepareArgument(null), future);

        assertThat(consumer).isNull();
        assertThat(future).isCompletedExceptionally();
    }

    @Test
    void should_failed_without_specified_consumer() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        MessageConsumer consumer = dispatcher.dispatch(prepareArgument("foo"), future);

        assertThat(consumer).isNull();
        assertThat(future).isCompletedExceptionally();
    }

    @SneakyThrows
    @Test
    void should_failed_cause_by_filter_failed() {

        Field field = NamedMessageDispatcher.class.getDeclaredField("filters");
        field.setAccessible(true);
        field.set(dispatcher, Collections.singletonList((MessageFilter) message -> false));

        CompletableFuture<Object> future = new CompletableFuture<>();
        MessageConsumer consumer = dispatcher
                .addConsumer("foo", (message, f) -> f.complete(message.getPayload()))
                .dispatch(prepareArgument("foo"), future);

        assertThat(consumer).isNull();
        assertThat(future).isCompletedExceptionally();

        field.set(dispatcher, Collections.emptyList());
    }

    @Test
    void should_success_with_specified_consumer() {
        CompletableFuture<Object> future = new CompletableFuture<>();
        MessageConsumer consumer = dispatcher.addConsumer("foo", (message, f) -> f.complete(message.getPayload()))
                .dispatch(prepareArgument("foo"), future);

        assertThat(consumer).isNotNull();
        assertThat(future).isNotCompletedExceptionally();
    }

}
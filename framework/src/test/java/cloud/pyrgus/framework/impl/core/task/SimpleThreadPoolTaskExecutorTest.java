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

package cloud.pyrgus.framework.impl.core.task;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageConsumer;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Mode;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskInterceptor;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.assertj.core.util.Maps;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.as;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class SimpleThreadPoolTaskExecutorTest {

    static SimpleThreadPoolTaskExecutor executor = new SimpleThreadPoolTaskExecutor();

    @BeforeAll
    static void configureExecutor() {
        executor.configure(mockServiceRegistry(), mockPropertyProvider());
    }

    private static ServiceRegistry mockServiceRegistry() {
        ServiceRegistry serviceRegistry = mock(ServiceRegistry.class);
        when(serviceRegistry.loadServices(ArgumentMatchers.eq(TaskInterceptor.class))).thenReturn(Collections.emptyList());
        return serviceRegistry;
    }

    private static PropertyProvider mockPropertyProvider() {
        return mock(PropertyProvider.class);
    }

    private static Tuple2<Message, MessageConsumer> prepareArguments() {
        return Tuple.of(new Message() {
            @Override
            public @NotNull Map<String, Object> getHeaders() {
                return new HashMap<>();
            }

            @Override
            public @NotNull Object getPayload() {
                return Thread.currentThread().getName();
            }
        }, (message, future) -> {
            future.complete(message.getPayload());
        });
    }

    @Test
    void should_return_task_when_submit() {
        Tuple2<Message, MessageConsumer> arguments = prepareArguments();
        Task task = executor.submit(arguments._1, arguments._2, null, Mode.Posting);
        assertThat(task).extracting("message").isEqualTo(arguments._1);
        assertThat(task).extracting("consumer").isEqualTo(arguments._2);
        assertThat(task).extracting("context", as(InstanceOfAssertFactories.MAP)).isEmpty();
        assertThat(task).extracting("state", as(InstanceOfAssertFactories.MAP)).isEmpty();
        assertThat(task).extracting("future", as(InstanceOfAssertFactories.COMPLETABLE_FUTURE))
                .isCompletedWithValue(arguments._1.getPayload());

        task = executor.submit(arguments._1, arguments._2, null, Mode.Background);
        assertThat(task.getFuture())
                .succeedsWithin(1, TimeUnit.SECONDS)
                .isNotEqualTo(Thread.currentThread().getName());
    }

    @Test
    void should_merge_state_when_passing_state() {
        Tuple2<Message, MessageConsumer> arguments = prepareArguments();
        Message message1 = arguments._1;
        arguments = prepareArguments();
        Message message2 = arguments._1;
        Task task = executor.submit(message1, (message, future) -> {
            Task executingTask = executor.executingTask().get();
            executingTask.getState().put("foo", "bar");
            executor.submit(message2, (msg, f) -> f.complete(msg.getPayload()), Maps.newHashMap("foo", "foo"), Mode.Posting)
                    .getFuture()
                    .handle((val, throwable) -> {
                        if (val != null) {
                            future.complete(val);
                        }
                        if (throwable != null) {
                            future.completeExceptionally(throwable);
                        }
                        return val;
                    });
        }, null, Mode.Posting);
        assertThat(task.getState())
                .asInstanceOf(InstanceOfAssertFactories.MAP)
                .containsEntry("foo", "foo");
    }

}
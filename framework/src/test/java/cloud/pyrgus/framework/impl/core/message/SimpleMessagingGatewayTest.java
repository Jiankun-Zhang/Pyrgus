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

import cloud.pyrgus.framework.core.message.MessageDispatcher;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Mode;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
class SimpleMessagingGatewayTest {

    @SneakyThrows
    @Test
    void should_receive_bar() {
        SimpleMessagingGateway gateway = new SimpleMessagingGateway();

        Task task = mock(Task.class);

        MessageDispatcher dispatcher = mock(MessageDispatcher.class);
        when(dispatcher.dispatch(any(), argThat(future -> {
            when(task.getFuture()).thenReturn(future);
            return true;
        })))
                .thenReturn(((message, future) -> future.complete(message.getPayload())));

        TaskExecutor executor = mock(TaskExecutor.class);
        when(executor.submit(argThat(message -> {
                    when(task.getMessage()).thenReturn(message);
                    return true;
                }),
                argThat(consumer -> {
                    when(task.getConsumer()).thenReturn(consumer);
                    return true;
                }), any(), any()))
                .then((Answer<Task>) invocationOnMock -> {
                    task.getConsumer().consume(task.getMessage(), task.getFuture());
                    return task;
                });

        ServiceRegistry registry = mock(ServiceRegistry.class);
        when(registry.loadService(MessageDispatcher.class)).thenReturn(dispatcher);
        when(registry.loadService(TaskExecutor.class)).thenReturn(executor);

        gateway.configure(registry, mock(PropertyProvider.class));

        CompletableFuture<Object> future = gateway.apply("bar", null, Mode.Posting);

        assertThat(future).isCompletedWithValue("bar");
    }

}
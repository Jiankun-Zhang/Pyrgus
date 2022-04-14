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
import cloud.pyrgus.framework.core.message.MessageDispatcher;
import cloud.pyrgus.framework.core.message.MessagingGateway;
import cloud.pyrgus.framework.core.service.Configurable;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Mode;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import io.vavr.control.Option;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public class SimpleMessagingGateway implements MessagingGateway, Configurable {

    private MessageDispatcher dispatcher = null;

    private TaskExecutor taskExecutor = null;


    private Message packMessage(@NotNull Object payload, @Nullable Map<String, Object> headers) {
        return new Message() {
            @Override
            public @NotNull Map<String, Object> getHeaders() {
                return Option.of(headers).getOrElse(HashMap::new);
            }

            @Override
            public @NotNull Object getPayload() {
                return payload;
            }
        };
    }

    /**
     * 当准备将服务实例提供给需求者时将调用此方法以确保服务配置完毕.<br/>
     * 请注意, 此方法可能被调用多次.
     *
     * @param serviceRegistry  服务注册表
     * @param propertyProvider 属性提供器
     */
    @SneakyThrows
    @Override
    public void configure(@NotNull ServiceRegistry serviceRegistry, @NotNull PropertyProvider propertyProvider) {
        if (dispatcher == null) {
            dispatcher = serviceRegistry.loadService(MessageDispatcher.class);
        }
        if (taskExecutor == null) {
            taskExecutor = serviceRegistry.loadService(TaskExecutor.class);
        }
    }

    /**
     * 发送消息
     *
     * @param payload 消息载荷
     * @param headers 消息标头
     * @param mode    执行模式
     * @return 消息处理成功时结果将作为 {@link CompletableFuture} 的内容返回.
     * @see MessageDispatcher#dispatch(Message, CompletableFuture)
     */
    @SuppressWarnings("unchecked")
    @Override
    public @NotNull <R> CompletableFuture<R> apply(@NotNull Object payload, @Nullable Map<String, Object> headers, @NotNull Mode mode) {
        Message message = packMessage(payload, headers);
        CompletableFuture<Object> future = new CompletableFuture<>();
        MessageConsumer consumer = dispatcher.dispatch(message, future);
        if (consumer != null) {
            taskExecutor.submit(message, consumer, null, mode);
        }
        return (CompletableFuture<R>) future;
    }
}

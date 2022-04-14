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
import cloud.pyrgus.framework.core.message.MessageFilter;
import cloud.pyrgus.framework.core.service.Configurable;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 此消息调度实现不会加载注册到 {@link ServiceRegistry} 的 {@link MessageConsumer}, 而是要求手动注册消费者时提供一个名称以进行绑定.<br/>
 * 调度消息时将依据消息标头的 {@code name} 值寻找相应的消费者进行消费.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public class NamedMessageDispatcher implements MessageDispatcher, Configurable {

    private final Map<String, MessageConsumer> consumerMap = new HashMap<>();

    private List<MessageFilter> filters = null;

    /**
     * 注册消息消费者, 当存在同名消费者时将覆盖旧值.
     *
     * @param name     消费者名称
     * @param consumer 消费者实例
     * @return {@link NamedMessageDispatcher} 实例.
     */
    public NamedMessageDispatcher addConsumer(String name, MessageConsumer consumer) {
        consumerMap.put(name, consumer);
        return this;
    }

    /**
     * 调度消息.
     *
     * @param message 需要调度的消息.
     * @param future  调度失败时会使用 {@link CompletableFuture#completeExceptionally(Throwable)} 结束调度.
     * @return MessageDispatcher 调度成功时返回对应的消费者, 否则返回 {@code null}.
     */
    @Override
    public MessageConsumer dispatch(@NotNull Message message, @NotNull CompletableFuture<Object> future) {
        String name = (String) message.getHeaders().getOrDefault("name", "");
        if (name.isEmpty()) {
            future.completeExceptionally(new IllegalArgumentException(String.format("非法消息: ( %s ).", message)));
            return null;
        }
        MessageConsumer consumer = consumerMap.get(name);
        if (consumer == null) {
            future.completeExceptionally(new IllegalArgumentException(String.format("指定的消息消费者不存在: ( %s ).", name)));
            return null;
        }
        for (MessageFilter filter : filters) {
            if (!filter.test(message)) {
                future.completeExceptionally(new IllegalArgumentException("消息已被过滤."));
                return null;
            }
        }
        return consumer;
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
        if (filters == null) {
            filters = serviceRegistry.loadServices(MessageFilter.class);
        }
    }
}

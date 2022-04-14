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

package cloud.pyrgus.framework.core.message.consumer;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageConsumer;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.impl.core.interceptor.LocalMethodArgumentResolveInterceptor;
import lombok.SneakyThrows;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * {@link MessageConsumer} 的本地方法消费者实现.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public interface LocalMethodMessageConsumer extends MessageConsumer {

    /**
     * 获取 {@link Method#invoke(Object, Object...)} 的目标对象.
     *
     * @return 消费执行的目标对象.
     */
    @NotNull
    Object getInvokeTarget();

    /**
     * 匹配此消费者适用于给定消息的方法实例.
     *
     * @param message 消息实例.
     * @return 适用的方法实例.
     */
    @NotNull
    Method matchMethod(@NotNull Message message);

    @SneakyThrows
    @Override
    default void consume(@NotNull Message message, @NotNull CompletableFuture<Object> future) {
        Object target = getInvokeTarget();
        Method method = matchMethod(message);
        Task task = Pyrgus.getInstance().loadService(TaskExecutor.class).executingTask().get();
        try {
            Map<String, Object> context = task.getContext();
            String key = LocalMethodArgumentResolveInterceptor.CTX_KEY_ARGS;
            Object[] arguments = (Object[]) context.get(key);
            Object value = method.invoke(target, arguments);
            context.remove(key);
            future.complete(value);
        } catch (IllegalAccessException e) {
            future.completeExceptionally(e);
        } catch (InvocationTargetException e) {
            future.completeExceptionally(e.getTargetException());
        }
    }

}

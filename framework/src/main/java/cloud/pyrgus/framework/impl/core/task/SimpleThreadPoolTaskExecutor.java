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
import cloud.pyrgus.framework.core.service.Configurable;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.*;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public class SimpleThreadPoolTaskExecutor implements TaskExecutor, Configurable {

    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final ThreadLocal<Task> executingTask = new ThreadLocal<>();
    private List<TaskInterceptor> interceptors = null;

    /**
     * 提交一个任务. 可以通过 {@link Task#getFuture()} 提前取消任务 (请检查 {@link CompletableFuture} 的状态以避免异常).
     *
     * @param message  此次任务涉及的消息实例
     * @param consumer 此次任务使用的消息消费者
     * @param state    此次任务由外部共享的状态, 置空则由执行器自行维护状态.
     * @param mode     此次任务的执行模式
     * @return {@link Task} 实例
     */
    @Override
    public @NotNull Task submit(@NotNull Message message, @NotNull MessageConsumer consumer, @Nullable Map<String, Object> state, @NotNull Mode mode) {
        Task task = packTask(message, consumer, state);
        if (mode == Mode.Posting) {
            execute(task);
        } else {
            executorService.submit(() -> execute(task));
        }
        return task;
    }

    /**
     * 执行任务.
     *
     * @param task 需要执行的任务
     */
    @Override
    public void execute(@NotNull Task task) {
        executingTask.set(task);
        CompletableFuture<Object> future = task.getFuture();
        if (future.isDone() || future.isCancelled() || future.isCompletedExceptionally()) {
            return;
        }
        new TaskInterceptorChain(task, interceptors).next();
        executingTask.remove();
    }

    /**
     * 获取当前正在执行的任务.
     *
     * @return 存在正在执行的任务时将传回该任务, 否则为 {@link Option#none()}.
     */
    @Override
    public Option<Task> executingTask() {
        return Option.of(executingTask.get());
    }

    private Task packTask(@NotNull Message message, @NotNull MessageConsumer consumer, @Nullable Map<String, Object> state) {
        Map<String, Object> executingTaskState = Option.of(executingTask.get()).map(Task::getState).getOrElse(HashMap::new);
        state = Option.of(state).getOrElse(HashMap::new);
        if (!executingTaskState.isEmpty() && !state.isEmpty()) {
            executingTaskState.putAll(state);
        }
        return new SimpleTask(message, consumer, new HashMap<>(), executingTaskState, new CompletableFuture<>());
    }

    /**
     * 当准备将服务实例提供给需求者时将调用此方法以确保服务配置完毕.<br/>
     * 请注意, 此方法可能被调用多次.
     *
     * @param serviceRegistry  服务注册表
     * @param propertyProvider 属性提供器
     */
    @Override
    public void configure(@NotNull ServiceRegistry serviceRegistry, @NotNull PropertyProvider propertyProvider) {
        if (interceptors == null) {
            interceptors = serviceRegistry.loadServices(TaskInterceptor.class);
        }
    }
}

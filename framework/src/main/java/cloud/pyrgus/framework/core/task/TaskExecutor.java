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

package cloud.pyrgus.framework.core.task;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageConsumer;
import cloud.pyrgus.framework.core.service.Service;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 任务执行器, 负责在合适的时机执行任务.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public interface TaskExecutor extends Service {


    /**
     * 提交一个任务. 可以通过 {@link Task#getFuture()} 提前取消任务 (请检查 {@link CompletableFuture} 的状态以避免异常).
     *
     * @param message  此次任务涉及的消息实例
     * @param consumer 此次任务使用的消息消费者
     * @param state    此次任务由外部共享的状态, 为空则继续使用当前正在执行的任务的状态, 否则将由执行器进行合并, 合并策略由具体实现决定.
     * @param mode     此次任务的执行模式
     * @return 提交任务成功时将返回 {@link Task} 实例, 否则返回 {@code null} (例如队列已满等情况, 具体失败原因取决于执行器的实现方式).
     */
    @Nullable
    Task submit(@NotNull Message message, @NotNull MessageConsumer consumer, @Nullable Map<String, Object> state, @NotNull Mode mode);

    /**
     * 执行任务.
     *
     * @param task 需要执行的任务
     */
    void execute(@NotNull Task task);

    /**
     * 获取当前正在执行的任务.
     *
     * @return 存在正在执行的任务时将传回该任务, 否则为 {@link Option#none()}.
     */
    Option<Task> executingTask();

}

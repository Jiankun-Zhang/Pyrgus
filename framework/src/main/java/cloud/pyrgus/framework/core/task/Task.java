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
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 当准备将消息交由消费者消费时, 为了便于控制执行时机与所用线程, 将使用任务抽象将此次消息消费相关的内容进行封装.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public interface Task {

    /**
     * 获取此次任务需要处理的消息.
     *
     * @return 消息实例.
     */
    @NotNull
    Message getMessage();

    /**
     * 获取此次任务需要使用的消息消费者.
     *
     * @return 消息消费者实例.
     */
    @NotNull
    MessageConsumer getConsumer();

    /**
     * 此次任务的上下文信息.
     *
     * @return 上下文信息.
     */
    @NotNull
    Map<String, Object> getContext();

    /**
     * 此次任务的状态信息.<br/>
     * 若一个消息的消费过程中触发了更多的消息消费, 则此次消费过程与后续的消费过程将共享此状态实例.
     *
     * @return 状态信息.
     */
    @NotNull
    Map<String, Object> getState();

    /**
     * 此次任务的 {@link CompletableFuture} 实例, 用于提前结束消费消费过程或将最终处理结果传递至源头.
     *
     * @return {@link CompletableFuture} 实例.
     */
    @NotNull
    CompletableFuture<Object> getFuture();

}

/*
 * Copyright (c) 2022. Zhang Jiankun
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
 *
 */

package cloud.pyrgus.framework.internal.simple.core.task;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageFilter;
import cloud.pyrgus.framework.core.message.MessageHandler;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageFilteringFailedException;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageHandlerNotFoundException;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageInboxUndefinedException;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class SimpleUnitOfWork implements UnitOfWork, SimpleImplementation {

    private final ThreadLocal<Task> previousTask = new ThreadLocal<>();

    @Override
    public CompletableFuture<Serializable> process(Message message, TaskExecutor.Mode executionMode) {
        ServiceRegistry serviceRegistry = Pyrgus.getServiceRegistry();
        MessageRouter messageRouter = serviceRegistry.loadServiceOrThrown(MessageRouter.class);
        CompletableFuture<Serializable> future = new CompletableFuture<>();
        List<MessageFilter> messageFilters = serviceRegistry.loadAllServices(MessageFilter.class);
        for (int i = 0, filtersNum = messageFilters.size(); i < filtersNum; i++) {
            MessageFilter messageFilter = messageFilters.get(i);
            try {
                if (!messageFilter.doFilter(message)) {
                    future.completeExceptionally(new MessageFilteringFailedException(i, messageFilter, message));
                    return future;
                }
            } catch (Exception e) {
                future.completeExceptionally(new MessageFilteringFailedException(i, messageFilter, message, e));
                return future;
            }
        }
        Option<MessageHandler> handler = Option.none();
        try {
            handler = messageRouter.match(message);
        } catch (MessageInboxUndefinedException e) {
            future.completeExceptionally(e);
        }
        if (handler.isEmpty()) {
            future.completeExceptionally(new MessageHandlerNotFoundException(message));
            return future;
        }
        SimpleTask task = new SimpleTask(previousTask.get(), message, handler.get(), new HashMap<>(), future);
        TaskExecutor taskExecutor = serviceRegistry.loadServiceOrThrown(TaskExecutor.class);
        taskExecutor.submit(task, executionMode, previousTask::set);
        return task.getFuture();
    }

}

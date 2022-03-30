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
import cloud.pyrgus.framework.core.message.MessageInterceptor;
import cloud.pyrgus.framework.core.message.MessageInterceptorChain;
import cloud.pyrgus.framework.core.message.MessageInterceptorChainFactory;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.cqrs.ActionExecutionException;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
@Slf4j
public class SimpleThreadPoolTaskExecutor implements TaskExecutor, SimpleImplementation {

    private final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);
    private final ExecutorService ioExecutor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() + 1);

    private final ThreadLocal<Task> currentTask = new ThreadLocal<>();

    @Override
    public Option<Task> currentTask() {
        return Option.of(currentTask.get());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void submit(Task task, Mode executionMode, Consumer<Task> onExecute) {
        CompletableFuture<Serializable> future = task.getFuture();
        ServiceRegistry serviceRegistry = Pyrgus.getServiceRegistry();
        List<MessageInterceptor> messageInterceptors = serviceRegistry.loadAllServices(MessageInterceptor.class);

        MessageInterceptorChain chain = serviceRegistry.loadServiceOrThrown(MessageInterceptorChainFactory.class).newChain(messageInterceptors, task.getMessageHandler(), task.getState());
        Runnable runnable = () -> {
            try {
                log.debug("task {} running on {}", task, executionMode);
                currentTask.set(task);
                onExecute.accept(task);
                Object response = chain.next(task.getMessage());
                if (response instanceof CompletableFuture) {
                    CompletableFuture<Serializable> completableFuture = (CompletableFuture<Serializable>) response;
                    completableFuture.handle((BiFunction<Serializable, Throwable, Void>) (serializable, throwable) -> {
                        if (throwable == null) {
                            future.complete(serializable);
                        } else {
                            future.completeExceptionally(throwable instanceof ActionExecutionException ? throwable.getCause() : throwable);
                        }
                        return null;
                    });
                    completableFuture.thenAccept(future::complete);
                } else if (response instanceof Serializable) {
                    future.complete((Serializable) response);
                } else {
                    throw new UnsupportedOperationException();
                }
            } catch (ActionExecutionException e) {
                future.completeExceptionally(e.getCause());
                log.error("task {} running failed: {}", task, e.getMessage());
            } catch (Exception e) {
                future.completeExceptionally(e);
                log.error("task {} running failed: {}", task, e.getMessage());
            } finally {
                currentTask.remove();
                log.debug("task {} done.", task);
            }
        };
        switch (executionMode) {
            case POSTING:
                runnable.run();
                break;
            case BACKGROUND:
                backgroundExecutor.execute(runnable);
                break;
            case IO:
                ioExecutor.execute(runnable);
        }
    }

}

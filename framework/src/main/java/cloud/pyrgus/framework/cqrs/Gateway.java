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

package cloud.pyrgus.framework.cqrs;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageFactory;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import cloud.pyrgus.framework.error.Error;
import io.vavr.control.Either;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/30
 */
public class Gateway {

    @SuppressWarnings("unchecked")
    public static <T> T applyNow(Action action, Map<String, Serializable> headers) {
        try {
            CompletableFuture<T> future = applyAction(action, headers, TaskExecutor.Mode.POSTING);
            return (T) extractSerializable(future.get(), false);
        } catch (ExecutionException e) {
            throw new ActionExecutionException(e.getCause());
        } catch (InterruptedException e) {
            throw new ActionExecutionException(e);
        }
    }

    public static <T> T applyAndWait(Action action, Map<String, Serializable> headers, long timeout, TimeUnit timeUnit) throws TimeoutException {
        return applyAndWait(action, headers, timeout, timeUnit, TaskExecutor.Mode.BACKGROUND);
    }

    @SuppressWarnings("unchecked")
    public static <T> T applyAndWait(Action action, Map<String, Serializable> headers, long timeout, TimeUnit timeUnit, TaskExecutor.Mode mode) throws TimeoutException {
        try {
            CompletableFuture<T> future = applyAction(action, headers, mode);
            return (T) extractSerializable(future.get(timeout, timeUnit), false);
        } catch (InterruptedException e) {
            throw new ActionExecutionException(e);
        } catch (ExecutionException e) {
            throw new ActionExecutionException(e.getCause());
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Either<Error, T> applyEitherNow(Action action, Map<String, Serializable> headers) {
        try {
            CompletableFuture<Object> future = applyAction(action, headers, TaskExecutor.Mode.POSTING);
            return (Either<Error, T>) extractSerializable(future.get(), true);
        } catch (ExecutionException e) {
            throw new ActionExecutionException(e.getCause());
        } catch (InterruptedException e) {
            throw new ActionExecutionException(e);
        }
    }

    public static <T> Either<Error, T> applyAndWaitEither(Action action, Map<String, Serializable> headers, long timeout, TimeUnit timeUnit) throws TimeoutException {
        return applyAndWaitEither(action, headers, timeout, timeUnit, TaskExecutor.Mode.BACKGROUND);
    }

    @SuppressWarnings("unchecked")
    public static <T> Either<Error, T> applyAndWaitEither(Action action, Map<String, Serializable> headers, long timeout, TimeUnit timeUnit, TaskExecutor.Mode mode) throws TimeoutException {
        try {
            CompletableFuture<Serializable> future = applyAction(action, headers, mode);
            return (Either<Error, T>) extractSerializable(future.get(timeout, timeUnit), true);
        } catch (ExecutionException e) {
            throw new ActionExecutionException(e.getCause());
        } catch (InterruptedException e) {
            throw new ActionExecutionException(e);
        }
    }

    public static <T> CompletableFuture<T> applyAsync(Action action, Map<String, Serializable> headers) {
        return applyAction(action, headers, TaskExecutor.Mode.BACKGROUND);
    }

    @SuppressWarnings("unchecked")
    public static <T> CompletableFuture<T> applyAction(Action action, Map<String, Serializable> headers, TaskExecutor.Mode executionMode) {
        ServiceRegistry serviceRegistry = Pyrgus.getServiceRegistry();
        MessageFactory factory = serviceRegistry.loadServiceOrThrown(MessageFactory.class);
        Message message = factory.packMessage(action, headers);
        return (CompletableFuture<T>) serviceRegistry.loadServiceOrThrown(UnitOfWork.class).process(message, executionMode);
    }

    public static Map<String, Serializable> mergeAllHeaders(Map<String, Serializable>[] headers) {
        Map<String, Serializable> postingHeaders = new HashMap<>();
        for (Map<String, Serializable> header : headers) {
            postingHeaders.putAll(header);
        }
        return postingHeaders;
    }

    @SuppressWarnings("unchecked")
    private static Object extractSerializable(Object object, boolean eitherMode) throws InterruptedException, ExecutionException {
        if (object instanceof Either) {
            Either<Error, Object> either = (Either<Error, Object>) object;
            if (eitherMode) {
                return either;
            }
            if (either.isLeft()) {
                throw new ActionExecutionException(either.getLeft());
            } else {
                return either.get();
            }
        } else if (eitherMode) {
            return Either.right(object);
        } else {
            return object;
        }
    }

}

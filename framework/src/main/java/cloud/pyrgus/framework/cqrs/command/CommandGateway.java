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

package cloud.pyrgus.framework.cqrs.command;

import cloud.pyrgus.framework.cqrs.Gateway;
import cloud.pyrgus.framework.error.Error;
import io.vavr.control.Either;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
public class CommandGateway {

    @SafeVarargs
    public static <T> T send(Command command, Map<String, Serializable>... headers) {
        return Gateway.applyNow(command, Gateway.mergeAllHeaders(headers));
    }

    @SafeVarargs
    public static <T> Either<Error, T> sendEither(Command command, Map<String, Serializable>... headers) {
        return Gateway.applyEitherNow(command, Gateway.mergeAllHeaders(headers));
    }

    @SafeVarargs
    public static <T> T sendAndWait(Command command, long timeout, TimeUnit timeUnit, Map<String, Serializable>... headers) throws TimeoutException {
        return Gateway.applyAndWait(command, Gateway.mergeAllHeaders(headers), timeout, timeUnit);
    }

    @SafeVarargs
    public static <T> Either<Error, T> sendAndWaitEither(Command command, long timeout, TimeUnit timeUnit, Map<String, Serializable>... headers) throws TimeoutException {
        return Gateway.applyAndWaitEither(command, Gateway.mergeAllHeaders(headers), timeout, timeUnit);
    }

    @SafeVarargs
    public static <T> CompletableFuture<T> sendAsync(Command command, Map<String, Serializable>... headers) {
        return Gateway.applyAsync(command, Gateway.mergeAllHeaders(headers));
    }

}

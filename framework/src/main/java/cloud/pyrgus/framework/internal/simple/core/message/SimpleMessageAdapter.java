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

package cloud.pyrgus.framework.internal.simple.core.message;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageAdapter;
import cloud.pyrgus.framework.core.message.MessageFactory;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.TaskExecutor;
import cloud.pyrgus.framework.core.task.UnitOfWork;
import io.vavr.control.Option;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class SimpleMessageAdapter implements MessageAdapter {

    public static final String NAME = "pyrgus.simple";

    public SimpleMessageAdapter() {
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Serializable, R extends Serializable> CompletableFuture<R> read(T input, Map<String, Serializable> headers) {
        ServiceRegistry serviceRegistry = Pyrgus.getServiceRegistry();
        MessageFactory messageFactory = serviceRegistry.loadServiceOrThrown(MessageFactory.class);
        UnitOfWork unitOfWork = serviceRegistry.loadServiceOrThrown(UnitOfWork.class);
        return (CompletableFuture<R>) unitOfWork.process(messageFactory.packMessage(input, Option.of(headers).getOrElse(new HashMap<>())), TaskExecutor.Mode.POSTING);
    }

    @Override
    public <R extends Serializable> CompletableFuture<R> write(Message message) {
        throw new UnsupportedOperationException();
    }
}

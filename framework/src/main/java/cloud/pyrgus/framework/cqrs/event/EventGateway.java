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

package cloud.pyrgus.framework.cqrs.event;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.cqrs.Gateway;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageHandlerNotFoundException;
import lombok.extern.slf4j.Slf4j;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static io.vavr.API.*;
import static io.vavr.Predicates.instanceOf;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/30
 */
@Slf4j
public class EventGateway {

    public static void publish(Event event) {
        publish(event, new HashMap<>());
    }

    public static void publish(Event event, Map<String, Serializable> headers) {
        Pyrgus.getServiceRegistry().loadServiceOrThrown(EventStore.class).storeEvent(event);
        try {
            Match(event)
                    .of(
                            Case($(instanceOf(DomainEvent.class)), o -> run(() -> Gateway.applyNow(event, headers))),
                            Case($(instanceOf(ApplicationEvent.class)), o -> run(() -> Gateway.applyAsync(event, headers)))
                    );
        } catch (Exception exception) {
            log.warn("event type [ {} ] doesn't have handler.", event.getClass().getName());
        }
    }

}

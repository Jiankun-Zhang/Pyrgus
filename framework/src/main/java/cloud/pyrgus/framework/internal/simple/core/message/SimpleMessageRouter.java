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

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageHandler;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import cloud.pyrgus.framework.internal.simple.core.message.exception.MessageInboxUndefinedException;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
@Slf4j
public class SimpleMessageRouter implements MessageRouter, SimpleImplementation {

    public static final String MSG_HEADER_INBOX = "pyrgus.simple.inbox";
    private final Map<String, MessageHandler> handlers = new HashMap<>();

    public SimpleMessageRouter addHandler(String name, MessageHandler handler) {
        handlers.put(name, handler);
        return this;
    }

    @Override
    public Option<MessageHandler> match(Message message) {
        String inbox = (String) message.getHeaders().get(MSG_HEADER_INBOX);
        if (inbox == null) {
            throw new MessageInboxUndefinedException(message);
        }
        Option<MessageHandler> handlerOption = Option.of(handlers.get(inbox));
        log.debug("message inbox: {} | match handler: {}", inbox, handlerOption.map(messageHandler -> messageHandler.getClass().getName()).getOrNull());
        return handlerOption;
    }
}

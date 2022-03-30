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
import cloud.pyrgus.framework.core.message.MessageInterceptor;
import cloud.pyrgus.framework.core.message.MessageInterceptorChain;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class SimpleMessageInterceptorChain implements MessageInterceptorChain {

    private static final String STATE_KEY_OFFSET = "pyrgus.interceptor.offset";
    private final List<MessageInterceptor> messageInterceptors;
    @Getter
    private final MessageHandler messageHandler;
    @Getter
    private final Map<String, Object> state;

    public SimpleMessageInterceptorChain(List<MessageInterceptor> messageInterceptors, MessageHandler messageHandler, Map<String, Object> state) {
        this.messageInterceptors = messageInterceptors;
        this.messageHandler = messageHandler;
        this.state = state;
    }

    @Override
    public Object next(Message message) {
        if (messageInterceptors.isEmpty()) {
            return messageHandler.handle(message, state);
        }
        int offset = (int) state.getOrDefault(STATE_KEY_OFFSET, -1) + 1;
        if (offset == messageInterceptors.size()) {
            return messageHandler.handle(message, state);
        }
        MessageInterceptor interceptor = messageInterceptors.get(offset);
        state.put(STATE_KEY_OFFSET, offset);
        return interceptor.intercept(message, messageHandler, this);
    }

}

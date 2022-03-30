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

package cloud.pyrgus.framework.internal.simple.core.message.exception;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageFilter;
import cloud.pyrgus.framework.core.task.TaskRejectionException;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/28
 */
public class MessageFilteringFailedException extends TaskRejectionException {

    private final int offset;
    private final MessageFilter messageFilter;
    private final Message message;

    public MessageFilteringFailedException(int offset, MessageFilter messageFilter, Message message) {
        this.offset = offset;
        this.messageFilter = messageFilter;
        this.message = message;
    }

    public MessageFilteringFailedException(int offset, MessageFilter messageFilter, Message message, Throwable cause) {
        super(cause);
        this.offset = offset;
        this.messageFilter = messageFilter;
        this.message = message;
    }

    @Override
    public String getMessage() {
        return String.format("filters[#%d]: ( %s ) | message: [ %s ]", offset, messageFilter.getClass().getName(), message);
    }
}

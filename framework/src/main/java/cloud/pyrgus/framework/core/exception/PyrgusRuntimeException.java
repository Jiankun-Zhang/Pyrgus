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

package cloud.pyrgus.framework.core.exception;

/**
 * pyrgus 运行时异常
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 0.1.0
 */
public class PyrgusRuntimeException extends RuntimeException {

    /**
     * pyrgus 运行时异常
     *
     * @param message 消息
     */
    public PyrgusRuntimeException(String message) {
        super(message);
    }

    /**
     * pyrgus 运行时异常
     *
     * @param message 消息
     * @param cause   原因
     */
    public PyrgusRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * pyrgus 运行时异常
     *
     * @param cause 原因
     */
    public PyrgusRuntimeException(Throwable cause) {
        super(cause);
    }
}
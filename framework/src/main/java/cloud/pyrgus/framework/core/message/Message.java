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

package cloud.pyrgus.framework.core.message;

import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * 消息抽象, 应确保此接口的实现类可以被合适的 {@link MessageEndpoint} 所(反)序列化.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @see <a href="https://www.enterpriseintegrationpatterns.com/patterns/messaging/Message.html">Message</a>
 * @since 2022/4/14
 */
public interface Message {

    /**
     * 获取消息标头信息.
     *
     * @return 返回此消息所携带的消息标头信息.
     */
    @NotNull
    Map<String, Object> getHeaders();

    /**
     * 获取消息载荷.
     *
     * @return 返回此消息所携带的消息载荷.
     */
    @NotNull
    Object getPayload();

}

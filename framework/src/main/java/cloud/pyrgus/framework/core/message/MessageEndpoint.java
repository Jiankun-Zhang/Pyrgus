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

import cloud.pyrgus.framework.core.service.Service;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 消息终端, 主要作为消息系统与外部应用的边界, 定义了如何收发消息.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @see <a href="https://www.enterpriseintegrationpatterns.com/patterns/messaging/MessageEndpoint.html">MessageEndpoint</a>
 * @since 2022/4/14
 */
public interface MessageEndpoint extends Service {

    /**
     * 获取此终端的名称.
     *
     * @return 返回此终端的名称
     */
    @NotNull
    String getName();

    /**
     * 使用此终端发送消息, 如发送 HTTP 请求、提交消息队列等.
     *
     * @param message 需要发送的消息实例.
     * @param <T>     期望返回的结果类型.
     * @return 返回一个 {@link CompletableFuture} 以标识何时发送成功, 如果接收端传回了响应则会作为 {@link CompletableFuture} 的内容返回.
     */
    @NotNull <T> CompletableFuture<T> send(@NotNull Message message);

    /**
     * 使用此终端接收内容并转换为 {@link Message } 后进行处理, 如接收 HTTP 请求、捕获消息队列等.
     *
     * @param payload 接收到的载荷.
     * @param headers 接收到的标头信息.
     * @param <T>     期望返回的结果类型。
     * @return 返回一个 {@link CompletableFuture} 以标识何时接收处理完毕, 如果应用提供了响应则会作为 {@link CompletableFuture} 的内容返回.
     */
    @NotNull <T> CompletableFuture<T> receive(@NotNull Object payload, @NotNull Map<String, Object> headers);

}

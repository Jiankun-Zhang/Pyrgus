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

package cloud.pyrgus.framework.impl.core.interceptor;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageConsumer;
import cloud.pyrgus.framework.core.message.consumer.LocalMethodMessageConsumer;
import cloud.pyrgus.framework.core.service.Configurable;
import cloud.pyrgus.framework.core.service.PropertyProvider;
import cloud.pyrgus.framework.core.service.ServiceRegistry;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.core.task.TaskInterceptor;
import cloud.pyrgus.framework.core.task.TaskInterceptorChain;
import cloud.pyrgus.framework.impl.core.argument_resolver.ArgumentResolver;
import io.vavr.control.Option;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

/**
 * 本地方法参数解析拦截器, 负责解析消息消费者上的参数列表.
 *
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/4/14
 */
public class LocalMethodArgumentResolveInterceptor implements TaskInterceptor, Configurable {

    public static final String CTX_KEY_ARGS = "pyrgus.consumer.local.action.arguments";

    private List<ArgumentResolver> resolvers = null;

    @Override
    public void intercept(Task task, TaskInterceptorChain chain) {
        MessageConsumer messageConsumer = task.getConsumer();
        if (!(messageConsumer instanceof LocalMethodMessageConsumer)) {
            chain.next();
            return;
        }
        LocalMethodMessageConsumer localMethodMessageConsumer = (LocalMethodMessageConsumer) messageConsumer;
        Message message = task.getMessage();
        Method method = localMethodMessageConsumer.matchMethod(message);
        Object[] arguments = new Object[method.getParameterCount()];
        Parameter[] parameters = method.getParameters();
        for (int i = 0, parametersLength = parameters.length; i < parametersLength; i++) {
            Parameter parameter = parameters[i];
            try {
                arguments[i] = resolvers.stream()
                        .map(argumentResolver -> argumentResolver.resolve(message, task.getState(), parameter))
                        .filter(Option::isDefined)
                        .findFirst()
                        .orElseGet(Option::none)
                        .getOrElseThrow(() -> new IllegalArgumentException(String.format("不受支持的参数类型: [ %s ]", parameter.getType().getName())));
            } catch (IllegalArgumentException e) {
                task.getFuture().completeExceptionally(e);
                return;
            }
        }
        task.getContext().put(CTX_KEY_ARGS, arguments);
        chain.next();
    }

    /**
     * 当准备将服务实例提供给需求者时将调用此方法以确保服务配置完毕.<br/>
     * 请注意, 此方法可能被调用多次.
     *
     * @param serviceRegistry  服务注册表
     * @param propertyProvider 属性提供器
     */
    @Override
    public void configure(@NotNull ServiceRegistry serviceRegistry, @NotNull PropertyProvider propertyProvider) {
        if (resolvers == null) {
            resolvers = serviceRegistry.loadServices(ArgumentResolver.class);
        }
    }
}

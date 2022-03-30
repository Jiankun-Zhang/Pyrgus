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

package cloud.pyrgus.framework.cqrs.interceptor.arguments;

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.MessageInterceptor;
import cloud.pyrgus.framework.core.message.MessageInterceptorChain;
import cloud.pyrgus.framework.core.service.CachedServiceInitiator;
import cloud.pyrgus.framework.cqrs.Action;
import cloud.pyrgus.framework.cqrs.ActionHandler;
import cloud.pyrgus.framework.cqrs.ActionMessage;
import cloud.pyrgus.framework.cqrs.LocalMethodActionHandler;
import io.vavr.control.Option;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
public class ArgumentResolveInterceptor implements ActionInterceptor {

    public static final String STATE_PAYLOAD_OFFSET = "pyrgus.cqrs.arguments.payload-offset";
    public static final String STATE_ARGUMENTS = "pyrgus.cqrs.arguments";
    private final List<ArgumentResolver> argumentResolvers;

    public ArgumentResolveInterceptor(List<ArgumentResolver> argumentResolvers) {
        this.argumentResolvers = argumentResolvers;
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Object intercept(ActionMessage actionMessage, ActionHandler actionHandler, MessageInterceptorChain chain) {
        if (!(actionHandler instanceof LocalMethodActionHandler)) {
            return chain.next(actionMessage);
        }
        LocalMethodActionHandler localMethodActionHandler = (LocalMethodActionHandler) actionHandler;
        Option<Method> methodOption = localMethodActionHandler.match(actionMessage);
        if (methodOption.isEmpty()) {
            return chain.next(actionMessage);
        }
        Map<String, Object> state = chain.getState();
        Method method = methodOption.get();
        Parameter[] parameters = method.getParameters();
        Object[] arguments = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            if (Action.class.isAssignableFrom(parameter.getType())) {
                arguments[i] = actionMessage.getPayload();
                state.put(STATE_PAYLOAD_OFFSET, i);
            } else {
                arguments[i] = resolve(actionHandler.getTargetObject(), method, parameter);
            }
        }
        state.put(STATE_ARGUMENTS, arguments);
        return chain.next(actionMessage);
    }

    private Object resolve(Object targetObject, Method method, Parameter parameter) {
        return argumentResolvers.stream()
                .map(argumentResolver -> argumentResolver.resolve(parameter, method, targetObject))
                .filter(Option::isDefined)
                .map(Option::get)
                .findFirst()
                .orElse(null);
    }

    public static class Initiator extends CachedServiceInitiator<MessageInterceptor> {
        @Override
        protected MessageInterceptor initiateServiceOnce() {
            return new ArgumentResolveInterceptor(Pyrgus.getServiceRegistry().loadAllServices(ArgumentResolver.class));
        }

        @Override
        public Class<MessageInterceptor> serviceInitiated() {
            return MessageInterceptor.class;
        }

        @Override
        public Class<? extends MessageInterceptor> implementationType() {
            return ArgumentResolveInterceptor.class;
        }
    }
}

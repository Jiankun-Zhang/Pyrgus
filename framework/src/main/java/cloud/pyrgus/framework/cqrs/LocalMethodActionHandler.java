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

package cloud.pyrgus.framework.cqrs;

import cloud.pyrgus.framework.cqrs.interceptor.arguments.ArgumentResolveInterceptor;
import io.vavr.control.Option;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
public abstract class LocalMethodActionHandler implements ActionHandler {

    protected final Map<Class<? extends Action>, Method> handlers = new HashMap<>();

    @Override
    public Object handle(ActionMessage message, Map<String, Object> state) {
        Method method = match(message).get();
        Object[] arguments = (Object[]) state.get(ArgumentResolveInterceptor.STATE_ARGUMENTS);
        int offset = (int) state.get(ArgumentResolveInterceptor.STATE_PAYLOAD_OFFSET);
        arguments[offset] = message.getPayload();
        state.remove(ArgumentResolveInterceptor.STATE_ARGUMENTS);
        state.remove(ArgumentResolveInterceptor.STATE_PAYLOAD_OFFSET);
        try {
            return method.invoke(getTargetObject(), arguments);
        } catch (IllegalAccessException ignore) {
            return null;
        } catch (InvocationTargetException e) {
            throw new ActionExecutionException(e.getTargetException());
        }
    }

    public Option<Method> match(ActionMessage actionMessage) {
        return Option.of(handlers.get(actionMessage.getPayload().getClass()));
    }

    public void appendMethodHandler(Class<? extends Action> actionType, Method method) {
        handlers.put(actionType, method);
    }
}

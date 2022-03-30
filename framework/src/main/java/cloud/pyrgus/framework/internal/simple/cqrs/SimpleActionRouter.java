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

package cloud.pyrgus.framework.internal.simple.cqrs;

import cloud.pyrgus.framework.core.message.Message;
import cloud.pyrgus.framework.core.message.MessageHandler;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.cqrs.Action;
import cloud.pyrgus.framework.cqrs.ActionHandler;
import cloud.pyrgus.framework.cqrs.ActionType;
import cloud.pyrgus.framework.cqrs.LocalMethodActionHandler;
import cloud.pyrgus.framework.cqrs.aggregate.Aggregate;
import cloud.pyrgus.framework.cqrs.aggregate.LocalAggregateMethodHandler;
import cloud.pyrgus.framework.cqrs.command.CommandHandler;
import cloud.pyrgus.framework.cqrs.domain.DomainService;
import cloud.pyrgus.framework.cqrs.domain.LocalDomainServiceActionHandler;
import cloud.pyrgus.framework.cqrs.event.EventHandler;
import cloud.pyrgus.framework.cqrs.query.QueryHandler;
import cloud.pyrgus.framework.internal.simple.SimpleImplementation;
import io.vavr.Tuple;
import io.vavr.Tuple2;
import io.vavr.control.Option;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import static io.vavr.API.*;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
@Slf4j
public class SimpleActionRouter implements MessageRouter, SimpleImplementation {

    private static final BiFunction<ActionType, Boolean, Predicate<ActionType>> predicates = (expectedActionType, bool) -> actionType -> expectedActionType.equals(actionType) && bool;
    private final Map<Class<? extends Action>, ActionHandler> actionHandlers = new HashMap<>();

    public void scanActionHandler(Class<? extends Aggregate> aggregateType) {
        LocalMethodActionHandler actionHandler = new LocalAggregateMethodHandler(aggregateType);
        scan(aggregateType, actionHandler);
    }

    public void scanActionHandler(Object targetObject) {
        if (!(targetObject instanceof DomainService)) {
            return;
        }
        DomainService domainService = (DomainService) targetObject;
        LocalMethodActionHandler actionHandler = new LocalDomainServiceActionHandler(domainService);
        scan(domainService.getClass(), actionHandler);
    }

    @SuppressWarnings("unchecked")
    private void scan(Class<?> clazz, LocalMethodActionHandler actionHandler) {
        log.debug("scanning action handlers on {} [ {} ]", Aggregate.class.isAssignableFrom(clazz) ? Aggregate.class.getSimpleName() : DomainService.class.getSimpleName(), clazz.getName());
        List<String> methods = new LinkedList<>();
        for (Method method : clazz.getMethods()) {
            Optional<Class<?>> actionOptional = Arrays.stream(method.getParameterTypes()).filter(Action.class::isAssignableFrom).findFirst();
            if (!actionOptional.isPresent()) {
                if (log.isDebugEnabled()) {
                    methods.add(String.format("\t> method: ( %s ) | skip", method.toGenericString()));
                }
                continue;
            }
            Class<? extends Action> actionType = (Class<? extends Action>) actionOptional.get();
            Runnable runnable = () -> {
                if (log.isDebugEnabled()) {
                    methods.add(String.format("\t> method: ( %s ) | registered for action [ %s ].", method.toGenericString(), actionType.getName()));
                }
                actionHandler.appendMethodHandler(actionType, method);
                actionHandlers.put(actionType, actionHandler);
            };
            Match(ActionType.parse(actionType))
                    .of(
                            Case($(predicates.apply(ActionType.Command, method.isAnnotationPresent(CommandHandler.class))), o -> run(runnable)),
                            Case($(predicates.apply(ActionType.Query, method.isAnnotationPresent(QueryHandler.class))), o -> run(runnable)),
                            Case($(predicates.apply(ActionType.DomainEvent, method.isAnnotationPresent(EventHandler.class))), o -> run(runnable)),
                            Case($(predicates.apply(ActionType.ApplicationEvent, method.isAnnotationPresent(EventHandler.class))), o -> run(runnable)),
                            Case($(), o -> {
                                throw new IllegalArgumentException(String.format("illegal actionType: %s", o.name()));
                            })
                    );
        }
        methods.forEach(log::debug);
    }

    @Override
    public Option<MessageHandler> match(Message message) {
        return Option.of(actionHandlers.get(message.getPayload().getClass()));
    }

}

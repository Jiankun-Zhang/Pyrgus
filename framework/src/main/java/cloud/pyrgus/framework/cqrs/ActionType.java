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

import java.util.function.Function;
import java.util.function.Predicate;

import static io.vavr.API.*;

public enum ActionType {
    Command, Query, DomainEvent, ApplicationEvent;

    final static Function<Class<? extends Action>, Predicate<Class<? extends Action>>> predicates = (expectedType -> expectedType::isAssignableFrom);

    public static ActionType parse(Action action) {
        return parse(action.getClass());
    }

    public static ActionType parse(Class<? extends Action> actionClass) {
        return Match(actionClass)
                .of(
                        Case($(predicates.apply(cloud.pyrgus.framework.cqrs.command.Command.class)), ActionType.Command),
                        Case($(predicates.apply(cloud.pyrgus.framework.cqrs.query.Query.class)), ActionType.Query),
                        Case($(predicates.apply(cloud.pyrgus.framework.cqrs.event.DomainEvent.class)), ActionType.DomainEvent),
                        Case($(predicates.apply(cloud.pyrgus.framework.cqrs.event.ApplicationEvent.class)), ActionType.ApplicationEvent),
                        Case($(), o -> {
                            throw new IllegalArgumentException(String.format("Unknown ActionType : [ %s ]", actionClass.getName()));
                        })
                );
    }

}
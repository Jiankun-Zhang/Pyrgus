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

import cloud.pyrgus.framework.Pyrgus;
import cloud.pyrgus.framework.core.message.MessageRouter;
import cloud.pyrgus.framework.core.task.Task;
import cloud.pyrgus.framework.cqrs.command.Command;
import cloud.pyrgus.framework.cqrs.command.CommandGateway;
import cloud.pyrgus.framework.cqrs.command.CommandHandler;
import cloud.pyrgus.framework.cqrs.domain.DomainService;
import cloud.pyrgus.framework.cqrs.interceptor.arguments.state.State;
import cloud.pyrgus.framework.cqrs.query.Query;
import cloud.pyrgus.framework.cqrs.query.QueryGateway;
import cloud.pyrgus.framework.cqrs.query.QueryHandler;
import cloud.pyrgus.framework.error.Error;
import cloud.pyrgus.framework.internal.simple.cqrs.SimpleActionRouter;
import io.vavr.control.Either;
import lombok.SneakyThrows;
import lombok.Value;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/29
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CQRSServiceRegistryTest implements DomainService {

    static {
        System.setProperty("org.slf4j.simpleLogger.defaultLogLevel", "trace");
    }

    @BeforeAll
    void beforeAll() {
        Pyrgus.builder()
                .withDefaultServiceRegistries()
                .build();

        ((SimpleActionRouter) Pyrgus.getServiceRegistry()
                .loadServiceOrThrown(MessageRouter.class))
                .scanActionHandler(this);
    }

    @SneakyThrows
    @Test
    void should_get_response_from_foo_command() {
        String response = CommandGateway.send(new FooCommand("Hello world!"));
        assertThat(response)
                .startsWith("Hi there!")
                .endsWith(Thread.currentThread().getName());
    }

    @Test
    void should_get_response_from_foo_command_async() throws ExecutionException, InterruptedException, TimeoutException {
        CompletableFuture<String> future = CommandGateway.sendAsync(new FooCommand("Hello world!"));
        assertThat(future.get(1, TimeUnit.MINUTES))
                .doesNotEndWith(Thread.currentThread().getName());
    }

    @Test
    void should_get_three_integers_from_foo_query() {
        List<Integer> response = QueryGateway.query(new FooQuery(3));
        assertThat(response).hasSize(3)
                .containsExactly(0, 1, 2);
    }

    @Test
    void should_throw_when_payload_is_null() {
        assertThatThrownBy(() -> CommandGateway.send(null))
                .isInstanceOf(NullPointerException.class);
    }

    @SneakyThrows
    @Test
    void should_get_first_command_name_from_second_query() {
        String response = CommandGateway.send(new FirstTaskCommand());
        assertThat(response).isEqualTo(FirstTaskCommand.class.getSimpleName());
    }

    @SneakyThrows
    @CommandHandler
    public String handle(FirstTaskCommand command, @State Map<String, Object> state) {
        state.put("from", FirstTaskCommand.class.getSimpleName());
        Either<Error, String> query = QueryGateway.queryEither(new SecondTaskQuery());
        CompletableFuture<List<Integer>> queryAsync = QueryGateway.queryAsync(new FooQuery(3));
        if (queryAsync.get().size() == 3) {
            return query.get();
        } else {
            return "";
        }
    }

    @QueryHandler
    public CompletableFuture<String> handle(SecondTaskQuery query, Task task) {
        return CompletableFuture.completedFuture((String) task.getPrevTask().getState().get("from"));
    }

    @QueryHandler
    public List<Integer> handle(FooQuery query) {
        List<Integer> integers = new ArrayList<>(query.length);
        for (int i = 0; i < query.length; i++) {
            integers.add(i);
        }
        return integers;
    }

    @CommandHandler
    public String handle(FooCommand command) {
        return "Hi there! reply from " + Thread.currentThread().getName();
    }

    @Value
    static class FooCommand implements Command {
        String message;
    }

    @Value
    static class FooQuery implements Query {
        int length;
    }

    @Value
    static class FirstTaskCommand implements Command {
    }

    @Value
    static class SecondTaskQuery implements Query {
    }

}

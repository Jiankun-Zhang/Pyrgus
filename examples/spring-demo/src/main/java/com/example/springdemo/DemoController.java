package com.example.springdemo;

import cloud.pyrgus.framework.cqrs.Gateway;
import cloud.pyrgus.framework.cqrs.command.CommandGateway;
import cloud.pyrgus.framework.cqrs.command.CommandHandler;
import cloud.pyrgus.framework.cqrs.domain.DomainService;
import cloud.pyrgus.framework.cqrs.event.EventGateway;
import cloud.pyrgus.framework.cqrs.query.Query;
import cloud.pyrgus.framework.cqrs.query.QueryGateway;
import cloud.pyrgus.framework.cqrs.query.QueryHandler;
import cloud.pyrgus.framework.error.ClientError;
import cloud.pyrgus.framework.error.Error;
import cloud.pyrgus.framework.error.ServerFault;
import cloud.pyrgus.framework.exception.ErrorException;
import com.example.springdemo.actioin.BarQuery;
import com.example.springdemo.actioin.FooBarEvent;
import com.example.springdemo.actioin.FooCommand;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.control.Either;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@RestController
public class DemoController implements DomainService {

    @GetMapping
    public int test(String name) {
        return CommandGateway.send(new FooCommand(name));
    }

    @ExceptionHandler
    public String handle(ErrorException exception) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(exception.getError());
    }

    @CommandHandler
    public Either<Error, Integer> handle(FooCommand command) {
        if (!StringUtils.hasText(command.getName())) {
            return Either.left(Error.clientError(ClientError.Codes.InvalidParameter, "name"));
        }
        return Either.right(QueryGateway.query(new BarQuery(command.getName(), ThreadLocalRandom.current().nextBoolean())));
    }

    @QueryHandler
    public int handle(BarQuery query) {
        if (query.isFiltered()) {
            throw new ErrorException(Error.serverFault(ServerFault.Codes.InternalError, "bad luck."));
        }
        EventGateway.publish(new FooBarEvent(query.getName()));
        return query.getName().length();
    }

}

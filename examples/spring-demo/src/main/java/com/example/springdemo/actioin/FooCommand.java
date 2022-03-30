package com.example.springdemo.actioin;

import cloud.pyrgus.framework.cqrs.command.Command;
import lombok.Value;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Value
public class FooCommand implements Command {

    String name;

}

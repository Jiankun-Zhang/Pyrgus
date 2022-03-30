package com.example.springdemo.actioin;

import cloud.pyrgus.framework.cqrs.query.Query;
import lombok.Value;

/**
 * @author <a href="mailto:git@krun.dev">Jiankun-Zhang</a>
 * @since 2022/3/31
 */
@Value
public class BarQuery implements Query {

    String name;

    boolean filtered;

}

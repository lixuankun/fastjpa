package com.efeichong.jpa;

import java.io.Serializable;
import java.util.function.Function;

/**
 * @author lxk
 * @date 2020/9/25
 * @description 支持序列化的 Function
 */
@FunctionalInterface
public interface SFunction<T, R> extends Function<T, R>, Serializable {
}

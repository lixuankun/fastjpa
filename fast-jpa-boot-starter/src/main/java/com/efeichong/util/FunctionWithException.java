package com.efeichong.util;

import com.efeichong.exception.JpaException;

import java.util.function.Function;

/**
 * @author lxk
 * @date 2020/11/5
 * @description 将异常类型转换为JpaException
 */
@FunctionalInterface
public interface FunctionWithException<T, R, E extends Exception> {

    static <T, R, E extends Exception> Function<T, R> wrapper(FunctionWithException<T, R, E> fe) {
        return arg -> {
            try {
                return fe.accept(arg);
            } catch (Exception e) {
                throw new JpaException(e);
            }

        };
    }

    R accept(T t) throws E;
}

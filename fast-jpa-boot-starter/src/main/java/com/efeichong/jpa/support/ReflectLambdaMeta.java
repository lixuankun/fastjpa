package com.efeichong.jpa.support;

import com.efeichong.cache.EntityCache;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;


/**
 * @author lxk
 * @date 2020/9/25
 * @description
 */
@Slf4j
public class ReflectLambdaMeta implements LambdaMeta {
    private static final Field FIELD_CAPTURING_CLASS;

    static {
        Field fieldCapturingClass;
        try {
            Class<SerializedLambda> aClass = SerializedLambda.class;
            fieldCapturingClass = EntityCache.setAccessible(aClass.getDeclaredField("capturingClass"));
        } catch (Throwable e) {
            log.warn(e.getMessage());
            fieldCapturingClass = null;
        }
        FIELD_CAPTURING_CLASS = fieldCapturingClass;
    }

    private final SerializedLambda lambda;

    public ReflectLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }


}

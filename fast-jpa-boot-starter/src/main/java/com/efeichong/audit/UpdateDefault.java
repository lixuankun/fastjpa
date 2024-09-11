package com.efeichong.audit;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lxk
 * @date 2020/12/07
 * @description 默认值 {@link #value()}和{@link #val()}二选一,如果两个都填则以{@link #val()}为准
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface UpdateDefault {
    /**
     * 默认值
     *
     * @return
     */
    String value() default "";

    /**
     * 默认值
     *
     * @return
     */
    Class<? extends DefaultValueProvider> val() default DefaultValueProvider.class;

}

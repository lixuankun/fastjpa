package com.efeichong.mapping;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lxk
 * @date 2020/12/07
 * @description vo和po映射注解，此注解加在vo层
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Mapping {
    /**
     * 映射po的字段
     *
     * @return
     */
    String poProperty();

    /**
     * 指定在哪种时候生效
     * UseMode.ALL 都生效(默认)
     * UseMode.TO_PO vo转po时生效
     * UseMode.TO_VO po转vo时生效
     * UseMode.QUERY 查询时生效
     *
     * @return
     */
    UseMode[] useMode() default UseMode.ALL;

    /**
     * 初步实现 未完善
     * 1.符号仅支持 ">", "<", "=", ">=", "<=", "!="
     * 2.只支持and连接
     * 3.语法 连表字段[条件1 and 提交2]
     *
     * 例如: @Mapping(poProperty = "clazz", onCondition = "clazz[name=1 and age>10]")
     *
     * @return
     */
    String onCondition() default "";
}

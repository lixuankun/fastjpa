package com.efeichong.logicDelete;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lxk
 * @date 2020/01/29
 * @description 开启逻辑删除, 字段类型必须是long/Long或者String类型
 * 1.字段值0(未删除),uuid生成的值(已删除)
 * 2.如果逻辑删除的表有唯一约束请将唯一约束的条件拼上逻辑删除字段
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogicDelete {
    /**
     * 逻辑删除字段名
     */
    String column();
}

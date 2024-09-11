package com.efeichong.multiTenant;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lxk
 * @date 2020/12/07
 * @description 开启多租户, 自动拼接查询条件，租户id从{@link EnableMultiTenant#column()}中取
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnableMultiTenant {
    /**
     * 对象字段，根据该字段实现多租户，查询条件  column=#{tenantId} or column is null
     *
     * @return
     */
    String column();
}

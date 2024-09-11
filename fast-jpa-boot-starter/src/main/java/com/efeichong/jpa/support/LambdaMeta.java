package com.efeichong.jpa.support;

/**
 * @author lxk
 * @date 2020/9/25
 * @description Lambda 信息
 */
public interface LambdaMeta {

    /**
     * 获取 lambda 表达式实现方法的名称
     *
     * @return lambda 表达式对应的实现方法名称
     */
    String getImplMethodName();

}

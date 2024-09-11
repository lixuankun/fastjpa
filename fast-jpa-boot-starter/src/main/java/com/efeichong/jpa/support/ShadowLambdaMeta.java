package com.efeichong.jpa.support;

/**
 * @author lxk
 * @date 2020/9/25
 * @description 基于 {@link SerializedLambda} 创建的元信息
 */
public class ShadowLambdaMeta implements LambdaMeta {
    private final SerializedLambda lambda;

    public ShadowLambdaMeta(SerializedLambda lambda) {
        this.lambda = lambda;
    }

    @Override
    public String getImplMethodName() {
        return lambda.getImplMethodName();
    }


}

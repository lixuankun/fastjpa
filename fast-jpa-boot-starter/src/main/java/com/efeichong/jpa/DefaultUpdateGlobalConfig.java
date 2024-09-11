package com.efeichong.jpa;

/**
 * @author lxk
 * @date 2022/1/27
 * @description 默认更新配置  所有字段都进行更新,所有字段都不能更新为空
 */
public class DefaultUpdateGlobalConfig implements UpdateGlobalConfig {

    /**
     * 忽略更新的字段
     *
     * @return
     */

    @Override
    public String[] ignoreUpdateProps() {
        return new String[]{UpdateGlobalConfig.NONE};
    }

    /**
     * 可以更新为空的字段
     *
     * @return
     */
    @Override
    public String[] canBeNullProps() {
        return new String[]{UpdateGlobalConfig.NONE};
    }
}

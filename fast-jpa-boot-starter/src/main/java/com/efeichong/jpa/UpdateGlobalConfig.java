package com.efeichong.jpa;

/**
 * @author lxk
 * @date 2022/1/27
 * @description 配置忽略更新的字段和可以更新为空的字段
 */
public interface UpdateGlobalConfig {
    /**
     * 默认更新配置 所有字段都进行更新,所有字段都不能更新为空
     */
    public static final UpdateGlobalConfig DEFAULT_UPDATE_GLOBAL_CONFIG = new DefaultUpdateGlobalConfig();

    /**
     * 所有字段
     */
    public static final String ALL = "all";

    /**
     * 没有字段
     */
    public static final String NONE = "none";

    /**
     * 忽略更新的字段
     *
     * @return
     */
    String[] ignoreUpdateProps();

    /**
     * 可以更新为空的字段
     *
     * @return
     */
    String[] canBeNullProps();
}

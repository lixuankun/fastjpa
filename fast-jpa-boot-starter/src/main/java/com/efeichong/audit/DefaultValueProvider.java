package com.efeichong.audit;

/**
 * @author lxk
 * @date 2020/12/18
 * @description 字段默认值
 */
public interface DefaultValueProvider<T> {
    /**
     * 获取默认值
     *
     * @return
     */
    T getValue();
}

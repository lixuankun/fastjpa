package com.efeichong.mapping;

/**
 * @author lxk
 * @date 2020/12/7
 * @description 指定在转po或者转vo时生效
 */
public enum UseMode {
    /**
     * po，vo,query都生效
     */
    ALL,
    /**
     * 当查询条件时有效
     */
    QUERY,
    /**
     * 转po时生效
     */
    TO_PO,
    /**
     * 转vo时生效
     */
    TO_VO;
}

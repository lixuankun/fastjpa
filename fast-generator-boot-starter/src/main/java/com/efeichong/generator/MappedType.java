package com.efeichong.generator;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author lxk
 * @date 2020/10/20
 * @description 关系映射类型
 */
@Getter
@AllArgsConstructor
public enum MappedType {
    /**
     * 多对多
     */
    MANY_TO_MANY(1, "@ManyToMany"),
    /**
     * 多对一
     */
    MANY_TO_ONE(2, "@ManyToOne"),
    /**
     * 一对多
     */
    ONE_TO_MANY(3, "@OneToMany"),
    /**
     * 一对一
     */
    ONE_TO_ONE(4, "@OneToOne");

    private int code;

    private String name;
}

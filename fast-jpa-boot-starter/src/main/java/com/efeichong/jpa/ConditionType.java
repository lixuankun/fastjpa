package com.efeichong.jpa;

import lombok.Getter;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 条件类型
 */
@Getter
public enum ConditionType {
    EQ(0, "="),
    NO_EQ(1, "<>"),
    GT(2, ">"),
    LT(3, "<"),
    GT_OR_EQ(4, ">="),
    LT_OR_EQ(5, "<="),
    LIKE(6, "like"),
    NO_LIKE(7, "not like"),
    IN(8, "in"),
    NO_IN(9, "not in"),
    IS_NULL(10, "is null"),
    IS_NO_NULL(11, "is not null"),
    BETWEEN(12, "between"),
    NO_BETWEEN(13, "not between");

    private Integer code;
    private String value;

    ConditionType(Integer code, String value) {
        this.code = code;
        this.value = value;
    }
}

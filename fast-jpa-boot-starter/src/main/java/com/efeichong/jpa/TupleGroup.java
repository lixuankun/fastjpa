package com.efeichong.jpa;

import lombok.Data;

/**
 * @author lxk
 * @date 2022/9/30
 * @description
 */
@Data
public class TupleGroup {
    private String group;
    private Class<?> type;

    public TupleGroup(String group, Class<?> type) {
        this.group = group;
        this.type = type;
    }
}

package com.efeichong.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lxk
 * @date 2020/10/22
 * @description 外键关联的表及字段信息
 */
@Setter
@Getter
public class ReferenceColumn {
    /**
     * 被关联的表
     */
    private String referencedTableName;
    /**
     * 被关联的表的字段
     */
    private String referencedColumnName;
    /**
     * 存在外键的表
     */
    private String tableName;
    /**
     * 存在外键的表的字段
     */
    private String columnName;
}

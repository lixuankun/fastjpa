package com.efeichong.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lxk
 * @date 2020/11/16
 * @description 索引字段
 */
@Setter
@Getter
public class IndexColumn {
    /**
     * 索引的字段名
     */
    private String columnName;
    /**
     * 是否唯一约束 0否 1是
     */
    private Integer hasUnique = 0;
    /**
     * 索引名
     */
    private String indexName;
}

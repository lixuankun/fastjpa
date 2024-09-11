package com.efeichong.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lxk
 * @date 2020/10/20
 * @description 数据库表的字段信息
 */
@Setter
@Getter
public class TableColumn {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 字段名
     */
    private String columnName;
    /**
     * 数据库字段类型
     */
    private String dataType;
    /**
     * java字段类型
     */
    private String fieldType;
    /**
     * java字段名
     */
    private String fieldName;
    /**
     * 字段注释
     */
    private String columnComment;
    /**
     * 是否必循（是否可为空）
     */
    private Integer hasRequired = 0;
    /**
     * 是否为主键 0否 1是
     */
    private Integer hasPrimaryKey = 0;
    /**
     * 是否为自增长 0否 1是
     */
    private Integer hasIncrement = 0;
    /**
     * 是否为唯一约束 0否 1是
     */
    private Integer hasUnique = 0;
    /**
     * 是否存表以此字段为外键 0否 1是
     */
    private Integer hasReference = 0;
    /**
     * 字段映射
     *
     * @see MappedType
     */
    private String mappedType;
    /**
     * 是否需要序列化 0否 1是
     */
    private Integer hasNeedSerialize = 1;
    /**
     * 字符串长度
     */
    private Integer varcharColumnLength;
    /**
     * 数值精度
     */
    private Integer numColumnPrecisionLength;
    /**
     * 数值小数精度
     */
    private Integer numColumnScaleLength;
    /**
     * 字段注解
     */
    private String columnAnn;
    /**
     * 指定关联的映射
     */
    private String joinTable;
    /**
     * 字段默认值
     */
    private String columnDefault;

    private String firstUpperFieldName;

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
        this.firstUpperFieldName = StringUtils.toUpperCaseFirstOne(fieldName);
    }
}

package com.efeichong.generator;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Set;

/**
 * @author lxk
 * @date 2020/10/16
 * @description 数据库表的信息
 */
@Setter
@Getter
public class Table {
    /**
     * 表名
     */
    private String tableName;
    /**
     * 表注释
     */
    private String tableComment;
    /**
     * java类名
     */
    private String javaName;
    /**
     * 实例名 相当于java类名首字母小写
     */
    private String instanceName;
    /**
     * 主键字段名
     */
    private String pkColumnName;
    /**
     * 主键字段名首字母大写
     */
    private String firstUpperPkColumnName;
    /**
     * 主键类型
     */
    private String pkColumnType;
    /**
     * 表字段
     */
    private List<TableColumn> tableColumns = Lists.newLinkedList();
    /**
     * 要导入的包
     */
    private Set<String> importList = Sets.newHashSet();
    /**
     * 配置
     */
    private GenConfiguration configuration;
    /**
     * 父类名
     */
    private String baseEntityName;
    /**
     * 父类包路径
     */
    private String baseEntityPkg;
    /**
     * 是否生成excel注解
     */
    private boolean hasExcel = false;
    /**
     * 是否生成swagger注解
     */
    private boolean hasSwagger = true;
    /**
     * 是否生成lombok注解
     */
    private boolean hasLombok = true;
    /**
     * 基础架构包路径
     */
    private String basePkg;
    /**
     * 表索引
     */
    private String tableIndex;
    /**
     * vo中用到的包
     */
    private Set voFieldTypes = Sets.newLinkedHashSet();
    /**
     * 逻辑删除
     */
    private String logicDelete;

    private String responsePkg;

    private String responseName;

    private String exceptionPkg;

    private String exceptionName;
}

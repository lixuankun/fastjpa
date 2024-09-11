package com.efeichong.generator;

import com.google.common.collect.ImmutableMap;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author lxk
 * @date 2020/10/21
 * @description 生成器的常量
 */
public class GenConstant {

    /*数据库字段和java字段的映射*/
    public static final ImmutableMap<String, Class> TYPE_MAP = new ImmutableMap.Builder<String, Class>()
            //字符串
            .put("char", String.class)
            .put("varchar", String.class)
            .put("binary", String.class)
            .put("blob", String.class)
            .put("mediumtext", String.class)
            .put("tinytext", String.class)
            .put("longtext", String.class)
            .put("text", String.class)
            //时间
            .put("datetime", Date.class)
            .put("time", Date.class)
            .put("date", Date.class)
            .put("timestamp", Date.class)
            //数字
            .put("tinyint", Integer.class)
            .put("int", Integer.class)
            .put("integer", Integer.class)
            .put("bigint", Long.class)
            .put("decimal", BigDecimal.class)
            .put("float", Float.class)
            .put("double", Double.class)
            .build();

    /*java文件注释时间格式*/
    public static final SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd");
    /*逻辑删除字段名*/
    public static final String FAKE_DEL_COLUMN = "hasDel";

    //------------------------------------生成的文件类型----------------------------------------//
    public static final int ALL = 126;

    /* 实体类 */
    public static final int DOMAIN_TYPE = 2;
    /* dao持久层 */
    public static final int DAO_TYPE = 2 << 1;
    /* service接口 */
    public static final int SERVICE_TYPE = 2 << 2;
    /* serviceImpl实现类 */
    public static final int SERVICE_IMPL_TYPE = 2 << 3;
    /* controller对外接口 */
    public static final int CONTROLLER_TYPE = 2 << 4;
    /* vo层 */
    public static final int VO_TYPE = 2 << 5;
    /* 生成vue代码 */
    public static final int VUE_TYPE = 2 << 6;
    //-------------------------------------包路径---------------------------------------------//
    public static final String LIST_PKG = "java.util.List";
    /*@LogicDelete的包路径*/
    public static final String LogicDelete_PKG = "com.efeichong.logicDelete.LogicDelete;";

}

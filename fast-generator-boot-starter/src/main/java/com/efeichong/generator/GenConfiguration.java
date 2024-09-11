package com.efeichong.generator;

import lombok.Getter;
import lombok.Setter;

/**
 * @author lxk
 * @date 2020/10/23
 * @description 代码生成器的配置，
 */
@Setter
@Getter
public class GenConfiguration {
    /**
     * 实体类的统一父类
     */
    private Class baseEntity;
    /**
     * 接口统一返回的类
     */
    private Class responseClazz;
    /**
     * 异常类
     */
    private Class exceptionClazz;
    /**
     * 模块路径 以点隔开
     */
    private String modulePath;
    /**
     * 实体类生成的包路径
     */
    private String domainPackage = "generation";
    /**
     * dao生成的包路径
     */
    private String daoPackage = "generation";
    /**
     * service生成的包路径
     */
    private String servicePackage = "generation";
    /**
     * serviceImpl实体类生成的包路径
     */
    private String serviceImplPackage = "generation";
    /**
     * controller生成的包路径
     */
    private String controllerPackage = "generation";
    /**
     * vo生成的包路径
     */
    private String voPackage = "generation";
    /**
     * vue生成的包路径
     */
    private String vuePackage = "generation";
    /**
     * 生成java文件中注释中的作者
     */
    private String author = "admin";
    /**
     * 生成java文件中注释中的时间
     */
    private String dateTime = GenConstant.format.format(System.currentTimeMillis());
}

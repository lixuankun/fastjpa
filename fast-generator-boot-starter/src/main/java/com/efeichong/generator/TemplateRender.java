package com.efeichong.generator;

import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.file.Files;

import static com.efeichong.generator.GenConstant.*;

/**
 * @author lxk
 * @date 2020/10/19
 * @description freemarker方法
 */
@Slf4j
public class TemplateRender {

    private static Configuration configuration = new Configuration(Configuration.getVersion());

    static {
        try {
            configuration.setDefaultEncoding("UTF-8");
            configuration.setWhitespaceStripping(true);
            configuration.setClassicCompatible(true);
            configuration.setClassLoaderForTemplateLoading(TemplateRender.class.getClassLoader(), "generate");
        } catch (Exception e) {
            log.error("代码生成器模板始化失败", e);
        }
    }

    /**
     * 渲染模板
     *
     * @param table
     * @param genConfiguration
     * @param generateModel    指定生成的文件类型,默认生成全部
     */
    @SneakyThrows
    public static void render(Table table, GenConfiguration genConfiguration, int generateModel) {
        //创建domain的文件
        Template template;
        String packageName;
        if ((generateModel & DOMAIN_TYPE) == DOMAIN_TYPE) {
            template = configuration.getTemplate("domain.java.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getDomainPackage());
            createFile(template, packageName, table.getJavaName(), ".java", table);
        }
        if ((generateModel & DAO_TYPE) == DAO_TYPE) {
            //创建dao的文件
            template = configuration.getTemplate("dao.java.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getDaoPackage());
            createFile(template, packageName, table.getJavaName(), "Dao.java", table);
        }
        if ((generateModel & SERVICE_TYPE) == SERVICE_TYPE) {
            //创建service文件
            template = configuration.getTemplate("serviceNoInterface.java.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getServicePackage());
            createFile(template, packageName, table.getJavaName(), "Service.java", table);
        }
//        if ((generateModel & SERVICE_IMPL_TYPE) == SERVICE_IMPL_TYPE) {
//            //创建serviceImpl文件
//            if ((generateModel & SERVICE_TYPE) == SERVICE_TYPE) {
//                template = configuration.getTemplate("serviceImpl.java.ftl");
//                packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getServiceImplPackage());
//                createFile(template, packageName, table.getJavaName(), "ServiceImpl.java", table);
//            } else {
//                template = configuration.getTemplate("serviceNoInterface.java.ftl");
//                packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getServiceImplPackage());
//                createFile(template, packageName, table.getJavaName(), "Service.java", table);
//            }
//
//        }
        if ((generateModel & CONTROLLER_TYPE) == CONTROLLER_TYPE) {
            //创建controller文件
            template = configuration.getTemplate("controller.java.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getControllerPackage());
            createFile(template, packageName, table.getJavaName(), "Controller.java", table);
        }
        if ((generateModel & VO_TYPE) == VO_TYPE) {
            //创建controller文件
            template = configuration.getTemplate("vo.java.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getVoPackage());
            createFile(template, packageName, table.getJavaName(), "Vo.java", table);
        }
        if ((generateModel & VUE_TYPE) == VUE_TYPE) {
            //创建VUE文件
            template = configuration.getTemplate("index.vue.ftl");
            packageName = builderPackageName(genConfiguration.getModulePath(), genConfiguration.getVoPackage());
            createFile(template, packageName, table.getJavaName() + "\\\\", "index.vue", table);
        }

    }

    /**
     * 构建包路径 将com.test 换成 src\main\java\com\test
     *
     * @param modulePath
     * @param packageName
     * @return
     */
    private static String builderPackageName(String modulePath, String packageName) {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNotBlank(modulePath)) {
            builder.append(modulePath.replaceAll("\\.", "\\\\")).append("\\\\");
        }
        builder
                .append("src\\main\\java\\")
                .append(packageName.replaceAll("\\.", "\\\\"))
                .append("\\\\");
        return builder.toString();
    }

    /**
     * 创建文件
     *
     * @param template    模板
     * @param packageName 包路径
     * @param prefixName  前缀 实体类名
     * @param suffixName  后缀 如：.java,Service.java,Controller.java
     * @param table       数据库表的信息
     */
    @SneakyThrows
    private static void createFile(Template template, String packageName, String prefixName, String suffixName, Table table) {
        File file = new File(packageName + prefixName + suffixName);
        if (file.exists()) {
            file.delete();
        }
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        @Cleanup Writer out = new OutputStreamWriter(Files.newOutputStream(file.toPath()));
        @Cleanup PrintWriter writer = new PrintWriter(out);
        template.process(table, writer);
        writer.flush();
    }

}

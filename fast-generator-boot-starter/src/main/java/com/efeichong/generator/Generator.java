package com.efeichong.generator;

import com.efeichong.exception.BaseException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static com.efeichong.generator.GenConstant.*;


/**
 * @author lxk
 * @date 2020/10/14
 * @description 生成器 通过数据库表生成代码，一对多默认不生成，如果用到手动添加,
 * 生成器对象通过建造者 builder()生成
 */
@Builder(setterPrefix = "add")
@Slf4j
public class Generator {
    /**
     * 每个表需要用到的包
     */
    private static final Map<String, Set<String>> importPkgMap = Maps.newLinkedHashMap();
    /**
     * 配置
     *
     * @nullable
     */
    private GenConfiguration genConfiguration;
    /**
     * 指定哪些表需要进行代码生成
     *
     * @nullable
     */
    private Set<String> requireTableNames;
    /**
     * 不需要生成的表
     *
     * @nullable
     */
    private Set<String> ignoreTableNames;
    /**
     * 是否生成excel注解
     */
    private boolean hasExcel;
    /**
     * 是否生成swagger注解
     */
    private boolean hasSwagger;
    /**
     * 是否生成lombok注解
     */
    @Builder.Default
    private boolean hasLombok = true;
    /**
     * 指定多对多映射的表维护的一方
     * key:多对多中间表  value：维护者 表名字
     */
    private Map<String, String> manyToManyMappedTableMap;
    /**
     * 数据库连接配置
     */
    private DataSource dataSource;
    /**
     * 指定生成的文件类型,默认生成全部
     */
    @Builder.Default
    private int generateModel = ALL;
    /**
     * 元数据查询
     */
    private TableInfoQuery $tableInfoQuery;
    /**
     * 相同的字段则通过后缀区分
     */
    private int $suffix = 0;

    /**
     * 添加导入的包
     *
     * @param tableName 表名
     * @param importPkg 需要导入的包
     */
    private static void addImportPkg(String tableName, String importPkg) {
        Set<String> importPkgList = importPkgMap.get(tableName);
        if (importPkgList == null) {
            importPkgList = Sets.newLinkedHashSet();
            importPkgList.add(importPkg);
            importPkgMap.put(tableName, importPkgList);
        } else {
            if (!importPkgList.contains(importPkg)) {
                importPkgList.add(importPkg);
            }
        }
    }

    /**
     * 驼峰
     *
     * @param name
     * @return
     */
    public static String convertToHump(String name) {
        String[] fields = name.split("_");
        StringBuilder builder = new StringBuilder(fields[0]);
        for (int i = 1; i < fields.length; i++) {
            char[] cs = fields[i].toCharArray();
            cs[0] -= 32;
            builder.append(String.valueOf(cs));
        }
        return builder.toString();
    }

    /**
     * 过滤掉父级的字段
     *
     * @param parentFields 父类的字段集合
     * @param columns      表字段
     * @param table        表
     * @return
     */
    private static void filterParentFields(Field[] parentFields, List<TableColumn> columns, Table table) {
        if (ArrayUtils.isNotEmpty(parentFields)) {
            for (Field parentField : parentFields) {
                Iterator<TableColumn> iterator = columns.iterator();
                while (iterator.hasNext()) {
                    TableColumn column = iterator.next();
                    if (parentField.getName().equals(convertToHump(column.getColumnName()))) {
                        column.setFieldName(convertToHump(column.getColumnName()));
                        Class fieldType = GenConstant.TYPE_MAP.getOrDefault(column.getDataType(), String.class);
                        if (fieldType == null) {
                            throw new BaseException("未定义的类型:" + column.getDataType());
                        }
                        //设置主键
                        setPk(column, table);
                        column.setFieldType(fieldType.getSimpleName());
                        iterator.remove();
                    }
                }
            }
        }
    }

    /**
     * 设置主键
     *
     * @param column 字段
     * @param table  表
     */
    private static void setPk(TableColumn column, Table table) {
        if (column.getHasPrimaryKey() == 1) {
            table.setPkColumnName(column.getFieldName());
            table.setFirstUpperPkColumnName(StringUtils.toUpperCaseFirstOne(column.getFieldName()));
            Class fieldType = GenConstant.TYPE_MAP.getOrDefault(column.getDataType(), String.class);
            if (fieldType == null) {
                throw new BaseException("未定义的类型:" + column.getDataType());
            }
            table.setPkColumnType(fieldType.getSimpleName());
        }
    }

    /**
     * 设置字段的@Column注解
     *
     * @param column 字段
     * @return
     */
    private static void setColumnAnn(TableColumn column) {
        StringBuilder builder = new StringBuilder();
        Class columnClazz = GenConstant.TYPE_MAP.getOrDefault(column.getDataType(), String.class);
        builder.append("@Column(name = \"[").append(column.getColumnName()).append("]\"");
        builder.append(", columnDefinition = \"").append(column.getDataType()).append(" ");

        if (columnClazz.isAssignableFrom(BigDecimal.class) || columnClazz.isAssignableFrom(Double.class)
                || columnClazz.isAssignableFrom(Float.class)) {
            builder.append("(");
            if (column.getNumColumnPrecisionLength() != null && column.getNumColumnPrecisionLength() != 0) {
                builder.append(column.getNumColumnPrecisionLength());
            }
            if (column.getNumColumnScaleLength() != null && column.getNumColumnScaleLength() != 0) {
                builder.append(",").append(column.getNumColumnScaleLength());
            }
            builder.append(") ");
        } else {
            if (columnClazz.isAssignableFrom(String.class) && column.getVarcharColumnLength() != null && column.getVarcharColumnLength() != 0) {
                builder.append("(").append(column.getVarcharColumnLength()).append(") ");
            }
        }
        if (column.getHasRequired() != null && column.getHasRequired() == 1) {
            builder.append("not null ");
        }
        if (StringUtils.isNotBlank(column.getColumnDefault())) {
            builder.append("default '").append(column.getColumnDefault()).append("' ");
        }
        if (StringUtils.isNotBlank(column.getColumnComment())) {
            builder.append("comment '").append(column.getColumnComment()).append("'");
        }
        builder.append("\")");
        column.setColumnAnn(builder.toString());
    }

    /**
     * 代码生成
     */
    public void generate() {
        try {
            log.info("=========================执行开始============================");
            if (dataSource == null) {
                throw new BaseException("请配置数据库连接 @link{com.efeichong.generator.DataSource}");
            }
            if (genConfiguration == null) {
                genConfiguration = new GenConfiguration();
            }
            if (CollectionUtils.isEmpty(ignoreTableNames)) {
                ignoreTableNames = Sets.newLinkedHashSet();
            }
            $tableInfoQuery = TableInfoQuery.getInstance(dataSource);
            generate(genConfiguration, requireTableNames);
            log.info("=========================执行结束============================");
        } catch (Exception e) {
            log.error("生成失败", e);
        } finally {

        }
    }

    /**
     * 代码生成
     *
     * @param tableNames    指定需要生成的表
     * @param configuration 指定配置
     */
    private void generate(GenConfiguration configuration, Set<String> tableNames) {
        //构建出一个table集合
        List<Table> tables = buildTables(tableNames, configuration);
        for (Table table : tables) {
            table.setImportList(importPkgMap.get(table.getTableName()));
            table.setHasExcel(hasExcel);
            table.setHasSwagger(hasSwagger);
            table.setHasLombok(hasLombok);
            //不需要生成的表跳过
            if (!ignoreTableNames.contains(table.getTableName())) {
                table.setConfiguration(configuration);
                //生成代码
                TemplateRender.render(table, configuration, generateModel);
                log.info(table.getTableName() + "表生成完毕");
            }
        }
    }

    /**
     * 构建table集合
     *
     * @param tableNames    指定需要生成的表
     * @param configuration 指定配置
     * @return
     */
    private List<Table> buildTables(Set<String> tableNames, GenConfiguration configuration) {
        //多对多映射  key：多对多表名  value：{@link ReferenceColumn}
        Map<String, List<ReferenceColumn>> manyToManyMap = Maps.newLinkedHashMap();
        Class baseEntity = configuration.getBaseEntity();
        List<Table> tables = $tableInfoQuery.queryTables(tableNames);
        String domainPackage = configuration.getDomainPackage();

        Class responseClazz = configuration.getResponseClazz();
        String responsePkg;
        String responseName;
        if (responseClazz == null) {
            responsePkg = "com.efeichong.common.domain.ResponseData";
            responseName = "ResponseData";
        } else {
            responsePkg = responseClazz.getName();
            responseName = responseClazz.getSimpleName();
        }
        Class exceptionClazz = configuration.getExceptionClazz();
        String exceptionPkg;
        String exceptionName;
        if (exceptionClazz == null) {
            exceptionPkg = "com.efeichong.exception.JpaException";
            exceptionName = "JpaException";
        } else {
            exceptionPkg = exceptionClazz.getName();
            exceptionName = exceptionClazz.getSimpleName();
        }

        Field[] parentFields = null;
        if (baseEntity != null) {
            EntityCache entityCache = EntityCache.forClass(baseEntity);
            parentFields = entityCache.getFields();
        }

        for (Table table : tables) {
            log.info(table.getTableName() + "表结构分析完毕");
            if (baseEntity != null) {
                table.setBaseEntityName(baseEntity.getSimpleName());
                table.setBaseEntityPkg(baseEntity.getName());
            }
            table.setResponsePkg(responsePkg);
            table.setResponseName(responseName);

            table.setExceptionPkg(exceptionPkg);
            table.setExceptionName(exceptionName);

            //表名驼峰
            String humpTableName = convertToHump(table.getTableName());
            //表名驼峰后首字母大写
            table.setJavaName(StringUtils.toUpperCaseFirstOne(humpTableName));
            table.setInstanceName(humpTableName);
            String tableName = table.getTableName();
            //查询表的所有字段
            List<TableColumn> columns = $tableInfoQuery.queryColumns(tableName);
            //查询表有索引字段
            List<IndexColumn> indexColumns = $tableInfoQuery.queryIndex(tableName);
            //筛选唯一约束
            List<String> uniqueColumns = indexColumns.stream().filter(indexColumn -> indexColumn.getHasUnique() == 1)
                    .map(IndexColumn::getColumnName).collect(Collectors.toList());
            //初始化索引字段
            initTableIndex(indexColumns, table);
            //过滤掉父级的字段
            filterParentFields(parentFields, columns, table);
            //初始化多对一的字段
            initManyToOneColumn(columns, table, domainPackage, uniqueColumns);
            //初始化没有外键的字段
            initNonFKColumn(columns, table);
            //统计相关多对多的表
            collectManyToManyTables(tableName, manyToManyMap);
        }
        //初始化多对多的字段，获取每个表中多对多的字段
        Map<String, List<TableColumn>> manyToManyColumnMap = initManyToManyColumn(manyToManyMap, domainPackage);
        //将多对多的信心存到table中
        for (Table table : tables) {
            if (manyToManyColumnMap.containsKey(table.getTableName())) {
                table.getTableColumns().addAll(manyToManyColumnMap.get(table.getTableName()));
            }
        }
        return tables;
    }

    /**
     * 初始化多对多的字段
     *
     * @param manyToManyMap 多对多映射  key：多对多表名  value：{@link ReferenceColumn}
     * @param domainPackage 实体类所在的包路径
     * @return Map<String, List < TableColumn>> key：表名 value：多对多字段
     */
    private Map<String, List<TableColumn>> initManyToManyColumn(Map<String, List<ReferenceColumn>> manyToManyMap, String domainPackage) {
        //key：表名 value：多对多字段
        Map<String, List<TableColumn>> manyToManyColumnMap = new HashMap();
        for (Map.Entry<String, List<ReferenceColumn>> entry : manyToManyMap.entrySet()) {
            List<ReferenceColumn> referenceColumns = entry.getValue();
            if (entry.getValue().size() == 1) {
                mapperManyToMany(referenceColumns.get(0), manyToManyColumnMap, domainPackage);
                continue;
            }
            ReferenceColumn first = referenceColumns.get(0);
            ReferenceColumn second = referenceColumns.get(1);
            if (MapUtils.isNotEmpty(manyToManyMappedTableMap)) {
                String mappedTable = manyToManyMappedTableMap.get(entry.getKey());
                if (StringUtils.isNotBlank(mappedTable)) {
                    mapperManyToMany(first, second, manyToManyColumnMap, domainPackage, mappedTable);
                } else {
                    mapperManyToMany(first, second, manyToManyColumnMap, domainPackage, first.getReferencedTableName());
                }
            } else {
                mapperManyToMany(first, second, manyToManyColumnMap, domainPackage, second.getReferencedTableName());
            }
        }
        return manyToManyColumnMap;
    }

    private void mapperManyToMany(ReferenceColumn referenceColumn, Map<String, List<TableColumn>> tableColumnMap, String domainPackage) {
        String tableName = referenceColumn.getReferencedTableName();
        TableColumn column = new TableColumn();
        StringBuilder joinTableBuilder = new StringBuilder("@JoinTable(name = \"")
                .append(referenceColumn.getTableName()).append("\"").append(",\n\t\t\t");
        joinTableBuilder.append("joinColumns = @JoinColumn(name = \"").append(referenceColumn.getReferencedColumnName())
                .append("\", ").append("referencedColumnName = \"").append(referenceColumn.getReferencedColumnName())
                .append("\"),\n\t\t\t");
        joinTableBuilder.append("inverseJoinColumns = @JoinColumn(name = \"").append(referenceColumn.getColumnName())
                .append("\", ").append("referencedColumnName = \"").append(referenceColumn.getReferencedColumnName())
                .append("\"))");
        column.setMappedType(MappedType.MANY_TO_MANY.getName() + "(fetch = FetchType.LAZY)");
        column.setJoinTable(joinTableBuilder.toString());
        if (tableColumnMap.containsKey(referenceColumn.getReferencedTableName())) {
            List<TableColumn> tableColumns = tableColumnMap.get(referenceColumn.getReferencedTableName());
            tableColumns.add(column);
        } else {
            List<TableColumn> tableColumns = Lists.newArrayList(column);
            tableColumnMap.put(referenceColumn.getReferencedTableName(), tableColumns);
        }
        column.setHasNeedSerialize(0);
        column.setFieldName(convertToHump(referenceColumn.getReferencedTableName() + "s"));
        String fieldType = StringUtils.toUpperCaseFirstOne(convertToHump(referenceColumn.getReferencedTableName()));
        column.setFieldType("List<" + fieldType + ">");
        addImportPkg(tableName, domainPackage + "." + fieldType);
        addImportPkg(tableName, LIST_PKG);
    }

    /**
     * 将多对多的表映射成map
     *
     * @param first          多对多表的第一个元素关联的表{@link ReferenceColumn}
     * @param second         多对多表的第二个元素关联的表{@link ReferenceColumn}
     * @param tableColumnMap key：多对多表名  value：{@link ReferenceColumn}
     * @param domainPackage  实体类所在的包路径
     * @param mappedTable    多对多表的维护者
     */
    private void mapperManyToMany(ReferenceColumn first, ReferenceColumn second, Map<String, List<TableColumn>> tableColumnMap
            , String domainPackage, String mappedTable) {

        List<TableColumn> tableColumns;
        if (first.getReferencedTableName().equals(mappedTable)) {
            TableColumn secondColumn = new TableColumn();
            String tableName = first.getReferencedTableName();
            if (tableColumnMap.containsKey(first.getReferencedTableName())) {
                tableColumns = tableColumnMap.get(first.getReferencedTableName());
                tableColumns.add(secondColumn);
            } else {
                tableColumns = Lists.newArrayList(secondColumn);
                tableColumnMap.put(first.getReferencedTableName(), tableColumns);
            }
            //设置是关系是维护者的表的信息
            secondColumn.setMappedType(MappedType.MANY_TO_MANY.getName()
                    + "(fetch = FetchType.LAZY,mappedBy = \"" + convertToHump(first.getReferencedTableName() + "s") + "\")");
            secondColumn.setHasNeedSerialize(0);
            String fieldName = convertToHump(second.getReferencedTableName() + "s");
            if (hasSameName(tableColumns, fieldName)) {
                $suffix++;
                secondColumn.setFieldName(convertToHump(second.getReferencedTableName() + $suffix + "s"));
            } else {
                secondColumn.setFieldName(fieldName);
            }
            String fieldType = StringUtils.toUpperCaseFirstOne(convertToHump(second.getReferencedTableName()));
            secondColumn.setFieldType("List<" + fieldType + ">");
            addImportPkg(tableName, domainPackage + "." + fieldType);
            addImportPkg(tableName, LIST_PKG);
            //设置是关系是被维护的表的信息
            TableColumn firstColumn = new TableColumn();
            StringBuilder joinTableBuilder = new StringBuilder("@JoinTable(name = \"")
                    .append(first.getTableName()).append("\"").append(",\n\t\t\t");
            joinTableBuilder.append("joinColumns = @JoinColumn(name = \"").append(second.getColumnName())
                    .append("\", ").append("referencedColumnName = \"").append(second.getReferencedColumnName())
                    .append("\"),\n\t\t\t");
            joinTableBuilder.append("inverseJoinColumns = @JoinColumn(name = \"").append(first.getColumnName())
                    .append("\", ").append("referencedColumnName = \"").append(first.getReferencedColumnName())
                    .append("\"))");
            if (tableColumnMap.containsKey(second.getReferencedTableName())) {
                tableColumns = tableColumnMap.get(second.getReferencedTableName());
                tableColumns.add(firstColumn);
            } else {
                tableColumns = Lists.newArrayList(firstColumn);
                tableColumnMap.put(second.getReferencedTableName(), tableColumns);
            }
            fieldName = convertToHump(first.getReferencedTableName() + "s");
            if (hasSameName(tableColumns, fieldName)) {
                $suffix++;
                firstColumn.setFieldName(convertToHump(first.getReferencedTableName() + $suffix + "s"));
                secondColumn.setMappedType(MappedType.MANY_TO_MANY.getName()
                        + "(fetch = FetchType.LAZY,mappedBy = \"" + convertToHump(first.getReferencedTableName() + $suffix + "s") + "\")");
            } else {
                firstColumn.setFieldName(fieldName);
            }
            fieldType = StringUtils.toUpperCaseFirstOne(convertToHump(first.getReferencedTableName()));
            firstColumn.setFieldType("List<" + fieldType + ">");
            firstColumn.setMappedType(MappedType.MANY_TO_MANY.getName() + "(fetch = FetchType.LAZY)");
            firstColumn.setJoinTable(joinTableBuilder.toString());
            firstColumn.setHasNeedSerialize(0);
            tableName = second.getReferencedTableName();
            addImportPkg(tableName, domainPackage + "." + fieldType);
            addImportPkg(tableName, LIST_PKG);
        } else {
            //设置是关系是维护者的表的信息
            TableColumn firstColumn = new TableColumn();
            firstColumn.setMappedType(MappedType.MANY_TO_MANY.getName()
                    + "(fetch = FetchType.LAZY,mappedBy = \"" + convertToHump(second.getReferencedTableName() + "s") + "\")");
            if (tableColumnMap.containsKey(second.getReferencedTableName())) {
                tableColumns = tableColumnMap.get(second.getReferencedTableName());
                tableColumns.add(firstColumn);
            } else {
                tableColumns = Lists.newArrayList(firstColumn);
                tableColumnMap.put(second.getReferencedTableName(), tableColumns);
            }
            firstColumn.setHasNeedSerialize(0);
            String fieldName = convertToHump(first.getReferencedTableName() + "s");
            if (hasSameName(tableColumns, fieldName)) {
                $suffix++;
                firstColumn.setFieldName(convertToHump(first.getReferencedTableName() + $suffix + "s"));
            } else {
                firstColumn.setFieldName(fieldName);
            }
            String fieldType = StringUtils.toUpperCaseFirstOne(convertToHump(first.getReferencedTableName()));
            firstColumn.setFieldType("List<" + fieldType + ">");
            String tableName = second.getReferencedTableName();
            addImportPkg(tableName, domainPackage + "." + fieldType);
            addImportPkg(tableName, LIST_PKG);
            //设置是关系是被维护的表的信息
            TableColumn secondColumn = new TableColumn();
            StringBuilder joinTableBuilder = new StringBuilder("@JoinTable(name = \"")
                    .append(first.getTableName()).append("\"").append(",\n\t\t\t");
            joinTableBuilder.append("joinColumns = @JoinColumn(name = \"").append(first.getColumnName())
                    .append("\", ").append("referencedColumnName = \"").append(first.getReferencedColumnName())
                    .append("\"),\n\t\t\t");
            joinTableBuilder.append("inverseJoinColumns = @JoinColumn(name = \"").append(second.getColumnName())
                    .append("\", ").append("referencedColumnName = \"").append(second.getReferencedColumnName())
                    .append("\"))");
            secondColumn.setMappedType(MappedType.MANY_TO_MANY.getName() + "(fetch = FetchType.LAZY)");
            secondColumn.setJoinTable(joinTableBuilder.toString());
            if (tableColumnMap.containsKey(first.getReferencedTableName())) {
                tableColumns = tableColumnMap.get(first.getReferencedTableName());
                tableColumns.add(secondColumn);
            } else {
                tableColumns = Lists.newArrayList(secondColumn);
                tableColumnMap.put(first.getReferencedTableName(), tableColumns);
            }
            secondColumn.setHasNeedSerialize(0);

            fieldName = convertToHump(second.getReferencedTableName() + "s");
            if (hasSameName(tableColumns, fieldName)) {
                $suffix++;
                secondColumn.setFieldName(convertToHump(second.getReferencedTableName() + $suffix + "s"));
                firstColumn.setMappedType(MappedType.MANY_TO_MANY.getName()
                        + "(fetch = FetchType.LAZY,mappedBy = \"" + convertToHump(second.getReferencedTableName() + $suffix + "s") + "\")");
            } else {
                secondColumn.setFieldName(fieldName);
            }
            fieldType = StringUtils.toUpperCaseFirstOne(convertToHump(second.getReferencedTableName()));
            secondColumn.setFieldType("List<" + fieldType + ">");
            tableName = first.getReferencedTableName();
            addImportPkg(tableName, domainPackage + "." + fieldType);
            addImportPkg(tableName, LIST_PKG);
        }
    }

    /**
     * 判断是否已存在相同的字段名
     *
     * @param tableColumns
     * @param fieldName
     * @return
     */
    private boolean hasSameName(List<TableColumn> tableColumns, String fieldName) {
        for (TableColumn column : tableColumns) {
            if (column.getFieldName() != null && column.getFieldName().equals(fieldName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 初始化索引字段
     *
     * @param indexColumns 表具有的索引信息
     * @param table        表
     */
    private void initTableIndex(List<IndexColumn> indexColumns, Table table) {
        if (CollectionUtils.isEmpty(indexColumns)) {
            return;
        }
        StringBuilder builder = new StringBuilder("@Table(indexes = {\n");
        for (IndexColumn indexColumn : indexColumns) {
            builder.append("\t\t@Index(name = \"").append(indexColumn.getIndexName()).append("\", ")
                    .append("columnList = \"").append(indexColumn.getColumnName()).append("\"");
            if (indexColumn.getHasUnique() == 1) {
                builder.append(", unique = true),\n");
            } else {
                builder.append("),\n");
            }
        }
        builder.replace(builder.lastIndexOf("\n"), builder.length(), "");
        builder.append("\n})");
        table.setTableIndex(builder.toString());
    }

    /**
     * 初始化多对一的字段
     *
     * @param columns       表中的字段信息
     * @param table         表
     * @param domainPackage 实体类的包路径
     * @param uniqueColumns 唯一约束信息
     */
    private void initManyToOneColumn(List<TableColumn> columns, Table table, String domainPackage, List<String> uniqueColumns) {
        String tableName = table.getTableName();
        List<TableColumn> tableColumns = table.getTableColumns();
        //查询表的所有外键
        List<ReferenceColumn> referenceColumns = $tableInfoQuery.queryColumnReference(tableName);
        if (CollectionUtils.isEmpty(referenceColumns)) {
            return;
        }
        for (ReferenceColumn referenceColumn : referenceColumns) {
            Iterator<TableColumn> iterator = columns.iterator();
            while (iterator.hasNext()) {
                TableColumn column = iterator.next();
                String referenceColumnColumnName = referenceColumn.getColumnName();
                if (column.getColumnName().equals(referenceColumnColumnName)) {
                    tableColumns.add(column);
                    iterator.remove();
                    column.setColumnAnn("@JoinColumn(name = \"[" + referenceColumnColumnName + "]\")");
                    if (uniqueColumns.contains(column.getColumnName())) {
                        column.setHasUnique(1);
                        //如果外键有唯一约束说明是一对一
                        column.setMappedType(MappedType.ONE_TO_ONE.getName() + "(fetch = FetchType.LAZY)");
                    } else {
                        //如果外键没有唯一约束说明是多对一
                        column.setMappedType(MappedType.MANY_TO_ONE.getName() + "(fetch = FetchType.LAZY)");
                    }
                    column.setHasNeedSerialize(0);
                    if (referenceColumnColumnName.indexOf("_id") != -1) {
                        column.setFieldName(convertToHump(referenceColumnColumnName.substring(0, referenceColumnColumnName.lastIndexOf("_id"))));
                    } else {
                        column.setFieldName(convertToHump(referenceColumnColumnName));
                    }
                    String fieldType = convertToHump(StringUtils.toUpperCaseFirstOne(referenceColumn.getReferencedTableName()));
                    column.setFieldType(fieldType);
                    addImportPkg(tableName, domainPackage + "." + fieldType);
                }
            }
        }
    }

    /**
     * 初始化没有外键的字段
     *
     * @param columns 表所有的字段信息
     * @param table   表
     */
    private void initNonFKColumn(List<TableColumn> columns, Table table) {
        for (TableColumn column : columns) {
            column.setFieldName(convertToHump(column.getColumnName()));
            if (FAKE_DEL_COLUMN.equals(column.getFieldName())) {
                table.setLogicDelete("@LogicDelete(column = \"" + FAKE_DEL_COLUMN + "\")");
                addImportPkg(table.getTableName(), LogicDelete_PKG);
            }
            Class fieldType = TYPE_MAP.getOrDefault(column.getDataType(), String.class);
            if (fieldType == null) {
                throw new BaseException("未定义的类型:" + column.getDataType());
            }
            table.getVoFieldTypes().add(fieldType.getName());
            //设置字段@Column注解
            setColumnAnn(column);
            //设置主键
            setPk(column, table);
            column.setFieldType(fieldType.getSimpleName());
            addImportPkg(table.getTableName(), fieldType.getName());
            table.getTableColumns().add(column);
        }
    }

    /**
     * 统计相关多对多的表
     *
     * @param tableName     表名 通过该表查询是否为多对多关系中的一个
     * @param manyToManyMap 多对多映射  key：多对多表名  value：{@link ReferenceColumn}
     */
    private void collectManyToManyTables(String tableName, Map<String, List<ReferenceColumn>> manyToManyMap) {
        //查询其它表外键指向这个表的字段
        List<ReferenceColumn> referencedColumns = $tableInfoQuery.queryColumnReferenced(tableName);
        //查询多对多的表字段
        List<ReferenceColumn> relationTables = $tableInfoQuery.getRelationTable(referencedColumns, tableName);
        if (CollectionUtils.isEmpty(relationTables)) {
            return;
        }
        for (ReferenceColumn relationTable : relationTables) {
            ignoreTableNames.add(relationTable.getTableName());
            String relationTableName = relationTable.getTableName();
            if (manyToManyMap.containsKey(relationTableName)) {
                List<ReferenceColumn> referenceColumns = manyToManyMap.get(relationTableName);
                boolean match = referenceColumns.stream().allMatch(referenceColumn ->
                        referenceColumn.getReferencedTableName().equals(relationTable.getReferencedTableName()));
                if (!match) {
                    referenceColumns.add(relationTable);
                }
            } else {
                List<ReferenceColumn> referenceColumns = Lists.newArrayList(relationTable);
                manyToManyMap.put(relationTableName, referenceColumns);
            }
        }
    }


}

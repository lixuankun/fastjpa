package com.efeichong.generator;


import java.util.*;

/**
 * @author lxk
 * @date 2020/10/20
 * @description 查询表的元数据 information_schema,该类为单例的
 */
public class TableInfoQuery {
    /**
     * 元数据查询的工具
     */
    private static TableInfoQuery instance;
    /**
     * 数据库查询工具
     */
    private static DBUtils dbUtils;

    private TableInfoQuery() {
    }

    private TableInfoQuery(DataSource dataSource) {
        dbUtils = new DBUtils(dataSource);
    }

    public static TableInfoQuery getInstance(DataSource dataSource) {
        if (instance == null) {
            instance = new TableInfoQuery(dataSource);
        }
        return instance;
    }

    /**
     * 查询表
     *
     * @param tableNames 指定表名字
     * @return
     */
    public List<Table> queryTables(Set<String> tableNames) {
        String sql;
        if (CollectionUtils.isNotEmpty(tableNames)) {
            sql = "select table_name, table_comment  " +
                    "from information_schema.tables " +
                    "where table_schema = (select database()) and table_name in (:tableNames)";
        } else {
            sql = "select table_name , table_comment , create_time " +
                    "from information_schema.tables " +
                    "where table_schema = (select database())  ";
        }
        Map params = new HashMap();
        params.put("tableNames", tableNames);
        return dbUtils.queryForList(sql, params, Table.class);
    }

    /**
     * 查询表字段
     *
     * @param tableName 指定表名字
     * @return
     */
    public List<TableColumn> queryColumns(String tableName) {
        String sql = "select column_name,column_comment,column_default, " +
                "(case when (is_nullable = 'no' and column_key != 'PRI') then '1' else '0' end) as has_required, " +
                "(case when column_key = 'PRI' then '1' else '0' end) as has_primary_key,ordinal_position as sort, " +
                "if(data_type = 'longtext' or data_type = 'text',0, CHARACTER_MAXIMUM_LENGTH) varcharColumnLength, " +
                "numeric_precision numColumnPrecisionLength,NUMERIC_SCALE numColumnScaleLength, " +
                "(case when extra = 'auto_increment' then '1' else '0' end) as has_increment, data_type " +
                "from information_schema.columns where table_schema = (select database()) and table_name = (:tableName) " +
                "order by ordinal_position";
        Map params = new HashMap();
        params.put("tableName", tableName);
        return dbUtils.queryForList(sql, params, TableColumn.class);
    }

    /**
     * 查询表索引
     *
     * @param tableName
     * @return
     */
    public List<IndexColumn> queryIndex(String tableName) {
        String sql = "SELECT column_name,if(non_unique = 0,1,0) hasUnique,index_name " +
                "FROM " +
                "INFORMATION_SCHEMA.STATISTICS " +
                "WHERE " +
                "TABLE_SCHEMA = (select database()) AND " +
                "TABLE_NAME = :tableName";
        Map params = new HashMap();
        params.put("tableName", tableName);
        return dbUtils.queryForList(sql, params, IndexColumn.class);
    }

    /**
     * 查询外键连接的表
     *
     * @param tableName
     * @return
     */
    public List<ReferenceColumn> queryColumnReference(String tableName) {
        String sql = "select  " +
                "column_name,referenced_table_name,referenced_column_name,table_name  " +
                "from INFORMATION_SCHEMA.KEY_COLUMN_USAGE  " +
                "where CONSTRAINT_SCHEMA =(select database()) AND  " +
                "TABLE_NAME = :tableName and referenced_column_name is not null";
        Map params = new HashMap();
        params.put("tableName", tableName);
        return dbUtils.queryForList(sql, params, ReferenceColumn.class);
    }

    /**
     * 查询有外键指向此表的表
     *
     * @param tableName
     * @return
     */
    public List<ReferenceColumn> queryColumnReferenced(String tableName) {
        String sql = "select  " +
                "column_name,referenced_table_name,referenced_column_name,table_name  " +
                "from INFORMATION_SCHEMA.KEY_COLUMN_USAGE  " +
                "where CONSTRAINT_SCHEMA =(select database()) AND  " +
                "referenced_table_name = :tableName;";
        Map params = new HashMap();
        params.put("tableName", tableName);
        return dbUtils.queryForList(sql, params, ReferenceColumn.class);
    }

    /**
     * 查询多对多的表
     *
     * @param referencedColumns
     * @param tableName
     * @return
     */
    public List<ReferenceColumn> getRelationTable(List<ReferenceColumn> referencedColumns, String tableName) {
        List<ReferenceColumn> relationTables = new ArrayList<>();
        for (ReferenceColumn referencedColumn : referencedColumns) {
            //如果该表的字段只有两个并且两个字段各关联一张表，那么这张表将被定义为多对多的中间表
            if (countColumns(referencedColumn.getTableName()) == 2) {
                List<ReferenceColumn> referenceColumns = queryColumnReference(referencedColumn.getTableName());
                for (ReferenceColumn referenceColumn : referenceColumns) {
                    //两个字段各关联一张表
                    if (referenceColumns.size() == 2) {
                        relationTables.add(referenceColumn);
                    }
                }
            }
        }
        return relationTables;
    }

    /**
     * 查询表的字段数
     *
     * @param tableName
     * @return
     */
    private Integer countColumns(String tableName) {
        String sql = "select count(1) " +
                "from information_schema.columns where table_schema = (select database()) and table_name = (:tableName) " +
                "order by ordinal_position";
        Map params = new HashMap();
        params.put("tableName", tableName);
        Optional<Integer> optional = dbUtils.queryForObject(sql, params, Integer.class);
        return optional.get();
    }
}


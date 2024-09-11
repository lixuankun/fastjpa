package com.efeichong.generator;


import lombok.Cleanup;
import lombok.NonNull;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;

/**
 * @author lxk
 * @date 2020/11/25
 * @description 数据库查询工具
 */
public class DBUtils {
    /**
     * 数据库连接地址
     */
    private String url;
    /**
     * 数据库驱动
     */
    private String driverName;
    /**
     * 数据库连接账号
     */
    private String username;
    /**
     * 数据库连接密码
     */
    private String password;

    @SneakyThrows
    public DBUtils(DataSource dataSource) {
        this.url = dataSource.getUrl();
        this.driverName = dataSource.getDriver();
        this.username = dataSource.getUsername();
        this.password = dataSource.getPassword();
        Class.forName(driverName);
    }

    /**
     * 反射机制 返回单条记录 T
     *
     * @param sql    将要执行的sql语句
     * @param params 查询的参数
     * @param clazz  返回的对象
     * @return
     */
    @SneakyThrows
    public <T> Optional<T> queryForObject(String sql, Map<String, Object> params, Class<T> clazz) {
        @Cleanup Connection connection = DriverManager.getConnection(url, username, password);
        Map<Integer, Object> statementParam = new TreeMap<>();
        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                int index = sql.indexOf(":" + entry.getKey());
                if (index != -1) {
                    if (entry.getValue() instanceof Iterable) {
                        StringBuilder builder = new StringBuilder();
                        for (Object value : (Iterable) entry.getValue()) {
                            builder.append("?").append(",");
                            statementParam.put(index, value);
                            index++;
                        }
                        sql = sql.replace(":" + entry.getKey(), builder.substring(0, builder.length() - 1));
                    } else if (entry.getValue().getClass().isArray()) {
                        StringBuilder builder = new StringBuilder();
                        for (Object value : (Object[]) entry.getValue()) {
                            builder.append("?").append(",");
                            statementParam.put(index, value);
                            index++;
                        }
                        sql = sql.replace(":" + entry.getKey(), builder.substring(0, builder.length() - 1));
                    } else {
                        sql = sql.replace(":" + entry.getKey(), "?");
                        statementParam.put(index, entry.getValue());
                    }
                }
            }
        }
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int index = 1;
        for (Object value : statementParam.values()) {
            preparedStatement.setString(index, value.toString());
            index++;
        }
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colLength = metaData.getColumnCount();
        Optional<T> optional = Optional.empty();
        if (Number.class.isAssignableFrom(clazz)) {
            while (resultSet.next()) {
                Object value = resultSet.getObject(1);
                return Optional.<T>of(TypeUtils.cast(value, clazz));
            }
        }
        while (resultSet.next()) {
            EntityCache entityCache = EntityCache.forClass(clazz);
            T instance = (T) entityCache.newInstance();
            for (int i = 0; i < colLength; i++) {
                String columnName;
                String labelName = metaData.getColumnLabel(i + 1);
                if (StringUtils.isNotBlank(labelName)) {
                    columnName = labelName;
                } else {
                    columnName = metaData.getColumnName(i + 1);
                }
                Object columnValue = resultSet.getObject(columnName);
                if (columnValue == null) {
                    columnValue = "";
                }
                String fieldName = StringUtils.convertToHump(columnName);
                Optional<Field> fieldOptional = entityCache.getField(fieldName);
                if (fieldOptional.isPresent()) {
                    Field field = fieldOptional.get();
                    entityCache.setValue(instance, field.getName(), TypeUtils.cast(columnValue, field.getType()));
                }
            }
            optional = Optional.of(instance);
        }
        return optional;
    }

    /**
     * 查询多条记录
     *
     * @param sql    将要执行的sql语句
     * @param params 查询的参数
     * @param clazz  返回的对象
     * @return
     */
    @SneakyThrows
    public <T> List<T> queryForList(@NonNull String sql, Map<String, Object> params, @NonNull Class<T> clazz) {
        @Cleanup Connection connection = DriverManager.getConnection(url, username, password);
        Map<Integer, Object> statementParam = new TreeMap<>();
        List<T> instanceList = new ArrayList<T>();
        if (MapUtils.isNotEmpty(params)) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                int index = sql.indexOf(":" + entry.getKey());
                if (index != -1) {
                    if (entry.getValue() instanceof Iterable) {
                        StringBuilder builder = new StringBuilder();
                        for (Object value : (Iterable) entry.getValue()) {
                            builder.append("?").append(",");
                            statementParam.put(index, value);
                            index++;
                        }
                        sql = sql.replace(":" + entry.getKey(), builder.substring(0, builder.length() - 1));
                    } else if (entry.getValue().getClass().isArray()) {
                        StringBuilder builder = new StringBuilder();
                        for (Object value : (Object[]) entry.getValue()) {
                            builder.append("?").append(",");
                            statementParam.put(index, value);
                            index++;
                        }
                        sql = sql.replace(":" + entry.getKey(), builder.substring(0, builder.length() - 1));
                    } else {
                        sql = sql.replace(":" + entry.getKey(), "?");
                        statementParam.put(index, entry.getValue());
                    }
                }
            }
        }
        @Cleanup PreparedStatement preparedStatement = connection.prepareStatement(sql);
        int index = 1;
        for (Object value : statementParam.values()) {
            preparedStatement.setString(index, value.toString());
            index++;
        }
        @Cleanup ResultSet resultSet = preparedStatement.executeQuery();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int colLength = metaData.getColumnCount();
        while (resultSet.next()) {
            EntityCache entityCache = EntityCache.forClass(clazz);
            T instance = (T) entityCache.newInstance();
            for (int i = 0; i < colLength; i++) {
                String columnName;
                String labelName = metaData.getColumnLabel(i + 1);
                if (StringUtils.isNotBlank(labelName)) {
                    columnName = labelName;
                } else {
                    columnName = metaData.getColumnName(i + 1);
                }
                Object columnValue = resultSet.getObject(columnName);
                if (columnValue == null) {
                    columnValue = "";
                }
                String fieldName = StringUtils.convertToHump(columnName);
                Optional<Field> optional = entityCache.getField(fieldName);
                if (optional.isPresent()) {
                    Field field = optional.get();
                    entityCache.setValue(instance, field.getName(), TypeUtils.cast(columnValue, field.getType()));
                }
            }
            instanceList.add(instance);
        }
        return instanceList;
    }
}

package com.efeichong.jpa;

import com.efeichong.common.PageData;
import lombok.NonNull;
import org.springframework.data.domain.Pageable;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author lxk
 * @date 2024/5/30
 * @description
 */
public interface FastJpaSqlRepository {

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @param params      查询参数
     * @return
     */
    <T> List<T> selectBySql(@NonNull String sql, @NonNull Class<T> resultClazz, @NonNull Map<String, Object> params);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @return
     */
    <T> List<T> selectBySql(@NonNull String sql, @NonNull Class<T> resultClazz);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @param params      查询参数
     * @return
     */
    <T> T selectOneBySql(@NonNull String sql, @NonNull Class<T> resultClazz, @NonNull Map<String, Object> params);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @return
     */
    <T> T selectOneBySql(@NonNull String sql, @NonNull Class<T> resultClazz);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @param params      查询参数
     * @return
     */
    <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Pageable page, @NonNull Class<T> resultClazz, Map<String, Object> params);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @return
     */
    <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Pageable page, @NonNull Class<T> resultClazz);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @return
     */
    <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Class<T> resultClazz);

    /**
     * native查询
     *
     * @param sql         查询sql
     * @param resultClazz 返回的对象 class
     * @return
     */
    <T> PageData<T> selectByPage(@NonNull String sql, @NonNull Class<T> resultClazz, Map<String, Object> params);

    /**
     * 批量新增 直接执行sql
     * @param sql INSERT INTO user (name, email) VALUES (?, ?)
     * @param entities
     * @param batchSize
     */
    void batchSave(String sql, Collection<Map<String, Object>>  entities, int batchSize);

    void save(String sql, Map<String, Object>  entity);

    <T> void batchSaveEntities(String sql, Collection<T>  entities, int batchSize);

    <T> void saveEntity(String sql, T t);

    void batchDelete(String tableName);

    /**
     * 批量删除
     * @param ids
     * @param <ID>
     */
    <ID> void batchDelete(String tableName,@NonNull Collection<ID> ids);

}

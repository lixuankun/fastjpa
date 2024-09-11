package com.efeichong.jpa;

import com.efeichong.common.PageData;
import lombok.NonNull;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.Repository;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 基础dao 包含增删改查及native查询
 */
@NoRepositoryBean
public interface FastJpaRepository<T, ID> extends Repository<T, ID> {
    /**
     * 查询全表
     *
     * @return 如果没有结果将返回空集合并非null
     */
    List<T> selectAll();

    /**
     * 查询全表返回Vo
     *
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return
     */
    <V> List<V> selectAll(Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 查询结果只有一条
     *
     * @param example 查询条件  无法使用分页
     * @return 如果不存在将会返回{@link Optional#empty()}
     * @throws javax.persistence.NonUniqueResultException 如果返回对象数超过一个将会抛这个异常
     */
    Optional<T> selectOne(@NonNull JExample<T> example);

    /**
     * 查询结果只有一条
     *
     * @param example          查询条件  无法使用分页
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return 如果不存在将会返回{@link Optional#empty()}
     * @throws javax.persistence.NonUniqueResultException 如果返回对象数超过一个将会抛这个异常
     */
    <V> Optional<V> selectOne(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 条件查询
     *
     * @param example 查询条件  开启分页后返回分页后的list对象
     * @return 如果没有结果将返回空集合并非null
     */
    List<T> selectAll(@Nullable JExample<T> example);

    /**
     * 条件查询返回Vo
     *
     * @param example          查询条件  开启分页后返回分页后的list对象
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return
     */
    <V> List<V> selectAll(@Nullable JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 通过主键查询
     *
     * @param id 主键
     * @return 如果不存在将会返回{@link Optional#empty()}
     */
    Optional<T> selectById(@NonNull ID id);

    /**
     * 通过主键查询 悲观锁
     *
     * @param id 主键
     * @return 如果不存在将会返回{@link Optional#empty()}
     */
    Optional<T> selectByIdForUpdate(@NonNull ID id);

    /**
     * 通过主键查询返回vo
     *
     * @param id
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @param <V>
     * @return
     */
    <V> Optional<V> selectById(@NonNull ID id, Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 通过id查询列表
     *
     * @param ids
     * @return
     */
    List<T> selectByIds(@NonNull Collection<ID> ids);

    /**
     * 通过id查询列表返回Vo
     *
     * @param ids
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return
     */
    <V> List<V> selectByIds(@NonNull Collection<ID> ids, Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 根据条件查询库中的行数
     *
     * @param example 查询条件
     * @return 行数
     */
    long count(@Nullable JExample<T> example);

    /**
     * 是否存在满足该条件的记录
     *
     * @param example 查询条件
     * @return false不存在  true存在
     */
    boolean exist(@Nullable JExample<T> example);

    /**
     * 是否存在该id
     *
     * @param id 主键
     * @return false不存在  true存在
     */
    boolean existsById(@NonNull ID id);

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @param example 查询条件
     * @return 返回分页对象
     */
    PageData<T> selectByPage(@NonNull JExample<T> example);

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @param example          查询条件
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return 返回分页对象
     */
    <V> PageData<V> selectByPage(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @return 返回分页对象
     */
    PageData<T> selectByPage();

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @param voClazz          返回的vo  里面的字段可以通过{@link com.efeichong.mapping.Mapping} 进行映射
     * @param ignoreProperties 不需要查到vo的字段
     * @return 返回分页对象
     */
    <V> PageData<V> selectByPage(Class<? extends V> voClazz, String... ignoreProperties);

    /**
     * 新增/更新  id不在则新增，存在则更新(默认所有字段不能更改为空)
     *
     * @param entity
     * @param <S>
     * @return
     */
    <S extends T> S save(@NonNull S entity);

    <S extends T> List<S> saveAll(@NonNull Collection<S> entities);


    /**
     * 删除 （当实体类有@LogicDelete注解时触发逻辑删除）
     *
     * @param entity
     */
    void delete(@NonNull T entity);

    /**
     * 通过id删除（当实体类有@LogicDelete注解时触发逻辑删除）
     *
     * @param id
     */
    void deleteById(@NonNull ID id);

    /**
     * 删除全部（当实体类有@LogicDelete注解时触发逻辑删除）
     */
    void deleteAll();

    /**
     * 批量删除（当实体类有@LogicDelete注解时触发逻辑删除）
     *
     * @param entities 对象集合
     */
    void deleteAll(@NonNull Collection<? extends T> entities);

    /**
     * 根据id批量删除（当实体类有@LogicDelete注解时触发逻辑删除）
     *
     * @param ids id集合
     * @return
     */
    void deleteAllByIds(@NonNull Collection<ID> ids);

    /**
     * 根据条件删除（当实体类有@LogicDelete注解时触发逻辑删除）
     *
     * @param example
     */
    void deleteAll(@NonNull JExample<T> example);

    /**
     * 将持久化上下文同步到底层数据库
     */
    void flush();

    /**
     * 检查实例是否是属于当前持久性上下文的托管实体实例
     */
    boolean contains(Object entity);

    /**
     * 清除持久性上下文，导致所有托管实体分离。 对尚未刷新到数据库的实体所做的更改将不会被持久化
     */
    void clear();

    /**
     * 从持久化上下文中删除给定的实体，导致托管实体分离。 对实体所做的未刷新更改（包括删除实体）将不会同步到数据库。 先前引用分离实体的实体将继续引用它。
     *
     * @param entity
     */
    void detach(Object entity);

    /**
     * 从持久化上下文中删除给定的实体，导致托管实体分离。 对实体所做的未刷新更改（包括删除实体）将不会同步到数据库。 先前引用分离实体的实体将继续引用它。
     *
     * @param entities
     */
    void detachCollection(Collection entities);




}

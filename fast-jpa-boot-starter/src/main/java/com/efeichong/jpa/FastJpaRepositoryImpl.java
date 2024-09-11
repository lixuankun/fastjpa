package com.efeichong.jpa;

import com.efeichong.audit.DefaultValueProvider;
import com.efeichong.audit.InsertDefault;
import com.efeichong.audit.UpdateDefault;
import com.efeichong.cache.EntityCache;
import com.efeichong.common.PageData;
import com.efeichong.exception.JpaException;
import com.efeichong.logicDelete.LogicDelete;
import com.efeichong.mapping.Mapping;
import com.efeichong.mapping.UseMode;
import com.efeichong.multiTenant.EnableMultiTenant;
import com.efeichong.multiTenant.MultiTenantIdHolder;
import com.efeichong.util.EntityUtils;
import com.efeichong.util.FastJpaSpringUtils;
import com.efeichong.util.TransformUtils;
import com.efeichong.uuid.IdGenerator;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.NonNull;
import lombok.SneakyThrows;
import org.hibernate.query.criteria.internal.path.AbstractPathImpl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.EmptySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.persistence.metamodel.Attribute;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 基础dao 需在这里指定 {@link EnableJpaRepositories#repositoryBaseClass() }
 */
@NoRepositoryBean //告诉JPA不要创建对应接口的bean对象
@Transactional(readOnly = true)
public class FastJpaRepositoryImpl<T, ID> extends AbstractFastJpaRepository<T, ID> implements FastJpaRepository<T, ID> {


    private static final String AND = "and";
    private static final String GT = ">";
    private static final String GE = ">=";
    private static final String LT = "<";
    private static final String LE = "<=";
    private static final String EQ = "=";
    private static final String NE = "!=";

    private Cache cache = CacheBuilder.newBuilder()
            .build();

    public FastJpaRepositoryImpl(Class<T> domainClass, EntityManager em) {
        super(domainClass, em);
    }

    public FastJpaRepositoryImpl(JpaEntityInformation<T, ?> entityInformation, EntityManager entityManager) {
        super(entityInformation, entityManager);
    }


    /**
     * 字段值转字符串
     *
     * @param value
     * @return
     */
    private static String convertString(Object value) {
        if (value == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder("'");
        return builder.append(value.toString()).append("'").toString();
    }

    /**
     * 有序分组
     *
     * @param classifier
     * @param <T>
     * @param <K>
     * @return
     */
    private static <T, K> Collector<T, ?, LinkedHashMap<K, List<T>>> groupingBy(Function<? super T, ? extends K> classifier) {
        return Collectors.groupingBy(classifier, LinkedHashMap::new, Collectors.toList());
    }

    @Override
    public List<T> selectAll() {
        TypedQuery<T> query;
        if (hasEnableMultiTenant || hasFakeDel) {
            JExample example = new JExample();
            query = super.getQuery(example, Sort.unsorted());
            query.setFirstResult(0);
            if (hasFakeDel && !example.getIgnoreLogicDel()) {
                example.and().andEqualTo(logicDeleteColumn, 0);
            }
            if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
                queryMultiTenant(example);
            }
        } else {
            query = super.getQuery(null, Sort.unsorted());
            query.setFirstResult(0);
        }
        return query.getResultList();
    }

    @Override
    public <V> List<V> selectAll(Class<? extends V> voClazz, String... ignoreProperties) {
        JExample example = new JExample();
        return this.selectAll(example, voClazz, ignoreProperties);
    }

    @Override
    public Optional<T> selectOne(JExample<T> example) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        TypedQuery<T> query = super.getQuery(example, Sort.unsorted());
        if (example.getPage() != null) {
            Pageable page = example.getPage();
            query.setFirstResult((int) example.getPage().getOffset());
            query.setMaxResults(page.getPageSize());
        } else {
            query.setFirstResult(0);
        }
        if (example.getForUpdate()) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        List<T> resultList = query.getResultList();
        if (EntityUtils.isEmpty(resultList)) {
            return Optional.empty();
        } else {
            if (resultList.size() > 1) {
                throw new JpaException("query did not return a unique result: " + resultList.size());
            }
            return Optional.of(resultList.get(0));
        }
    }

    @Override
    public <V> Optional<V> selectOne(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        QueryCacheResult queryCacheResult = cacheResult(criteriaBuilder, voClazz, ignoreProperties, example);
        CriteriaQuery query = queryCacheResult.getQuery();
        Root root = queryCacheResult.getRoot();
        List<TupleGroup> groups = queryCacheResult.getGroups();
        Predicate predicate = example.toPredicate(root, query, criteriaBuilder);
        if (predicate != null) {
            query.where(predicate);
        }
        TypedQuery typedQuery = entityManager.createQuery(query);
        List<V> buildVos;
        if (example.getPage() != null) {
            Pageable page = example.getPage();
            typedQuery.setFirstResult((int) example.getPage().getOffset());
            typedQuery.setMaxResults(page.getPageSize());
            Page<Tuple> tuplePage = PageableExecutionUtils.getPage(typedQuery.getResultList(), example.getPage(),
                    () -> count(example));
            EntityCache voEntityCache = EntityCache.forClass(voClazz);
            buildVos = buildVos(groups, tuplePage.getContent(), voEntityCache);
        } else {
            typedQuery.setFirstResult(0);
            EntityCache voEntityCache = EntityCache.forClass(voClazz);
            buildVos = buildVos(groups, typedQuery.getResultList(), voEntityCache);

        }
        if (EntityUtils.isEmpty(buildVos)) {
            return Optional.empty();
        }
        if (buildVos.size() > 1) {
            throw new JpaException("query did not return a unique result: " + buildVos.size());
        }
        return Optional.of(buildVos.get(0));
    }

    @Override
    public List<T> selectAll(JExample<T> example) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        TypedQuery<T> query = super.getQuery(example, Sort.unsorted());
        if (example.getPage() != null) {
            Pageable page = example.getPage();
            query.setFirstResult((int) example.getPage().getOffset());
            query.setMaxResults(page.getPageSize());
        } else {
            query.setFirstResult(0);
        }
        if (example.getForUpdate()) {
            query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
        }
        return query.getResultList();
    }

    @Override
    public <V> List<V> selectAll(JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        QueryCacheResult queryCacheResult = cacheResult(criteriaBuilder, voClazz, ignoreProperties, example);
        List<TupleGroup> groups = queryCacheResult.getGroups();
        if (groups.isEmpty()) {
            Root root = queryCacheResult.getRoot();
            CriteriaQuery query = queryCacheResult.getQuery();
            Predicate predicate = example.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
            TypedQuery typedQuery = entityManager.createQuery(query);
            if (example.getPage() != null) {
                Pageable page = example.getPage();
                typedQuery.setFirstResult((int) example.getPage().getOffset());
                typedQuery.setMaxResults(page.getPageSize());
                Page<Tuple> tuplePage = PageableExecutionUtils.getPage(typedQuery.getResultList(), example.getPage(),
                        () -> count(example));
                EntityCache voEntityCache = EntityCache.forClass(voClazz);
                List<V> buildVos = buildVos(groups, tuplePage.getContent(), voEntityCache);
                return buildVos;
            } else {
                typedQuery.setFirstResult(0);
                EntityCache voEntityCache = EntityCache.forClass(voClazz);
                List<V> buildVos = buildVos(groups, typedQuery.getResultList(), voEntityCache);
                return buildVos;
            }

        } else {
            CriteriaQuery<Tuple> idQuery = criteriaBuilder.createTupleQuery();
            Root root = idQuery.from(entityCache.getClazz());
            idQuery.multiselect(root.get(primaryKey));
            Predicate predicate = example.toPredicate(root, idQuery, criteriaBuilder);
            if (predicate != null) {
                idQuery.where(predicate);
            }
            TypedQuery<Tuple> idTupleTypedQuery = entityManager.createQuery(idQuery);
            if (example.getPage() != null) {
                Pageable page = example.getPage();
                idTupleTypedQuery.setFirstResult((int) example.getPage().getOffset());
                idTupleTypedQuery.setMaxResults(page.getPageSize());
                Page<Tuple> tuplePage = PageableExecutionUtils.getPage(idTupleTypedQuery.getResultList(), example.getPage(),
                        () -> count(example));
                List ids = tuplePage.getContent().stream().map(v -> v.get(0)).collect(Collectors.toList());
                if (EntityUtils.isEmpty(ids)) {
                    return Collections.emptyList();
                }
                Root resultRoot = queryCacheResult.getRoot();
                CriteriaQuery query = queryCacheResult.getQuery();
                query.where(JExample.in(ids, criteriaBuilder, resultRoot.get(primaryKey)));
                example.createGroupBy(criteriaBuilder, resultRoot, query, example.groupByPropertyList);
                example.createOrderBy(criteriaBuilder, resultRoot, query, example.orderByList);
                List<Tuple> tuples = entityManager.createQuery(query).getResultList();
                EntityCache voEntityCache = EntityCache.forClass(voClazz);
                List<V> buildVos = buildVos(groups, tuples, voEntityCache);
                return buildVos;
            } else {
                idTupleTypedQuery.setFirstResult(0);
                List ids = idTupleTypedQuery.getResultList().stream().map(v -> v.get(0)).collect(Collectors.toList());
                if (EntityUtils.isEmpty(ids)) {
                    return Collections.emptyList();
                }
                Root resultRoot = queryCacheResult.getRoot();
                CriteriaQuery query = queryCacheResult.getQuery();
                query.where(JExample.in(ids, criteriaBuilder, resultRoot.get(primaryKey)));
                example.createGroupBy(criteriaBuilder, resultRoot, query, example.groupByPropertyList);
                example.createOrderBy(criteriaBuilder, resultRoot, query, example.orderByList);
                List<Tuple> tuples = entityManager.createQuery(query).getResultList();
                EntityCache voEntityCache = EntityCache.forClass(voClazz);
                List<V> buildVos = buildVos(groups, tuples, voEntityCache);
                return buildVos;
            }

        }
    }

    @Override
    public Optional<T> selectById(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        JExample example = new JExample();
        example.and().andEqualTo(primaryKey, id);
        return this.selectOne(example);
    }

    @Override
    public Optional<T> selectByIdForUpdate(ID id) {
        if (id == null) {
            return Optional.empty();
        }
        JExample example = new JExample();
        example.createCriteria()
                .andEqualTo(primaryKey, id);
        example.setForUpdate(true);
        return this.selectOne(example);
    }



    @Override
    public <V> Optional<V> selectById(@NonNull ID id, Class<? extends V> voClazz, String... ignoreProperties) {
        JExample example = new JExample();
        example.and().andEqualTo(primaryKey, id);
        return this.selectOne(example, voClazz, ignoreProperties);
    }

    @Override
    public List<T> selectByIds(Collection<ID> ids) {
        if (EntityUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        JExample example = new JExample();
        example.and().andIn(primaryKey, ids);
        return this.selectAll(example);
    }

    @Override
    public <V> List<V> selectByIds(@NonNull Collection<ID> ids, @NonNull Class<? extends V> voClazz, String... ignoreProperties) {
        if (EntityUtils.isEmpty(ids)) {
            return new ArrayList<>();
        }
        JExample example = new JExample();
        example.and().andIn(primaryKey, ids);
        return this.selectAll(example, voClazz, ignoreProperties);

    }

    @Override
    public long count(JExample<T> example) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        return super.count(example);
    }

    @Override
    public boolean exist(JExample<T> example) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        return super.count(example) > 0;
    }

    @Override
    public boolean existsById(ID id) {
        if (id == null) {
            return false;
        }
        if (hasEnableMultiTenant || hasFakeDel) {
            return selectById(id).isPresent();
        }
        return super.existsById(id);
    }

    @Override
    public PageData<T> selectByPage(JExample<T> example) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        startPage(example);

        PageData<T> pageData = new PageData();
        Page page = super.findAll(example, example.getPage());
        pageData.setList(page.getContent());
        pageData.setPageIndex(page.getNumber() + 1);
        pageData.setPageSize(page.getSize());
        pageData.setTotalPage(page.getTotalPages());
        pageData.setTotalRows((int) page.getTotalElements());
        return pageData;
    }

    @Override
    public <V> PageData<V> selectByPage(@NonNull JExample<T> example, Class<? extends V> voClazz, String... ignoreProperties) {
        if (hasFakeDel && !example.getIgnoreLogicDel()) {
            example.and().andEqualTo(logicDeleteColumn, 0);
        }
        if (hasEnableMultiTenant && !example.getIgnoreMultiTenant()) {
            queryMultiTenant(example);
        }
        PageData<V> pageData = new PageData();
        startPage(example);
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        QueryCacheResult queryCacheResult = cacheResult(criteriaBuilder, voClazz, ignoreProperties, example);
        List<TupleGroup> groups = queryCacheResult.getGroups();
        if (groups.isEmpty()) {
            Root root = queryCacheResult.getRoot();
            CriteriaQuery query = queryCacheResult.getQuery();
            Predicate predicate = example.toPredicate(root, query, criteriaBuilder);
            if (predicate != null) {
                query.where(predicate);
            }
            TypedQuery typedQuery = entityManager.createQuery(query);
            typedQuery.setFirstResult((int) example.getPage().getOffset());
            typedQuery.setMaxResults(example.getPage().getPageSize());
            Page<Tuple> tuplePage = PageableExecutionUtils.getPage(typedQuery.getResultList(), example.getPage(),
                    () -> count(example));
            EntityCache voEntityCache = EntityCache.forClass(voClazz);
            List<V> buildVos = buildVos(groups, tuplePage.getContent(), voEntityCache);
            pageData.setList(buildVos);
            pageData.setPageIndex(tuplePage.getNumber() + 1);
            pageData.setPageSize(tuplePage.getSize());
            pageData.setTotalPage(tuplePage.getTotalPages());
            pageData.setTotalRows((int) tuplePage.getTotalElements());
            return pageData;
        } else {
            CriteriaQuery<Tuple> idQuery = criteriaBuilder.createTupleQuery();
            Root root = idQuery.from(entityCache.getClazz());
            idQuery.multiselect(root.get(primaryKey));
            Predicate predicate = example.toPredicate(root, idQuery, criteriaBuilder);
            if (predicate != null) {
                idQuery.where(predicate);
            }
            TypedQuery<Tuple> idTupleTypedQuery = entityManager.createQuery(idQuery);
            idTupleTypedQuery.setFirstResult((int) example.getPage().getOffset());
            idTupleTypedQuery.setMaxResults(example.getPage().getPageSize());
            Page<Tuple> tuplePage = PageableExecutionUtils.getPage(idTupleTypedQuery.getResultList(), example.getPage(),
                    () -> count(example));
            List ids = tuplePage.getContent().stream().map(v -> v.get(0)).collect(Collectors.toList());
            if (EntityUtils.isEmpty(ids)) {
                pageData.setList(Collections.emptyList());
                pageData.setPageIndex(tuplePage.getNumber() + 1);
                pageData.setPageSize(tuplePage.getSize());
                pageData.setTotalPage(tuplePage.getTotalPages());
                pageData.setTotalRows((int) tuplePage.getTotalElements());
                return pageData;
            }
            Root resultRoot = queryCacheResult.getRoot();
            CriteriaQuery query = queryCacheResult.getQuery();
            query.where(JExample.in(ids, criteriaBuilder, resultRoot.get(primaryKey)));

            example.createGroupBy(criteriaBuilder, resultRoot, query, example.groupByPropertyList);
            example.createOrderBy(criteriaBuilder, resultRoot, query, example.orderByList);
            List<Tuple> tuples = entityManager.createQuery(query).getResultList();
            EntityCache voEntityCache = EntityCache.forClass(voClazz);
            List<V> buildVos = buildVos(groups, tuples, voEntityCache);
            pageData.setList(buildVos);
            pageData.setPageIndex(tuplePage.getNumber() + 1);
            pageData.setPageSize(tuplePage.getSize());
            pageData.setTotalPage(tuplePage.getTotalPages());
            pageData.setTotalRows((int) tuplePage.getTotalElements());
            return pageData;
        }
    }

    @Override
    public PageData<T> selectByPage() {
        JExample<T> example = new JExample();
        return this.selectByPage(example);
    }

    @Override
    public <V> PageData<V> selectByPage(Class<? extends V> voClazz, String... ignoreProperties) {
        JExample<T> example = new JExample();
        return this.selectByPage(example, voClazz, ignoreProperties);
    }



    /**
     * 新增/更新  id不在则新增，存在则更新(默认所有字段不能更改为空)
     *
     * @param entity
     * @param <S>
     * @return
     */
    @Transactional
    @Override
    public <S extends T> S save(S entity) {

        if (EntityUtils.isCglibProxy(entity.getClass())) {
            Object proxy = TransformUtils.getEntityByProxy(entity);
            EntityUtils.copyProperties(entity, proxy);
            entity = (S) proxy;
        }
        EntityCache entityCache = EntityCache.forClass(entity.getClass());
        Field idField = entityCache.getField(primaryKey);
        if (idField == null) {
            throw new JpaException("No identifier specified for entity");
        }
        if (contains(entity)) {
            return (S) this.update(null, entity);
        } else {
            Object value = entityCache.getValue(entity, idField.getName());
            if (value == null) {
                return this.persist(entity);
            }

            Optional<T> optional = super.findById((ID) value);
            if (optional.isPresent()) {
                return (S) this.update(entity, optional.get());
            }
            return this.persist(entity);
        }

    }


    /**
     * 批量新增/更新  id不在则新增，存在则更新(默认所有字段不能更改为空)
     *
     * @return
     */
    @Transactional
    @Override
    public <S extends T> List<S> saveAll(Collection<S> entities) {
        List<S> list = new ArrayList<>();
        for (S t : entities) {
            S save = this.save(t);
            list.add(save);
        }
        return list;
    }

    @Transactional
    @Override
    public void delete(T entity) {
        if (hasFakeDel) {
            entityCache.setValue(entity, logicDeleteColumn, TypeUtils.cast(IdGenerator.getId(), logicDeleteField.getType()));
            this.save(entity);
        } else {
            super.delete(entity);
        }
    }

    @Transactional
    @Override
    public void deleteById(ID id) {
        this.delete(selectById(id).orElseThrow(() -> new JpaException(
                String.format("No %s entity with id %s exists!", entityInformation.getJavaType(), id))));
    }

    @Transactional
    @Override
    public void deleteAll() {
        for (T element : selectAll()) {
            this.delete(element);
        }
    }

    @Transactional
    @Override
    public void deleteAll(Collection<? extends T> entities) {
        for (T entity : entities) {
            this.delete(entity);
        }
    }

    @Transactional
    @Override
    public void deleteAllByIds(Collection<ID> ids) {
        for (ID id : ids) {
            this.deleteById(id);
        }
    }

    @Transactional
    @Override
    public void deleteAll(JExample<T> example) {
        for (T entity : selectAll(example)) {
            if (hasFakeDel && !example.getIgnoreLogicDel()) {
                entityCache.setValue(entity, logicDeleteColumn, TypeUtils.cast(IdGenerator.getId(), logicDeleteField.getType()));
                this.save(entity);
            } else {
                super.delete(entity);
            }
        }
    }
    @Override
    public boolean contains(Object entity) {
        try {
            return entityManager.contains(entity);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void clear() {
        entityManager.clear();
    }

    @Override
    public void detach(Object entity) {
        if (contains(entity)) {
            entityManager.detach(entity);
        }
    }

    @Override
    public void detachCollection(Collection entities) {
        entities.forEach(this::detach);
    }


    /**
     * 拼接值
     *
     * @param sqlBuilder
     * @param fields
     * @param entity
     */
    private void subConcat(StringBuilder sqlBuilder, List<Field> fields, Object entity) {
        if (contains(entity)) {
            throw new JpaException("请勿使用hibernate托管的对象,如需使用请先执行detach()方法");
        }
        sqlBuilder.append("(");
        Iterator<Field> iterator = fields.iterator();
        if (iterator.hasNext()) {
            Object columnValue = getColumnValue(iterator.next(), entity);
            sqlBuilder.append(columnValue);
            while (iterator.hasNext()) {
                sqlBuilder.append(",");
                sqlBuilder.append(getColumnValue(iterator.next(), entity));
            }
        }
        sqlBuilder.append(")");
    }

    /**
     * 获取字段值
     *
     * @param field
     * @param entity
     * @return
     */
    @SneakyThrows
    private Object getColumnValue(Field field, Object entity) {
        if (!TransformUtils.hasPersist(field)) {
            Object value = entityCache.getValue(entity, field.getName());
            if (field.isAnnotationPresent(InsertDefault.class) && EntityUtils.isEmpty(value)) {
                InsertDefault insertDefault = field.getAnnotation(InsertDefault.class);

                value = insertDefault.value();
                if (EntityUtils.isNotEmpty(value)) {
                    return convertString(value);
                }

                Class<? extends DefaultValueProvider> clazz = insertDefault.val();
                DefaultValueProvider defaultValueProvider = EntityUtils.newInstance(clazz);
                value = defaultValueProvider.getValue();
                return convertString(value);
            } else {
                return convertString(value);
            }
        }
        return null;
    }

    /**
     * 获取字段值
     *
     * @param field
     * @param entity
     * @return
     */
    @SneakyThrows
    private Object getColumnValueNoDefault(Field field, Object entity) {
        if (!TransformUtils.hasPersist(field)) {
            Object value = entityCache.getValue(entity, field.getName());
            return value;
        }
        return null;
    }

    /**
     * 获取字段名
     *
     * @param field
     * @return
     */
    private String getColumnName(Field field) {
        if (field.isAnnotationPresent(Column.class)) {
            Column column = field.getAnnotation(Column.class);
            if (EntityUtils.isEmpty(column.name())) {
                return wrapper(EntityUtils.convertToLine(field.getName()));
            }
            return wrapper(dealWithColumn(column.name()));
        } else if (field.isAnnotationPresent(JoinColumn.class)) {
            JoinColumn column = field.getAnnotation(JoinColumn.class);
            if (EntityUtils.isEmpty(column.name())) {
                return wrapper(EntityUtils.convertToLine(field.getName()));
            }
            return wrapper(dealWithColumn(column.name()));
        } else {
            return wrapper(EntityUtils.convertToLine(field.getName()));
        }
    }

    private String wrapper(String name) {
        return "`" + name + "`";
    }

    /**
     * 将字段名中的特殊字符去掉
     *
     * @param column
     * @return
     */
    private String dealWithColumn(String column) {
        return column.replace("[", "").replace("]", "")
                .replace("`", "");
    }

    /**
     * 分页查询，默认使用前端传的参数 @params{pageIndex,pageSize},如果前端没有传这个参数
     * 也可以通过{@link JExample#startPage(int, int)}开启，如果同时存在优先使用手动开启的
     * 不指定则默认 pageIndex=1 pageSize=10
     *
     * @param example
     */
    private void startPage(JExample example) {
        if (example.getPage() != null) {
            return;
        }
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            String pageIndex = request.getParameter("pageIndex");
            String pageSize = request.getParameter("pageSize");
            if (EntityUtils.isNotEmpty(pageIndex) && EntityUtils.isNotEmpty(pageSize)) {
                example.startPage(Integer.valueOf(pageIndex), Integer.valueOf(pageSize));
            }
        }
        if (example.getPage() == null) {
            example.startPage(1, 10);
        }
    }

    /**
     * 设置新增时字段默认值
     *
     * @param field
     * @param entity
     * @param entityCache
     * @return
     */
    @SneakyThrows
    private boolean setInsertDefaultValue(Field field, Object entity, EntityCache entityCache) {
        if (field.isAnnotationPresent(InsertDefault.class) && EntityUtils.isEmpty(entityCache.getValue(entity, field.getName()))) {
            InsertDefault insertDefault = field.getAnnotation(InsertDefault.class);

            String value = insertDefault.value();
            if (EntityUtils.isNotEmpty(value)) {
                entityCache.setValue(entity, field.getName(), TypeUtils.cast(value, field.getType()));
                return true;
            }

            Class<? extends DefaultValueProvider> clazz = insertDefault.val();
            DefaultValueProvider defaultValueProvider = EntityUtils.newInstance(clazz);
            Object val = defaultValueProvider.getValue();
            if (EntityUtils.isNotEmpty(val)) {
                entityCache.setValue(entity, field.getName(), TypeUtils.cast(val, field.getType()));
                return true;
            }
        }
        return false;
    }

    /**
     * 设置更新人信息
     *
     * @param field
     * @param entity
     * @param entityCache
     * @return
     */
    @SneakyThrows
    private boolean setUpdateDefaultValue(Field field, Object entity, EntityCache entityCache) {
        if (field.isAnnotationPresent(UpdateDefault.class)) {
            UpdateDefault updateDefault = field.getAnnotation(UpdateDefault.class);

            String value = updateDefault.value();
            if (EntityUtils.isNotEmpty(value)) {
                entityCache.setValue(entity, field.getName(), TypeUtils.cast(value, field.getType()));
                return true;
            }

            Class<? extends DefaultValueProvider> clazz = updateDefault.val();
            DefaultValueProvider defaultValueProvider = EntityUtils.newInstance(clazz);
            Object val = defaultValueProvider.getValue();
            if (EntityUtils.isNotEmpty(val)) {
                entityCache.setValue(entity, field.getName(), TypeUtils.cast(val, field.getType()));
                return true;
            }
        }
        return false;
    }

    /**
     * 逻辑删除字段默认值给0
     *
     * @param field
     * @param entity
     * @param entityCache
     * @return
     */
    @SneakyThrows
    private boolean setLogicDeleteValue(Field field, Object entity, EntityCache entityCache) {
        if (field.getName().equals(logicDeleteColumn)) {
            entityCache.setValue(entity, field.getName(), TypeUtils.cast(0, field.getType()));
            return true;
        }
        return false;
    }

    /**
     * 乐观锁字段新增时默认给0
     *
     * @param field
     * @param entity
     * @param entityCache
     * @return
     */
    @SneakyThrows
    private boolean setOptimisticValue(Field field, Object entity, EntityCache entityCache) {
        if (field.isAnnotationPresent(Version.class)) {
            entityCache.setValue(entity, field.getName(), TypeUtils.cast(0, field.getType()));
            return true;
        }
        return false;
    }


    /**
     * 新增数据设置多租户信息
     *
     * @param entity
     * @param entityCache
     * @return
     */
    private Field setMultiTenant(Object entity, EntityCache entityCache) {
        Class<?> clazz = entityCache.getClazz();
        if (clazz.isAnnotationPresent(EnableMultiTenant.class)) {
            EnableMultiTenant annotation = clazz.getAnnotation(EnableMultiTenant.class);
            String column = EntityUtils.convertToHump(annotation.column());
            Field field = entityCache.getField(column);
            if (field != null && MultiTenantIdHolder.getTenantId() != null) {
                entityCache.setValue(entity, field.getName(), MultiTenantIdHolder.getTenantId());
                return field;
            }
        }
        return null;
    }

    /**
     * 查询数据拼接多租户条件
     *
     * @return
     */
    private void queryMultiTenant(JExample<T> example) {
        EnableMultiTenant annotation = entityInformation.getJavaType().getAnnotation(EnableMultiTenant.class);
        String column = EntityUtils.convertToHump(annotation.column());
        if (MultiTenantIdHolder.getTenantId() != null) {
            if (MultiTenantIdHolder.nonValueGet()){
                example.and().andEqualTo(column, MultiTenantIdHolder.getTenantId())
                        .orIsNull(column);
            }else {
                example.and().andEqualTo(column, MultiTenantIdHolder.getTenantId());
            }


        }
    }

    @SneakyThrows
    private <S extends T> S persist(S entity) {
        if (EntityUtils.isCglibProxy(entity.getClass())) {
            Object proxy = TransformUtils.getEntityByProxy(entity);
            EntityUtils.copyProperties(entity, proxy);
            entity = (S) proxy;
        }
        if (contains(entity)) {
            throw new JpaException("数据库已存在该对象,请勿重复新增");
        }
        List<Field> fields = entityCache.getFields();
        //开启多租户
        Field multiTenantField = setMultiTenant(entity, entityCache);
        for (Field field : fields) {
            String fieldName = field.getName();
            //设置新增时字段默认值
            if (setInsertDefaultValue(field, entity, entityCache)) {
                continue;
            }
            //设置更新时字段默认值
            if (setUpdateDefaultValue(field, entity, entityCache)) {
                continue;
            }
            //逻辑删除字段默认值给0
            if (setLogicDeleteValue(field, entity, entityCache)) {
                continue;
            }
            //乐观锁字段新增时默认给0
            if (setOptimisticValue(field, entity, entityCache)) {
                continue;
            }
            if (field == multiTenantField) {
                continue;
            }

            Object propValue = toPersist(field, entityCache.getValue(entity, fieldName));
            if (propValue != null) {
                entityCache.setValue(entity, fieldName, propValue);
            }
        }
        entityManager.persist(entity);
        return entity;
    }

    /**
     * 更新(默认所有字段不能更改为空)
     *
     * @param from   游离的对象
     * @param target 本地的对象（持久化对象）
     * @param <S>
     * @return
     */
    @SneakyThrows
    private <S extends T> S update(@Nullable S from,@NonNull S target) {
        EntityCache entityCache = EntityCache.forClass(target.getClass());
        List<Field> fields = entityCache.getFields();

        for (Field field : fields) {
            String propertyName = field.getName();
            //字段为更新人无需再去查询
            if (setUpdateDefaultValue(field, target, entityCache)) {
                continue;
            }
            if (from != null) {
                Object propValue = toPersist(field, entityCache.getValue(from, propertyName));
                entityCache.setValue(target, propertyName, propValue);
            }
        }
        return (S)entityManager.merge(target);
    }

    private Object toPersist(Field field, Object propValue) {
        if (EntityUtils.isEmpty(propValue)) {
            return null;
        }
        //将对象类型的属性先查询出来变为托管状态，这个被定义为子对象 subEntity
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
            //子对象转持久化对象
            return subEntityPersist(propValue);
        }
//        if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
//            //子对象集合转持久化对象
//            return subEntitiesPersist(propValue);
//        }
        return propValue;
    }

    /**
     * 子对象转持久化对象
     *
     * @param subEntity
     * @return
     */
    private Object subEntityPersist(Object subEntity) {
        if (EntityUtils.isCglibProxy(subEntity.getClass())) {
            subEntity = TransformUtils.getEntityByProxy(subEntity);
        }
        if (contains(subEntity)) {
            return subEntity;
        }
        EntityCache subEntityCache = EntityCache.forClass(subEntity.getClass());
        //获取子对象的id
        List<Field> fields = subEntityCache.getField(Id.class);
        if (EntityUtils.isNotEmpty(fields)) {
            Class<?> aClass = subEntity.getClass();

            Field idField = fields.get(0);
            //从entity中拿到子对象
            Object idValue = subEntityCache.getValue(subEntity, idField.getName());
            if (idValue != null) {
                Object val = entityManager.find(aClass, TypeUtils.cast(idValue, idField.getType()));
                if (val != null) {
                    return val;
                } else {
                    throw new JpaException("数据不存在:" + aClass.getSimpleName() + "中不存在id -> " + idValue);
                }
            }
        }
        return null;
    }

    /**
     * 子对象集合转持久化对象
     *
     * @param subEntities
     */
    private Collection subEntitiesPersist(Object subEntities) {
        Collection persists;
        if (subEntities instanceof List) {
            persists = Lists.newArrayList();
        } else {
            persists = Sets.newHashSet();
        }
        //用户存放持久化之后的子对象集合
        //未持久化的子对象集合
        Collection collection = (Collection) subEntities;
        //将未持久化的子对象集合遍历转为持久化
        for (Object subEntity : collection) {
            Object persist = subEntityPersist(subEntity);
            if (persist != null) {
                persists.add(persist);
            }
        }
        collection.clear();
        return persists;
    }

    /**
     * 将返回值映射到Vo
     *
     * @param groups        需要进行分组的表
     * @param tuples        结果集
     * @param voEntityCache
     * @param <V>
     * @return
     */
    private <V> List<V> buildVos(List<TupleGroup> groups, List<Tuple> tuples, EntityCache voEntityCache) {
        if (EntityUtils.isEmpty(tuples)) {
            return Collections.emptyList();
        }
        if (groups.isEmpty()) {
            List vos = Lists.newArrayListWithCapacity(tuples.size());
            for (Tuple tuple : tuples) {
                if (voEntityCache.hasBasicType()) {
                    Object value = tuple.get(tuple.getElements().get(0));
                    vos.add(TypeUtils.cast(value, voEntityCache.getClazz()));
                } else {
                    V vo = voEntityCache.newInstance();
                    vos.add(vo);
                    for (TupleElement<?> element : tuple.getElements()) {
                        Attribute.PersistentAttributeType persistentAttributeType = ((AbstractPathImpl) element).getAttribute().getPersistentAttributeType();
                        Object value = tuple.get(element);
                        Field field = voEntityCache.getField(element.getAlias());
                        if (field != null && value != null) {
                            if (!Collection.class.isAssignableFrom(field.getType())) {
                                if (persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                                        persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_ONE) {
                                    Object transformVo = TransformUtils.toVo(value, voEntityCache.getField(element.getAlias()).getType());
                                    voEntityCache.setValue(vo, element.getAlias(), transformVo);
                                } else {
                                    voEntityCache.setValue(vo, element.getAlias(), value);
                                }
                            }
                        }
                    }
                }
            }
            return vos;
        } else {
            Map<Object, List<Tuple>> rootMap = tuples.stream().collect(groupingBy(tuple -> tuple.get(primaryKey)));
            List<V> vos = Lists.newArrayListWithCapacity(rootMap.size());
            for (Map.Entry<Object, List<Tuple>> entry : rootMap.entrySet()) {
                V vo = voEntityCache.newInstance();
                vos.add(vo);
                List<Tuple> tupleList = entry.getValue();
                Tuple voTuple = tupleList.get(0);
                for (TupleElement<?> element : voTuple.getElements()) {
                    Attribute.PersistentAttributeType persistentAttributeType = ((AbstractPathImpl) element).getAttribute().getPersistentAttributeType();
                    Object value = voTuple.get(element);
                    Field field = voEntityCache.getField(element.getAlias());
                    if (field != null && value != null) {
                        if (!Collection.class.isAssignableFrom(field.getType())) {
                            if (persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_ONE ||
                                    persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_ONE) {
                                Object transformVo = TransformUtils.toVo(value, voEntityCache.getField(element.getAlias()).getType());
                                voEntityCache.setValue(vo, element.getAlias(), transformVo);
                            } else {
                                voEntityCache.setValue(vo, element.getAlias(), value);
                            }
                        }
                    }
                }
                for (TupleGroup tupleGroup : groups) {
                    String group = tupleGroup.getGroup();
                    Field groupField = voEntityCache.getField(group);
                    if (!Collection.class.isAssignableFrom(groupField.getType())) {
                        throw new JpaException(group + "必须为集合类型");
                    }
                    List subVos = new ArrayList();
                    List subIds = new ArrayList();
                    for (Tuple tuple : tupleList) {
                        Optional<TupleElement<?>> optional = tuple.getElements().stream().filter(t -> t.getAlias().equals(group)).findFirst();
                        if (optional.isPresent()) {
                            Object value = tuple.get(group);
                            if (value == null){
                                continue;
                            }

                            TupleElement<?> element = optional.get();
                            Attribute.PersistentAttributeType persistentAttributeType = ((AbstractPathImpl) element).getAttribute().getPersistentAttributeType();
                            if (persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_MANY ||
                                    persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_MANY) {
                                EntityCache subEntityCache = EntityCache.forClass(element.getJavaType());
                                List<Field> idFields = subEntityCache.getField(Id.class);
                                if (EntityUtils.isNotEmpty(idFields)){
                                    Object idValue = subEntityCache.getValue(value, idFields.get(0).getName());
                                    if (subIds.contains(idValue)){
                                        continue;
                                    }
                                    subIds.add(idValue);
                                }
                                subVos.add(TransformUtils.toVo(value, tupleGroup.getType()));
                            }else {
                                subVos.add(value);
                            }
                        }
                    }
                    voEntityCache.setValue(vo, group, subVos);
                }
            }
            return vos;
        }
    }

    /**
     * 拼接要返回字段并缓存
     *
     * @param criteriaBuilder
     * @param voClazz
     * @param ignoreProperties
     * @param example
     * @return
     */
    private QueryCacheResult cacheResult(CriteriaBuilder criteriaBuilder, Class<?> voClazz, String[] ignoreProperties, JExample<T> example) {
//        String ignorePropertyJoin = EntityUtils.join(ignoreProperties, ",");
//        String queryKey = "CriteriaQuery" + voClazz.getName() + ignorePropertyJoin;
//        QueryCacheResult queryCacheResult = (QueryCacheResult) cache.get(queryKey, selection -> {
        List<TupleGroup> groups = new ArrayList();
        CriteriaQuery<Tuple> tupleQuery = criteriaBuilder.createTupleQuery();
        Root root = tupleQuery.from(entityCache.getClazz());
        List<Field> poFields = entityCache.getFields()
                .stream()
                .filter(field -> !field.isAnnotationPresent(Transient.class) &&
                                !field.isAnnotationPresent(org.springframework.data.annotation.Transient.class)
                        )
                .collect(Collectors.toList());
        EntityCache voEntityCache = EntityCache.forClass(voClazz);
        List<Field> voFields = voEntityCache.getFields();
        List<Selection<?>> selections = Lists.newArrayListWithCapacity(voFields.size() + 1);

        if (EntityUtils.isNotEmpty(example.getSelectionProperties())) {
            for (String selectionProperty : example.getSelectionProperties()) {
                selections.add(root.get(selectionProperty).alias(selectionProperty));
            }
            tupleQuery.multiselect(selections);
            return new QueryCacheResult(root, tupleQuery, groups);
        }

        if (EntityUtils.isNotEmpty(example.getSelectionExpressions())) {
            for (SelectionExpression selectionExpression : example.getSelectionExpressions()) {
                Selection selection = selectionExpression.genSelection(root, criteriaBuilder);
                selections.add(selection);
            }
            tupleQuery.multiselect(selections);
            return new QueryCacheResult(root, tupleQuery, groups);
        }

        for (Field voField : voFields) {
            if (EntityUtils.contains(ignoreProperties, voField.getName())) {
                continue;
            }

            if (voField.isAnnotationPresent(Mapping.class)) {
                Mapping mapping = voField.getAnnotation(Mapping.class);
                if (EntityUtils.contains(mapping.useMode(), UseMode.ALL) || EntityUtils.contains(mapping.useMode(), UseMode.TO_VO)) {
                    String poProperty = mapping.poProperty();
                    boolean hasGroupBy = false;
                    if (poProperty.indexOf(".") != -1) {
                        Path path = root;
                        String[] recursionProperties = poProperty.split("\\.");
                        for (int i = 0; i < recursionProperties.length; i++) {
                            String recursionProperty = recursionProperties[i];
                            Attribute.PersistentAttributeType persistentAttributeType = ((AbstractPathImpl) path.get(recursionProperty)).getAttribute().getPersistentAttributeType();
                            if (persistentAttributeType == Attribute.PersistentAttributeType.BASIC) {
                                if (EntityUtils.isNotEmpty(mapping.onCondition())) {
                                    onCondition(criteriaBuilder, mapping, root);
                                }
                                path = path.get(recursionProperty);


                            } else {
                                if (persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_MANY ||
                                        persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_MANY) {
                                    hasGroupBy = true;
                                }
                                path = JExample.join(((From) path), recursionProperty, criteriaBuilder);
                            }
                            if (i == recursionProperties.length - 1) {
                                if (hasGroupBy) {
                                    Type[] typeArguments = ((ParameterizedType) voField.getGenericType()).getActualTypeArguments();
                                    Class typeArgument = (Class) typeArguments[0];
                                    groups.add(new TupleGroup(voField.getName(), typeArgument));
                                }
                                selections.add(path.alias(voField.getName()));
                            }
                        }
                    } else {
                        Attribute.PersistentAttributeType persistentAttributeType = ((AbstractPathImpl) root.get(poProperty)).getAttribute().getPersistentAttributeType();
                        if (persistentAttributeType == Attribute.PersistentAttributeType.BASIC) {
                            selections.add(root.get(poProperty).alias(voField.getName()));
                        } else {
                            if (persistentAttributeType == Attribute.PersistentAttributeType.MANY_TO_MANY ||
                                    persistentAttributeType == Attribute.PersistentAttributeType.ONE_TO_MANY) {
                                Type[] typeArguments = ((ParameterizedType) voField.getGenericType()).getActualTypeArguments();
                                Class typeArgument = (Class) typeArguments[0];
                                groups.add(new TupleGroup(voField.getName(), typeArgument));
                            }
                            Join join = JExample.join(root, poProperty, criteriaBuilder);
                            onCondition(criteriaBuilder, mapping, root);
                            selections.add(join.alias(voField.getName()));
                        }
                    }
                }
            } else {
                boolean anyMatch = poFields.stream().anyMatch(poField -> poField.getName().equals(voField.getName()) && ClassUtils.isAssignable(voField.getType(), poField.getType()));
                if (anyMatch) {
                    selections.add(root.get(voField.getName()).alias(voField.getName()));
                }
            }
        }
        if (!selections.contains(root.get(primaryKey))) {
            selections.add(root.get(primaryKey).alias(primaryKey));
        }
        tupleQuery.multiselect(selections);
        return new QueryCacheResult(root, tupleQuery, groups);
//        });
//        return queryCacheResult;
    }


    private void onCondition(CriteriaBuilder criteriaBuilder, Mapping mapping, Root root) {
        String onCondition = mapping.onCondition();
        if (EntityUtils.isEmpty(onCondition)) {
            return;
        }
        onCondition = onCondition.replaceAll(" ", "");
        Predicate predicate = null;
        int start = onCondition.indexOf("[");
        int end = onCondition.indexOf("]");
        String joinPropertyStr = onCondition.substring(0, start);
        String[] joinProperties = joinPropertyStr.split("\\.");
        From join = root;
        for (String property : joinProperties) {
            join = JExample.join(join, property, criteriaBuilder);
        }
        String[] conditions = onCondition.substring(start + 1, end).split(AND);

        for (int i = 0; i < conditions.length; i++) {
            if (i == 0) {
                predicate = buildPredicate(criteriaBuilder, conditions[i], join);
            } else {
                predicate = criteriaBuilder.and(predicate, buildPredicate(criteriaBuilder, conditions[i], join));
            }
        }

        if (join instanceof Join) {
            ((Join) join).on(predicate);
        }
    }

    private Predicate buildPredicate(CriteriaBuilder criteriaBuilder, String onCondition, From root) {
        Predicate predicate = null;
        String exp = getExp(onCondition);
        int indexOf = onCondition.indexOf(exp);
        if (indexOf != -1) {
            String[] arr = onCondition.split(exp);
            String propertyStr = arr[0];
            Object value = arr[1];

            Path path = root.get(propertyStr);

            value = TypeUtils.cast(value, path.getJavaType());
            switch (exp) {
                case ">":
                    predicate = criteriaBuilder.greaterThan(path, (Comparable) criteriaBuilder.literal(value));
                    break;
                case "<":
                    predicate = criteriaBuilder.lessThan(path, (Comparable) criteriaBuilder.literal(value));
                    break;
                case "=":
                    predicate = criteriaBuilder.equal(path, criteriaBuilder.literal(value));
                    break;
                case ">=":
                    predicate = criteriaBuilder.greaterThanOrEqualTo(path, (Comparable) criteriaBuilder.literal(value));
                    break;
                case "<=":
                    predicate = criteriaBuilder.lessThanOrEqualTo(path, (Comparable) criteriaBuilder.literal(value));
                    break;
                case "!=":
                    predicate = criteriaBuilder.notEqual(path, criteriaBuilder.literal(value));
                    break;
            }
        }
        return predicate;
    }

    private String getExp(String condition) {
        if (condition.contains(GE)) {
            return GE;
        }
        if (condition.contains(LE)) {
            return LE;
        }
        if (condition.contains(NE)) {
            return NE;
        }
        if (condition.contains(GT)) {
            return GT;
        }
        if (condition.contains(LT)) {
            return LT;
        }
        return EQ;
    }


}

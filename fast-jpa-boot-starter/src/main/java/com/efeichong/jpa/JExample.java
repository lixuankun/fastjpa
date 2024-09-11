package com.efeichong.jpa;

import com.efeichong.cache.EntityCache;
import com.efeichong.exception.JpaException;
import com.efeichong.jpa.support.LambdaMeta;
import com.efeichong.mapping.Mapping;
import com.efeichong.mapping.UseMode;
import com.efeichong.util.*;
import com.google.common.collect.Lists;
import lombok.*;
import org.hibernate.collection.internal.PersistentBag;
import org.hibernate.query.criteria.internal.path.AbstractPathImpl;
import org.hibernate.query.criteria.internal.path.SingularAttributePath;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author lxk
 * @date 2020/9/8
 * @description example查询，重写spring.data的条件连接功能
 * 支持联表查询 查询子表字段方式  主表字段.子表字段
 */
public class JExample<T> implements Specification<T>, Cloneable {
    /**
     * 默认的联表查询方式为左连接
     */
    private static final JoinType JOIN_TYPE = JoinType.LEFT;
    /**
     * 条件组
     */
    protected List<Criteria> criteriaList = null;
    /**
     * 排序
     */
    protected List<OrderBy> orderByList = new ArrayList<>();
    /**
     * 指定字段进行group by操作,有序
     */
    protected List<String> groupByPropertyList = new ArrayList<>();
    /**
     * 是否开启查询结果去重
     */
    private boolean distinct = false;
    /**
     * 分页参数
     */
    @Getter
    private Pageable page = null;
    /**
     * 是否忽略逻辑删除的条件
     * 为true时 进行逻辑删除过的数据也会被查询来
     */
    @Getter
    private Boolean ignoreLogicDel = false;
    /**
     * 是否忽略多租户查询的条件
     * 为true时 查询不区分多租户
     */
    @Getter
    private Boolean ignoreMultiTenant = false;
    /**
     * sql拼接for update
     */
    private boolean forUpdate;

    @Getter
    private SelectionExpression[] selectionExpressions;

    @Getter
    private String[] selectionProperties;

    public JExample() {
        criteriaList = new ArrayList();
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(String property, Object value,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(property, value, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(String property1, Object value1,
                                     String property2, Object value2,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(property1, value1, removeEmptyValueCondition)
                .andEqualTo(property2, value2, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(String property1, Object value1,
                                     String property2, Object value2,
                                     String property3, Object value3,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(property1, value1, removeEmptyValueCondition)
                .andEqualTo(property2, value2, removeEmptyValueCondition)
                .andEqualTo(property3, value3, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(String property1, Object value1,
                                     String property2, Object value2,
                                     String property3, Object value3,
                                     String property4, Object value4,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(property1, value1, removeEmptyValueCondition)
                .andEqualTo(property2, value2, removeEmptyValueCondition)
                .andEqualTo(property3, value3, removeEmptyValueCondition)
                .andEqualTo(property4, value4, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(String property1, Object value1,
                                     String property2, Object value2,
                                     String property3, Object value3,
                                     String property4, Object value4,
                                     String property5, Object value5,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(property1, value1, removeEmptyValueCondition)
                .andEqualTo(property2, value2, removeEmptyValueCondition)
                .andEqualTo(property3, value3, removeEmptyValueCondition)
                .andEqualTo(property4, value4, removeEmptyValueCondition)
                .andEqualTo(property5, value5, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(SFunction<T, ?> function1,
                                     Object value,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(getColumnName(function1), value, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(SFunction<T, ?> function1, Object value1,
                                     SFunction<T, ?> function2, Object value2,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(getColumnName(function1), value1, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function2), value2, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(SFunction<T, ?> function1, Object value1,
                                     SFunction<T, ?> function2, Object value2,
                                     SFunction<T, ?> function3, Object value3,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(getColumnName(function1), value1, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function2), value2, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function3), value3, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(SFunction<T, ?> function1, Object value1,
                                     SFunction<T, ?> function2, Object value2,
                                     SFunction<T, ?> function3, Object value3,
                                     SFunction<T, ?> function4, Object value4,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(getColumnName(function1), value1, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function2), value2, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function3), value3, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function4), value4, removeEmptyValueCondition);
        return example;
    }

    /**
     * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
     *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
     * @parmm value 为字段时使用 {@link Column#of(String)}
     */
    public static <T> JExample<T> of(SFunction<T, ?> function1, Object value1,
                                     SFunction<T, ?> function2, Object value2,
                                     SFunction<T, ?> function3, Object value3,
                                     SFunction<T, ?> function4, Object value4,
                                     SFunction<T, ?> function5, Object value5,
                                     boolean... removeEmptyValueCondition) {
        JExample example = new JExample<>();
        JExample.Criteria criteria = example.createCriteria();
        criteria.andEqualTo(getColumnName(function1), value1, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function2), value2, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function3), value3, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function4), value4, removeEmptyValueCondition)
                .andEqualTo(getColumnName(function5), value5, removeEmptyValueCondition);
        return example;
    }

    /**
     * in查询
     *
     * @param value           查询的值
     * @param criteriaBuilder
     * @param path
     * @return
     */
    protected static Predicate in(Object value, CriteriaBuilder criteriaBuilder, Path path) {
        Iterable iterable = (Iterable) value;
        Iterator iterator = iterable.iterator();
        CriteriaBuilder.In<Object> in = criteriaBuilder.in(path);
        while (iterator.hasNext()) {
            in.value(iterator.next());
        }
        return in;
    }

    private static String getColumnName(SFunction function) {
        LambdaMeta meta = LambdaUtils.extract(function);
        return PropertyNamer.methodToProperty(meta.getImplMethodName());
    }

    /**
     * 初始化参数
     *
     * @param queryParam 实体类
     * @return
     */
    public JExample<T> initExample(Object queryParam) {
        EntityCache entityCache = EntityCache.forClass(queryParam.getClass());
        List<Field> fields = entityCache.getFields();
        Criteria criteria = this.createCriteria();
        for (Field field : fields) {
            String fieldName = field.getName();
            String property;
            if (field.isAnnotationPresent(Mapping.class)) {
                Mapping mapping = field.getAnnotation(Mapping.class);
                if (EntityUtils.contains(mapping.useMode(), UseMode.ALL) || EntityUtils.contains(mapping.useMode(), UseMode.QUERY)) {
                    property = mapping.poProperty();
                } else {
                    property = field.getName();
                }
            } else {
                property = field.getName();
            }
            Object value = entityCache.getValue(queryParam, fieldName);
            if (field.getType().isAssignableFrom(String.class)) {
                //字符串的参数默认like
                if (value != null) {
                    String stringValue = (String) value;
                    if (stringValue.startsWith("*") && stringValue.endsWith("*")) {
                        criteria.andLike(property, stringValue.substring(1, stringValue.length() - 1), LikeType.CONTAINS, true);
                    } else if (stringValue.startsWith("*")) {
                        criteria.andLike(property, stringValue.substring(1), LikeType.LEFT, true);
                    } else if (stringValue.endsWith("*")) {
                        criteria.andLike(property, stringValue.substring(0, stringValue.length() - 1), LikeType.RIGHT, true);
                    } else {
                        criteria.andEqualTo(property, stringValue, true);
                    }
                }
            } else if (Collection.class.isAssignableFrom(field.getType())) {
                //集合类型默认使用in
                criteria.andIn(property, (Collection) value, true);
            } else if (field.getType().isArray()) {
                //数组类型默认使用in
                if (value != null) {
                    criteria.andIn(property, Arrays.asList(value), true);
                }
            } else {
                criteria.andEqualTo(property, value, true);
            }
        }
        //区间查询拼接参数
        RequestAttributes attributes = RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = ((ServletRequestAttributes) attributes).getRequest();
            Enumeration<String> parameterNames = request.getParameterNames();
            while (parameterNames.hasMoreElements()) {
                String element = parameterNames.nextElement();
                String parameter = request.getParameter(element);
                if (EntityUtils.isNotEmpty(parameter)) {
                    if (element.startsWith("begin")) {
                        String begin = EntityUtils.toLowerCaseFirstOne(element.replace("begin", ""));
                        criteria.andGreaterThanEqualTo(begin, DateUtils.parse(request.getParameter(element)));
                    }
                    if (element.startsWith("end")) {
                        String endValue = request.getParameter(element);
                        String end = EntityUtils.toLowerCaseFirstOne(element.replace("end", ""));
                        Field field = entityCache.getField(end);
                        if (field != null) {
                            if (Date.class.isAssignableFrom(field.getType())) {
                                criteria.andLessThanEqualTo(end, DateUtils.parse(endValue + " 23:59:59"));
                            } else {
                                criteria.andLessThanEqualTo(end, endValue);
                            }
                        }
                    }
                    if (element.equals("sortOptions")) {
                        Map<String, Object> sortOptions = JsonParserFactory.getJsonParser().parseMap(parameter);
                        for (Object entryObj : sortOptions.entrySet()) {
                            Map.Entry entry = (Map.Entry) entryObj;
                            Object column = entry.getKey();
                            Object sortType = entry.getValue();
                            if (sortType.equals("asc")) {
                                this.orderBy().asc((String) column);
                            } else {
                                this.orderBy().desc((String) column);
                            }
                        }
                    }
                }
            }
        }
        return this;
    }

    /**
     * 创建条件连接
     *
     * @return
     */
    public Criteria createCriteria() {
        Criteria criteria = new Criteria();
        criteriaList.add(criteria);
        return criteria;
    }

    /**
     * 新建一个条件组以and符进行连接
     */
    public Criteria and() {
        Criteria criteria = createCriteria();
        criteria.hasOr = false;
        return criteria;
    }

    /**
     * 新建条件组以or符进行连接
     */
    public Criteria or() {
        Criteria criteria = createCriteria();
        criteria.hasOr = true;
        return criteria;
    }

    /**
     * 查询指定字段 不支持(例如:user.name)格式
     *
     * @param properties
     */
    public void selectProperties(String... properties) {
        this.selectionProperties = properties;
    }

    /**
     * 查询指定字段 不支持(例如:user.name)格式
     *
     * @param sFunctions
     */
    public void selectProperties(SFunction<T, ?>... sFunctions) {
        this.selectionProperties = Arrays.stream(sFunctions).map(f -> getColumnName(f)).toArray(String[]::new);
    }

    /**
     * 查询指定字段 不支持(例如:user.name)格式
     *
     * @param selectionExpressions
     */
    public void selectProperties(SelectionExpression... selectionExpressions) {
        this.selectionExpressions = selectionExpressions;
    }

    /**
     * 排序 参数有序
     *
     * @return
     */
    public OrderBy orderBy() {
        return new OrderBy();
    }

    /**
     * 分组 参数有序
     *
     * @param properties
     */
    public JExample<T> groupBy(String... properties) {
        groupByPropertyList = Lists.newArrayList(properties);
        return this;
    }

    public JExample<T> groupBy(SFunction<T, ?>... sFunctions) {
        groupByPropertyList = Arrays.stream(sFunctions).map(f -> getColumnName(f)).collect(Collectors.toList());
        return this;
    }

    public JExample<T> setForUpdate(boolean forUpdate) {
        this.forUpdate = forUpdate;
        return this;
    }

    /**
     * 手动开启分页查询
     * <p>
     * 当进行分页查询时  baseDao.selectByPage
     * 如果没有手动开启则使用前端传的 pageIndex pageSize开启分页
     * 如果没有手动开启且前端未传参数则默认 pageIndex=1  pageSize-10开启分页
     * <p/>
     *
     * @param pageIndex 从1开始
     * @param pageSize  每页行数
     */
    public JExample<T> startPage(int pageIndex, int pageSize) {
        if (pageIndex < 1) {
            throw new JpaException("页码最小为1");
        }
        //hibernate页码是从0开始的，所以要减1
        this.page = PageRequest.of(pageIndex - 1, pageSize);
        return this;
    }

    /**
     * 关闭手动分页查询
     * <p>
     * baseDao.selectAll()不进行分页
     * baseDao.selectByPage使用前端或默认分页
     * <p/>
     */
    public JExample<T> stopPage() {
        this.page = null;
        return this;
    }

    /**
     * 查询不携带逻辑删除的条件
     */
    public JExample<T> ignoreLogicDelete() {
        this.ignoreLogicDel = true;
        return this;
    }

    /**
     * 查询不区分多租户
     */
    public JExample<T> ignoreMultiTenant() {
        this.ignoreMultiTenant = true;
        return this;
    }

    public JExample<T> setDistinct(boolean distinct) {
        this.distinct = distinct;
        return this;
    }

    /**
     * 创建一个where语句
     *
     * @param root            {@link Root}
     * @param criteriaQuery
     * @param criteriaBuilder
     * @return
     */
    @Override
    public Predicate toPredicate(Root<T> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        //查询结果是否去重
        criteriaQuery.distinct(distinct);
        //分组统计
        if (EntityUtils.isNotEmpty(groupByPropertyList)) {
            createGroupBy(criteriaBuilder, root, criteriaQuery, this.groupByPropertyList);
        }
        //拼接排序
        if (EntityUtils.isNotEmpty(orderByList)) {
            createOrderBy(criteriaBuilder, root, criteriaQuery, this.orderByList);
        }
        boolean firstPredicate = true;
        //拼接查询条件
        Predicate predicate = null;

        for (int i = 0; i < criteriaList.size(); i++) {
            Criteria criteria = criteriaList.get(i);
            //去除空条件
            if (criteria.criterionList.size() == 0) {
                continue;
            }
            if (firstPredicate) {
                predicate = generatePredicate(criteria, root, criteriaBuilder);
                if (predicate != null) {
                    firstPredicate = false;
                }
                continue;
            }
            Predicate current = generatePredicate(criteria, root, criteriaBuilder);
            if (criteria.hasOr != null && criteria.hasOr) {
                predicate = criteriaBuilder.or(predicate, current);
            } else {
                predicate = criteriaBuilder.and(predicate, current);
            }
        }
        return predicate;
    }

    /**
     * 将条件组进行拼接
     *
     * @param criteria
     * @param root
     * @param criteriaBuilder
     * @return
     */
    private Predicate generatePredicate(Criteria criteria, Root<T> root, CriteriaBuilder criteriaBuilder) {
        List<Criterion> criterionList = criteria.criterionList;
        Predicate predicate = null;
        boolean firstPredicate = true;
        for (int i = 0; i < criterionList.size(); i++) {
            Criterion criterion = criterionList.get(i);
            ExecExpression execExpression = criterion.getExecExpression();
            boolean notExpression = execExpression == null;
            if (!hasPersistField(criterion, root) && notExpression) {
                continue;
            }
            if (firstPredicate) {
                if (notExpression) {
                    predicate = generatePredicate(root, criteriaBuilder, criterion);
                } else {
                    predicate = execExpression.genExpression(root, criteriaBuilder);
                }
                firstPredicate = false;
                continue;
            }
            Predicate current;
            if (notExpression) {
                current = generatePredicate(root, criteriaBuilder, criterion);
            } else {
                current = execExpression.genExpression(root, criteriaBuilder);
            }
            if (criterion.getHasOr()) {
                predicate = criteriaBuilder.or(predicate, current);
            } else {
                predicate = criteriaBuilder.and(predicate, current);
            }
        }
        return predicate;
    }

    /**
     * 构建分组
     *
     * @param root
     * @param criteriaQuery
     */
    protected void createGroupBy(CriteriaBuilder criteriaBuilder, Root root, CriteriaQuery criteriaQuery, List<String> groupByPropertyList) {
        for (String groupBy : groupByPropertyList) {
            Path group;
            if (groupBy.indexOf(".") != -1) {
                //关联查询为字段 子表.属性
                String[] properties = groupBy.split("\\.");
                Join<Object, Object> join = null;
                for (int i = 0; i < properties.length - 1; i++) {
                    String property = properties[i];
                    if (i == 0) {
                        join = join(root, property, criteriaBuilder);
                    } else {
                        join = join(join, property, criteriaBuilder);
                    }
                }
                group = join.get(properties[properties.length - 1]);
            } else {
                group = root.get(groupBy);
            }
            criteriaQuery.groupBy(group);
        }
    }

    /**
     * 构建排序
     *
     * @param criteriaBuilder
     * @param root
     * @param criteriaQuery
     * @return
     */
    protected void createOrderBy(CriteriaBuilder criteriaBuilder, Root<T> root, CriteriaQuery<?> criteriaQuery, List<OrderBy> orderByList) {
        List<Order> orders = new ArrayList<>();
        for (OrderBy orderBy : orderByList) {
            Path path;
            String orderProperty = orderBy.getProperty();
            SortExecExpression execExpression = orderBy.getExecExpression();

            if (EntityUtils.isNotEmpty(orderProperty)){
                if (orderProperty.indexOf(".") != -1) {
                    //关联查询为字段 子表.属性
                    String[] properties = orderProperty.split("\\.");
                    Join<Object, Object> join = null;
                    for (int i = 0; i < properties.length - 1; i++) {
                        String property = properties[i];
                        if (i == 0) {
                            join = join(root, property, criteriaBuilder);
                        } else {
                            join = join(join, property, criteriaBuilder);
                        }
                    }
                    path = join.get(properties[properties.length - 1]);
                } else {
                    path = root.get(orderProperty);
                }

            } else {
               path = (Path) execExpression.genExpression(root, criteriaBuilder);
            }
            if (orderBy.getHasAsc()) {
                orders.add(criteriaBuilder.asc(path));
            } else {
                orders.add(criteriaBuilder.desc(path));
            }
        }
        criteriaQuery.orderBy(orders);
    }

    /**
     * 将条件进行拼接
     *
     * @param root
     * @param criteriaBuilder
     * @param criterion
     * @return
     */
    private Predicate generatePredicate(Root<T> root, CriteriaBuilder criteriaBuilder, Criterion criterion) {
        String property = criterion.getProperty();
        Object criterionValue = criterion.getValue();
        ConditionType conditionType = criterion.getConditionType();

        boolean isColumnValue;
        //集合1 数组2 其它0
        int isArrayOrCollection;
        if (criterionValue instanceof Collection) {
            Object first = ((Collection) criterionValue).iterator().next();
            isColumnValue = first instanceof javax.persistence.Column;
            isArrayOrCollection = 1;
        } else if (criterionValue instanceof Array) {
            Object first = Array.get(criterionValue, 0);
            isColumnValue = first instanceof javax.persistence.Column;
            isArrayOrCollection = 2;
        } else {
            isColumnValue = criterionValue instanceof javax.persistence.Column;
            isArrayOrCollection = 0;
        }

        if (isColumnValue) {
            return whereColumn(property, criterionValue, conditionType, root, criteriaBuilder, isArrayOrCollection);
        } else {
            return whereValue(property, criterionValue, conditionType, root, criteriaBuilder);
        }

    }

    /**
     * @param property        查询字段
     * @param criterionValue  查询值
     * @param conditionType   条件类型
     * @param root
     * @param criteriaBuilder
     * @return
     */
    private Predicate whereValue(String property, Object criterionValue, ConditionType conditionType, Root<T> root, CriteriaBuilder criteriaBuilder) {
        Path columnPath;
        String[] properties = property.split("\\.");
        if (property.indexOf(".") != -1) {
            //关联查询为字段 子表.属性
            String lastProperty = properties[properties.length - 1];
            SubQuery subQuery = subQuery(root, properties, conditionType, criteriaBuilder);
            From join = subQuery.getFrom();
            if (subQuery.getHasBasicField()) {
                columnPath = join.get(lastProperty);
            } else {
                columnPath = join;
            }
            criterionValue = valueToPersist(lastProperty, criterionValue, join);
        } else {
            criterionValue = valueToPersist(property, criterionValue, root);
            From join = getJoin(root, property, conditionType, true, criteriaBuilder);
            if (join instanceof Root) {
                columnPath = join.get(property);
            } else {
                columnPath = join;
            }
        }
        switch (conditionType) {
            case EQ:
                return criteriaBuilder.equal(columnPath, criterionValue);
            case NO_EQ:
                return criteriaBuilder.notEqual(columnPath, criterionValue);
            case GT:
                return criteriaBuilder.greaterThan(columnPath, (Comparable) criterionValue);
            case LT:
                return criteriaBuilder.lessThan(columnPath, (Comparable) criterionValue);
            case GT_OR_EQ:
                return criteriaBuilder.greaterThanOrEqualTo(columnPath, (Comparable) criterionValue);
            case LT_OR_EQ:
                return criteriaBuilder.lessThanOrEqualTo(columnPath, (Comparable) criterionValue);
            case LIKE:
                return criteriaBuilder.like(columnPath, criterionValue.toString());
            case NO_LIKE:
                return criteriaBuilder.notLike(columnPath, criterionValue.toString());
            case BETWEEN:
                Comparable value1 = (Comparable) Array.get(criterionValue, 0);
                Comparable value2 = (Comparable) Array.get(criterionValue, 1);
                return criteriaBuilder.between(columnPath, value1, value2);
            case NO_BETWEEN:
                value1 = (Comparable) Array.get(criterionValue, 0);
                value2 = (Comparable) Array.get(criterionValue, 1);
                return criteriaBuilder.not(criteriaBuilder.between(columnPath, value1, value2));
            case IS_NULL:
                return criteriaBuilder.isNull(columnPath);
            case IS_NO_NULL:
                return criteriaBuilder.isNotNull(columnPath);
            case IN:
                return in(criterionValue, criteriaBuilder, columnPath);
            case NO_IN:
                return criteriaBuilder.not(in(criterionValue, criteriaBuilder, columnPath));
        }
        throw new JpaException("未定义的条件类型:" + conditionType.getValue());
    }

    /**
     * @param property            查询字段
     * @param criterionValue      查询值
     * @param conditionType       条件类型
     * @param root
     * @param criteriaBuilder
     * @param isArrayOrCollection 集合1 数组2 其它0
     * @return
     */
    private Predicate whereColumn(String property, Object criterionValue, ConditionType conditionType, Root<T> root,
                                  CriteriaBuilder criteriaBuilder, int isArrayOrCollection) {

        Path columnPath = subJoin(property, root, conditionType, criteriaBuilder);

        Path valuePath = null;
        Path[] valuePathArray = null;
        List valuePathList = null;

        if (isArrayOrCollection == 1) {
            valuePathList = new ArrayList();
            Collection collection = (Collection) criterionValue;
            Iterator iterator = collection.iterator();
            if (iterator.hasNext()) {
                Column column = (Column) iterator.next();
                valuePathList.add(subJoin(column.getColumn(), root, conditionType, criteriaBuilder));
            }
        } else if (isArrayOrCollection == 2) {
            valuePathArray = new Path[2];
            Column one = (Column) Array.get(valuePathArray, 0);
            Column two = (Column) Array.get(valuePathArray, 1);
            valuePathArray[0] = subJoin(one.getColumn(), root, conditionType, criteriaBuilder);
            valuePathArray[1] = subJoin(two.getColumn(), root, conditionType, criteriaBuilder);
        } else {
            Column column = (Column) criterionValue;
            valuePath = subJoin(column.getColumn(), root, conditionType, criteriaBuilder);
        }

        switch (conditionType) {
            case EQ:
                return criteriaBuilder.equal(columnPath, valuePath);
            case NO_EQ:
                return criteriaBuilder.notEqual(columnPath, valuePath);
            case GT:
                return criteriaBuilder.greaterThan(columnPath, valuePath);
            case LT:
                return criteriaBuilder.lessThan(columnPath, valuePath);
            case GT_OR_EQ:
                return criteriaBuilder.greaterThanOrEqualTo(columnPath, valuePath);
            case LT_OR_EQ:
                return criteriaBuilder.lessThanOrEqualTo(columnPath, valuePath);
            case LIKE:
                return criteriaBuilder.like(columnPath, valuePath);
            case NO_LIKE:
                return criteriaBuilder.notLike(columnPath, valuePath);
            case BETWEEN:
                Expression value1 = (Expression) Array.get(valuePathArray, 0);
                Expression value2 = (Expression) Array.get(valuePathArray, 1);
                return criteriaBuilder.between(columnPath, value1, value2);
            case NO_BETWEEN:
                value1 = (Expression) Array.get(valuePathArray, 0);
                value2 = (Expression) Array.get(valuePathArray, 1);
                return criteriaBuilder.not(criteriaBuilder.between(columnPath, value1, value2));
            case IS_NULL:
                return criteriaBuilder.isNull(columnPath);
            case IS_NO_NULL:
                return criteriaBuilder.isNotNull(columnPath);
            case IN:
                return in(valuePathList, criteriaBuilder, columnPath);
            case NO_IN:
                return criteriaBuilder.not(in(valuePathList, criteriaBuilder, columnPath));
        }
        throw new JpaException("未定义的条件类型:" + conditionType.getValue());
    }

    /**
     * 如果查询的参数为数据库对象，则将参数状态改为托管状态
     *
     * @param property
     * @param value
     * @param root
     * @return
     */
    private Object valueToPersist(String property, Object value, Path root) {
        if (value == null) {
            return null;
        }
        if (EntityUtils.isCglibProxy(value.getClass())) {
            value = TransformUtils.getEntityByProxy(value);
        }
        Class<T> javaType = root.getModel().getBindableJavaType();
        EntityCache entityCache = EntityCache.forClass(javaType);
        Field field = entityCache.getField(property);
        if (field != null) {
            if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                if (contains(value)) {
                    return value;
                }
            }
            if (field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
                if (value instanceof PersistentBag) {
                    return value;
                }
            }
            if (Collection.class.isAssignableFrom(value.getClass())) {
                EntityCache subEntityCache;
                if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class)) {
                    subEntityCache = EntityCache.forClass(field.getType());
                } else if (field.isAnnotationPresent(OneToMany.class) || field.isAnnotationPresent(ManyToMany.class)) {
                    Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                    subEntityCache = EntityCache.forClass((Class) typeArguments[0]);
                } else {
                    return value;
                }
                Collection subInstances;
                if (List.class.isAssignableFrom(value.getClass())) {
                    subInstances = new ArrayList();
                } else {
                    subInstances = new HashSet();
                }
                Iterator iterator = ((Iterable) value).iterator();
                while (iterator.hasNext()) {
                    Object subEntity = iterator.next();
                    List<Field> fields = subEntityCache.getField(Id.class);
                    if (EntityUtils.isNotEmpty(fields)) {
                        Class<?> aClass = subEntity.getClass();

                        EntityManager entityManager = FastJpaSpringUtils.getBean(EntityManager.class);
                        Field idField = fields.get(0);
                        if (subEntity != null) {
                            Object idValue = subEntityCache.getValue(subEntity, idField.getName());
                            if (idValue != null) {
                                Object val = entityManager.find(aClass, TypeUtils.cast(idValue, idField.getType()));
                                if (val != null) {
                                    subInstances.add(val);
                                } else {
                                    throw new JpaException("数据不存在:" + aClass.getSimpleName() + "中不存在id -> " + idValue.toString());
                                }
                            }
                        }
                    }
                }
                return subInstances;
            } else {
                EntityCache subEntityCache = EntityCache.forClass(value.getClass());
                List<Field> fields = subEntityCache.getField(Id.class);
                if (EntityUtils.isNotEmpty(fields)) {
                    Class<?> aClass = value.getClass();
                    EntityManager entityManager = FastJpaSpringUtils.getBean(EntityManager.class);
                    Field idField = fields.get(0);
                    Object idValue = subEntityCache.getValue(value, idField.getName());
                    if (idValue != null) {
                        Object val = entityManager.find(aClass, TypeUtils.cast(idValue, idField.getType()));
                        if (val != null) {
                            return val;
                        } else {
                            throw new JpaException("数据不存在:" + aClass.getSimpleName() + "中不存在id -> " + idValue.toString());
                        }
                    }
                }
            }
        }
        return value;
    }

    private boolean contains(Object entity) {
        try {
            EntityManager entityManager = FastJpaSpringUtils.getBean(EntityManager.class);
            return entityManager.contains(entity);
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 关联查询为字段 子表.属性
     *
     * @param root
     * @param conditionType
     * @return
     */
    private SubQuery subQuery(From root, String[] properties, ConditionType conditionType, CriteriaBuilder criteriaBuilder) {
        //关联查询为字段 子表.属性
        From manyToManyJoin = null;
        boolean hasBasicField = false;
        for (int i = 0; i < properties.length; i++) {
            manyToManyJoin = getJoin(root, properties[i], conditionType, i == properties.length - 1, criteriaBuilder);
            hasBasicField = root == manyToManyJoin;
            root = manyToManyJoin;
        }
        return new SubQuery(hasBasicField, manyToManyJoin);
    }

    /**
     * 是否为实体类字段
     *
     * @param criterion
     * @param root
     */
    private boolean hasPersistField(Criterion criterion, Root root) {
        if (EntityUtils.isEmpty(criterion.getProperty())) {
            return false;
        }
        if (criterion.getProperty().indexOf(".") > -1) {
            return true;
        }
        Class<T> javaType = root.getModel().getJavaType();
        EntityCache entityCache = EntityCache.forClass(javaType);
        Field field = entityCache.getField(criterion.getProperty());
        if (field != null) {
            return true;
        }
        return false;
    }

    /**
     * 获取多对多联表结果
     *
     * @param root
     * @param property
     * @param conditionType
     * @return
     */
    private From getJoin(From root, String property, ConditionType conditionType, boolean isLast, CriteriaBuilder criteriaBuilder) {
        //如果是最后一个字段且查询条件为isNull，isNotNull则无需关联
        if (isLast && skipJoin(conditionType, root, property)) {
            return root;
        }

        //获取主体的class
        Class<T> javaType = root.getModel().getBindableJavaType();
        EntityCache entityCache = EntityCache.forClass(javaType);
        Field field = entityCache.getField(property);
        if (field != null) {
            //主体中多对多映射的字段
            if (TransformUtils.hasPersist(field)) {
                Join join = join(root, property, criteriaBuilder);
                return join;
            }
        }
        return root;
    }

    /**
     * 联表
     *
     * @param root     表
     * @param property 字段
     * @return
     */
    protected static Join join(From root, String property, CriteriaBuilder criteriaBuilder) {
        Optional<Join> optional = root.getJoins().stream().filter(join -> ((AbstractPathImpl) join).getAttribute().getName().equals(property)).findAny();
        if (optional.isPresent()) {
            return optional.get();
        }
        return root.join(property, JOIN_TYPE);
    }

    /**
     * 根据 .进行拆分并联表
     *
     * @param property      字段
     * @param root          表
     * @param conditionType 条件类型
     * @return
     */
    private Path subJoin(String property, Root<T> root, ConditionType conditionType, CriteriaBuilder criteriaBuilder) {
        Path path;
        String[] properties = property.split("\\.");
        if (property.indexOf(".") != -1) {
            //关联查询为字段 子表.属性
            String lastProperty = properties[properties.length - 1];
            SubQuery subQuery = subQuery(root, properties, conditionType, criteriaBuilder);
            From join = subQuery.getFrom();
            if (subQuery.getHasBasicField()) {
                path = join.get(lastProperty);
            } else {
                path = join;
            }
        } else {
            From join = getJoin(root, property, conditionType, true, criteriaBuilder);
            if (join instanceof Root) {
                path = join.get(property);
            } else {
                path = join;
            }
        }
        return path;
    }

    /**
     * 是否为空或非空条件
     *
     * @param conditionType
     * @param root
     * @param property
     * @return
     */
    private boolean skipJoin(ConditionType conditionType, From root, String property) {
        boolean nullCondition = conditionType == ConditionType.IS_NO_NULL
                || conditionType == ConditionType.IS_NULL;
        Path path = root.get(property);
        if (path instanceof SingularAttributePath && nullCondition) {
            return true;
        }
        return false;
    }

    protected boolean getForUpdate() {
        return this.forUpdate;
    }

    /**
     * 复制出一个新的example对象
     *
     * @return
     */
    @SneakyThrows
    public Object copy() {
        return super.clone();
    }

    /**
     * 排序
     */
    @Getter
    @Setter
    public class OrderBy {
        /**
         * 字段名 和字段名二选一
         */
        private String property;
        /**
         * 执行表达式 和字段名二选一
         */
        private SortExecExpression execExpression;
        /**
         * false降序  true升序
         */
        private Boolean hasAsc;

        private OrderBy() {

        }

        private OrderBy(String property, Boolean hasAsc) {
            this.property = property;
            this.hasAsc = hasAsc;
        }

        private OrderBy(SortExecExpression execExpression, Boolean hasAsc) {
            this.execExpression = execExpression;
            this.hasAsc = hasAsc;
        }

        public OrderBy asc(@NonNull String property) {
            orderByList.add(new OrderBy(property, true));
            return this;
        }

        public OrderBy desc(@NonNull String property) {
            orderByList.add(new OrderBy(property, false));
            return this;
        }

        public OrderBy asc(@NonNull SFunction<T, ?> function) {
            orderByList.add(new OrderBy(getColumnName(function), true));
            return this;
        }

        public OrderBy desc(@NonNull SFunction<T, ?> function) {
            orderByList.add(new OrderBy(getColumnName(function), false));
            return this;
        }

        public OrderBy asc(@NonNull SortExecExpression execExpression) {
            orderByList.add(new OrderBy(execExpression, true));
            return this;
        }

        public OrderBy desc(@NonNull SortExecExpression execExpression) {
            orderByList.add(new OrderBy(execExpression, false));
            return this;
        }
    }

    /**
     * 条件组
     */
    public class Criteria implements Serializable {
        /**
         * 条件集合
         */
        protected List<Criterion> criterionList = new ArrayList<>();
        /**
         * 条件组的连接   false 以or连接   true 以and连接
         */
        protected Boolean hasOr;

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andNotEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }


        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andGreaterThan(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.GT, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andGreaterThan(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.GT, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andLessThan(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LT, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andLessThan(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LT, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andGreaterThanEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.GT_OR_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andGreaterThanEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.GT_OR_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andLessThanEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LT_OR_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andLessThanEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LT_OR_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andBetween(String property, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.BETWEEN, property, value1, value2, removeEmptyValueCondition);
            return this;
        }

        public Criteria andBetween(SFunction<T, ?> function, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.BETWEEN, getColumnName(function), value1, value2, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andNotBetween(String property, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_BETWEEN, property, value1, value2, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotBetween(SFunction<T, ?> function, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_BETWEEN, getColumnName(function), value1, value2, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andIn(String property, Iterable iterable, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.IN, property, iterable, removeEmptyValueCondition);
            return this;
        }

        public Criteria andIn(SFunction<T, ?> function, Iterable iterable, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.IN, getColumnName(function), iterable, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria andNotIn(String property, Iterable iterable, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_IN, property, iterable, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotIn(SFunction<T, ?> function, Iterable iterable, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_IN, getColumnName(function), iterable, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         * @parmm likeType 模糊查询类型 开头,结尾,包含  {@link LikeType}
         */
        public Criteria andLike(String property, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LIKE, likeType, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andLike(SFunction<T, ?> function, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LIKE, likeType, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andLike(String property, String value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LIKE, LikeType.CONTAINS, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andLike(SFunction<T, ?> function, String value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.LIKE, LikeType.CONTAINS, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         * @parmm likeType 模糊查询类型 开头,结尾,包含  {@link LikeType}
         */
        public Criteria andNotLike(String property, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_LIKE, likeType, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotLike(SFunction<T, ?> function, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_LIKE, likeType, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotLike(String property, String value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_LIKE, LikeType.CONTAINS, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andNotLike(SFunction<T, ?> function, String value, boolean... removeEmptyValueCondition) {
            addAndCriteria(ConditionType.NO_LIKE, LikeType.CONTAINS, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        public Criteria andIsNull(String property) {
            addAndCriteria(ConditionType.IS_NULL, property);
            return this;
        }

        public Criteria andIsNull(SFunction<T, ?> function) {
            addAndCriteria(ConditionType.IS_NULL, getColumnName(function));
            return this;
        }

        public Criteria andIsNotNull(String property) {
            addAndCriteria(ConditionType.IS_NO_NULL, property);
            return this;
        }

        public Criteria andIsNotNull(SFunction<T, ?> function) {
            addAndCriteria(ConditionType.IS_NO_NULL, getColumnName(function));
            return this;
        }

        /**
         * 拼接一个条件
         *
         * @param execExpression
         * @return
         */
        public Criteria andExpression(ExecExpression execExpression) {
            Criterion criterion = new Criterion();
            criterion.setHasOr(false);
            criterion.setExecExpression(execExpression);
            criterionList.add(criterion);
            return this;
        }

        /**
         * 拼接一个条件
         *
         * @param execExpression
         * @return
         */
        public Criteria orExpression(ExecExpression execExpression) {
            Criterion criterion = new Criterion();
            criterion.setHasOr(true);
            criterion.setExecExpression(execExpression);
            criterionList.add(criterion);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orNotEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orNotEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orGreaterThan(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.GT, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orGreaterThan(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.GT, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orLessThan(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LT, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orLessThan(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LT, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }


        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orGreaterThanEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.GT_OR_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orGreaterThanEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.GT_OR_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orLessThanEqualTo(String property, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LT_OR_EQ, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orLessThanEqualTo(SFunction<T, ?> function, Object value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LT_OR_EQ, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orBetween(String property, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.BETWEEN, property, value1, value2, removeEmptyValueCondition);
            return this;
        }

        public Criteria orBetween(SFunction<T, ?> function, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.BETWEEN, getColumnName(function), value1, value2, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orNotBetween(String property, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_BETWEEN, property, value1, value2, removeEmptyValueCondition);
            return this;
        }

        public Criteria orNotBetween(SFunction<T, ?> function, Object value1, Object value2, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_BETWEEN, getColumnName(function), value1, value2, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orIn(String property, Iterable iterable, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.IN, property, iterable, removeEmptyValueCondition);
            return this;
        }

        public Criteria orIn(SFunction<T, ?> function, Iterable iterable, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.IN, getColumnName(function), iterable, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orNotIn(String property, Iterable iterable, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_IN, property, iterable, removeEmptyValueCondition);
            return this;
        }

        public Criteria orNotIn(SFunction<T, ?> function, Iterable iterable, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_IN, getColumnName(function), iterable, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orLike(String property, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LIKE, likeType, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orLike(SFunction<T, ?> function, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LIKE, likeType, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orLike(String property, String value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LIKE, LikeType.CONTAINS, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orLike(SFunction<T, ?> function, String value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.LIKE, LikeType.CONTAINS, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        public Criteria orNotLike(String property, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_LIKE, likeType, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orNotLike(SFunction<T, ?> function, String value, LikeType likeType, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_LIKE, likeType, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }


        public Criteria orNotLike(String property, String value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_LIKE, LikeType.CONTAINS, property, value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orNotLike(SFunction<T, ?> function, String value, boolean... removeEmptyValueCondition) {
            addOrCriteria(ConditionType.NO_LIKE, LikeType.CONTAINS, getColumnName(function), value, removeEmptyValueCondition);
            return this;
        }

        public Criteria orIsNull(String property) {
            addOrCriteria(ConditionType.IS_NULL, property);
            return this;
        }

        public Criteria orIsNull(SFunction<T, ?> function) {
            addOrCriteria(ConditionType.IS_NULL, getColumnName(function));
            return this;
        }

        public Criteria orIsNotNull(String property) {
            addOrCriteria(ConditionType.IS_NO_NULL, property);
            return this;
        }

        public Criteria orIsNotNull(SFunction<T, ?> function) {
            addOrCriteria(ConditionType.IS_NO_NULL, getColumnName(function));
            return this;
        }

        private void addOrCriteria(ConditionType conditionType, @NonNull String property) {
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setHasOr(true);
            criterionList.add(criterion);
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        private void addOrCriteria(ConditionType conditionType, LikeType likeType, @NonNull String property, Object value, boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value)) {
                    throw new JpaException("value不能为空");
                }
            }
            if (likeType == LikeType.LEFT) {
                value = "%" + value.toString();
            } else if (likeType == LikeType.RIGHT) {
                value = value.toString() + "%";
            } else {
                value = "%" + value.toString() + "%";
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(value);
            criterion.setHasOr(true);
            criterionList.add(criterion);
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        private void addOrCriteria(ConditionType conditionType, @NonNull String property, Object value, boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value)) {
                    throw new JpaException("value不能为空");
                }
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(value);
            criterion.setHasOr(true);
            criterionList.add(criterion);
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        private void addOrCriteria(ConditionType conditionType, @NonNull String property, Object value1, Object value2
                , boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value1) || EntityUtils.isEmpty(value2)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value1)) {
                    throw new JpaException("value1不能为空");
                }
                if (EntityUtils.isEmpty(value2)) {
                    throw new JpaException("value2不能为空");
                }
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(new Object[]{value1, value2});
            criterion.setHasOr(true);
            criterionList.add(criterion);
        }

        private void addAndCriteria(ConditionType conditionType, @NonNull String property) {
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setHasOr(false);
            criterionList.add(criterion);
        }

        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        private void addAndCriteria(ConditionType conditionType, LikeType likeType, @NonNull String property, Object value, boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value)) {
                    throw new JpaException("value不能为空");
                }
            }
            if (likeType == LikeType.LEFT) {
                value = "%" + value.toString();
            } else if (likeType == LikeType.RIGHT) {
                value = value.toString() + "%";
            } else {
                value = "%" + value.toString() + "%";
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(value);
            criterion.setHasOr(false);
            criterionList.add(criterion);
        }


        /**
         * @param removeEmptyValueCondition 默认为false  为true时 value为空时条件不生效  in集合为空条件不生效  between任意一个为空条件不生效
         *                                  为false时  value为空会报错 in集合为空报错 between任意一个为空报错
         * @parmm value 为字段时使用 {@link Column#of(String)}
         */
        private void addAndCriteria(ConditionType conditionType, @NonNull String property, Object value, boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value)) {
                    throw new JpaException("value不能为空");
                }
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(value);
            criterion.setHasOr(false);
            criterionList.add(criterion);
        }

        private void addAndCriteria(ConditionType conditionType, @NonNull String property, Object value1, Object value2
                , boolean... removeEmptyValueCondition) {
            boolean removeEmptyValue;
            if (EntityUtils.isEmpty(removeEmptyValueCondition)) {
                removeEmptyValue = false;
            } else {
                removeEmptyValue = removeEmptyValueCondition[0];
            }
            if (removeEmptyValue) {
                if (EntityUtils.isEmpty(value1) || EntityUtils.isEmpty(value2)) {
                    return;
                }
            } else {
                if (EntityUtils.isEmpty(value1)) {
                    throw new JpaException("value1不能为空");
                }
                if (EntityUtils.isEmpty(value2)) {
                    throw new JpaException("value2不能为空");
                }
            }
            Criterion criterion = new Criterion();
            criterion.setConditionType(conditionType);
            criterion.setProperty(property);
            criterion.setValue(new Object[]{value1, value2});
            criterion.setHasOr(false);
            criterionList.add(criterion);
        }

    }

    /**
     * 条件类
     */
    @Setter
    @Getter
    protected class Criterion {
        /**
         * 条件类型
         */
        private ConditionType conditionType;
        /**
         * 字段名  关联查询子表字段格式为 子表.字段名 如：查询用户的角色名  role.name
         */
        private String property;
        /**
         * 筛选的值
         */
        private Object value;
        /**
         * 条件连接符  false 以or连接   true 以and连接
         */
        private Boolean hasOr;

        private ExecExpression execExpression;
    }

    @AllArgsConstructor
    @Getter
    class SubQuery {
        private Boolean hasBasicField;
        private From from;
    }
}

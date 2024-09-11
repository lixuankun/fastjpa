package com.efeichong.cache;

import com.efeichong.exception.BaseException;
import com.efeichong.util.EntityUtils;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author lxk
 * @date 2020/9/25
 * @description 实体类的缓存类
 * Cache生命周期: 从创建到回收
 */
public class EntityCache {

    private static final String FIELD_MAP = "FieldMap";
    private static final String FIELDS = "Fields";
    private static final String PD_MAP = "PdMap";

    /**
     * class缓存
     */
    private static Cache CLASS_CACHE = CacheBuilder.newBuilder()
//            .weakKeys()
            .build();

    /**
     * 字段,方法缓存
     */
    private Cache cache = CacheBuilder.newBuilder()
//            .weakKeys()
            .build();
    /**
     * 实体类的class
     */
    private Class<?> clazz;

    /**
     * 私有构造，通过 {@link #forClass(Class) 创建}
     */
    private EntityCache() {

    }

    /**
     * 对class的反射做缓存
     *
     * @param clazz 被缓存的class
     * @return
     */
    @SneakyThrows
    @Synchronized
    public static EntityCache forClass(@NonNull Class<?> clazz) {
        EntityCache entityCache = (EntityCache) CLASS_CACHE.getIfPresent(clazz);
        if (entityCache == null) {
            entityCache = new EntityCache();
            CLASS_CACHE.put(clazz.getName(), entityCache);
            entityCache.clazz = clazz;
            return entityCache;
        }
        return entityCache;
    }

    public boolean hasBasicType() {
        if (clazz == boolean.class || clazz == Boolean.class) {
            return true;
        }

        if (clazz == byte.class || clazz == Byte.class) {
            return true;
        }

        if (clazz == char.class || clazz == Character.class) {
            return true;
        }

        if (clazz == short.class || clazz == Short.class) {
            return true;
        }

        if (clazz == int.class || clazz == Integer.class) {
            return true;
        }

        if (clazz == long.class || clazz == Long.class) {
            return true;
        }

        if (clazz == float.class || clazz == Float.class) {
            return true;
        }

        if (clazz == double.class || clazz == Double.class) {
            return true;
        }

        if (clazz == String.class) {
            return true;
        }

        if (clazz == BigDecimal.class) {
            return true;
        }

        if (clazz == BigInteger.class) {
            return true;
        }

        if (clazz == Date.class) {
            return true;
        }
        return false;
    }

    /**
     * <p>
     * 排序重置父类属性
     * </p>
     *
     * @param fields         子类属性
     * @param superFieldList 父类属性
     */
    public static Map<String, Field> excludeOverrideSuperField(Field[] fields, List<Field> superFieldList) {
        // 子类属性
        Map<String, Field> fieldMap = Stream.of(fields).collect(toMap(Field::getName, identity(),
                (u, v) -> {
                    throw new IllegalStateException(String.format("Duplicate key %s", u));
                },
                LinkedHashMap::new));
        superFieldList.stream().filter(field -> !fieldMap.containsKey(field.getName()))
                .forEach(f -> fieldMap.put(f.getName(), f));
        return fieldMap;
    }

    /**
     * 设置可访问对象的可访问权限为 true
     *
     * @param object 可访问的对象
     * @param <T>    类型
     * @return 返回设置后的对象
     */
    public static <T extends AccessibleObject> T setAccessible(T object) {
        return AccessController.doPrivileged(new SetAccessibleAction<>(object));
    }

    /**
     * 通过字段名查询字段
     *
     * @param filedName 字段名
     * @return
     */
    @Synchronized
    @SneakyThrows
    public Field getField(@NonNull String filedName) {
        return getFieldMap().get(filedName);
    }

    /**
     * 通过注解查询字段
     *
     * @param annotation 字段上的注解
     * @return
     */
    @Synchronized
    @SneakyThrows
    public List<Field> getField(@NonNull Class<? extends Annotation> annotation) {
        List<Field> fields = (List<Field>) cache.getIfPresent(annotation);
        if (EntityUtils.isEmpty(fields)) {
            fields = getFields().stream().filter(field -> field.isAnnotationPresent(annotation)).collect(Collectors.toList());
            if (EntityUtils.isNotEmpty(fields)) {
                cache.put(annotation, fields);
            }
        }
        return fields;
    }

    /**
     * 创建一个新的对象
     *
     * @param <T>
     * @return
     */
    @SneakyThrows
    public <T> T newInstance() {
        return (T) EntityUtils.newInstance(this.clazz);
    }

    /**
     * 获取字段的值
     *
     * @return
     */
    @SneakyThrows
    public Object getValue(@NonNull Object instance, @NonNull String fieldName) {
        Object value = getValueWithIntrospector(instance, fieldName);
        if (value != null){
            return value;
        }
        return getValueWithReflect(instance, fieldName);
    }

    /**
     * 通过反射获取字段值
     *
     * @param instance
     * @param fieldName
     * @return
     */
    @SneakyThrows
    public Object getValueWithReflect(@NonNull Object instance, @NonNull String fieldName) {
        Field field = getField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            return field.get(instance);
        }
        throw new BaseException(clazz.toString() + " not exist field " + fieldName);
    }

    /**
     * 通过内省获取字段值
     *
     * @param instance
     * @param fieldName
     * @return
     */
    @SneakyThrows
    public Object getValueWithIntrospector(@NonNull Object instance, @NonNull String fieldName) {
        PropertyDescriptor pd = getPd(fieldName);
        if (pd != null){
            return pd.getReadMethod().invoke(instance);
        }
        return null;
    }


    /**
     * 设置字段的值
     *
     * @param instance  对象
     * @param fieldName 字段
     * @param value     值
     */
    public void setValue(@NonNull Object instance, @NonNull String fieldName, Object value) {
        if (EntityUtils.isHibernateProxy(clazz)) {
            setValueWithIntrospector(instance, fieldName, value);
        } else {
            setValueWithReflect(instance, fieldName, value);
        }
    }

    /**
     * 通过反射设置字段值
     *
     * @param instance
     * @param fieldName
     * @param value
     */
    @SneakyThrows
    public void setValueWithReflect(@NonNull Object instance, @NonNull String fieldName, Object value) {
        Field field = getField(fieldName);
        if (field != null) {
            field.setAccessible(true);
            field.set(instance, value);
        } else {
            throw new BaseException(clazz.toString() + " not exist field " + fieldName);
        }
    }

    /**
     * 通过内省设置字段值
     *
     * @param instance
     * @param fieldName
     * @param value
     */
    @SneakyThrows
    public void setValueWithIntrospector(@NonNull Object instance, @NonNull String fieldName, Object value) {
        PropertyDescriptor pd = getPd(fieldName);
        if (pd != null){
            pd.getWriteMethod().invoke(instance, value);
        }
    }


    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * <p>
     * 获取该类的所有属性列表
     * </p>
     */
    @SneakyThrows
    public List<Field> getFields() {
        if (Objects.isNull(clazz)) {
            return Collections.emptyList();
        }
        return getAllFields().stream()  /* 过滤静态属性 */
                .filter(f -> !Modifier.isStatic(f.getModifiers()))
                /* 过滤 transient关键字修饰的属性 */
                .filter(f -> !Modifier.isTransient(f.getModifiers()))
//                    .filter(f -> !f.isAnnotationPresent(Transient.class))
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public List<Field> getAllFields() {
        if (Objects.isNull(clazz)) {
            return Collections.emptyList();
        }
        return (List<Field>) cache.get(FIELDS, () -> {
            Field[] fields = this.clazz.getDeclaredFields();
            List<Field> superFields = new ArrayList<>();
            Class<?> currentClass = this.clazz.getSuperclass();
            while (currentClass != null) {
                Field[] declaredFields = currentClass.getDeclaredFields();
                Collections.addAll(superFields, declaredFields);
                currentClass = currentClass.getSuperclass();
            }
            /* 排除重载属性 */
            Map<String, Field> fieldMap = excludeOverrideSuperField(fields, superFields);
            /*
             * 重写父类属性过滤后处理忽略部分，支持过滤父类属性功能
             * 场景：中间表不需要记录创建时间，忽略父类 createTime 公共属性
             * 中间表实体重写父类属性 ` private transient Date createTime; `
             */
            List<Field> a = fieldMap.values().stream()
                    .collect(Collectors.toList());
            return a;
        });
    }

    /**
     * 获取该类的所有属性列表
     */
    @SneakyThrows
    public Map<String, Field> getFieldMap() {
        return (Map<String, Field>) cache.get(FIELD_MAP, () -> {
            List<Field> fieldList = getFields();
            return EntityUtils.isNotEmpty(fieldList) ? fieldList.stream().collect(toMap(Field::getName, identity())) : Collections.emptyMap();
        });
    }

    /**
     * 获取 pd
     *
     * @param fieldName
     * @return
     */
    @SneakyThrows
    public PropertyDescriptor getPd(String fieldName) {
        return getPdMap().get(fieldName);
    }

    @SneakyThrows
    public Map<String, PropertyDescriptor> getPdMap() {
        return (Map<String, PropertyDescriptor>) cache.get(PD_MAP, () -> {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
            return EntityUtils.isNotEmpty(propertyDescriptors) ? Arrays.stream(propertyDescriptors).collect(toMap(PropertyDescriptor::getName, identity())) : Collections.emptyMap();
        });
    }


    public void clear() {
        this.cache.invalidateAll();
        CLASS_CACHE.invalidateAll();
    }

}

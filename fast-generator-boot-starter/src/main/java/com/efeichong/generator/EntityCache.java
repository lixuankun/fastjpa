package com.efeichong.generator;

import com.efeichong.exception.BaseException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.Synchronized;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * @author lxk
 * @date 2020/9/25
 * @description 实体类的缓存类
 * Cache生命周期: 从创建到回收
 */
class EntityCache {

    private static final String FIELD = "Field";
    private static final String FIELD_PD = "FieldPropertyDescriptor";
    private static final String FIELDS = "Fields";
    private static final String METHOD_DESCRIPTORS = "MethodDescriptors";
    private static final String PROPERTY_DESCRIPTORS = "PropertyDescriptors";

    /**
     * class缓存
     */
    private static Cache CLASS_CACHE = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES)
//            .weakKeys()
            .build();

    /**
     * 字段,方法缓存
     */
    private Cache cache = CacheBuilder.newBuilder()
            .expireAfterWrite(5L, TimeUnit.MINUTES)
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

    /**
     * 查询所有字段描述
     *
     * @return
     */
    @Synchronized
    @SneakyThrows
    public PropertyDescriptor[] getPropDescriptors() {
        PropertyDescriptor[] descriptors = (PropertyDescriptor[]) cache.getIfPresent(PROPERTY_DESCRIPTORS);
        if (descriptors == null) {
            descriptors = Introspector.getBeanInfo(this.clazz).getPropertyDescriptors();
            this.cache.put(PROPERTY_DESCRIPTORS, descriptors);
        }
        return descriptors;
    }

    /**
     * 通过字段名查询字段描述
     *
     * @param filedName 字段名
     * @return
     */
    @Synchronized
    @SneakyThrows
    public Optional<PropertyDescriptor> getPropDescriptor(@NonNull String filedName) {
        Optional<PropertyDescriptor> optional = (Optional<PropertyDescriptor>) cache.getIfPresent(FIELD_PD + filedName);
        if (optional == null || !optional.isPresent()) {
            PropertyDescriptor[] propDescriptors = getPropDescriptors();
            for (PropertyDescriptor propDescriptor : propDescriptors) {
                if (propDescriptor.getName().equals(filedName)) {
                    PropertyDescriptor propertyDescriptor = null;
                    try {
                        propertyDescriptor = new PropertyDescriptor(filedName, this.clazz);
                    } catch (IntrospectionException e) {
                        return Optional.empty();
                    }
                    optional = Optional.of(propertyDescriptor);
                }
            }
            cache.put(FIELD_PD + filedName, optional);
        }
        return optional == null ? Optional.empty() : optional;
    }

    /**
     * 通过字段名查询字段
     *
     * @param filedName 字段名
     * @return
     */
    @Synchronized
    @SneakyThrows
    public Optional<Field> getField(@NonNull String filedName) {
        Optional<Field> optional = (Optional<Field>) cache.getIfPresent(FIELD + filedName);
        if (optional == null || !optional.isPresent()) {
            optional = Arrays.stream(getFields()).filter(field -> field.getName().equals(filedName)).findAny();
            if (!optional.isPresent()) {
                return Optional.empty();
            }
            cache.put(FIELD + filedName, optional);
        }
        return optional;
    }

    /**
     * 通过注解查询字段
     *
     * @param annotation 字段上的注解
     * @return
     */
    @Synchronized
    @SneakyThrows
    public Field[] getField(@NonNull Class<? extends Annotation> annotation) {
        Field[] fields = (Field[]) cache.getIfPresent(annotation);
        if (ArrayUtils.isEmpty(fields)) {
            fields = Arrays.stream(getFields()).filter(field -> field.isAnnotationPresent(annotation)).toArray(Field[]::new);
            if (ArrayUtils.isNotEmpty(fields)) {
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
        return (T) this.clazz.newInstance();
    }

    /**
     * 查询所有方法
     *
     * @return
     */
    @SneakyThrows
    public MethodDescriptor[] getMethodDescriptors() {
        MethodDescriptor[] methodDescriptors = (MethodDescriptor[]) cache.getIfPresent(METHOD_DESCRIPTORS);
        if (methodDescriptors == null) {
            methodDescriptors = Introspector.getBeanInfo(this.clazz).getMethodDescriptors();
            cache.put(METHOD_DESCRIPTORS, methodDescriptors);
        }
        return methodDescriptors;
    }

    /**
     * 获取字段的值
     *
     * @return
     */
    @SneakyThrows
    public Object getValue(@NonNull Object instance, @NonNull String fieldName) {
        Optional<PropertyDescriptor> propDescriptorOptional = getPropDescriptor(fieldName);
        if (propDescriptorOptional.isPresent()) {
            PropertyDescriptor propertyDescriptor = propDescriptorOptional.get();
            Method readMethod = propertyDescriptor.getReadMethod();
            if (readMethod != null) {
                if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                    readMethod.setAccessible(true);
                }
                return readMethod.invoke(instance);
            }
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
    @SneakyThrows
    public void setValue(@NonNull Object instance, @NonNull String fieldName, Object value) {
        Optional<PropertyDescriptor> propDescriptorOptional = getPropDescriptor(fieldName);
        if (propDescriptorOptional.isPresent()) {
            PropertyDescriptor propertyDescriptor = propDescriptorOptional.get();
            Method writeMethod = propertyDescriptor.getWriteMethod();
            if (writeMethod != null) {
                if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                    writeMethod.setAccessible(true);
                }
                writeMethod.invoke(instance, value);
            }
        } else {
            throw new BaseException(this.clazz.getName() + "不存在" + fieldName + "的set方法");
        }
    }

    public Class<?> getClazz() {
        return clazz;
    }

    /**
     * 查询所有字段 只查询private修饰的字段
     * PUBLIC: 1
     * PRIVATE: 2
     * PROTECTED: 4
     * STATIC: 8
     * FINAL: 16
     * SYNCHRONIZED: 32
     * VOLATILE: 64
     * TRANSIENT: 128
     * NATIVE: 256
     * INTERFACE: 512
     * ABSTRACT: 1024
     * STRICT: 2048
     *
     * @return
     */
    @Synchronized
    @SneakyThrows
    public Field[] getFields() {
        Field[] fields = (Field[]) cache.getIfPresent(FIELDS);
        if (fields == null) {
            fields = getDeclaredFields(this.clazz, this.clazz.getDeclaredFields());
            fields = Arrays.stream(fields)
                    .filter(field -> field.getModifiers() == 2)
                    .toArray(Field[]::new);
            cache.put(FIELDS, fields);
        }
        return fields;
    }

    /**
     * 获取自身及父级类的所有字段
     *
     * @param clazz
     * @param fields
     * @return
     */
    private Field[] getDeclaredFields(Class<?> clazz, Field[] fields) {
        Class superclass = clazz.getSuperclass();
        if (superclass.getClassLoader() == null) {
            return fields;
        }
        for (Field declaredField : superclass.getDeclaredFields()) {
            boolean match = Arrays.stream(fields).anyMatch(field -> field.getName().equals(declaredField.getName()));
            if (!match) {
                fields = ArrayUtils.add(fields, declaredField);
            }
        }
        return getDeclaredFields(superclass, fields);
    }

}

package com.efeichong.util;

import com.efeichong.cache.EntityCache;
import com.efeichong.exception.JpaException;
import com.efeichong.mapping.Mapping;
import com.efeichong.mapping.UseMode;
import com.efeichong.proxy.DynamicBean;
import com.efeichong.proxy.PropDesc;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ObjectUtils;

import javax.persistence.*;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;
import java.util.stream.Collectors;

import static com.efeichong.util.EntityUtils.isEmpty;
import static org.springframework.beans.BeanUtils.copyProperties;


/**
 * @author lxk
 * @date 2021/3/22
 * @description
 */
@Slf4j
public class TransformUtils {

    private static Cache cache = CacheBuilder.newBuilder()
            .build();

    /**
     * 将po拷贝到vo
     * <p>
     * po的对象会被转为vo对应的id，对象数组会被转为ids
     * po与vo映射的字段均为对象，对象的属性值会进行拷贝（两个对象可以不是同一Class）
     * </p>
     *
     * @param po
     * @param vo
     * @param ignoreProperties
     */
    @SneakyThrows
    private static <V, P> V toVo(P po, V vo, String... ignoreProperties) {
        if (po == null) {
            return vo;
        }
        copyProperties(po, vo, ignoreProperties);
        EntityCache poEntityCache = EntityCache.forClass(po.getClass());
        EntityCache voEntityCache = EntityCache.forClass(vo.getClass());
        List<Field> fields = voEntityCache.getFields();
        for (Field voField : fields) {
            String fieldName = voField.getName();
            if (voField.isAnnotationPresent(Mapping.class)) {
                Mapping mapping = voField.getAnnotation(Mapping.class);
                if (!EntityUtils.contains(mapping.useMode(), UseMode.ALL) && !EntityUtils.contains(mapping.useMode(), UseMode.TO_VO)) {
                    continue;
                }
                boolean match = Arrays.stream(ignoreProperties).anyMatch(fieldName::equals);
                if (match) {
                    continue;
                }
                //子对象的缓存类
                EntityCache subEntityCache = poEntityCache;
                //子对象
                Object subPo = po;
                //最后一个字段即实际映射的字段名
                String lastProperty;
                if (mapping.poProperty().indexOf(".") != -1) {
                    String[] recursionProperties = mapping.poProperty().split("\\.");
                    //最后一个元素位置
                    int lastPos = recursionProperties.length - 1;
                    //最后一个元素的字段名
                    lastProperty = recursionProperties[lastPos];
                    //[0,lastPos) 左闭右开 之间的的数据遍历循环出最后一个对象的 EntityCache
                    for (int i = 0; i < lastPos; i++) {
                        String property = recursionProperties[i];
                        Field field = subEntityCache.getField(property);
                        if (field == null) {
                            throw new JpaException(subEntityCache.getClazz().getName() + "不存在字段：" + property);
                        }
                        //第i个字段
                        //获取第i个字段对应的属性值
                        subPo = subEntityCache.getValue(subPo, field.getName());
                        //缓存类进行传递赋值，集合类型取ActualTypeArguments否则取type
                        if (Collection.class.isAssignableFrom(field.getType())) {
                            Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                            Class typeArgument = (Class) typeArguments[0];
                            subEntityCache = EntityCache.forClass(typeArgument);
                        } else {
                            subEntityCache = EntityCache.forClass(field.getType());
                        }
                    }
                } else {
                    lastProperty = mapping.poProperty();
                }
                if (subPo == null) {
                    continue;
                }
                //获取具体的映射字段
                Field mapperPoField = subEntityCache.getField(lastProperty);
                if (mapperPoField == null) {
                    throw new JpaException(subEntityCache.getClazz().getName() + "不存在字段：" + lastProperty);
                }
                if (hasPersist(mapperPoField)) {
                    //转vo时 po与vo映射的字段均为对象，对象的值会进行拷贝（两个对象可以不是同一Class）
                    voFieldCopy(voEntityCache, voField, subPo, vo, mapperPoField, subEntityCache);
                } else {
                    //转vo时映射的字段 po的对象会被转为vo对应的字段
                    voEntityToProperty(voEntityCache, voField, subPo, vo, mapperPoField, subEntityCache);
                }
            }
        }
        return vo;
    }

    /**
     * 将vo拷贝到po
     * <p>
     * vo指定的id会被转为po对应的对象
     * vo与po映射的字段均为对象，对象的属性值会进行拷贝（两个对象可以不是同一Class）
     * </p>
     *
     * @param vo
     * @param po
     * @param ignoreProperties
     */
    @SneakyThrows
    private static <V, P> P toPo(V vo, P po, String... ignoreProperties) {
        if (vo == null) {
            return po;
        }
        copyProperties(vo, po, ignoreProperties);
        EntityCache poEntityCache = EntityCache.forClass(po.getClass());
        EntityCache voEntityCache = EntityCache.forClass(vo.getClass());
        List<Field> fields = voEntityCache.getFields();
        Set<String> copyNames = Sets.newLinkedHashSet();
        for (Field voField : fields) {
            copyNames.add(voField.getName());
        }
        fields = sortedField(fields, voEntityCache);
        for (Field voField : fields) {
            String fieldName = voField.getName();
            Mapping mapping = voField.getAnnotation(Mapping.class);
            if (!EntityUtils.contains(mapping.useMode(), UseMode.ALL) && !EntityUtils.contains(mapping.useMode(), UseMode.TO_PO)) {
                continue;
            }
            boolean match = Arrays.stream(ignoreProperties).anyMatch(fieldName::equals);
            if (match) {
                continue;
            }
            Object value = voEntityCache.getValue(vo, fieldName);
            if (ObjectUtils.isEmpty(value)) {
                String property;
                if (mapping.poProperty().contains(".")) {
                    String[] recursionProperties = mapping.poProperty().split("\\.");
                    property = recursionProperties[0];
                } else {
                    property = mapping.poProperty();
                }
                Field field = poEntityCache.getField(property);
                if (field != null) {
                    copyNames.add(property);
                    poEntityCache.setValue(po, property, null);
                }
                continue;
            }
            //子对象缓存类
            EntityCache subEntityCache = poEntityCache;
            //子对象
            Object subPo = po;
            Object subPoVal = po;
            //最后一个字段即实际映射的字段名
            String lastProperty;
            if (mapping.poProperty().contains(".")) {
                String[] recursionProperties = mapping.poProperty().split("\\.");
                copyNames.add(recursionProperties[0]);
                //最后一个元素位置
                int lastPos = recursionProperties.length - 1;
                //最后一个元素的字段名
                lastProperty = recursionProperties[lastPos];
                //[0,lastPos) 左闭右开 之间的的数据遍历循环出最后一个对象的EntityCache
                for (int i = 0; i < lastPos; i++) {
                    String property = recursionProperties[i];
                    Field field = subEntityCache.getField(property);
                    if (field == null) {
                        throw new JpaException(subEntityCache.getClazz().getName() + "不存在字段：" + property);
                    }

                    //如果字段为集合类型则需创建对应的对象集合
                    if (Collection.class.isAssignableFrom(field.getType())) {
                        Collection values = (Collection) value;
                        Type[] typeArguments = ((ParameterizedType) field.getGenericType()).getActualTypeArguments();
                        Class typeArgument = (Class) typeArguments[0];
                        Object referenceVal = subEntityCache.getValue(subPo, field.getName());
                        if (!isEmpty(referenceVal)) {
                            subEntityCache = EntityCache.forClass(typeArgument);
                            subPo = referenceVal;
                            subPoVal = referenceVal;
                            continue;
                        }
                        Collection collection = null;
                        if (List.class.isAssignableFrom(field.getType())) {
                            collection = Lists.newArrayList();
                        } else {
                            collection = Sets.newHashSet();
                        }
                        //最后一个对象如果是集合类型要根据vo的数组长度创建vo.size()个
                        if (i == lastPos - 1) {
                            Object subValue = null;
                            for (int j = 0; j < values.size(); j++) {
                                subValue = EntityUtils.newInstance(typeArgument);
                                collection.add(subValue);
                                subEntityCache.setValue(subPo, field.getName(), collection);
                            }
                            subPo = subValue;
                        } else {
                            Object subValue = EntityUtils.newInstance(typeArgument);
                            collection.add(subValue);
                            subEntityCache.setValue(subPo, field.getName(), collection);
                            subPo = subValue;
                        }
                        subPoVal = collection;
                        subEntityCache = EntityCache.forClass(typeArgument);
                    } else {
                        Object referenceVal = subEntityCache.getValue(subPo, field.getName());
                        if (!isEmpty(referenceVal)) {
                            subEntityCache = EntityCache.forClass(field.getType());
                            subPo = referenceVal;
                            subPoVal = referenceVal;
                            continue;
                        }
                        Object subValue = EntityUtils.newInstance(field.getType());
                        subEntityCache.setValue(subPo, field.getName(), subValue);
                        subPo = subValue;
                        subPoVal = subValue;
                        subEntityCache = EntityCache.forClass(field.getType());
                    }
                }
            } else {
                lastProperty = mapping.poProperty();
                copyNames.add(lastProperty);
            }
            Field mapperPoField = subEntityCache.getField(lastProperty);
            if (mapperPoField == null) {
                throw new JpaException(subEntityCache.getClazz().getName() + "不存在字段：" + lastProperty);
            }
            if (hasPersist(mapperPoField)) {
                //转vo时 po与vo映射的字段均为对象，对象的值会进行拷贝（两个对象可以不是同一Class）
                poFieldCopy(subPoVal, mapperPoField, subEntityCache, value);
            } else {
                //转po时映射的字段 vo的id会被转为po对应的字段
                poEntityToProperty(subPoVal, mapperPoField, subEntityCache, value);
            }
        }
        List<PropDesc> propDescs = PropDesc.builder()
                .add("propNames", Set.class, copyNames)
                .add("proxied", po.getClass(), po)
                .build();
        DynamicBean dynamicBean = new DynamicBean(propDescs, po);
        return (P) dynamicBean.getObject();
    }

    /**
     * Collection<T>类型转换为List<V>  将po集合转为vo集合
     *
     * @param froms   原集合
     * @param voClazz 目标Type
     * @return
     */
    @SneakyThrows
    public static <V, P> List<V> toVos(Collection<P> froms, Class<? extends V> voClazz, String... ignoreProperties) {
        List<V> vos = Lists.newArrayList();
        for (P po : froms) {
            V vo = EntityUtils.newInstance(voClazz);
            toVo(po, vo, ignoreProperties);
            vos.add(vo);
        }
        return vos;
    }

    /**
     * Collection<V>类型转换为List<T> 将vo集合转为po集合
     *
     * @param froms   原集合
     * @param poClazz 目标Type
     * @return
     */
    @SneakyThrows
    public static <V, P> List<P> toPos(Collection<V> froms, Class<? extends P> poClazz, String... ignoreProperties) {
        List<P> pos = Lists.newArrayList();
        for (V vo : froms) {
            P po = EntityUtils.newInstance(poClazz);
            toPo(vo, po, ignoreProperties);
            pos.add(po);
        }
        return pos;

    }

    /**
     * 判断该字段的值是否为对象类型
     *
     * @param field
     * @return
     */
    public static boolean hasPersist(Field field) {
        if (field.isAnnotationPresent(ManyToOne.class) || field.isAnnotationPresent(OneToOne.class) ||
                field.isAnnotationPresent(ManyToMany.class) || field.isAnnotationPresent(OneToMany.class)) {
            return true;
        }
        return false;
    }

    /**
     * po转vo
     *
     * @param voClazz          vo的class
     * @param ignoreProperties 忽略拷贝的字段
     * @return
     */
    @SneakyThrows
    public static <V, P> V toVo(P p, Class<V> voClazz, String... ignoreProperties) {
        if (p == null){
            return null;
        }
        V vo = EntityUtils.newInstance(voClazz);
        return TransformUtils.toVo(p, vo, ignoreProperties);
    }

    /**
     * vo转po
     *
     * @param poClazz          po的class
     * @param ignoreProperties 忽略拷贝的字段
     * @return
     */
    @SneakyThrows
    public static <V, P> P toPo(V v, Class<P> poClazz, String... ignoreProperties) {
        if (v == null){
            return null;
        }
        P po = EntityUtils.newInstance(poClazz);
        return TransformUtils.toPo(v, po, ignoreProperties);
    }

    /**
     * 根据映射的字段长度升序排序
     *
     * @param originalFields
     * @param voEntityCache
     * @return
     */
    private static List<Field> sortedField(List<Field> originalFields, EntityCache voEntityCache) {
        Class<?> clazz = voEntityCache.getClazz();
        List<Field> fields = (List<Field>) cache.getIfPresent(clazz);
        if (EntityUtils.isNotEmpty(fields)) {
            return fields;
        }
        fields = originalFields.stream().filter(propertyDescriptor -> {
            Field field = voEntityCache.getField(propertyDescriptor.getName());
            if (field != null) {
                if (field.isAnnotationPresent(Mapping.class)) {
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }).sorted(new Comparator<Field>() {
            @Override
            public int compare(Field field1, Field field2) {
                int length1 = voEntityCache.getField(field1.getName()).getAnnotation(Mapping.class).poProperty().length();
                int length2 = voEntityCache.getField(field2.getName()).getAnnotation(Mapping.class).poProperty().length();
                return length1 - length2;
            }
        }).collect(Collectors.toList());
        cache.put(clazz, fields);
        return fields;
    }

    /**
     * 转vo时映射的字段 po的对象会被转为vo对应的id，对象数组会被转为ids
     *
     * @param voEntityCache  vo的缓存类
     * @param voField        vo字段
     * @param subPo          po的值
     * @param vo             vo对象
     * @param mapperPoField  vo映射po中的字段
     * @param subEntityCache 子对象缓存类
     */
    private static void voEntityToProperty(EntityCache voEntityCache, Field voField, Object subPo, Object vo, Field mapperPoField, EntityCache subEntityCache) {
        try {
            if (subPo instanceof Collection) {
                Collection propertyValues;
                if (List.class.isAssignableFrom(voField.getType())) {
                    propertyValues = Lists.newArrayList();
                } else {
                    propertyValues = Sets.newHashSet();
                }
                for (Object obj : (Collection) subPo) {
                    //获取属性值
                    Object subValue = subEntityCache.getValue(obj, mapperPoField.getName());
                    //加入属性值集合中
                    propertyValues.add(subValue);
                }
                voEntityCache.setValue(vo, voField.getName(), propertyValues);
            } else {
                //获取属性值
                Object subValue = subEntityCache.getValue(subPo, mapperPoField.getName());
                //将属性值设置到vo字段中
                voEntityCache.setValue(vo, voField.getName(), subValue);
            }
        } catch (IllegalArgumentException e) {
            log.error("字段类型匹配错误", e);
            throw new JpaException(voField.getType() + "与" + mapperPoField.getType() + "类型无法匹配");
        }
    }

    /**
     * 转vo时 po与vo映射的字段均为对象，对象的值会进行拷贝（两个对象可以不是同一Class）
     *
     * @param voEntityCache  vo的缓存类
     * @param voField        vo字段
     * @param subPo          po的值
     * @param vo             vo对象
     * @param mapperPoField  映射字段
     * @param subEntityCache 子对象缓存类
     */
    @SneakyThrows
    private static void voFieldCopy(EntityCache voEntityCache, Field voField, Object subPo, Object vo, Field mapperPoField, EntityCache subEntityCache) {
        try {
            //是否相同Class
            boolean hasSameClazz = true;
            if (Collection.class.isAssignableFrom(voField.getType())) {
                //po对象数组拷贝到vo对象数组
                //获取vo字段对象数组的对象类型
                Type[] typeArguments = ((ParameterizedType) voField.getGenericType()).getActualTypeArguments();
                Class typeArgument = (Class) typeArguments[0];
                Object subValue = subEntityCache.getValue(subPo, mapperPoField.getName());
                if (typeArgument != subValue.getClass()) {
                    //字段类型不同需要额外转换赋值
                    Collection instances;
                    if (List.class.isAssignableFrom(voField.getType())) {
                        instances = Lists.newArrayList();
                    } else {
                        instances = Sets.newHashSet();
                    }
                    for (Object obj : (Collection) subValue) {
                        Object subVo = EntityUtils.newInstance(typeArgument);
                        toVo(obj, subVo);
                        instances.add(subVo);
                    }
                    voEntityCache.setValue(vo, voField.getName(), instances);
                    hasSameClazz = false;
                }
            } else {
                //po对象拷贝到vo对象
                if (voField.getType() != subPo.getClass()) {
                    Object subValue = subEntityCache.getValue(subPo, mapperPoField.getName());
                    //字段类型不同需要额外转换赋值
                    Object subVo = EntityUtils.newInstance(voField.getType());
                    toVo(subValue, subVo);
                    voEntityCache.setValue(vo, voField.getName(), subVo);
                    hasSameClazz = false;
                }
            }
            //字段类型相同直接赋值
            if (hasSameClazz) {
                voEntityCache.setValue(vo, voField.getName(), subPo);
            }
        } catch (Exception e) {
            log.error("字段类型匹配错误", e);
            throw new JpaException(voField.getType() + "与" + mapperPoField.getType() + "类型无法匹配");
        }
    }

    /**
     * 转po时 vo与po映射的字段均为对象，对象的值会进行拷贝（两个对象可以不是同一Class）
     *
     * @param mapperPoField  po映射字段
     * @param subEntityCache po缓存类
     * @param value          vo值
     */
    @SneakyThrows
    private static void poFieldCopy(Object subPoVal, Field mapperPoField, EntityCache subEntityCache, Object value) {
        boolean hasSameClazz = true;
        if (Collection.class.isAssignableFrom(mapperPoField.getType())) {
            Type[] typeArguments = ((ParameterizedType) mapperPoField.getGenericType()).getActualTypeArguments();
            Class typeArgument = (Class) typeArguments[0];

            Collection instances;
            if (List.class.isAssignableFrom(mapperPoField.getType())) {
                instances = Lists.newArrayList();
            } else {
                instances = Sets.newHashSet();
            }
            if (Collection.class.isAssignableFrom(subPoVal.getClass())) {
                Collection values = (Collection) value;
                Collection subPos = (Collection) subPoVal;
                int i = 0;
                for (Object po : subPos) {
                    int j = 0;
                    for (Object val : values) {
                        if (i == j) {
                            toPo(val, po);
                            instances.add(po);
                        }
                        j++;
                    }
                    i++;
                }
                if (typeArgument != value.getClass()) {
                    hasSameClazz = false;
                }
            } else {
                if (typeArgument != value.getClass()) {
                    //字段类型不同需要额外转换赋值
                    for (Object obj : (Collection) value) {
                        Object subPo = EntityUtils.newInstance(typeArgument);
                        toPo(obj, subPo);
                        instances.add(subPo);
                    }
                    if (EntityUtils.isNotEmpty(subEntityCache.getValue(subPoVal, mapperPoField.getName()))) {
                        return;
                    }
                    subEntityCache.setValue(subPoVal, mapperPoField.getName(), instances);
                    hasSameClazz = false;
                }
            }

        } else {
            //vo对象拷贝到po对象
            if (mapperPoField.getType() != value.getClass()) {
                if (subEntityCache.getValue(subPoVal, mapperPoField.getName()) != null) {
                    return;
                }
                //字段类型不同需要额外转换赋值
                Object subPo = EntityUtils.newInstance(mapperPoField.getType());
                toPo(value, subPo);
                subEntityCache.setValue(subPoVal, mapperPoField.getName(), subPo);
                hasSameClazz = false;
            }
        }
        //字段类型相同直接赋值
        if (hasSameClazz) {
            if (subEntityCache.getValue(subPoVal, mapperPoField.getName()) != null) {
                return;
            }
            subEntityCache.setValue(subPoVal, mapperPoField.getName(), value);
        }
    }

    private static void poEntityToProperty(Object subPoVal, Field mapperPoField, EntityCache subEntityCache, Object value) {
        if (!mapperPoField.isAnnotationPresent(Id.class)) {
            return;
        }

        //po中映射字段对象缓存类
        if (Collection.class.isAssignableFrom(value.getClass())) {
            Collection values = (Collection) value;
            Collection subPos = (Collection) subPoVal;
            int i = 0;
            for (Object po : subPos) {
                int j = 0;
                for (Object val : values) {
                    if (i == j) {
                        Object poValue = subEntityCache.getValue(po, mapperPoField.getName());
                        if (poValue == null) {
                            subEntityCache.setValue(po, mapperPoField.getName(), val);
                        }
                    }
                    j++;
                }
                i++;
            }
        } else {
            Object subValue = subEntityCache.getValue(subPoVal, mapperPoField.getName());
            if (subValue != null) {
                return;
            }
            //创建po中字段的对象
            subEntityCache.setValue(subPoVal, mapperPoField.getName(), value);
        }
    }

    /**
     * 获取po代理类中的po对象
     *
     * @param proxy
     * @return
     */
    public static Object getEntityByProxy(Object proxy) {
        EntityCache entityCache = EntityCache.forClass(proxy.getClass());
        Field field = entityCache.getField("$cglib_prop_proxied");
        if (field != null) {
            return entityCache.getValue(proxy, field.getName());
        }
        return null;
    }
}

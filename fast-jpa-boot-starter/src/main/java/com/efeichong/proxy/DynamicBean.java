package com.efeichong.proxy;

import com.efeichong.cache.EntityCache;
import lombok.NonNull;
import org.springframework.cglib.beans.BeanGenerator;
import org.springframework.cglib.beans.BeanMap;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author lxk
 * @date 2021/1/18
 * @description 生成一个动态的类
 */
public class DynamicBean {

    /**
     * 动态生成的类
     */
    private Object object;
    /**
     * 存放属性名称以及属性的类型
     */
    private BeanMap beanMap;
    /**
     * K:属性名 V:属性值
     */
    private Map<String, Object> propMap = new HashMap();


    public DynamicBean(@NonNull List<PropDesc> propDescList, @NonNull Object obj) {
        this.object = generateBean(propDescList, obj);
        this.beanMap = BeanMap.create(this.object);
        this.beanMap.putAll(propMap);
    }

    /**
     * 生成代理类
     *
     * @param propDescList
     * @param proxied
     * @return
     */
    private Object generateBean(List<PropDesc> propDescList, Object proxied) {
        BeanGenerator generator = new BeanGenerator();
        EntityCache entityCache = EntityCache.forClass(proxied.getClass());
        for (PropDesc propDesc : propDescList) {
            generator.addProperty(propDesc.getFieldName(), propDesc.getClazz());
            propMap.put(propDesc.getFieldName(), propDesc.getValue());
        }

        List<Field> propFields = entityCache.getFields();
        for (Field field : propFields) {
            String fieldName = field.getName();
            Object value = entityCache.getValue(proxied, fieldName);
            if (!"class".equals(fieldName)) {
                generator.addProperty(fieldName, field.getType());
                propMap.put(fieldName, value);
            }
        }
        generator.setSuperclass(proxied.getClass());
        return generator.create();
    }

    /**
     * 得到该实体bean对象
     *
     * @return
     */
    public Object getObject() {
        return this.object;
    }
}
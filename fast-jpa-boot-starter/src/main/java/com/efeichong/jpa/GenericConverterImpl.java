package com.efeichong.jpa;

import com.efeichong.exception.JpaException;
import com.efeichong.util.EntityUtils;
import com.efeichong.util.TransformUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author lxk
 * @date 2020/9/7
 * @description 将hibernate返回的Map类型转换为 {@link Convert}
 */
@Slf4j
public class GenericConverterImpl implements GenericConverter {

    /**
     * 指定要匹配转换的类型
     *
     * @return
     */
    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        ConvertiblePair pair = new ConvertiblePair(Map.class, Convert.class);
        Set<ConvertiblePair> set = new HashSet<>();
        set.add(pair);
        return set;
    }

    /**
     * 进行转换
     *
     * @param source     源数据
     * @param sourceType 源类型
     * @param targetType 目标类型
     * @return
     */
    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {

        PropertyDescriptor targetPd = null;
        try {
            if (source instanceof Map) {
                Map<String, Object> sourceMap = (Map) source;
                Object target = EntityUtils.newInstance(targetType.getType());
                for (Map.Entry<String, Object> entry : sourceMap.entrySet()) {
                    String fieldName = entry.getKey();
                    targetPd = BeanUtils.getPropertyDescriptor(targetType.getType(), fieldName);
                    if (targetPd != null) {
                        Method writeMethod = targetPd.getWriteMethod();
                        if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                            writeMethod.setAccessible(true);
                        }
                        writeMethod.invoke(target, TypeUtils.cast(entry.getValue(), targetPd.getPropertyType()));
                    }
                }
                return target;
            } else {
                Object target = TransformUtils.toVo(source, targetType.getType());
                return target;
            }
        } catch (Exception e) {
            String message = targetPd == null ? "" : targetPd.getName();
            throw new JpaException("属性转换异常：" + message, e);
        }
    }

}





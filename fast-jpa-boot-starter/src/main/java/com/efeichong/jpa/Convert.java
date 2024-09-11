package com.efeichong.jpa;

import org.springframework.core.convert.TypeDescriptor;

/**
 * @author lxk
 * @date 2020/9/7
 * @description 实现该接口标记哪些类需要被转换 可以通过spring转换器将hibernate返回的Map转换为该对象
 * {@link GenericConverterImpl#convert(Object map, TypeDescriptor sourceType, TypeDescriptor targetType)}
 */
public interface Convert {
}

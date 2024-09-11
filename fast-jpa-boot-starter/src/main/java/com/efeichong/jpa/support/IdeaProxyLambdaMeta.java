package com.efeichong.jpa.support;

import com.efeichong.cache.EntityCache;
import com.efeichong.exception.JpaException;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;


/**
 * @author lxk
 * @date 2020/9/25
 * @description 在 IDEA 的 Evaluate 中执行的 Lambda 表达式元数据需要使用该类处理元数据
 */
public class IdeaProxyLambdaMeta implements LambdaMeta {
    private static final Field FIELD_MEMBER_NAME;
    private static final Field FIELD_MEMBER_NAME_CLAZZ;
    private static final Field FIELD_MEMBER_NAME_NAME;

    static {
        try {
            Class<?> classDirectMethodHandle = Class.forName("java.lang.invoke.DirectMethodHandle");
            FIELD_MEMBER_NAME = EntityCache.setAccessible(classDirectMethodHandle.getDeclaredField("member"));
            Class<?> classMemberName = Class.forName("java.lang.invoke.MemberName");
            FIELD_MEMBER_NAME_CLAZZ = EntityCache.setAccessible(classMemberName.getDeclaredField("clazz"));
            FIELD_MEMBER_NAME_NAME = EntityCache.setAccessible(classMemberName.getDeclaredField("name"));
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new JpaException(e);
        }
    }

    private final Class<?> clazz;
    private final String name;

    public IdeaProxyLambdaMeta(Proxy func) {
        InvocationHandler handler = Proxy.getInvocationHandler(func);
        try {
            Object dmh = EntityCache.setAccessible(handler.getClass().getDeclaredField("val$target")).get(handler);
            Object member = FIELD_MEMBER_NAME.get(dmh);
            clazz = (Class<?>) FIELD_MEMBER_NAME_CLAZZ.get(member);
            name = (String) FIELD_MEMBER_NAME_NAME.get(member);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            throw new JpaException(e);
        }
    }

    @Override
    public String getImplMethodName() {
        return name;
    }


    @Override
    public String toString() {
        return clazz.getSimpleName() + "::" + name;
    }

}

package com.efeichong.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * @author : lxk
 * @Date : 2018/8/31 11:36
 * @Description : 获取springBean 工具类
 */
@Component
public class FastJpaSpringUtils implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    /**
     * 获取applicationContext
     *
     * @return
     */
    public static ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (FastJpaSpringUtils.applicationContext == null) {
            FastJpaSpringUtils.applicationContext = applicationContext;
        }
    }

    public static Object get() {
        return getApplicationContext();
    }

    /**
     * 通过name获取 Bean.
     *
     * @param name
     * @return
     */
    public static <T> T getBean(String name) {
        return (T) getApplicationContext().getBean(name);
    }

    /**
     * 通过class获取Bean.
     *
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(Class<T> clazz) {
        return getApplicationContext().getBean(clazz);
    }

    /**
     * 通过name,以及Clazz返回指定的Bean
     *
     * @param name
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T getBean(String name, Class<T> clazz) {
        return getApplicationContext().getBean(name, clazz);
    }

    /**
     * 是否为spring运行环境
     *
     * @return
     */
    public static boolean hasSpringRunTime() {
        return applicationContext != null;
    }
}

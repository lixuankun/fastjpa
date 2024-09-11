package com.efeichong.proxy;

import lombok.NonNull;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author lxk
 * @date 2021/1/18
 * @description 生成一个代理类
 */
public class ProxyFactory implements MethodInterceptor {

    /**
     * 要代理的真实对象
     */
    private Object object;
    /**
     * 前置操作
     */
    private Before before;
    /**
     * 后置操作
     */
    private After after;
    /**
     * 切入的方法名
     */
    private String methodName;

    public Object createProxy(@NonNull Object target, Before before, After after, @NonNull String methodName) {
        this.object = target;
        this.methodName = methodName;
        this.before = before;
        this.after = after;
        Enhancer enhancer = new Enhancer();
        //设置代理目标
        enhancer.setSuperclass(this.object.getClass());
        //设置单一回调对象，在调用中拦截对目标方法的调用
        enhancer.setCallback(this);
        return enhancer.create();
    }

    @Override
    public Object intercept(Object obj, Method method, Object[] objects, MethodProxy proxy) throws Throwable {
        Object result;
        if (method.getName().equals(methodName)) {
            if (before != null) {
                before.before();
            }
            result = proxy.invokeSuper(obj, objects);
            if (after != null) {
                after.after();
            }
        } else {
            result = proxy.invokeSuper(obj, objects);
        }
        return result;
    }

    /**
     * 前置操作
     */
    public interface Before {
        void before();
    }

    /**
     * 后置操作
     */
    public interface After {
        void after();
    }

}


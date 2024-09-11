package com.efeichong.proxy;

import lombok.SneakyThrows;

import java.util.List;

/**
 * @author lxk
 * @date 2021/1/18
 * @description
 */
public class Test {
    private Integer a;

    @SneakyThrows
    public static void main(String[] args) {

        Test test = new Test();
        test.setA(11);

        List<PropDesc> propDescs = PropDesc.builder()
                .add("c", String.class, "333")
                .add("b", String.class, "222")
                .build();
        DynamicBean dynamicBean = new DynamicBean(propDescs, test);
        Object object = dynamicBean.getObject();
        System.out.println(object.getClass());


        ProxyFactory proxy = new ProxyFactory();
        Test proxy1 = (Test) proxy.createProxy(test, new ProxyFactory.Before() {
            @Override
            public void before() {
                System.out.println("before");
            }
        }, new ProxyFactory.After() {
            @Override
            public void after() {
                System.out.println("after");
            }
        }, "setA");
        proxy1.setA(22);
        System.out.println(proxy1);
    }

    public Integer getA() {
        return a;
    }

    public void setA(Integer a) {
        this.a = a;
    }
}

package com.efeichong.proxy;

import com.efeichong.util.EntityUtils;
import lombok.SneakyThrows;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lxk
 * @date 2020/9/8
 * @description 测试类加载器
 */
public class ClassLoaderTest {

    @SneakyThrows
    public static void main(String[] args) {
        ClassLoader myLoad = new ClassLoader() {
            @SneakyThrows
            @Override
            public Class<?> loadClass(String name) {
                String fileName = name.substring(name.lastIndexOf(".") + 1) + ".class";
                InputStream is = getClass().getResourceAsStream(fileName);
                if (null == is) {
                    return super.loadClass(name);
                }
                byte[] b = new byte[is.available()];
                is.read(b);
                return defineClass(name, b, 0, b.length);
            }
        };
        Object obj = EntityUtils.newInstance(myLoad.loadClass("com.efeichong.proxy.ClassLoaderTest"));
        Map map = new HashMap();
        System.out.println(obj.getClass());
        System.out.println(ClassLoaderTest.class);
        System.out.println(obj instanceof ClassLoaderTest);
        System.out.println(obj.getClass() == ClassLoaderTest.class);
        System.out.println(obj.getClass().toString() == ClassLoaderTest.class.toString());
        map.put(ClassLoaderTest.class.toString(), "22");
        System.out.println(map.get(obj.getClass().toString()));
    }

}
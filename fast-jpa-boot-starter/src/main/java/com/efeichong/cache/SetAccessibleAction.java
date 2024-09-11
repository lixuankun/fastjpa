package com.efeichong.cache;

import java.lang.reflect.AccessibleObject;
import java.security.PrivilegedAction;

/**
 * @author lxk
 * @date 2020/9/25
 * @description
 */
public class SetAccessibleAction<T extends AccessibleObject> implements PrivilegedAction<T> {
    private final T obj;

    public SetAccessibleAction(T obj) {
        this.obj = obj;
    }

    @Override
    public T run() {
        obj.setAccessible(true);
        return obj;
    }

}

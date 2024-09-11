/*
 * Copyright (c) 2011-2022, baomidou (jobob@qq.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.efeichong.util;

import com.efeichong.cache.EntityCache;
import com.efeichong.jpa.SFunction;
import com.efeichong.jpa.support.*;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author lxk
 * @date 2020/9/25
 * @description Lambda 解析工具类
 */
public final class LambdaUtils {

    /**
     * 该缓存可能会在任意不定的时间被清除
     *
     * @param func 需要解析的 lambda 对象
     * @param <T>  类型，被调用的 Function 对象的目标类型
     * @return 返回解析后的结果
     */
    public static <T> LambdaMeta extract(SFunction<T, ?> func) {
        // 1. IDEA 调试模式下 lambda 表达式是一个代理
        if (func instanceof Proxy) {
            return new IdeaProxyLambdaMeta((Proxy) func);
        }
        // 2. 反射读取
        try {
            Method method = func.getClass().getDeclaredMethod("writeReplace");
            return new ReflectLambdaMeta((SerializedLambda) EntityCache.setAccessible(method).invoke(func));
        } catch (Throwable e) {
            // 3. 反射失败使用序列化的方式读取
            return new ShadowLambdaMeta(SerializedLambda.extract(func));
        }
    }


}

package com.efeichong.generator;

import java.util.Map;

/**
 * @author lxk
 * @date 2022/1/19
 * @description
 */
public class MapUtils {

    public static boolean isEmpty(Map map) {
        return map == null || map.isEmpty();
    }

    public static boolean isNotEmpty(Map map) {
        return !isEmpty(map);
    }


}

package com.efeichong.util;

import com.efeichong.exception.BaseException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lxk
 * @date 2021/1/4
 * @description 时间处理工具类 基于joda
 */
public class DateUtils {
    /**
     * 默认加载的时间格式
     */
    private static final Map<String, String> patternMap = ImmutableMap.<String, String>builder()
            .put("\\d{4}-\\d{2}-\\d{2}", "yyyy-MM-dd")
            .put("\\d{4}\\d{2}\\d{2}", "yyyyMMdd")
            .put("\\d{4}/\\d{2}/\\d{2}", "yyyy/MM/dd")
            .put("\\d{4}年\\d{2}月\\d{2}日", "yyyy年MM月dd日")
            .put("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2}", "yyyy-MM-dd HH:mm:ss")
            .put("\\d{4}/\\d{2}/\\d{2} \\d{2}:\\d{2}:\\d{2}", "yyyy/MM/dd HH:mm:ss")
            .put("\\d{4}年\\d{2}月\\d{2}日 \\d{2}时\\d{2}分\\d{2}秒", "yyyy年MM月dd日 HH时mm分ss秒")
            .put("\\d{4}年\\d{2}月\\d{2}日 \\d{2}:\\d{2}:\\d{2}", "yyyy年MM月dd日 HH:mm:ss")
            .build();

    private static Cache<String, SimpleDateFormat> cache = CacheBuilder.newBuilder()
//            .weakKeys()
            .build();

    /**
     * 字符串转日期
     *
     * @param date
     * @return
     */
    @SneakyThrows
    public static Date parse(String date) {
        date = URLDecoder.decode(date, "utf-8");
        String pattern = matcher(date);
        if (pattern == null) {
            throw new BaseException("未匹配到表达式");
        }
        SimpleDateFormat format = cache.get(pattern, () -> new SimpleDateFormat(pattern));
        return format.parse(date);
    }

    /**
     * 字符串转日期
     *
     * @param date
     * @return
     */
    @SneakyThrows
    public static Date parse(String date, String pattern) {
        SimpleDateFormat format = cache.get(pattern, () -> new SimpleDateFormat(pattern));
        return format.parse(date);
    }

    /**
     * 匹配字符串的日期格式
     *
     * @param date
     * @return
     */
    private static String matcher(String date) {
        for (Map.Entry<String, String> entry : patternMap.entrySet()) {
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher match = pattern.matcher(date);
            if (match.matches()) {
                return entry.getValue();
            }
        }
        return null;
    }
}

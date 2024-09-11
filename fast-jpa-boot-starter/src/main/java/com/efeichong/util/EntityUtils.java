package com.efeichong.util;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.lang.Nullable;
import org.springframework.util.ObjectUtils;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Array;
import java.util.*;

/**
 * @author lxk
 * @date 2020/11/26
 * @description 对象操作工具类
 */
@Slf4j
public class EntityUtils {

    /**
     * 判断两个对象的属性值是否相同
     *
     * @param source
     * @param target
     * @param properties 指定字段，默认所有字段一 一对比
     * @return
     */
    public static boolean propertyHasSimilar(Object source, Object target, boolean ignoreCase, String... properties) {
        BeanWrapper src = new BeanWrapperImpl(source);
        BeanWrapper trg = new BeanWrapperImpl(target);
        boolean isSimilar = true;

        if (properties != null && properties.length > 0) {
            for (String property : properties) {
                isSimilar = propertyHasSimilar(src, trg, property, ignoreCase);
                if (!isSimilar) {
                    return isSimilar;
                }
            }
        } else {
            PropertyDescriptor[] pds = src.getPropertyDescriptors();
            for (PropertyDescriptor pd : pds) {
                isSimilar = propertyHasSimilar(src, trg, pd.getName(), ignoreCase);
                if (!isSimilar) {
                    return isSimilar;
                }
            }
        }
        return isSimilar;
    }

    /**
     * 是否为cglib代理类
     *
     * @param clazz
     * @return
     */
    public static boolean isCglibProxy(Class<?> clazz) {
        return clazz.getName().contains("$$BeanGeneratorByCGLIB");
    }

    /**
     * 是否为hibernate代理类
     *
     * @param clazz
     * @return
     */
    public static boolean isHibernateProxy(Class<?> clazz) {
        return clazz.getName().contains("$HibernateProxy");
    }

    /**
     * 判断两个对象是否相等
     *
     * @param object1
     * @param object2
     * @return
     */
    public static boolean equals(Object object1, Object object2) {
        return object1 == object2 ? true : (object1 != null && object2 != null ? object1.equals(object2) : false);
    }

    /**
     * 判断两个对象是不否相等
     *
     * @param object1
     * @param object2
     * @return
     */
    public static boolean notEquals(Object object1, Object object2) {
        return !equals(object1, object2);
    }

    /**
     * 判断两个对象是否相等，字符串类型忽略大小写
     *
     * @param object1
     * @param object2
     * @return
     */
    public static boolean equalsIgnoreCase(Object object1, Object object2) {
        return object1 == object2 ? true : (object1 != null && object2 != null ?
                object1.toString().equalsIgnoreCase(object2.toString()) : false);
    }

    //---------------------------------------------------对象拷贝--------------------------------------------------------//

    /**
     * 对象字段拷贝（浅拷贝）
     *
     * @param source
     * @param target
     * @param ignoreProperties
     */
    public static void copyProperties(@NonNull Object source, @NonNull Object target, String... ignoreProperties) {
        BeanUtils.copyProperties(source, target, ignoreProperties);
    }

    /**
     * 对象字段拷贝（浅拷贝） 忽略空，空字符，空集合，空格，空map
     *
     * @param source
     * @param target
     * @param ignoreProperties
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target, String... ignoreProperties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, ignoreProperties));
    }

    /**
     * 对象字段拷贝（浅拷贝） 忽略空，空字符，空集合，空格，空map
     *
     * @param source
     * @param target
     * @param ignoreProperties
     * @param canBeNullProps
     */
    public static void copyPropertiesIgnoreNull(Object source, Object target, String[] ignoreProperties, String[] canBeNullProps) {
        Set<String> nullPropertyNames = getNullPropertyNames(source);
        if (isNotEmpty(canBeNullProps)) {
            Iterator<String> iterator = nullPropertyNames.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                boolean anyMatch = Arrays.stream(canBeNullProps).anyMatch(prop -> equalsIgnoreCase(prop, next));
                if (anyMatch) {
                    iterator.remove();
                }
            }
        }

        int ignorePropertiesSize = 0;
        if (ignoreProperties != null) {
            ignorePropertiesSize = ignoreProperties.length;
            nullPropertyNames.addAll(Arrays.asList(ignoreProperties));
        }
        String[] result = new String[nullPropertyNames.size() + ignorePropertiesSize];
        copyProperties(source, target, nullPropertyNames.toArray(result));
    }


    /**
     * 对象字段拷贝 指定字段
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesWithProps(Object source, Object target, String... properties) {
        copyProperties(source, target, getIgnorePropertyNames(source, properties));
    }

    /**
     * 对象字段拷贝 指定字段且非空
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesWithPropsIgnoreNull(Object source, Object target, String... properties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, getIgnorePropertyNames(source, properties)));
    }

    /**
     * 对象字段拷贝 指定字段且非空
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesWithPropsIgnoreNull(Object source, Object target, String[] properties, String[] canBeNullProps) {
        Set<String> nullPropertyNames = getNullPropertyNames(source);
        if (isNotEmpty(canBeNullProps)) {
            Iterator<String> iterator = nullPropertyNames.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                boolean anyMatch = Arrays.stream(canBeNullProps).anyMatch(prop -> equalsIgnoreCase(prop, next));
                if (anyMatch) {
                    iterator.remove();
                }
            }
        }

        String[] ignoreProperties = getIgnorePropertyNames(source, properties);

        int ignorePropertiesSize = 0;
        if (ignoreProperties != null) {
            ignorePropertiesSize = ignoreProperties.length;
            nullPropertyNames.addAll(Arrays.asList(ignoreProperties));
        }
        String[] result = new String[nullPropertyNames.size() + ignorePropertiesSize];
        copyProperties(source, target, nullPropertyNames.toArray(result));
    }

    //---------------------------------------------对象转map--------------------------------------------------------//

    /**
     * 对象转map
     *
     * @param source           对象
     * @param target           map
     * @param ignoreProperties
     */
    public static void copyProperties(@NonNull Object source, @NonNull Map<Object, Object> target, String... ignoreProperties) {
        BeanMap beanMap = BeanMap.create(source);
        for (Object entryObj : beanMap.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            if (!contains(ignoreProperties, entry.getKey())) {
                target.put(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * 对象转map
     *
     * @param source           对象
     * @param target           map
     * @param ignoreProperties
     */
    public static void copyPropertiesIgnoreNull(@NonNull Object source, @NonNull Map<Object, Object> target, String... ignoreProperties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, ignoreProperties));
    }

    /**
     * 对象转map
     *
     * @param source     对象
     * @param target     map
     * @param properties
     */
    public static void copyPropertiesWithProps(@NonNull Object source, @NonNull Map<Object, Object> target, String... properties) {
        copyProperties(source, target, getIgnorePropertyNames(source, properties));
    }

    /**
     * 对象转map
     *
     * @param source     对象
     * @param target     map
     * @param properties
     */
    public static void copyPropertiesWithPropsIgnoreNull(@NonNull Object source, @NonNull Map<Object, Object> target, String... properties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, getIgnorePropertyNames(source, properties)));
    }

    //---------------------------------------------map转对象--------------------------------------------------------//


    /**
     * map转对象
     *
     * @param source
     * @param target
     * @param ignoreProperties
     */
    public static void copyProperties(@NonNull Map<Object, Object> source, @NonNull Object target, String... ignoreProperties) {
        BeanMap beanMap = BeanMap.create(target);
        for (Object entryObj : source.entrySet()) {
            Map.Entry entry = (Map.Entry) entryObj;
            if (!contains(ignoreProperties, entry.getKey())) {
                beanMap.put(entry.getKey(), entry.getValue());
            }
        }
    }


    /**
     * map转对象
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesIgnoreNull(@NonNull Map<Object, Object> source, @NonNull Object target, String... properties) {
        copyProperties(source, target, getIgnorePropertyNames(source, properties));
    }


    /**
     * map转对象
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesWithProps(@NonNull Map<Object, Object> source, @NonNull Object target, String... properties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, properties));
    }


    /**
     * map转对象
     *
     * @param source
     * @param target
     * @param properties
     */
    public static void copyPropertiesWithPropsIgnoreNull(@NonNull Map<Object, Object> source, @NonNull Object target, String... properties) {
        copyProperties(source, target, getNullPropertyNamesAndConcat(source, getIgnorePropertyNames(source, properties)));
    }

    /**
     * 判断对象是否为空，空字符，空集合，空格，空map
     *
     * @param obj
     * @return
     */
    public static boolean isEmpty(@Nullable Object obj) {
        try {
            if (obj == null) {
                return true;
            }
            if (obj instanceof Optional) {
                return !((Optional) obj).isPresent();
            }
            if (obj instanceof CharSequence) {
                CharSequence charSequence = (CharSequence) obj;
                return charSequence.length() == 0 && isBlank(charSequence);
            }
            if (obj.getClass().isArray()) {
                return Array.getLength(obj) == 0;
            }
            if (obj instanceof Collection) {
                return ((Collection) obj).isEmpty();
            }
            if (obj instanceof Map) {
                return ((Map) obj).isEmpty();
            }
            return false;
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断对象是否不为空，空字符，空集合，空格，空map
     *
     * @param obj
     * @return
     */
    public static boolean isNotEmpty(@Nullable Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 查询为空的字段(空，空字符，空集合，空格，空map)
     *
     * @param source
     * @param ignoreProperties
     * @return
     */
    private static String[] getNullPropertyNamesAndConcat(Object source, String[] ignoreProperties) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (isEmpty(srcValue)) {
                emptyNames.add(pd.getName());
            }
        }
        int ignorePropertiesSize = 0;
        if (ignoreProperties != null) {
            ignorePropertiesSize = ignoreProperties.length;
            emptyNames.addAll(Arrays.asList(ignoreProperties));
        }
        String[] result = new String[emptyNames.size() + ignorePropertiesSize];
        return emptyNames.toArray(result);
    }

    /**
     * 查询为空的字段(空，空字符，空集合，空格，空map)
     *
     * @param source
     * @return
     */
    private static Set<String> getNullPropertyNames(Object source) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (isEmpty(srcValue)) {
                emptyNames.add(pd.getName());
            }
        }
        return emptyNames;
    }

    /**
     * 筛选需要忽略拷贝的字段
     *
     * @param source
     * @return
     */
    private static String[] getIgnorePropertyNames(Object source, String[] properties) {
        BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> ignoreNames = new HashSet();
        for (PropertyDescriptor pd : pds) {
            if (!contains(properties, pd.getName())) {
                ignoreNames.add(pd.getName());
            }
        }
        return ignoreNames.toArray(new String[ignoreNames.size()]);
    }

    /**
     * 判断值是否相等
     *
     * @param src
     * @param trg
     * @param property
     * @param ignoreCase
     * @return
     */
    private static boolean propertyHasSimilar(BeanWrapper src, BeanWrapper trg, String property, boolean ignoreCase) {
        Object srcValue = src.getPropertyValue(property);
        Object trgValue = trg.getPropertyValue(property);
        if (ignoreCase) {
            return equalsIgnoreCase(srcValue, trgValue);
        }
        return equals(srcValue, trgValue);
    }

    /**
     * 驼峰转下划线
     *
     * @param name
     * @return
     */
    public static String convertToLine(@NonNull String name) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(name);
    }

    /**
     * 下划线转驼峰
     *
     * @param name
     * @return
     */
    public static String convertToHump(@NonNull String name) {
        if (name.indexOf("_") == -1) {
            return name;
        }
        return CaseFormat.LOWER_UNDERSCORE.converterTo(CaseFormat.LOWER_CAMEL).convert(name);
    }

    /**
     * 首字母转小写
     *
     * @param name
     * @return
     */
    public static String toLowerCaseFirstOne(String name) {
        if (Character.isLowerCase(name.charAt(0))) {
            return name;
        } else {
            return (new StringBuilder()).append(Character.toLowerCase(name.charAt(0))).append(name.substring(1)).toString();
        }
    }

    /**
     * 首字母转大写
     *
     * @param name
     * @return
     */
    public static String toUpperCaseFirstOne(String name) {
        if (Character.isUpperCase(name.charAt(0))) {
            return name;
        } else {
            return (new StringBuilder()).append(Character.toUpperCase(name.charAt(0))).append(name.substring(1)).toString();
        }
    }

    /**
     * 去掉字符串前后的指定字符
     *
     * @param str
     * @param splitter
     * @return
     */
    public static String trimBothChars(String str, String splitter) {
        String regex = "^" + splitter + "*|" + splitter + "*$";
        return str.replaceAll(regex, "");
    }

    public static String join(Iterable<?> parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(Iterator<?> parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(Object[] parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(@org.checkerframework.checker.nullness.qual.Nullable Object first, @org.checkerframework.checker.nullness.qual.Nullable Object second, String separator, Object... rest) {
        return Joiner.on(separator).join(first, second, separator, rest);
    }

    private static boolean isBlank(final CharSequence cs) {
        int strLen;
        if (cs == null || (strLen = cs.length()) == 0) {
            return true;
        }
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(cs.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean contains(final Object[] array, final Object objectToFind) {
        return ObjectUtils.containsElement(array, objectToFind);
    }

    @SneakyThrows
    public static <T> T newInstance(Class<? extends T> clazz) {
        return clazz.getDeclaredConstructor().newInstance();
    }

    /**
     * <p>
     * Replaces all occurrences of Strings within another String.
     * </p>
     *
     * <p>
     * A {@code null} reference passed to this method is a no-op, or if
     * any "search string" or "string to replace" is null, that replace will be
     * ignored. This will not repeat. For repeating replaces, call the
     * overloaded method.
     * </p>
     *
     * <pre>
     *  StringUtils.replaceEach(null, *, *)        = null
     *  StringUtils.replaceEach("", *, *)          = ""
     *  StringUtils.replaceEach("aba", null, null) = "aba"
     *  StringUtils.replaceEach("aba", new String[0], null) = "aba"
     *  StringUtils.replaceEach("aba", null, new String[0]) = "aba"
     *  StringUtils.replaceEach("aba", new String[]{"a"}, null)  = "aba"
     *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""})  = "b"
     *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"})  = "aba"
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"})  = "wcte"
     *  (example of how it does not repeat)
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"})  = "dcte"
     * </pre>
     *
     * @param text            text to search and replace in, no-op if null
     * @param searchList      the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @return the text with any replacements processed, {@code null} if
     * null String input
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *                                  and/or size 0)
     * @since 2.4
     */
    public static String replaceEach(final String text, final String[] searchList, final String[] replacementList) {
        return replaceEach(text, searchList, replacementList, false, 0);
    }

    /**
     * 转换为字符串<br>
     * 如果给定的值为null，或者转换失败，返回默认值<br>
     * 转换失败不会报错
     *
     * @param value        被转换的值
     * @param defaultValue 转换错误时的默认值
     * @return 结果
     */
    public static String toStr(Object value, String defaultValue) {
        if (null == value) {
            return defaultValue;
        }
        if (value instanceof String) {
            return (String) value;
        }
        return value.toString();
    }

    /**
     * 转换为字符串<br>
     * 如果给定的值为<code>null</code>，或者转换失败，返回默认值<code>null</code><br>
     * 转换失败不会报错
     *
     * @param value 被转换的值
     * @return 结果
     */
    public static String toStr(Object value) {
        return toStr(value, null);
    }

    /**
     * 去空格
     */
    public static String trim(String str) {
        return (str == null ? "" : str.trim());
    }

    /**
     * <p>
     * Replace all occurrences of Strings within another String.
     * This is a private recursive helper method for {@link #replaceEach(String, String[], String[])} and
     * {@link #replaceEach(String, String[], String[])}
     * </p>
     *
     * <p>
     * A {@code null} reference passed to this method is a no-op, or if
     * any "search string" or "string to replace" is null, that replace will be
     * ignored.
     * </p>
     *
     * <pre>
     *  StringUtils.replaceEach(null, *, *, *, *) = null
     *  StringUtils.replaceEach("", *, *, *, *) = ""
     *  StringUtils.replaceEach("aba", null, null, *, *) = "aba"
     *  StringUtils.replaceEach("aba", new String[0], null, *, *) = "aba"
     *  StringUtils.replaceEach("aba", null, new String[0], *, *) = "aba"
     *  StringUtils.replaceEach("aba", new String[]{"a"}, null, *, *) = "aba"
     *  StringUtils.replaceEach("aba", new String[]{"a"}, new String[]{""}, *, >=0) = "b"
     *  StringUtils.replaceEach("aba", new String[]{null}, new String[]{"a"}, *, >=0) = "aba"
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"w", "t"}, *, >=0) = "wcte"
     *  (example of how it repeats)
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, false, >=0) = "dcte"
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "t"}, true, >=2) = "tcte"
     *  StringUtils.replaceEach("abcde", new String[]{"ab", "d"}, new String[]{"d", "ab"}, *, *) = IllegalStateException
     * </pre>
     *
     * @param text            text to search and replace in, no-op if null
     * @param searchList      the Strings to search for, no-op if null
     * @param replacementList the Strings to replace them with, no-op if null
     * @param repeat          if true, then replace repeatedly
     *                        until there are no more possible replacements or timeToLive < 0
     * @param timeToLive      if less than 0 then there is a circular reference and endless
     *                        loop
     * @return the text with any replacements processed, {@code null} if
     * null String input
     * @throws IllegalStateException    if the search is repeating and there is an endless loop due
     *                                  to outputs of one being inputs to another
     * @throws IllegalArgumentException if the lengths of the arrays are not the same (null is ok,
     *                                  and/or size 0)
     * @since 2.4
     */
    private static String replaceEach(
            final String text, final String[] searchList, final String[] replacementList, final boolean repeat, final int timeToLive) {

        // mchyzer Performance note: This creates very few new objects (one major goal)
        // let me know if there are performance requests, we can create a harness to measure

        // if recursing, this shouldn't be less than 0
        if (timeToLive < 0) {
            final Set<String> searchSet = new HashSet<>(Arrays.asList(searchList));
            final Set<String> replacementSet = new HashSet<>(Arrays.asList(replacementList));
            searchSet.retainAll(replacementSet);
            if (!searchSet.isEmpty()) {
                throw new IllegalStateException("Aborting to protect against StackOverflowError - " +
                        "output of one loop is the input of another");
            }
        }

        if (isEmpty(text) || isEmpty(searchList) || isEmpty(replacementList) || (isNotEmpty(searchList) && timeToLive == -1)) {
            return text;
        }

        final int searchLength = searchList.length;
        final int replacementLength = replacementList.length;

        // make sure lengths are ok, these need to be equal
        if (searchLength != replacementLength) {
            throw new IllegalArgumentException("Search and Replace array lengths don't match: "
                    + searchLength
                    + " vs "
                    + replacementLength);
        }

        // keep track of which still have matches
        final boolean[] noMoreMatchesForReplIndex = new boolean[searchLength];

        // index on index that the match was found
        int textIndex = -1;
        int replaceIndex = -1;
        int tempIndex = -1;

        // index of replace array that will replace the search string found
        // NOTE: logic duplicated below START
        for (int i = 0; i < searchLength; i++) {
            if (noMoreMatchesForReplIndex[i] || isEmpty(searchList[i]) || replacementList[i] == null) {
                continue;
            }
            tempIndex = text.indexOf(searchList[i]);

            // see if we need to keep searching for this
            if (tempIndex == -1) {
                noMoreMatchesForReplIndex[i] = true;
            } else if (textIndex == -1 || tempIndex < textIndex) {
                textIndex = tempIndex;
                replaceIndex = i;
            }
        }
        // NOTE: logic mostly below END

        // no search strings found, we are done
        if (textIndex == -1) {
            return text;
        }

        int start = 0;

        // get a good guess on the size of the result buffer so it doesn't have to double if it goes over a bit
        int increase = 0;

        // count the replacement text elements that are larger than their corresponding text being replaced
        for (int i = 0; i < searchList.length; i++) {
            if (searchList[i] == null || replacementList[i] == null) {
                continue;
            }
            final int greater = replacementList[i].length() - searchList[i].length();
            if (greater > 0) {
                increase += 3 * greater; // assume 3 matches
            }
        }
        // have upper-bound at 20% increase, then let Java take over
        increase = Math.min(increase, text.length() / 5);

        final StringBuilder buf = new StringBuilder(text.length() + increase);

        while (textIndex != -1) {

            for (int i = start; i < textIndex; i++) {
                buf.append(text.charAt(i));
            }
            buf.append(replacementList[replaceIndex]);

            start = textIndex + searchList[replaceIndex].length();

            textIndex = -1;
            replaceIndex = -1;
            // find the next earliest match
            // NOTE: logic mostly duplicated above START
            for (int i = 0; i < searchLength; i++) {
                if (noMoreMatchesForReplIndex[i] || searchList[i] == null ||
                        searchList[i].isEmpty() || replacementList[i] == null) {
                    continue;
                }
                tempIndex = text.indexOf(searchList[i], start);

                // see if we need to keep searching for this
                if (tempIndex == -1) {
                    noMoreMatchesForReplIndex[i] = true;
                } else if (textIndex == -1 || tempIndex < textIndex) {
                    textIndex = tempIndex;
                    replaceIndex = i;
                }
            }
            // NOTE: logic duplicated above END

        }
        final int textLength = text.length();
        for (int i = start; i < textLength; i++) {
            buf.append(text.charAt(i));
        }
        final String result = buf.toString();
        if (!repeat) {
            return result;
        }

        return replaceEach(result, searchList, replacementList, repeat, timeToLive - 1);
    }

    /**
     * <p>Adds all the elements of the given arrays into a new array.
     * <p>The new array contains all of the element of {@code array1} followed
     * by all of the elements {@code array2}. When an array is returned, it is always
     * a new array.
     *
     * <pre>
     * ArrayUtils.addAll(null, null)     = null
     * ArrayUtils.addAll(array1, null)   = cloned copy of array1
     * ArrayUtils.addAll(null, array2)   = cloned copy of array2
     * ArrayUtils.addAll([], [])         = []
     * ArrayUtils.addAll([null], [null]) = [null, null]
     * ArrayUtils.addAll(["a", "b", "c"], ["1", "2", "3"]) = ["a", "b", "c", "1", "2", "3"]
     * </pre>
     *
     * @param <T>    the component type of the array
     * @param array1 the first array whose elements are added to the new array, may be {@code null}
     * @param array2 the second array whose elements are added to the new array, may be {@code null}
     * @return The new array, {@code null} if both arrays are {@code null}.
     * The type of the new array is the type of the first array,
     * unless the first array is null, in which case the type is the same as the second array.
     * @throws IllegalArgumentException if the array types are incompatible
     * @since 2.1
     */
    public static <T> T[] addAll(final T[] array1, final T... array2) {
        if (array1 == null) {
            return clone(array2);
        } else if (array2 == null) {
            return clone(array1);
        }
        final Class<?> type1 = array1.getClass().getComponentType();
        @SuppressWarnings("unchecked") // OK, because array is of type T
        final T[] joinedArray = (T[]) Array.newInstance(type1, array1.length + array2.length);
        System.arraycopy(array1, 0, joinedArray, 0, array1.length);
        try {
            System.arraycopy(array2, 0, joinedArray, array1.length, array2.length);
        } catch (final ArrayStoreException ase) {
            // Check if problem was due to incompatible types
            /*
             * We do this here, rather than before the copy because:
             * - it would be a wasted check most of the time
             * - safer, in case check turns out to be too strict
             */
            final Class<?> type2 = array2.getClass().getComponentType();
            if (!type1.isAssignableFrom(type2)) {
                throw new IllegalArgumentException("Cannot store " + type2.getName() + " in an array of "
                        + type1.getName(), ase);
            }
            throw ase; // No, so rethrow original
        }
        return joinedArray;
    }

    // Clone
    //-----------------------------------------------------------------------

    /**
     * <p>Shallow clones an array returning a typecast result and handling
     * {@code null}.
     *
     * <p>The objects in the array are not cloned, thus there is no special
     * handling for multi-dimensional arrays.
     *
     * <p>This method returns {@code null} for a {@code null} input array.
     *
     * @param <T>   the component type of the array
     * @param array the array to shallow clone, may be {@code null}
     * @return the cloned array, {@code null} if {@code null} input
     */
    public static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }


}

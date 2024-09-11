package com.efeichong.generator;

import com.google.common.base.CaseFormat;
import com.google.common.base.Joiner;
import lombok.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Iterator;

/**
 * @author lxk
 * @date 2020/10/21
 * @description 字符串处理工具类
 */
class StringUtils {
    private static final int PAD_LIMIT = 8192;

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
     * 驼峰转下划线
     *
     * @param name
     * @return
     */
    public static String convertToLine(@NonNull String name) {
        return CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.LOWER_UNDERSCORE).convert(name);
    }

    /**
     * 判断为空，空格，空字符
     *
     * @param cs 字符
     * @return
     */
    public static boolean isBlank(CharSequence cs) {
        int strLen;
        if (cs != null && (strLen = cs.length()) != 0) {
            for (int i = 0; i < strLen; ++i) {
                if (!Character.isWhitespace(cs.charAt(i))) {
                    return false;
                }
            }

            return true;
        } else {
            return true;
        }
    }

    /**
     * 判断不为空，空格，空字符
     *
     * @param cs 字符
     * @return
     */
    public static boolean isNotBlank(CharSequence cs) {
        return !isBlank(cs);
    }

    /**
     * 所有字符都不为空，空格，空字符
     *
     * @param css
     * @return
     */
    public static boolean isNoneBlank(CharSequence... css) {
        return !isAnyBlank(css);
    }

    /**
     * 存在字符为空，空格，空字符
     *
     * @param css
     * @return
     */
    public static boolean isAnyBlank(CharSequence... css) {
        if (ArrayUtils.isEmpty(css)) {
            return false;
        } else {
            CharSequence[] var1 = css;
            int var2 = css.length;

            for (int var3 = 0; var3 < var2; ++var3) {
                CharSequence cs = var1[var3];
                if (isBlank(cs)) {
                    return true;
                }
            }

            return false;
        }
    }

    /**
     * 所有字符都为空，空格，空字符
     *
     * @param css
     * @return
     */
    public static boolean isAllBlank(final CharSequence... css) {
        if (ArrayUtils.isEmpty(css)) {
            return true;
        }
        for (final CharSequence cs : css) {
            if (isNotBlank(cs)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断两个字符想等
     *
     * @param cs1
     * @param cs2
     * @return
     */
    public static boolean equals(final CharSequence cs1, final CharSequence cs2) {
        if (cs1 == cs2) {
            return true;
        }
        if (cs1 == null || cs2 == null) {
            return false;
        }
        if (cs1.length() != cs2.length()) {
            return false;
        }
        if (cs1 instanceof String && cs2 instanceof String) {
            return cs1.equals(cs2);
        }
        final int length = cs1.length();
        for (int i = 0; i < length; i++) {
            if (cs1.charAt(i) != cs2.charAt(i)) {
                return false;
            }
        }
        return true;
    }


    /**
     * 首字母大写
     *
     * @param str
     * @return
     */
    public static String capitalize(final String str) {
        int strLen;
        if (str == null || (strLen = str.length()) == 0) {
            return str;
        }

        final int firstCodepoint = str.codePointAt(0);
        final int newCodePoint = Character.toTitleCase(firstCodepoint);
        if (firstCodepoint == newCodePoint) {
            // already capitalized
            return str;
        }

        final int newCodePoints[] = new int[strLen]; // cannot be longer than the char array
        int outOffset = 0;
        newCodePoints[outOffset++] = newCodePoint; // copy the first codepoint
        for (int inOffset = Character.charCount(firstCodepoint); inOffset < strLen; ) {
            final int codepoint = str.codePointAt(inOffset);
            newCodePoints[outOffset++] = codepoint; // copy the remaining ones
            inOffset += Character.charCount(codepoint);
        }
        return new String(newCodePoints, 0, outOffset);
    }

    /**
     * 重复一个字符串{@code repeat}次以形成一个*新的字符串，每次都插入一个字符串分隔符。
     *
     * @param str       原字符
     * @param separator 重复字
     * @param repeat    重复次数
     * @return
     */
    public static String repeat(final String str, final String separator, final int repeat) {
        if (str == null || separator == null) {
            return repeat(str, repeat);
        }
        // given that repeat(String, int) is quite optimized, better to rely on it than try and splice this into it
        final String result = repeat(str + separator, repeat);
        return removeEnd(result, separator);
    }

    /**
     * 重复一个字符串{@code repeat}次以形成一个*新的字符串，每次都插入一个字符串分隔符。
     *
     * @param str    原字符
     * @param repeat 重复次数
     * @return
     */
    public static String repeat(final String str, final int repeat) {
        // Performance tuned for 2.0 (JDK1.4)

        if (str == null) {
            return null;
        }
        if (repeat <= 0) {
            return "";
        }
        final int inputLength = str.length();
        if (repeat == 1 || inputLength == 0) {
            return str;
        }
        if (inputLength == 1 && repeat <= PAD_LIMIT) {
            return repeat(str.charAt(0), repeat);
        }

        final int outputLength = inputLength * repeat;
        switch (inputLength) {
            case 1:
                return repeat(str.charAt(0), repeat);
            case 2:
                final char ch0 = str.charAt(0);
                final char ch1 = str.charAt(1);
                final char[] output2 = new char[outputLength];
                for (int i = repeat * 2 - 2; i >= 0; i--, i--) {
                    output2[i] = ch0;
                    output2[i + 1] = ch1;
                }
                return new String(output2);
            default:
                final StringBuilder buf = new StringBuilder(outputLength);
                for (int i = 0; i < repeat; i++) {
                    buf.append(str);
                }
                return buf.toString();
        }
    }


    /**
     * 重复一个字符串{@code repeat}次以形成一个*新的字符串，每次都插入一个字符串分隔符。
     *
     * @param ch     原字符
     * @param repeat 重复次数
     * @return
     */
    public static String repeat(final char ch, final int repeat) {
        if (repeat <= 0) {
            return "";
        }
        final char[] buf = new char[repeat];
        for (int i = repeat - 1; i >= 0; i--) {
            buf[i] = ch;
        }
        return new String(buf);
    }

    /**
     * 检查CharSequence是否在给定*字符集中包含任何字符
     *
     * @param cs
     * @param searchChars
     * @return
     */
    public static boolean containsAny(final CharSequence cs, final char... searchChars) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchChars)) {
            return false;
        }
        final int csLength = cs.length();
        final int searchLength = searchChars.length;
        final int csLast = csLength - 1;
        final int searchLast = searchLength - 1;
        for (int i = 0; i < csLength; i++) {
            final char ch = cs.charAt(i);
            for (int j = 0; j < searchLength; j++) {
                if (searchChars[j] == ch) {
                    if (Character.isHighSurrogate(ch)) {
                        if (j == searchLast) {
                            // missing low surrogate, fine, like String.indexOf(String)
                            return true;
                        }
                        if (i < csLast && searchChars[j + 1] == cs.charAt(i + 1)) {
                            return true;
                        }
                    } else {
                        // ch is in the Basic Multilingual Plane
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 检查CharSequence是否在给定*字符集中包含任何字符
     *
     * @param cs
     * @param searchCharSequences
     * @return
     */
    public static boolean containsAny(final CharSequence cs, final CharSequence... searchCharSequences) {
        if (isEmpty(cs) || ArrayUtils.isEmpty(searchCharSequences)) {
            return false;
        }
        for (final CharSequence searchCharSequence : searchCharSequences) {
            if (contains(cs, searchCharSequence)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 是否包含字符
     *
     * @param seq
     * @param searchSeq
     * @return
     */
    public static boolean contains(final CharSequence seq, final CharSequence searchSeq) {
        if (seq == null || searchSeq == null) {
            return false;
        }
        return CharSequenceUtils.indexOf(seq, searchSeq, 0) >= 0;
    }

    /**
     * 检查CharSequence是否为空（“”）或null
     *
     * @param cs
     * @return
     */
    public static boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    /**
     * 检查CharSequence是否为空（“”）或null
     *
     * @param cs
     * @return
     */
    public static boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    /**
     * 截取字符串
     *
     * @param str   字符
     * @param start 开始位置
     * @return
     */
    public static String substring(final String str, int start) {
        if (str == null) {
            return null;
        }

        // handle negatives, which means last n characters
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        if (start < 0) {
            start = 0;
        }
        if (start > str.length()) {
            return "";
        }

        return str.substring(start);
    }

    /**
     * 截取字符串
     *
     * @param str   字符
     * @param start 开始位置
     * @param end   结束位置
     * @return
     */
    public static String substring(final String str, int start, int end) {
        if (str == null) {
            return null;
        }

        // handle negatives
        if (end < 0) {
            end = str.length() + end; // remember end is negative
        }
        if (start < 0) {
            start = str.length() + start; // remember start is negative
        }

        // check length next
        if (end > str.length()) {
            end = str.length();
        }

        // if start is greater than end, return ""
        if (start > end) {
            return "";
        }

        if (start < 0) {
            start = 0;
        }
        if (end < 0) {
            end = 0;
        }

        return str.substring(start, end);
    }

    /**
     * 仅当子字符串位于源字符串的末尾时才删除子字符串，否则返回源字符串
     *
     * @param str    原字符
     * @param remove 删除的字符
     * @return
     */
    public static String removeEnd(final String str, final String remove) {
        if (isEmpty(str) || isEmpty(remove)) {
            return str;
        }
        if (str.endsWith(remove)) {
            return str.substring(0, str.length() - remove.length());
        }
        return str;
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

    public static String join(Iterable<?> parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(Iterator<?> parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(Object[] parts, String separator) {
        return Joiner.on(separator).join(parts);
    }

    public static String join(@Nullable Object first, @Nullable Object second, String separator, Object... rest) {
        return Joiner.on(separator).join(first, second, separator, rest);
    }
}

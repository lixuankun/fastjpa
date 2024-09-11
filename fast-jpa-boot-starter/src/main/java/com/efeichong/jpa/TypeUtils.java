package com.efeichong.jpa;

import com.efeichong.exception.BaseException;
import com.efeichong.util.EntityUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author lxk
 * @date 2020/12/17
 * @description 类型转换
 */
public class TypeUtils {
    private static final Pattern NUMBER_WITH_TRAILING_ZEROS_PATTERN = Pattern.compile("\\.0*$");
    public static TimeZone defaultTimeZone = TimeZone.getDefault();
    public static Locale defaultLocale = Locale.getDefault();
    public static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public static <T> T cast(Object obj, Class<T> clazz) {
        if (obj == null) {
            if (clazz == int.class) {
                return (T) Integer.valueOf(0);
            } else if (clazz == long.class) {
                return (T) Long.valueOf(0);
            } else if (clazz == short.class) {
                return (T) Short.valueOf((short) 0);
            } else if (clazz == byte.class) {
                return (T) Byte.valueOf((byte) 0);
            } else if (clazz == float.class) {
                return (T) Float.valueOf(0);
            } else if (clazz == double.class) {
                return (T) Double.valueOf(0);
            } else if (clazz == boolean.class) {
                return (T) Boolean.FALSE;
            }
            return null;
        }

        if (clazz == null) {
            throw new IllegalArgumentException("clazz is null");
        }

        if (clazz == obj.getClass()) {
            return (T) obj;
        }

        if (clazz.isAssignableFrom(obj.getClass())) {
            return (T) obj;
        }

        if (clazz == boolean.class || clazz == Boolean.class) {
            return (T) castToBoolean(obj);
        }

        if (clazz == byte.class || clazz == Byte.class) {
            return (T) castToByte(obj);
        }

        if (clazz == char.class || clazz == Character.class) {
            return (T) castToChar(obj);
        }

        if (clazz == short.class || clazz == Short.class) {
            return (T) castToShort(obj);
        }

        if (clazz == int.class || clazz == Integer.class) {
            return (T) castToInt(obj);
        }

        if (clazz == long.class || clazz == Long.class) {
            return (T) castToLong(obj);
        }

        if (clazz == float.class || clazz == Float.class) {
            return (T) castToFloat(obj);
        }

        if (clazz == double.class || clazz == Double.class) {
            return (T) castToDouble(obj);
        }

        if (clazz == String.class) {
            return (T) castToString(obj);
        }

        if (clazz == BigDecimal.class) {
            return (T) castToBigDecimal(obj);
        }

        if (clazz == BigInteger.class) {
            return (T) castToBigInteger(obj);
        }

        if (clazz == Date.class) {
            return (T) castToDate(obj);
        }

        if (Calendar.class.isAssignableFrom(clazz)) {
            Date date = castToDate(obj);
            Calendar calendar;
            if (clazz == Calendar.class) {
                calendar = Calendar.getInstance(defaultTimeZone, defaultLocale);
            } else {
                try {
                    calendar = (Calendar) EntityUtils.newInstance(clazz);
                } catch (Exception e) {
                    throw new BaseException("can not cast to : " + clazz.getName(), e);
                }
            }
            calendar.setTime(date);
            return (T) calendar;
        }
        throw new BaseException("can not cast to : " + clazz.getName());
    }


    public static Boolean castToBoolean(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }

        if (value instanceof BigDecimal) {
            return intValue((BigDecimal) value) == 1;
        }

        if (value instanceof Number) {
            return ((Number) value).intValue() == 1;
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if ("true".equalsIgnoreCase(strVal) //
                    || "1".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("false".equalsIgnoreCase(strVal) //
                    || "0".equals(strVal)) {
                return Boolean.FALSE;
            }
            if ("Y".equalsIgnoreCase(strVal) //
                    || "T".equals(strVal)) {
                return Boolean.TRUE;
            }
            if ("F".equalsIgnoreCase(strVal) //
                    || "N".equals(strVal)) {
                return Boolean.FALSE;
            }
        }
        throw new BaseException("can not cast to boolean, value : " + value);
    }

    public static int intValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.intValue();
        }

        return decimal.intValueExact();
    }


    public static Byte castToByte(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return byteValue((BigDecimal) value);
        }

        if (value instanceof Number) {
            return ((Number) value).byteValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            return Byte.parseByte(strVal);
        }
        throw new BaseException("can not cast to byte, value : " + value);
    }

    public static byte byteValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.byteValue();
        }

        return decimal.byteValueExact();
    }

    public static Character castToChar(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Character) {
            return (Character) value;
        }
        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0) {
                return null;
            }
            if (strVal.length() != 1) {
                throw new BaseException("can not cast to char, value : " + value);
            }
            return strVal.charAt(0);
        }
        throw new BaseException("can not cast to char, value : " + value);
    }

    public static Short castToShort(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return shortValue((BigDecimal) value);
        }

        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            return Short.parseShort(strVal);
        }

        throw new BaseException("can not cast to short, value : " + value);
    }

    public static short shortValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.shortValue();
        }

        return decimal.shortValueExact();
    }

    public static Integer castToInt(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof Integer) {
            return (Integer) value;
        }

        if (value instanceof BigDecimal) {
            return intValue((BigDecimal) value);
        }

        if (value instanceof Number) {
            return ((Number) value).intValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }

            Matcher matcher = NUMBER_WITH_TRAILING_ZEROS_PATTERN.matcher(strVal);
            if (matcher.find()) {
                strVal = matcher.replaceAll("");
            }
            return Integer.parseInt(strVal);
        }

        if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        }
        if (value instanceof Map) {
            Map map = (Map) value;
            if (map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")) {
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToInt(value2);
            }
        }
        throw new BaseException("can not cast to int, value : " + value);
    }

    public static Long castToLong(Object value) {
        if (value == null) {
            return null;
        }

        if (value instanceof BigDecimal) {
            return longValue((BigDecimal) value);
        }

        if (value instanceof Number) {
            return ((Number) value).longValue();
        }

        if (value instanceof String) {
            String strVal = (String) value;
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            try {
                return Long.parseLong(strVal);
            } catch (NumberFormatException ex) {
                //
            }

        }

        if (value instanceof Map) {
            Map map = (Map) value;
            if (map.size() == 2
                    && map.containsKey("andIncrement")
                    && map.containsKey("andDecrement")) {
                Iterator iter = map.values().iterator();
                iter.next();
                Object value2 = iter.next();
                return castToLong(value2);
            }
        }

        throw new BaseException("can not cast to long, value : " + value);
    }

    public static long longValue(BigDecimal decimal) {
        if (decimal == null) {
            return 0;
        }

        int scale = decimal.scale();
        if (scale >= -100 && scale <= 100) {
            return decimal.longValue();
        }

        return decimal.longValueExact();
    }

    public static Float castToFloat(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).floatValue();
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            return Float.parseFloat(strVal);
        }
        throw new BaseException("can not cast to float, value : " + value);
    }

    public static Double castToDouble(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value instanceof String) {
            String strVal = value.toString();
            if (strVal.length() == 0 //
                    || "null".equals(strVal) //
                    || "NULL".equals(strVal)) {
                return null;
            }
            if (strVal.indexOf(',') != -1) {
                strVal = strVal.replaceAll(",", "");
            }
            return Double.parseDouble(strVal);
        }
        throw new BaseException("can not cast to double, value : " + value);
    }

    public static String castToString(Object value) {
        if (value == null) {
            return null;
        }
        return value.toString();
    }

    public static BigDecimal castToBigDecimal(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof BigInteger) {
            return new BigDecimal((BigInteger) value);
        }
        String strVal = value.toString();
        if (strVal.length() == 0) {
            return null;
        }
        if (value instanceof Map && ((Map) value).size() == 0) {
            return null;
        }
        return new BigDecimal(strVal);
    }

    public static BigInteger castToBigInteger(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof BigInteger) {
            return (BigInteger) value;
        }
        if (value instanceof Float || value instanceof Double) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        if (value instanceof BigDecimal) {
            BigDecimal decimal = (BigDecimal) value;
            int scale = decimal.scale();
            if (scale > -1000 && scale < 1000) {
                return ((BigDecimal) value).toBigInteger();
            }
        }
        String strVal = value.toString();
        if (strVal.length() == 0 //
                || "null".equals(strVal) //
                || "NULL".equals(strVal)) {
            return null;
        }
        return new BigInteger(strVal);
    }

    public static Date castToDate(Object value) {
        return castToDate(value, null);
    }

    public static Date castToDate(Object value, String format) {
        if (value == null) {
            return null;
        }

        if (value instanceof Date) { // 使用频率最高的，应优先处理
            return (Date) value;
        }

        if (value instanceof Calendar) {
            return ((Calendar) value).getTime();
        }

        long longValue = -1;

        if (value instanceof BigDecimal) {
            longValue = longValue((BigDecimal) value);
            return new Date(longValue);
        }

        if (value instanceof Number) {
            longValue = ((Number) value).longValue();
            if ("unixtime".equals(format)) {
                longValue *= 1000;
            }
            return new Date(longValue);
        }

        if (value instanceof String) {
            String strVal = (String) value;

            if (strVal.startsWith("/Date(") && strVal.endsWith(")/")) {
                strVal = strVal.substring(6, strVal.length() - 2);
            }

            if (strVal.indexOf('-') > 0 || strVal.indexOf('+') > 0) {
                if (format == null) {
                    if (strVal.length() == DEFAULT_DATE_FORMAT.length()
                            || (strVal.length() == 22 && DEFAULT_DATE_FORMAT.equals("yyyyMMddHHmmssSSSZ"))) {
                        format = DEFAULT_DATE_FORMAT;
                    } else if (strVal.length() == 10) {
                        format = "yyyy-MM-dd";
                    } else if (strVal.length() == "yyyy-MM-dd HH:mm:ss".length()) {
                        format = "yyyy-MM-dd HH:mm:ss";
                    } else if (strVal.length() == 29
                            && strVal.charAt(26) == ':'
                            && strVal.charAt(28) == '0') {
                        format = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
                    } else if (strVal.length() == 23 && strVal.charAt(19) == ',') {
                        format = "yyyy-MM-dd HH:mm:ss,SSS";
                    } else {
                        format = "yyyy-MM-dd HH:mm:ss.SSS";
                    }
                }

                SimpleDateFormat dateFormat = new SimpleDateFormat(format, defaultLocale);
                dateFormat.setTimeZone(defaultTimeZone);
                try {
                    return dateFormat.parse(strVal);
                } catch (ParseException e) {
                    throw new BaseException("can not cast to Date, value : " + strVal);
                }
            }
            if (strVal.length() == 0) {
                return null;
            }
            longValue = Long.parseLong(strVal);
        }
        return new Date(longValue);
    }
}

package com.efeichong.generator;

import java.lang.reflect.Array;

/**
 * @author lxk
 * @date 2020/10/21
 * @description 数组处理工具类
 */
class ArrayUtils {
    public static final int INDEX_NOT_FOUND = -1;

    /**
     * 数组是否为空
     *
     * @param array
     * @return
     */
    public static boolean isEmpty(Object[] array) {
        return getLength(array) == 0;
    }

    /**
     * 返回数组长度
     *
     * @param array
     * @return
     */
    public static int getLength(Object array) {
        return array == null ? 0 : Array.getLength(array);
    }

    /**
     * 数组不为空
     *
     * @param array
     * @return
     */
    public static boolean isNotEmpty(Object[] array) {
        return !isEmpty(array);
    }

    /**
     * 数组包含某元素
     *
     * @param array
     * @param objectToFind
     * @return
     */
    public static boolean contains(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind) != INDEX_NOT_FOUND;
    }

    /**
     * 返回数组中某元素的位置
     *
     * @param array
     * @param objectToFind
     * @return
     */
    public static int indexOf(final Object[] array, final Object objectToFind) {
        return indexOf(array, objectToFind, 0);
    }

    /**
     * 从指定位置起返回数组中某元素的位置
     *
     * @param array
     * @param objectToFind
     * @return
     */
    public static int indexOf(final Object[] array, final Object objectToFind, int startIndex) {
        if (array == null) {
            return INDEX_NOT_FOUND;
        }
        if (startIndex < 0) {
            startIndex = 0;
        }
        if (objectToFind == null) {
            for (int i = startIndex; i < array.length; i++) {
                if (array[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = startIndex; i < array.length; i++) {
                if (objectToFind.equals(array[i])) {
                    return i;
                }
            }
        }
        return INDEX_NOT_FOUND;
    }

    /**
     * 添加多个元素到数组中
     *
     * @param array1
     * @param array2
     * @param <T>
     * @return
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

    /**
     * 克隆数组
     *
     * @param array
     * @param <T>
     * @return
     */
    public static <T> T[] clone(final T[] array) {
        if (array == null) {
            return null;
        }
        return array.clone();
    }

    /**
     * 返回数组为空
     *
     * @param array
     * @return
     */
    public static boolean isEmpty(final char[] array) {
        return getLength(array) == 0;
    }

    /**
     * 数组反向
     *
     * @param array
     */
    public static void reverse(final boolean[] array) {
        if (array == null) {
            return;
        }
        reverse(array, 0, array.length);
    }

    /**
     * 数组反向
     *
     * @param array
     * @param startIndexInclusive 开始位置
     * @param endIndexExclusive   结束位置
     */
    public static void reverse(final boolean[] array, final int startIndexInclusive, final int endIndexExclusive) {
        if (array == null) {
            return;
        }
        int i = startIndexInclusive < 0 ? 0 : startIndexInclusive;
        int j = Math.min(array.length, endIndexExclusive) - 1;
        boolean tmp;
        while (j > i) {
            tmp = array[j];
            array[j] = array[i];
            array[i] = tmp;
            j--;
            i++;
        }
    }

    public static <T> T[] add(Object[] array, T element) {
        Class type;
        if (array != null) {
            type = array.getClass();
        } else if (element != null) {
            type = element.getClass();
        } else {
            type = Object.class;
        }
        T[] newArray = (T[]) copyArrayGrow1(array, type);
        newArray[newArray.length - 1] = element;
        return newArray;
    }

    /**
     * Returns a copy of the given array of size 1 greater than the argument.
     * The last value of the array is left to the default value.
     *
     * @param array                 The array to copy, must not be <code>null</code>.
     * @param newArrayComponentType If <code>array</code> is <code>null</code>, create a
     *                              size 1 array of this type.
     * @return A new copy of the array of size 1 greater than the input.
     */
    private static Object copyArrayGrow1(Object array, Class newArrayComponentType) {
        if (array != null) {
            int arrayLength = Array.getLength(array);
            Object newArray = Array.newInstance(array.getClass().getComponentType(), arrayLength + 1);
            System.arraycopy(array, 0, newArray, 0, arrayLength);
            return newArray;
        }
        return Array.newInstance(newArrayComponentType, 1);
    }
}

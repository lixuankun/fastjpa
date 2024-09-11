package com.efeichong.generator;

import java.util.*;

/**
 * @author lxk
 * @date 2020/10/21
 * @description 集合处理工具类
 */
class CollectionUtils {

    /**
     * An empty unmodifiable collection.
     * The JDK provides empty Set and List implementations which could be used for
     * this purpose. However they could be cast to Set or List which might be
     * undesirable. This implementation only implements Collection.
     */
    @SuppressWarnings("rawtypes") // we deliberately use the raw type here
    public static final Collection EMPTY_COLLECTION = Collections.emptyList();

    /**
     * <code>com.efeichong.util.CollectionUtils</code> should not normally be instantiated.
     */
    private CollectionUtils() {
    }

    /**
     * Returns the immutable EMPTY_COLLECTION with generic type safety.
     *
     * @param <T> the element type
     * @return immutable empty collection
     * @see #EMPTY_COLLECTION
     * @since 4.0
     */
    @SuppressWarnings("unchecked") // OK, empty collection is compatible with any type
    public static <T> Collection<T> emptyCollection() {
        return EMPTY_COLLECTION;
    }

    /**
     * Returns an immutable empty collection if the argument is <code>null</code>,
     * or the argument itself otherwise.
     *
     * @param <T>        the element type
     * @param collection the collection, possibly <code>null</code>
     * @return an empty collection if the argument is <code>null</code>
     */
    public static <T> Collection<T> emptyIfNull(final Collection<T> collection) {
        return collection == null ? CollectionUtils.<T>emptyCollection() : collection;
    }

    /**
     * Returns a {@link Collection} containing the union of the given
     * {@link Iterable}s.
     * <p>
     * The cardinality of each element in the returned {@link Collection} will
     * be equal to the maximum of the cardinality of that element in the two
     * given {@link Iterable}s.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the union of the two collections
     * @see Collection#addAll
     */
    public static <O> Collection<O> union(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.max(obj));
        }
        return helper.list();
    }

    /**
     * Returns a {@link Collection} containing the intersection of the given
     * {@link Iterable}s.
     * <p>
     * The cardinality of each element in the returned {@link Collection} will
     * be equal to the minimum of the cardinality of that element in the two
     * given {@link Iterable}s.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the intersection of the two collections
     * @see Collection#retainAll
     * @see #containsAny
     */
    public static <O> Collection<O> intersection(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.min(obj));
        }
        return helper.list();
    }

    /**
     * Returns a {@link Collection} containing the exclusive disjunction
     * (symmetric difference) of the given {@link Iterable}s.
     * <p>
     * The cardinality of each element <i>e</i> in the returned
     * {@link Collection} will be equal to
     * <code>max(cardinality(<i>e</i>,<i>a</i>),cardinality(<i>e</i>,<i>b</i>)) - min(cardinality(<i>e</i>,<i>a</i>),
     * cardinality(<i>e</i>,<i>b</i>))</code>.
     * <p>
     * This is equivalent to
     * {@code ({@link #union union(a,b)},{@link #intersection intersection(a,b)})}
     * or
     * {@code {@link #union union}({@link #subtract subtract(a,b)},{@link #subtract subtract(b,a)})}.
     *
     * @param a   the first collection, must not be null
     * @param b   the second collection, must not be null
     * @param <O> the generic type that is able to represent the types contained
     *            in both input collections.
     * @return the symmetric difference of the two collections
     */
    public static <O> Collection<O> disjunction(final Iterable<? extends O> a, final Iterable<? extends O> b) {
        final SetOperationCardinalityHelper<O> helper = new SetOperationCardinalityHelper<>(a, b);
        for (final O obj : helper) {
            helper.setCardinality(obj, helper.max(obj) - helper.min(obj));
        }
        return helper.list();
    }

    /**
     * Returns <code>true</code> iff all elements of {@code coll2} are also contained
     * in {@code coll1}. The cardinality of values in {@code coll2} is not taken into account,
     * which is the same behavior as {@link Collection#containsAll(Collection)}.
     * <p>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> has the same cardinality as
     * the set of unique values from {@code coll2}. In case {@code coll2} is empty, {@code true}
     * will be returned.
     * <p>
     * This method is intended as a replacement for {@link Collection#containsAll(Collection)}
     * with a guaranteed runtime complexity of {@code O(n + m)}. Depending on the type of
     * {@link Collection} provided, this method will be much faster than calling
     * {@link Collection#containsAll(Collection)} instead, though this will come at the
     * cost of an additional space complexity O(n).
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the intersection of the collections has the same cardinality
     * as the set of unique elements from the second collection
     * @since 4.0
     */
    public static boolean containsAll(final Collection<?> coll1, final Collection<?> coll2) {
        if (coll2.isEmpty()) {
            return true;
        }
        final Iterator<?> it = coll1.iterator();
        final Set<Object> elementsAlreadySeen = new HashSet<>();
        for (final Object nextElement : coll2) {
            if (elementsAlreadySeen.contains(nextElement)) {
                continue;
            }

            boolean foundCurrentElement = false;
            while (it.hasNext()) {
                final Object p = it.next();
                elementsAlreadySeen.add(p);
                if (nextElement == null ? p == null : nextElement.equals(p)) {
                    foundCurrentElement = true;
                    break;
                }
            }

            if (!foundCurrentElement) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns <code>true</code> iff at least one element is in both collections.
     * <p>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> is not empty.
     *
     * @param <T>   the type of object to lookup in <code>coll1</code>.
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the intersection of the collections is non-empty
     * @see #intersection
     * @since 4.2
     */
    public static <T> boolean containsAny(final Collection<?> coll1, @SuppressWarnings("unchecked") final T... coll2) {
        if (coll1.size() < coll2.length) {
            for (final Object aColl1 : coll1) {
                if (ArrayUtils.contains(coll2, aColl1)) {
                    return true;
                }
            }
        } else {
            for (final Object aColl2 : coll2) {
                if (coll1.contains(aColl2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns <code>true</code> iff at least one element is in both collections.
     * <p>
     * In other words, this method returns <code>true</code> iff the
     * {@link #intersection} of <i>coll1</i> and <i>coll2</i> is not empty.
     *
     * @param coll1 the first collection, must not be null
     * @param coll2 the second collection, must not be null
     * @return <code>true</code> iff the intersection of the collections is non-empty
     * @see #intersection
     * @since 2.1
     */
    public static boolean containsAny(final Collection<?> coll1, final Collection<?> coll2) {
        if (coll1.size() < coll2.size()) {
            for (final Object aColl1 : coll1) {
                if (coll2.contains(aColl1)) {
                    return true;
                }
            }
        } else {
            for (final Object aColl2 : coll2) {
                if (coll1.contains(aColl2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a {@link Map} mapping each unique element in the given
     * {@link Collection} to an {@link Integer} representing the number
     * of occurrences of that element in the {@link Collection}.
     * <p>
     * Only those elements present in the collection will appear as
     * keys in the map.
     *
     * @param <O>  the type of object in the returned {@link Map}. This is a super type of &lt;I&gt;.
     * @param coll the collection to get the cardinality map for, must not be null
     * @return the populated cardinality map
     */
    public static <O> Map<O, Integer> getCardinalityMap(final Iterable<? extends O> coll) {
        final Map<O, Integer> count = new HashMap<>();
        for (final O obj : coll) {
            final Integer c = count.get(obj);
            if (c == null) {
                count.put(obj, Integer.valueOf(1));
            } else {
                count.put(obj, Integer.valueOf(c.intValue() + 1));
            }
        }
        return count;
    }

    /**
     * Returns {@code true} iff <i>a</i> is a sub-collection of <i>b</i>,
     * that is, iff the cardinality of <i>e</i> in <i>a</i> is less than or
     * equal to the cardinality of <i>e</i> in <i>b</i>, for each element <i>e</i>
     * in <i>a</i>.
     *
     * @param a the first (sub?) collection, must not be null
     * @param b the second (super?) collection, must not be null
     * @return <code>true</code> iff <i>a</i> is a sub-collection of <i>b</i>
     * @see #isProperSubCollection
     * @see Collection#containsAll
     */
    public static boolean isSubCollection(final Collection<?> a, final Collection<?> b) {
        final CardinalityHelper<Object> helper = new CardinalityHelper<>(a, b);
        for (final Object obj : a) {
            if (helper.freqA(obj) > helper.freqB(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns {@code true} iff <i>a</i> is a <i>proper</i> sub-collection of <i>b</i>,
     * that is, iff the cardinality of <i>e</i> in <i>a</i> is less
     * than or equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i>, and there is at least one
     * element <i>f</i> such that the cardinality of <i>f</i> in <i>b</i>
     * is strictly greater than the cardinality of <i>f</i> in <i>a</i>.
     * <p>
     * The implementation assumes
     * <ul>
     *    <li><code>a.size()</code> and <code>b.size()</code> represent the
     *    total cardinality of <i>a</i> and <i>b</i>, resp. </li>
     *    <li><code>a.size() &lt; Integer.MAXVALUE</code></li>
     * </ul>
     *
     * @param a the first (sub?) collection, must not be null
     * @param b the second (super?) collection, must not be null
     * @return <code>true</code> iff <i>a</i> is a <i>proper</i> sub-collection of <i>b</i>
     * @see #isSubCollection
     * @see Collection#containsAll
     */
    public static boolean isProperSubCollection(final Collection<?> a, final Collection<?> b) {
        return a.size() < b.size() && CollectionUtils.isSubCollection(a, b);
    }

    /**
     * Returns {@code true} iff the given {@link Collection}s contain
     * exactly the same elements with exactly the same cardinalities.
     * <p>
     * That is, iff the cardinality of <i>e</i> in <i>a</i> is
     * equal to the cardinality of <i>e</i> in <i>b</i>,
     * for each element <i>e</i> in <i>a</i> or <i>b</i>.
     *
     * @param a the first collection, must not be null
     * @param b the second collection, must not be null
     * @return <code>true</code> iff the collections contain the same elements with the same cardinalities.
     */
    public static boolean isEqualCollection(final Collection<?> a, final Collection<?> b) {
        if (a.size() != b.size()) {
            return false;
        }
        final CardinalityHelper<Object> helper = new CardinalityHelper<>(a, b);
        if (helper.cardinalityA.size() != helper.cardinalityB.size()) {
            return false;
        }
        for (final Object obj : helper.cardinalityA.keySet()) {
            if (helper.freqA(obj) != helper.freqB(obj)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds an element to the collection unless the element is null.
     *
     * @param <T>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param object     the object to add, if null it will not be added
     * @return true if the collection changed
     * @throws NullPointerException if the collection is null
     * @since 3.2
     */
    public static <T> boolean addIgnoreNull(final Collection<T> collection, final T object) {
        if (collection == null) {
            throw new NullPointerException("The collection must not be null");
        }
        return object != null && collection.add(object);
    }

    /**
     * Adds all elements in the {@link Iterable} to the given collection. If the
     * {@link Iterable} is a {@link Collection} then it is cast and will be
     * added using {@link Collection#addAll(Collection)} instead of iterating.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param iterable   the iterable of elements to add, must not be null
     * @return a boolean indicating whether the collection has changed or not.
     * @throws NullPointerException if the collection or iterator is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Iterable<? extends C> iterable) {
        if (iterable instanceof Collection<?>) {
            return collection.addAll((Collection<? extends C>) iterable);
        }
        return addAll(collection, iterable.iterator());
    }

    //-----------------------------------------------------------------------

    /**
     * Adds all elements in the iteration to the given collection.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param iterator   the iterator of elements to add, must not be null
     * @return a boolean indicating whether the collection has changed or not.
     * @throws NullPointerException if the collection or iterator is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Iterator<? extends C> iterator) {
        boolean changed = false;
        while (iterator.hasNext()) {
            changed |= collection.add(iterator.next());
        }
        return changed;
    }

    /**
     * Adds all elements in the enumeration to the given collection.
     *
     * @param <C>         the type of object the {@link Collection} contains
     * @param collection  the collection to add to, must not be null
     * @param enumeration the enumeration of elements to add, must not be null
     * @return {@code true} if the collections was changed, {@code false} otherwise
     * @throws NullPointerException if the collection or enumeration is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final Enumeration<? extends C> enumeration) {
        boolean changed = false;
        while (enumeration.hasMoreElements()) {
            changed |= collection.add(enumeration.nextElement());
        }
        return changed;
    }

    /**
     * Adds all elements in the array to the given collection.
     *
     * @param <C>        the type of object the {@link Collection} contains
     * @param collection the collection to add to, must not be null
     * @param elements   the array of elements to add, must not be null
     * @return {@code true} if the collection was changed, {@code false} otherwise
     * @throws NullPointerException if the collection or array is null
     */
    public static <C> boolean addAll(final Collection<C> collection, final C... elements) {
        boolean changed = false;
        for (final C element : elements) {
            changed |= collection.add(element);
        }
        return changed;
    }

    /**
     * Ensures an index is not negative.
     *
     * @param index the index to check.
     * @throws IndexOutOfBoundsException if the index is negative.
     */
    static void checkIndexBounds(final int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("Index cannot be negative: " + index);
        }
    }

    /**
     * Null-safe check if the specified collection is empty.
     * <p>
     * Null returns true.
     *
     * @param coll the collection to check, may be null
     * @return true if empty or null
     * @since 3.2
     */
    public static boolean isEmpty(final Collection<?> coll) {
        return coll == null || coll.isEmpty();
    }

    /**
     * Null-safe check if the specified collection is not empty.
     * <p>
     * Null returns false.
     *
     * @param coll the collection to check, may be null
     * @return true if non-null and non-empty
     * @since 3.2
     */
    public static boolean isNotEmpty(final Collection<?> coll) {
        return !isEmpty(coll);
    }

    //-----------------------------------------------------------------------

    /**
     * 两个数据取交集
     *
     * @param collection
     * @param retain
     * @param <E>
     * @return
     */
    public static <E> List<E> retainAll(final Collection<E> collection, final Collection<?> retain) {
        final List<E> list = new ArrayList<>(Math.min(collection.size(), retain.size()));

        for (final E obj : collection) {
            if (retain.contains(obj)) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * 移除全部元素
     *
     * @param collection
     * @param remove
     * @param <E>
     * @return
     */
    public static <E> List<E> removeAll(final Collection<E> collection, final Collection<?> remove) {
        final List<E> list = new ArrayList<>();
        for (final E obj : collection) {
            if (!remove.contains(obj)) {
                list.add(obj);
            }
        }
        return list;
    }

    /**
     * Helper class to easily access cardinality properties of two collections.
     *
     * @param <O> the element type
     */
    private static class CardinalityHelper<O> {

        /**
         * Contains the cardinality for each object in collection A.
         */
        final Map<O, Integer> cardinalityA;

        /**
         * Contains the cardinality for each object in collection B.
         */
        final Map<O, Integer> cardinalityB;

        /**
         * Create a new CardinalityHelper for two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public CardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            cardinalityA = CollectionUtils.<O>getCardinalityMap(a);
            cardinalityB = CollectionUtils.<O>getCardinalityMap(b);
        }

        /**
         * Returns the maximum frequency of an object.
         *
         * @param obj the object
         * @return the maximum frequency of the object
         */
        public final int max(final Object obj) {
            return Math.max(freqA(obj), freqB(obj));
        }

        /**
         * Returns the minimum frequency of an object.
         *
         * @param obj the object
         * @return the minimum frequency of the object
         */
        public final int min(final Object obj) {
            return Math.min(freqA(obj), freqB(obj));
        }

        /**
         * Returns the frequency of this object in collection A.
         *
         * @param obj the object
         * @return the frequency of the object in collection A
         */
        public int freqA(final Object obj) {
            return getFreq(obj, cardinalityA);
        }

        /**
         * Returns the frequency of this object in collection B.
         *
         * @param obj the object
         * @return the frequency of the object in collection B
         */
        public int freqB(final Object obj) {
            return getFreq(obj, cardinalityB);
        }

        private int getFreq(final Object obj, final Map<?, Integer> freqMap) {
            final Integer count = freqMap.get(obj);
            if (count != null) {
                return count.intValue();
            }
            return 0;
        }
    }

    /**
     * Helper class for set-related operations, e.g. union, subtract, intersection.
     *
     * @param <O> the element type
     */
    private static class SetOperationCardinalityHelper<O> extends CardinalityHelper<O> implements Iterable<O> {

        /**
         * Contains the unique elements of the two collections.
         */
        private final Set<O> elements;

        /**
         * Output collection.
         */
        private final List<O> newList;

        /**
         * Create a new set operation helper from the two collections.
         *
         * @param a the first collection
         * @param b the second collection
         */
        public SetOperationCardinalityHelper(final Iterable<? extends O> a, final Iterable<? extends O> b) {
            super(a, b);
            elements = new HashSet<>();
            addAll(elements, a);
            addAll(elements, b);
            // the resulting list must contain at least each unique element, but may grow
            newList = new ArrayList<>(elements.size());
        }

        @Override
        public Iterator<O> iterator() {
            return elements.iterator();
        }

        /**
         * Add the object {@code count} times to the result collection.
         *
         * @param obj   the object to add
         * @param count the count
         */
        public void setCardinality(final O obj, final int count) {
            for (int i = 0; i < count; i++) {
                newList.add(obj);
            }
        }

        /**
         * Returns the resulting collection.
         *
         * @return the result
         */
        public Collection<O> list() {
            return newList;
        }

    }
}

package xss.it.jux.reactive.collections;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.collections.internal.MapListenerHelper;

import java.util.AbstractList;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Set;

/**
 * Utility class that consists of static methods for creating and manipulating observable collections.
 *
 * <p>The wrapper methods (like {@link #unmodifiableObservableList(ObservableList)} or
 * {@link #emptyObservableList()}) have exactly the same functionality as the corresponding
 * methods in {@link Collections}, with the exception that they return observable collection
 * types and are therefore suitable for methods that require observable collections on input.</p>
 *
 * <p>The utility methods are provided mainly for performance reasons. All methods are optimized
 * so that they yield only a limited number of notifications. In contrast, {@link Collections}
 * methods might call modification methods on an {@code ObservableList} multiple times, resulting
 * in a number of notifications.</p>
 *
 * @since JUX 1.0
 */
public class JuxCollections {

    /** Not to be instantiated. */
    private JuxCollections() {}

    // =================================================================================================================
    // Observable list factories
    // =================================================================================================================

    /**
     * Constructs an {@code ObservableList} that is backed by the specified list.
     * Mutation operations on the {@code ObservableList} instance will be reported
     * to observers that have registered on that instance.
     *
     * <p>Note that mutation operations made directly to the underlying list are
     * <em>not</em> reported to observers of any {@code ObservableList} that wraps it.</p>
     *
     * @param <E>  the type of elements in the list
     * @param list a concrete {@code List} that backs this {@code ObservableList}
     * @return a newly created {@code ObservableList}
     * @throws NullPointerException if {@code list} is {@code null}
     */
    public static <E> ObservableList<E> observableList(List<E> list) {
        if (list == null) {
            throw new NullPointerException("Backing list must not be null");
        }
        return new ObservableListWrapper<>(list);
    }

    /**
     * Creates a new empty observable list that is backed by an {@link ArrayList}.
     *
     * @param <E> the type of elements in the list
     * @return a newly created {@code ObservableList}
     * @see #observableList(List)
     */
    public static <E> ObservableList<E> observableArrayList() {
        return observableList(new ArrayList<>());
    }

    /**
     * Creates a new observable array list with the specified items added to it.
     *
     * @param <E>   the type of elements in the list
     * @param items the items that will be in the new observable list
     * @return a newly created {@code ObservableList} containing the specified items
     */
    @SafeVarargs
    public static <E> ObservableList<E> observableArrayList(E... items) {
        return observableList(new ArrayList<>(Arrays.asList(items)));
    }

    /**
     * Creates a new observable array list and adds the content of the specified
     * collection to it.
     *
     * @param <E> the type of elements in the list
     * @param col a collection whose content should be added to the {@code ObservableList}
     * @return a newly created {@code ObservableList} containing the collection elements
     * @throws NullPointerException if {@code col} is {@code null}
     */
    public static <E> ObservableList<E> observableArrayList(Collection<? extends E> col) {
        return observableList(new ArrayList<>(col));
    }

    // =================================================================================================================
    // Observable map factories
    // =================================================================================================================

    /**
     * Constructs an {@code ObservableMap} that is backed by the specified map.
     * Mutation operations on the {@code ObservableMap} instance will be reported
     * to observers that have registered on that instance.
     *
     * <p>Note that mutation operations made directly to the underlying map are
     * <em>not</em> reported to observers of any {@code ObservableMap} that wraps it.</p>
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @param map a {@code Map} that backs this {@code ObservableMap}
     * @return a newly created {@code ObservableMap}
     * @throws NullPointerException if {@code map} is {@code null}
     */
    public static <K, V> ObservableMap<K, V> observableMap(Map<K, V> map) {
        if (map == null) {
            throw new NullPointerException("Backing map must not be null");
        }
        return new ObservableMapWrapper<>(map);
    }

    /**
     * Creates a new empty observable map that is backed by a {@link HashMap}.
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @return a newly created observable {@code HashMap}
     */
    public static <K, V> ObservableMap<K, V> observableHashMap() {
        return observableMap(new HashMap<>());
    }

    // =================================================================================================================
    // Observable set factories
    // =================================================================================================================

    /**
     * Constructs an {@code ObservableSet} that is backed by the specified set.
     * Mutation operations on the {@code ObservableSet} instance will be reported
     * to observers that have registered on that instance.
     *
     * <p>Note that mutation operations made directly to the underlying set are
     * <em>not</em> reported to observers of any {@code ObservableSet} that wraps it.</p>
     *
     * @param <E> the type of elements in the set
     * @param set a {@code Set} that backs this {@code ObservableSet}
     * @return a newly created {@code ObservableSet}
     * @throws NullPointerException if {@code set} is {@code null}
     */
    public static <E> ObservableSet<E> observableSet(Set<E> set) {
        if (set == null) {
            throw new NullPointerException("Backing set must not be null");
        }
        return new ObservableSetWrapper<>(set);
    }

    /**
     * Constructs an {@code ObservableSet} backed by a {@link LinkedHashSet}
     * that contains all the specified elements.
     *
     * @param <E>      the type of elements in the set
     * @param elements elements that will be added into the returned {@code ObservableSet}
     * @return a newly created {@code ObservableSet}
     * @throws NullPointerException if {@code elements} is {@code null}
     */
    @SafeVarargs
    public static <E> ObservableSet<E> observableSet(E... elements) {
        if (elements == null) {
            throw new NullPointerException("Elements array must not be null");
        }
        Set<E> set = new LinkedHashSet<>(elements.length);
        Collections.addAll(set, elements);
        return new ObservableSetWrapper<>(set);
    }

    // =================================================================================================================
    // Unmodifiable wrappers
    // =================================================================================================================

    /**
     * Creates and returns an unmodifiable wrapper on top of the provided observable list.
     *
     * <p>Only mutation operations made to the underlying {@code ObservableList} will be reported
     * to observers that have registered on the unmodifiable instance. This allows clients to
     * track changes in a list but disallows the ability to modify it.</p>
     *
     * @param <E>  the type of elements in the list
     * @param list an {@code ObservableList} that is to be wrapped
     * @return an unmodifiable {@code ObservableList} wrapper
     * @throws NullPointerException if {@code list} is {@code null}
     * @see Collections#unmodifiableList(List)
     */
    public static <E> ObservableList<E> unmodifiableObservableList(ObservableList<E> list) {
        if (list == null) {
            throw new NullPointerException("List must not be null");
        }
        return new UnmodifiableObservableListImpl<>(list);
    }

    /**
     * Creates and returns an unmodifiable wrapper on top of the provided observable map.
     *
     * <p>Only mutation operations made to the underlying {@code ObservableMap} will be reported
     * to observers that have registered on the unmodifiable instance. This allows clients to
     * track changes in a map but disallows the ability to modify it.</p>
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @param map an {@code ObservableMap} that is to be wrapped
     * @return an unmodifiable {@code ObservableMap} wrapper
     * @throws NullPointerException if {@code map} is {@code null}
     * @see Collections#unmodifiableMap(Map)
     */
    public static <K, V> ObservableMap<K, V> unmodifiableObservableMap(ObservableMap<K, V> map) {
        if (map == null) {
            throw new NullPointerException("Map must not be null");
        }
        return new UnmodifiableObservableMapImpl<>(map);
    }

    /**
     * Creates and returns an unmodifiable wrapper on top of the provided observable set.
     *
     * <p>Only mutation operations made to the underlying {@code ObservableSet} will be reported
     * to observers that have registered on the unmodifiable instance. This allows clients to
     * track changes in a set but disallows the ability to modify it.</p>
     *
     * @param <E> the type of elements in the set
     * @param set an {@code ObservableSet} that is to be wrapped
     * @return an unmodifiable {@code ObservableSet} wrapper
     * @throws NullPointerException if {@code set} is {@code null}
     * @see Collections#unmodifiableSet(Set)
     */
    public static <E> ObservableSet<E> unmodifiableObservableSet(ObservableSet<E> set) {
        if (set == null) {
            throw new NullPointerException("Set must not be null");
        }
        return new UnmodifiableObservableSetImpl<>(set);
    }

    // =================================================================================================================
    // Empty collections (singletons)
    // =================================================================================================================

    private static final ObservableList<?> EMPTY_OBSERVABLE_LIST = new EmptyObservableList<>();

    /**
     * Creates an empty unmodifiable observable list.
     *
     * @param <E> the type of elements in the list
     * @return an empty unmodifiable observable list
     * @see Collections#emptyList()
     */
    @SuppressWarnings("unchecked")
    public static <E> ObservableList<E> emptyObservableList() {
        return (ObservableList<E>) EMPTY_OBSERVABLE_LIST;
    }

    private static final ObservableMap<?, ?> EMPTY_OBSERVABLE_MAP = new EmptyObservableMap<>();

    /**
     * Creates an empty unmodifiable observable map.
     *
     * @param <K> the type of keys in the map
     * @param <V> the type of values in the map
     * @return an empty unmodifiable observable map
     * @see Collections#emptyMap()
     */
    @SuppressWarnings("unchecked")
    public static <K, V> ObservableMap<K, V> emptyObservableMap() {
        return (ObservableMap<K, V>) EMPTY_OBSERVABLE_MAP;
    }

    private static final ObservableSet<?> EMPTY_OBSERVABLE_SET = new EmptyObservableSet<>();

    /**
     * Creates an empty unmodifiable observable set.
     *
     * @param <E> the type of elements in the set
     * @return an empty unmodifiable observable set
     * @see Collections#emptySet()
     */
    @SuppressWarnings("unchecked")
    public static <E> ObservableSet<E> emptyObservableSet() {
        return (ObservableSet<E>) EMPTY_OBSERVABLE_SET;
    }

    // =================================================================================================================
    // Singleton observable list
    // =================================================================================================================

    /**
     * Creates an unmodifiable observable list with a single element.
     *
     * @param <E> the type of elements in the list
     * @param e   the sole element that will be contained in this singleton observable list
     * @return a singleton observable list
     * @throws NullPointerException if the element is {@code null}
     * @see Collections#singletonList(Object)
     */
    public static <E> ObservableList<E> singletonObservableList(E e) {
        return new SingletonObservableList<>(e);
    }

    // =================================================================================================================
    // Concatenation
    // =================================================================================================================

    /**
     * Concatenates multiple observable lists into one. The resulting list
     * is backed by an {@link ArrayList}.
     *
     * @param <E>   the type of elements in the lists
     * @param lists the observable lists to concatenate
     * @return a new observable array list concatenated from the arguments
     */
    @SafeVarargs
    public static <E> ObservableList<E> concat(ObservableList<E>... lists) {
        if (lists.length == 0) {
            return observableArrayList();
        }
        if (lists.length == 1) {
            return observableArrayList(lists[0]);
        }
        ArrayList<E> backingList = new ArrayList<>();
        for (ObservableList<E> s : lists) {
            backingList.addAll(s);
        }
        return observableList(backingList);
    }

    // =================================================================================================================
    // Sort, shuffle, reverse
    // =================================================================================================================

    /**
     * Sorts the provided observable list using natural ordering.
     * Fires only <b>one</b> change notification on the list.
     *
     * @param <T>  the type of elements in the list
     * @param list the list to be sorted
     * @see Collections#sort(List)
     */
    public static <T extends Comparable<? super T>> void sort(ObservableList<T> list) {
        sort(list, Comparator.naturalOrder());
    }

    /**
     * Sorts the provided observable list using the given comparator.
     * Fires only <b>one</b> change notification on the list.
     *
     * @param <T>        the type of elements in the list
     * @param list       the list to sort
     * @param comparator the comparator to determine the order of the list; a {@code null} value
     *                   indicates that the elements' <i>natural ordering</i> should be used
     * @see Collections#sort(List, Comparator)
     */
    public static <T> void sort(ObservableList<T> list, Comparator<? super T> comparator) {
        if (list instanceof SortableList) {
            list.sort(comparator);
        } else {
            List<T> newContent = new ArrayList<>(list);
            newContent.sort(comparator);
            list.setAll(newContent);
        }
    }

    /**
     * Shuffles all elements in the observable list.
     * Fires only <b>one</b> change notification on the list.
     *
     * @param list the list to shuffle
     * @see Collections#shuffle(List)
     */
    public static void shuffle(ObservableList<?> list) {
        if (r == null) {
            r = new Random();
        }
        shuffle(list, r);
    }

    private static Random r;

    /**
     * Shuffles all elements in the observable list using the provided random generator.
     * Fires only <b>one</b> change notification on the list.
     *
     * @param list the list to be shuffled
     * @param rnd  the random generator used for shuffling
     * @see Collections#shuffle(List, Random)
     */
    @SuppressWarnings("unchecked")
    public static void shuffle(ObservableList<?> list, Random rnd) {
        Object[] newContent = list.toArray();

        for (int i = list.size(); i > 1; i--) {
            swap(newContent, i - 1, rnd.nextInt(i));
        }

        ((ObservableList<Object>) list).setAll(newContent);
    }

    private static void swap(Object[] arr, int i, int j) {
        Object tmp = arr[i];
        arr[i] = arr[j];
        arr[j] = tmp;
    }

    /**
     * Reverses the order in the observable list.
     * Fires only <b>one</b> change notification on the list.
     *
     * @param list the list to be reversed
     * @see Collections#reverse(List)
     */
    @SuppressWarnings("unchecked")
    public static void reverse(ObservableList<?> list) {
        Object[] newContent = list.toArray();
        for (int i = 0; i < newContent.length / 2; ++i) {
            Object tmp = newContent[i];
            newContent[i] = newContent[newContent.length - i - 1];
            newContent[newContent.length - i - 1] = tmp;
        }
        ((ObservableList<Object>) list).setAll(newContent);
    }

    // =================================================================================================================
    // Inner class: EmptyObservableList
    // =================================================================================================================

    private static class EmptyObservableList<E> extends AbstractList<E> implements ObservableList<E> {

        private final ListIterator<E> iterator = new ListIterator<>() {

            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }

            @Override
            public boolean hasPrevious() {
                return false;
            }

            @Override
            public E previous() {
                throw new NoSuchElementException();
            }

            @Override
            public int nextIndex() {
                return 0;
            }

            @Override
            public int previousIndex() {
                return -1;
            }

            @Override
            public void set(E e) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(E e) {
                throw new UnsupportedOperationException();
            }
        };

        public EmptyObservableList() {
        }

        @Override
        public final void addListener(InvalidationListener listener) {
        }

        @Override
        public final void removeListener(InvalidationListener listener) {
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean contains(Object o) {
            return false;
        }

        @Override
        public Iterator<E> iterator() {
            return iterator;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return c.isEmpty();
        }

        @Override
        public E get(int index) {
            throw new IndexOutOfBoundsException();
        }

        @Override
        public int indexOf(Object o) {
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return -1;
        }

        @Override
        public ListIterator<E> listIterator() {
            return iterator;
        }

        @Override
        public ListIterator<E> listIterator(int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return iterator;
        }

        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            if (fromIndex != 0 || toIndex != 0) {
                throw new IndexOutOfBoundsException();
            }
            return this;
        }

        @Override
        public boolean addAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(Collection<? extends E> col) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(int from, int to) {
            throw new UnsupportedOperationException();
        }
    }

    // =================================================================================================================
    // Inner class: SingletonObservableList
    // =================================================================================================================

    private static class SingletonObservableList<E> extends AbstractList<E> implements ObservableList<E> {

        private final E element;

        public SingletonObservableList(E element) {
            if (element == null) {
                throw new NullPointerException("Singleton element must not be null");
            }
            this.element = element;
        }

        @Override
        public boolean addAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(Collection<? extends E> col) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(E... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(int from, int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addListener(InvalidationListener listener) {
        }

        @Override
        public void removeListener(InvalidationListener listener) {
        }

        @Override
        public void addListener(ListChangeListener<? super E> listener) {
        }

        @Override
        public void removeListener(ListChangeListener<? super E> listener) {
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(Object o) {
            return element.equals(o);
        }

        @Override
        public E get(int index) {
            if (index != 0) {
                throw new IndexOutOfBoundsException();
            }
            return element;
        }
    }

    // =================================================================================================================
    // Inner class: UnmodifiableObservableListImpl
    // =================================================================================================================

    private static class UnmodifiableObservableListImpl<T> extends ObservableListBase<T> {

        private final ObservableList<T> backingList;
        private final ListChangeListener<T> listener;

        public UnmodifiableObservableListImpl(ObservableList<T> backingList) {
            this.backingList = backingList;
            listener = c -> {
                fireChange(new SourceAdapterListChange<>(UnmodifiableObservableListImpl.this, c));
            };
            this.backingList.addListener(listener);
        }

        @Override
        public T get(int index) {
            return backingList.get(index);
        }

        @Override
        public int size() {
            return backingList.size();
        }

        @Override
        public boolean addAll(T... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(T... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean setAll(Collection<? extends T> col) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(T... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(T... elements) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(int from, int to) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean add(T t) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T set(int index, T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(int index, T element) {
            throw new UnsupportedOperationException();
        }

        @Override
        public T remove(int index) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    // =================================================================================================================
    // Inner class: SourceAdapterListChange (adapts change events from backing list to wrapper)
    // =================================================================================================================

    private static class SourceAdapterListChange<E> extends ListChangeListener.Change<E> {

        private final ListChangeListener.Change<? extends E> change;

        public SourceAdapterListChange(ObservableList<E> list, ListChangeListener.Change<? extends E> change) {
            super(list);
            this.change = change;
        }

        @Override
        public boolean next() {
            return change.next();
        }

        @Override
        public void reset() {
            change.reset();
        }

        @Override
        public int getFrom() {
            return change.getFrom();
        }

        @Override
        public int getTo() {
            return change.getTo();
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<E> getRemoved() {
            return (List<E>) change.getRemoved();
        }

        @Override
        protected int[] getPermutation() {
            if (change.wasPermutated()) {
                int[] perm = new int[change.getTo() - change.getFrom()];
                for (int i = change.getFrom(); i < change.getTo(); i++) {
                    perm[i - change.getFrom()] = change.getPermutation(i);
                }
                return perm;
            }
            return new int[0];
        }

        @Override
        public boolean wasUpdated() {
            return change.wasUpdated();
        }
    }

    // =================================================================================================================
    // Inner class: UnmodifiableObservableMapImpl
    // =================================================================================================================

    private static class UnmodifiableObservableMapImpl<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {

        private final ObservableMap<K, V> backingMap;
        private MapListenerHelper<K, V> listenerHelper;
        private MapChangeListener<K, V> mapListener;

        public UnmodifiableObservableMapImpl(ObservableMap<K, V> backingMap) {
            this.backingMap = backingMap;
        }

        private void initListener() {
            if (mapListener == null) {
                mapListener = c -> {
                    MapChangeListener.Change<K, V> adapted = new MapChangeListener.Change<>(UnmodifiableObservableMapImpl.this) {
                        @Override
                        public boolean wasAdded() {
                            return c.wasAdded();
                        }

                        @Override
                        public boolean wasRemoved() {
                            return c.wasRemoved();
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public K getKey() {
                            return (K) c.getKey();
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public V getValueAdded() {
                            return (V) c.getValueAdded();
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public V getValueRemoved() {
                            return (V) c.getValueRemoved();
                        }
                    };
                    MapListenerHelper.fireValueChangedEvent(listenerHelper, adapted);
                };
                this.backingMap.addListener(mapListener);
            }
        }

        @Override
        public void addListener(InvalidationListener listener) {
            initListener();
            listenerHelper = MapListenerHelper.addListener(listenerHelper, listener);
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            listenerHelper = MapListenerHelper.removeListener(listenerHelper, listener);
        }

        @Override
        public void addListener(MapChangeListener<? super K, ? super V> listener) {
            initListener();
            listenerHelper = MapListenerHelper.addListener(listenerHelper, listener);
        }

        @Override
        public void removeListener(MapChangeListener<? super K, ? super V> listener) {
            listenerHelper = MapListenerHelper.removeListener(listenerHelper, listener);
        }

        @Override
        public int size() {
            return backingMap.size();
        }

        @Override
        public boolean isEmpty() {
            return backingMap.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            return backingMap.containsKey(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return backingMap.containsValue(value);
        }

        @Override
        public V get(Object key) {
            return backingMap.get(key);
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            return Collections.unmodifiableSet(backingMap.entrySet());
        }

        @Override
        public Set<K> keySet() {
            return Collections.unmodifiableSet(backingMap.keySet());
        }

        @Override
        public Collection<V> values() {
            return Collections.unmodifiableCollection(backingMap.values());
        }

        @Override
        public V put(K key, V value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public V remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    // =================================================================================================================
    // Inner class: UnmodifiableObservableSetImpl
    // =================================================================================================================

    private static class UnmodifiableObservableSetImpl<E> extends AbstractSet<E> implements ObservableSet<E> {

        private final ObservableSet<E> backingSet;
        private SetChangeListener<E> setListener;

        /** Inline listener helper to avoid dependency on SetListenerHelper, which may not yet exist. */
        private InvalidationListener[] invalidationListeners;
        private int invalidationSize;
        @SuppressWarnings("rawtypes")
        private SetChangeListener[] changeListeners;
        private int changeSize;

        public UnmodifiableObservableSetImpl(ObservableSet<E> backingSet) {
            this.backingSet = backingSet;
        }

        private void initListener() {
            if (setListener == null) {
                setListener = c -> {
                    SetChangeListener.Change<E> adapted = new SetChangeListener.Change<>(UnmodifiableObservableSetImpl.this) {
                        @Override
                        public boolean wasAdded() {
                            return c.wasAdded();
                        }

                        @Override
                        public boolean wasRemoved() {
                            return c.wasRemoved();
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public E getElementAdded() {
                            return (E) c.getElementAdded();
                        }

                        @Override
                        @SuppressWarnings("unchecked")
                        public E getElementRemoved() {
                            return (E) c.getElementRemoved();
                        }
                    };
                    fireValueChangedEvent(adapted);
                };
                this.backingSet.addListener(setListener);
            }
        }

        @SuppressWarnings("unchecked")
        private void fireValueChangedEvent(SetChangeListener.Change<? extends E> change) {
            for (int i = 0; i < invalidationSize; i++) {
                invalidationListeners[i].invalidated(this);
            }
            for (int i = 0; i < changeSize; i++) {
                changeListeners[i].onChanged(change);
            }
        }

        @Override
        public void addListener(InvalidationListener listener) {
            initListener();
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[1];
            } else if (invalidationSize == invalidationListeners.length) {
                invalidationListeners = Arrays.copyOf(invalidationListeners, invalidationSize * 2);
            }
            invalidationListeners[invalidationSize++] = listener;
        }

        @Override
        public void removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int i = 0; i < invalidationSize; i++) {
                    if (invalidationListeners[i].equals(listener)) {
                        int numMoved = invalidationSize - i - 1;
                        if (numMoved > 0) {
                            System.arraycopy(invalidationListeners, i + 1, invalidationListeners, i, numMoved);
                        }
                        invalidationListeners[--invalidationSize] = null;
                        break;
                    }
                }
            }
        }

        @Override
        @SuppressWarnings("unchecked")
        public void addListener(SetChangeListener<? super E> listener) {
            initListener();
            if (changeListeners == null) {
                changeListeners = new SetChangeListener[1];
            } else if (changeSize == changeListeners.length) {
                changeListeners = Arrays.copyOf(changeListeners, changeSize * 2);
            }
            changeListeners[changeSize++] = listener;
        }

        @Override
        public void removeListener(SetChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int i = 0; i < changeSize; i++) {
                    if (changeListeners[i].equals(listener)) {
                        int numMoved = changeSize - i - 1;
                        if (numMoved > 0) {
                            System.arraycopy(changeListeners, i + 1, changeListeners, i, numMoved);
                        }
                        changeListeners[--changeSize] = null;
                        break;
                    }
                }
            }
        }

        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                private final Iterator<? extends E> i = backingSet.iterator();

                @Override
                public boolean hasNext() {
                    return i.hasNext();
                }

                @Override
                public E next() {
                    return i.next();
                }
            };
        }

        @Override
        public int size() {
            return backingSet.size();
        }

        @Override
        public boolean isEmpty() {
            return backingSet.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return backingSet.contains(o);
        }

        @Override
        public boolean add(E e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }
    }

    // =================================================================================================================
    // Inner class: EmptyObservableSet
    // =================================================================================================================

    private static class EmptyObservableSet<E> extends AbstractSet<E> implements ObservableSet<E> {

        private final Iterator<E> iterator = new Iterator<>() {
            @Override
            public boolean hasNext() {
                return false;
            }

            @Override
            public E next() {
                throw new NoSuchElementException();
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };

        public EmptyObservableSet() {
        }

        @Override
        public void addListener(InvalidationListener listener) {
        }

        @Override
        public void removeListener(InvalidationListener listener) {
        }

        @Override
        public void addListener(SetChangeListener<? super E> listener) {
        }

        @Override
        public void removeListener(SetChangeListener<? super E> listener) {
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean contains(Object obj) {
            return false;
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return c.isEmpty();
        }

        @Override
        public Object[] toArray() {
            return new Object[0];
        }

        @Override
        public <X> X[] toArray(X[] a) {
            if (a.length > 0) {
                a[0] = null;
            }
            return a;
        }

        @Override
        public Iterator<E> iterator() {
            return iterator;
        }
    }

    // =================================================================================================================
    // Inner class: EmptyObservableMap
    // =================================================================================================================

    private static class EmptyObservableMap<K, V> extends AbstractMap<K, V> implements ObservableMap<K, V> {

        public EmptyObservableMap() {
        }

        @Override
        public void addListener(InvalidationListener listener) {
        }

        @Override
        public void removeListener(InvalidationListener listener) {
        }

        @Override
        public void addListener(MapChangeListener<? super K, ? super V> listener) {
        }

        @Override
        public void removeListener(MapChangeListener<? super K, ? super V> listener) {
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public boolean containsKey(Object key) {
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            return false;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @Override
        public Set<K> keySet() {
            return emptyObservableSet();
        }

        @Override
        public Collection<V> values() {
            return emptyObservableList();
        }

        @Override
        public Set<Map.Entry<K, V>> entrySet() {
            return emptyObservableSet();
        }

        @Override
        public boolean equals(Object o) {
            return (o instanceof Map<?, ?> m) && m.isEmpty();
        }

        @Override
        public int hashCode() {
            return 0;
        }
    }

}

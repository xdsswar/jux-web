package xss.it.jux.reactive.collections;

import xss.it.jux.reactive.collections.internal.NonIterableChange;

import java.util.BitSet;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

/**
 * A {@link List} wrapper that implements {@link ObservableList}.
 *
 * <p>This is a package-private implementation that wraps a standard {@link List} and adds
 * observability by extending {@link ModifiableObservableListBase}. All structural modifications
 * (add, remove, set) are delegated to the backing list, and change notifications are fired
 * automatically by the base class.</p>
 *
 * <p>The class also implements {@link SortableList} to report sort operations as a single
 * permutation change, and {@link RandomAccess} to indicate that indexed access is efficient.</p>
 *
 * @param <E> the type of elements in this list
 * @see ModifiableObservableListBase
 * @see SortableList
 * @see JuxCollections#observableArrayList()
 */
class ObservableListWrapper<E> extends ModifiableObservableListBase<E> implements SortableList<E>, RandomAccess {

    private final List<E> backingList;

    /**
     * Creates a new observable wrapper around the specified backing list.
     *
     * <p>The backing list is used directly (not copied), so any external modifications
     * to it will not trigger change notifications. All modifications should be made
     * through this wrapper to ensure proper listener notification.</p>
     *
     * @param list the backing list to wrap, must not be {@code null}
     */
    public ObservableListWrapper(List<E> list) {
        backingList = list;
    }

    @Override
    public E get(int index) {
        return backingList.get(index);
    }

    @Override
    public int size() {
        return backingList.size();
    }

    @Override
    protected void doAdd(int index, E element) {
        Objects.checkIndex(index, size() + 1);
        backingList.add(index, element);
    }

    @Override
    protected E doSet(int index, E element) {
        return backingList.set(index, element);
    }

    @Override
    protected E doRemove(int index) {
        return backingList.remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return backingList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return backingList.lastIndexOf(o);
    }

    @Override
    public boolean contains(Object o) {
        return backingList.contains(o);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return backingList.containsAll(c);
    }

    @Override
    public void clear() {
        if (hasListeners()) {
            beginChange();
            nextRemove(0, this);
        }
        backingList.clear();
        ++modCount;
        if (hasListeners()) {
            endChange();
        }
    }

    @Override
    public void remove(int fromIndex, int toIndex) {
        Objects.checkFromToIndex(fromIndex, toIndex, size());
        beginChange();
        for (int i = fromIndex; i < toIndex; ++i) {
            remove(fromIndex);
        }
        endChange();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        // implicit check to ensure c != null
        if (c.isEmpty() || backingList.isEmpty()) {
            return false;
        }

        beginChange();
        BitSet bs = new BitSet(c.size());
        for (int i = 0; i < size(); ++i) {
            if (c.contains(get(i))) {
                bs.set(i);
            }
        }
        if (!bs.isEmpty()) {
            int cur = size();
            while ((cur = bs.previousSetBit(cur - 1)) >= 0) {
                remove(cur);
            }
        }
        endChange();
        return !bs.isEmpty();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        // implicit check to ensure c != null
        if (c.isEmpty() && !backingList.isEmpty()) {
            clear();
            return true;
        }

        if (backingList.isEmpty()) {
            return false;
        }

        beginChange();
        BitSet bs = new BitSet(c.size());
        for (int i = 0; i < size(); ++i) {
            if (!c.contains(get(i))) {
                bs.set(i);
            }
        }
        if (!bs.isEmpty()) {
            int cur = size();
            while ((cur = bs.previousSetBit(cur - 1)) >= 0) {
                remove(cur);
            }
        }
        endChange();
        return !bs.isEmpty();
    }

    private SortHelper helper;

    @Override
    public void doSort(Comparator<? super E> comparator) {
        int[] perm = getSortHelper().sort(backingList, comparator);
        fireChange(new NonIterableChange.SimplePermutationChange<>(0, size(), perm, this));
    }

    private SortHelper getSortHelper() {
        if (helper == null) {
            helper = new SortHelper();
        }
        return helper;
    }
}

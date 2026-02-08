package xss.it.jux.reactive.collections.transformation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import xss.it.jux.reactive.collections.ListChangeListener.Change;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.collections.internal.NonIterableChange.SimplePermutationChange;
import xss.it.jux.reactive.collections.SortHelper;
import xss.it.jux.reactive.collections.internal.SourceAdapterChange;
import xss.it.jux.reactive.property.ObjectProperty;
import xss.it.jux.reactive.property.ObjectPropertyBase;

/**
 * Wraps an {@link ObservableList} and sorts its content.
 *
 * <p>All changes in the source {@code ObservableList} are propagated
 * immediately to this {@code SortedList}. When the comparator changes,
 * the entire list is re-sorted and listeners are notified.</p>
 *
 * <p>Note: an invalid SortedList (as a result of a broken comparator) does
 * not send any notification to listeners upon becoming valid again.</p>
 *
 * @param <E> the list element type
 * @see TransformationList
 */
public final class SortedList<E> extends TransformationList<E, E> {

    private Comparator<Element<E>> elementComparator;
    private Element<E>[] sorted;
    private int[] perm;
    private int size;

    private final SortHelper helper = new SortHelper();

    private final Element<E> tempElement = new Element<>(null, -1);

    /**
     * Creates a new SortedList wrapped around the source list.
     * The source list will be sorted using the comparator provided. If null is
     * provided, the list stays unordered and is equal to the source list.
     *
     * @param source     a list to wrap
     * @param comparator a comparator to use or null for unordered List
     */
    @SuppressWarnings("unchecked")
    public SortedList(ObservableList<? extends E> source, Comparator<? super E> comparator) {
        super(source);
        sorted = new Element[source.size() * 3 / 2 + 1];
        perm = new int[sorted.length];
        size = source.size();
        for (int i = 0; i < size; ++i) {
            sorted[i] = new Element<>(source.get(i), i);
            perm[i] = i;
        }
        if (comparator != null) {
            setComparator(comparator);
        }
    }

    /**
     * Constructs a new unordered SortedList wrapper around the source list.
     *
     * @param source the source list
     * @see #SortedList(ObservableList, Comparator)
     */
    public SortedList(ObservableList<? extends E> source) {
        this(source, null);
    }

    // ------------------------------------------------------------------
    // Source change handling
    // ------------------------------------------------------------------

    @Override
    protected void sourceChanged(Change<? extends E> c) {
        if (elementComparator != null) {
            beginChange();
            while (c.next()) {
                if (c.wasPermutated()) {
                    updatePermutationIndexes(c);
                } else if (c.wasUpdated()) {
                    update(c);
                } else {
                    addRemove(c);
                }
            }
            endChange();
        } else {
            updateUnsorted(c);
            fireChange(new SourceAdapterChange<>(this, c));
        }
    }

    // ------------------------------------------------------------------
    // Comparator property
    // ------------------------------------------------------------------

    /**
     * The comparator that denotes the order of this SortedList.
     * Null for unordered SortedList.
     */
    private ObjectProperty<Comparator<? super E>> comparator;

    /**
     * Returns the comparator property. Changing the property value triggers
     * a re-sort.
     *
     * @return the comparator property
     */
    public final ObjectProperty<Comparator<? super E>> comparatorProperty() {
        if (comparator == null) {
            comparator = new ObjectPropertyBase<>() {
                @Override
                protected void invalidated() {
                    Comparator<? super E> current = get();
                    elementComparator = current != null ? new ElementComparator<>(current) : null;
                    doSortWithPermutationChange();
                }

                @Override
                public Object getBean() {
                    return SortedList.this;
                }

                @Override
                public String getName() {
                    return "comparator";
                }
            };
        }
        return comparator;
    }

    /**
     * Returns the current comparator, or {@code null} if the list is
     * unordered.
     *
     * @return the current comparator, or {@code null}
     */
    public final Comparator<? super E> getComparator() {
        return comparator == null ? null : comparator.get();
    }

    /**
     * Sets the comparator used to sort elements. Setting {@code null} means
     * the list is unordered (same order as the source list).
     *
     * @param comparator the comparator, or {@code null}
     */
    public final void setComparator(Comparator<? super E> comparator) {
        comparatorProperty().set(comparator);
    }

    // ------------------------------------------------------------------
    // List interface
    // ------------------------------------------------------------------

    /**
     * Returns the element at the specified position in this sorted list.
     *
     * @param index index of the element to return
     * @return the element at the specified position in this list
     * @throws IndexOutOfBoundsException if the index is out of range
     */
    @Override
    public E get(int index) {
        if (index >= size) {
            throw new IndexOutOfBoundsException();
        }
        return sorted[index].e;
    }

    /**
     * Returns the number of elements in this sorted list.
     *
     * @return the number of elements in this list
     */
    @Override
    public int size() {
        return size;
    }

    @Override
    public int getSourceIndex(int index) {
        Objects.checkIndex(index, size);
        return sorted[index].index;
    }

    @Override
    public int getViewIndex(int index) {
        Objects.checkIndex(index, size);
        return perm[index];
    }

    // ------------------------------------------------------------------
    // Sorting internals
    // ------------------------------------------------------------------

    private void doSortWithPermutationChange() {
        if (elementComparator != null) {
            int[] perm = helper.sort(sorted, 0, size, elementComparator);
            for (int i = 0; i < size; i++) {
                this.perm[sorted[i].index] = i;
            }
            fireChange(new SimplePermutationChange<>(0, size, perm, this));
        } else {
            int[] perm = new int[size];
            int[] rperm = new int[size];
            for (int i = 0; i < size; ++i) {
                perm[i] = rperm[i] = i;
            }
            boolean changed = false;
            int idx = 0;
            while (idx < size) {
                final int otherIdx = sorted[idx].index;
                if (otherIdx == idx) {
                    ++idx;
                    continue;
                }
                Element<E> other = sorted[otherIdx];
                sorted[otherIdx] = sorted[idx];
                sorted[idx] = other;
                this.perm[idx] = idx;
                this.perm[otherIdx] = otherIdx;
                perm[rperm[idx]] = otherIdx;
                perm[rperm[otherIdx]] = idx;
                int tp = rperm[idx];
                rperm[idx] = rperm[otherIdx];
                rperm[otherIdx] = tp;
                changed = true;
            }
            if (changed) {
                fireChange(new SimplePermutationChange<>(0, size, perm, this));
            }
        }
    }

    private void updatePermutationIndexes(Change<? extends E> change) {
        for (int i = 0; i < size; ++i) {
            int p = change.getPermutation(sorted[i].index);
            sorted[i].index = p;
            perm[p] = i;
        }
    }

    @SuppressWarnings("unchecked")
    private void updateUnsorted(Change<? extends E> c) {
        while (c.next()) {
            if (c.wasPermutated()) {
                Element<E>[] sortedTmp = new Element[sorted.length];
                for (int i = 0; i < size; ++i) {
                    if (i >= c.getFrom() && i < c.getTo()) {
                        int p = c.getPermutation(i);
                        sortedTmp[p] = sorted[i];
                        sortedTmp[p].index = p;
                        perm[i] = i;
                    } else {
                        sortedTmp[i] = sorted[i];
                    }
                }
                sorted = sortedTmp;
            }
            if (c.wasRemoved()) {
                final int removedTo = c.getFrom() + c.getRemovedSize();
                System.arraycopy(sorted, removedTo, sorted, c.getFrom(), size - removedTo);
                System.arraycopy(perm, removedTo, perm, c.getFrom(), size - removedTo);
                size -= c.getRemovedSize();
                updateIndices(removedTo, removedTo, -c.getRemovedSize());

                // Null out out-of-range array elements to avoid maintaining object references
                final int ct = size + c.getRemovedSize();
                for (int i = size; i < ct; i++) {
                    sorted[i] = null;
                }
            }
            if (c.wasAdded()) {
                ensureSize(size + c.getAddedSize());
                updateIndices(c.getFrom(), c.getFrom(), c.getAddedSize());
                System.arraycopy(sorted, c.getFrom(), sorted, c.getTo(), size - c.getFrom());
                System.arraycopy(perm, c.getFrom(), perm, c.getTo(), size - c.getFrom());
                size += c.getAddedSize();
                for (int i = c.getFrom(); i < c.getTo(); ++i) {
                    sorted[i] = new Element<>(c.getList().get(i), i);
                    perm[i] = i;
                }
            }
        }
    }

    // ------------------------------------------------------------------
    // Element wrapper and comparator
    // ------------------------------------------------------------------

    private static class Element<E> {
        private E e;
        private int index;

        public Element(E e, int index) {
            this.e = e;
            this.index = index;
        }
    }

    private static class ElementComparator<E> implements Comparator<Element<E>> {
        private final Comparator<? super E> comparator;

        public ElementComparator(Comparator<? super E> comparator) {
            this.comparator = comparator;
        }

        @Override
        public int compare(Element<E> o1, Element<E> o2) {
            return comparator.compare(o1.e, o2.e);
        }
    }

    // ------------------------------------------------------------------
    // Array management
    // ------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private void ensureSize(int size) {
        if (sorted.length < size) {
            Element<E>[] replacement = new Element[size * 3 / 2 + 1];
            System.arraycopy(sorted, 0, replacement, 0, this.size);
            sorted = replacement;
            int[] replacementPerm = new int[size * 3 / 2 + 1];
            System.arraycopy(perm, 0, replacementPerm, 0, this.size);
            perm = replacementPerm;
        }
    }

    private void updateIndices(int from, int viewFrom, int difference) {
        for (int i = 0; i < size; ++i) {
            if (sorted[i].index >= from) {
                sorted[i].index += difference;
            }
            if (perm[i] >= viewFrom) {
                perm[i] += difference;
            }
        }
    }

    private int findPosition(E e) {
        if (sorted.length == 0) {
            return 0;
        }
        tempElement.e = e;
        int pos = Arrays.binarySearch(sorted, 0, size, tempElement, elementComparator);
        tempElement.e = null;
        return pos;
    }

    private void insertToMapping(E e, int idx) {
        int pos = findPosition(e);
        if (pos < 0) {
            pos = ~pos;
        }
        ensureSize(size + 1);
        updateIndices(idx, pos, 1);
        System.arraycopy(sorted, pos, sorted, pos + 1, size - pos);
        sorted[pos] = new Element<>(e, idx);
        System.arraycopy(perm, idx, perm, idx + 1, size - idx);
        perm[idx] = pos;
        ++size;
        nextAdd(pos, pos + 1);
    }

    private void setAllToMapping(List<? extends E> list, int to) {
        ensureSize(to);
        size = to;
        for (int i = 0; i < to; ++i) {
            sorted[i] = new Element<>(list.get(i), i);
        }
        int[] perm = helper.sort(sorted, 0, size, elementComparator);
        System.arraycopy(perm, 0, this.perm, 0, size);
        nextAdd(0, size);
    }

    private void removeFromMapping(int idx, E e) {
        int pos = perm[idx];
        System.arraycopy(sorted, pos + 1, sorted, pos, size - pos - 1);
        System.arraycopy(perm, idx + 1, perm, idx, size - idx - 1);
        --size;
        sorted[size] = null;
        updateIndices(idx + 1, pos, -1);

        nextRemove(pos, e);
    }

    private void removeAllFromMapping() {
        List<E> removed = new ArrayList<>(this);
        for (int i = 0; i < size; ++i) {
            sorted[i] = null;
        }
        size = 0;
        nextRemove(0, removed);
    }

    private void update(Change<? extends E> c) {
        int[] perm = helper.sort(sorted, 0, size, elementComparator);
        for (int i = 0; i < size; i++) {
            this.perm[sorted[i].index] = i;
        }
        nextPermutation(0, size, perm);
        for (int i = c.getFrom(), to = c.getTo(); i < to; ++i) {
            nextUpdate(this.perm[i]);
        }
    }

    private void addRemove(Change<? extends E> c) {
        if (c.getFrom() == 0 && c.getRemovedSize() == size) {
            removeAllFromMapping();
        } else {
            for (int i = 0, sz = c.getRemovedSize(); i < sz; ++i) {
                removeFromMapping(c.getFrom(), c.getRemoved().get(i));
            }
        }
        if (size == 0) {
            setAllToMapping(c.getList(), c.getTo());
        } else {
            for (int i = c.getFrom(), to = c.getTo(); i < to; ++i) {
                insertToMapping(c.getList().get(i), i);
            }
        }
    }
}

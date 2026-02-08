package xss.it.jux.reactive.collections;

import java.util.Comparator;

/**
 * A helper and marker interface used for {@link ObservableList} implementations that
 * report sort operations as a single permutation change.
 *
 * @param <E> the type of elements in this list
 * @see JuxCollections#sort(ObservableList, Comparator)
 */
public interface SortableList<E> extends ObservableList<E> {

    @SuppressWarnings("unchecked")
    @Override
    default void sort(Comparator<? super E> comparator) {
        if (size() <= 1) {
            return;
        }
        comparator = comparator != null ? comparator : (Comparator<? super E>) Comparator.naturalOrder();
        doSort(comparator);
    }

    /**
     * Sorts the list and reports it as one change event.
     *
     * @param comparator the comparator for the sorting; never {@code null}
     */
    void doSort(Comparator<? super E> comparator);
}

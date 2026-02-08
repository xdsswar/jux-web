package xss.it.jux.reactive.collections.transformation;

import java.util.List;

import xss.it.jux.reactive.collections.ListChangeListener;
import xss.it.jux.reactive.collections.ListChangeListener.Change;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.collections.ObservableListBase;

/**
 * A base class for all lists that wrap another {@link ObservableList} in a way
 * that changes (transforms) the wrapped list's elements, order, size, or
 * structure.
 *
 * <p>If the source list is observable, a listener is automatically added to it
 * and the events are delegated to
 * {@link #sourceChanged(xss.it.jux.reactive.collections.ListChangeListener.Change)}.</p>
 *
 * @param <E> the type parameter of this (transformed) list
 * @param <F> the upper bound of the type of the source list
 */
public abstract class TransformationList<E, F> extends ObservableListBase<E> {

    /**
     * Contains the source list of this transformation list.
     * This is never null and should be used to directly access source list content.
     */
    private final ObservableList<? extends F> source;

    /**
     * Listener attached to the source list; delegates changes to
     * {@link #sourceChanged(Change)}.
     */
    private ListChangeListener<F> sourceListener;

    /**
     * Creates a new Transformation list wrapped around the source list.
     *
     * @param source the wrapped list
     * @throws NullPointerException if {@code source} is null
     */
    @SuppressWarnings("unchecked")
    protected TransformationList(ObservableList<? extends F> source) {
        if (source == null) {
            throw new NullPointerException("source list must not be null");
        }
        this.source = source;
        ((ObservableList<F>) source).addListener(getListener());
    }

    /**
     * The source list specified in the constructor of this transformation list.
     *
     * @return the list that is directly wrapped by this TransformationList
     */
    public final ObservableList<? extends F> getSource() {
        return source;
    }

    /**
     * Checks whether the provided list is in the chain under this
     * {@code TransformationList}.
     *
     * <p>This means the list is either the direct source as returned by
     * {@link #getSource()} or the direct source is a {@code TransformationList},
     * and the list is in its transformation chain.</p>
     *
     * @param list the list to check
     * @return true if the list is in the transformation chain
     */
    public final boolean isInTransformationChain(ObservableList<?> list) {
        if (source == list) {
            return true;
        }
        List<?> currentSource = source;
        while (currentSource instanceof TransformationList<?, ?> transformationList) {
            currentSource = transformationList.source;
            if (currentSource == list) {
                return true;
            }
        }
        return false;
    }

    private ListChangeListener<F> getListener() {
        if (sourceListener == null) {
            sourceListener = c -> TransformationList.this.sourceChanged(c);
        }
        return sourceListener;
    }

    /**
     * Called when a change from the source is triggered.
     *
     * @param c the change
     */
    protected abstract void sourceChanged(Change<? extends F> c);

    /**
     * Maps the index of this list's element to an index in the direct source
     * list.
     *
     * @param index the index in this list
     * @return the index of the element's origin in the source list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @see #getSource()
     */
    public abstract int getSourceIndex(int index);

    /**
     * Maps the index of this list's element to an index of the provided
     * {@code list}.
     *
     * <p>The {@code list} must be in the transformation chain.</p>
     *
     * @param list  a list from the transformation chain
     * @param index the index of an element in this list
     * @return the index of the element's origin in the provided list
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= size()})
     * @throws IllegalArgumentException if the list is not in the
     *         transformation chain
     * @see #isInTransformationChain(ObservableList)
     */
    public final int getSourceIndexFor(ObservableList<?> list, int index) {
        if (!isInTransformationChain(list)) {
            throw new IllegalArgumentException(
                    "Provided list is not in the transformation chain of this transformation list");
        }
        List<?> currentSource = source;
        int idx = getSourceIndex(index);
        while (currentSource != list && currentSource instanceof TransformationList<?, ?> tSource) {
            idx = tSource.getSourceIndex(idx);
            currentSource = tSource.source;
        }
        return idx;
    }

    /**
     * Maps the index of the direct source list's element to an index in this
     * list.
     *
     * @param index the index in the source list
     * @return the index of the element in this list if it is contained in this
     *         list or a negative value otherwise
     * @throws IndexOutOfBoundsException if the index is out of range
     *         ({@code index < 0 || index >= getSource().size()})
     * @see #getSource()
     * @see #getSourceIndex(int)
     */
    public abstract int getViewIndex(int index);
}

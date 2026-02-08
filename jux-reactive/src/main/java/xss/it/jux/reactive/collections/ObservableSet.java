package xss.it.jux.reactive.collections;

import java.util.Set;

import xss.it.jux.reactive.Observable;

/**
 * A set that allows observers to track changes when they occur. Implementations can be created using methods
 * in {@link JuxCollections} such as {@link JuxCollections#observableSet(Object...) observableSet}.
 *
 * @see SetChangeListener
 * @see SetChangeListener.Change
 * @param <E> the set element type
 */
public interface ObservableSet<E> extends Set<E>, Observable {

    /**
     * Add a listener to this observable set.
     * @param listener the listener for listening to the set changes
     */
    public void addListener(SetChangeListener<? super E> listener);

    /**
     * Tries to remove a listener from this observable set. If the listener is not
     * attached to this set, nothing happens.
     * @param listener a listener to remove
     */
    public void removeListener(SetChangeListener<? super E> listener);
}

package xss.it.jux.reactive.collections;

import java.util.Map;

import xss.it.jux.reactive.Observable;

/**
 * A map that allows observers to track changes when they occur. Implementations can be created using methods
 * in {@link JuxCollections} such as {@link JuxCollections#observableHashMap() observableHashMap}.
 *
 * @see MapChangeListener
 * @see MapChangeListener.Change
 * @param <K> the map key element type
 * @param <V> the map value element type
 */
public interface ObservableMap<K, V> extends Map<K, V>, Observable {

    /**
     * Add a listener to this observable map.
     * @param listener the listener for listening to the map changes
     */
    public void addListener(MapChangeListener<? super K, ? super V> listener);

    /**
     * Tries to remove a listener from this observable map. If the listener is not
     * attached to this map, nothing happens.
     * @param listener a listener to remove
     */
    public void removeListener(MapChangeListener<? super K, ? super V> listener);
}

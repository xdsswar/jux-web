package xss.it.jux.reactive;

import xss.it.jux.reactive.value.ObservableValue;

/**
 * A listener that is notified whenever the value of an {@link ObservableValue}
 * changes. Unlike {@link InvalidationListener}, a {@code ChangeListener}
 * receives both the old and new values.
 *
 * @param <T> the type of the observed value
 * @see ObservableValue
 */
@FunctionalInterface
public interface ChangeListener<T> {

    /**
     * Called when the value of an {@link ObservableValue} changes.
     *
     * @param observable the {@code ObservableValue} whose value changed
     * @param oldValue   the old value
     * @param newValue   the new value
     */
    void changed(ObservableValue<? extends T> observable, T oldValue, T newValue);
}

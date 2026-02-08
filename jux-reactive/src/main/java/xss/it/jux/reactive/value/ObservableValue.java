package xss.it.jux.reactive.value;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.Subscription;

/**
 * An observable typed value. Extends {@link Observable} to support both
 * {@link xss.it.jux.reactive.InvalidationListener}s (lightweight, lazy) and
 * {@link ChangeListener}s (receive old and new values).
 *
 * @param <T> the type of the wrapped value
 * @see ChangeListener
 */
public interface ObservableValue<T> extends Observable {

    T getValue();

    void addListener(ChangeListener<? super T> listener);

    void removeListener(ChangeListener<? super T> listener);

    default Subscription subscribe(ChangeListener<? super T> listener) {
        addListener(listener);
        return () -> removeListener(listener);
    }
}

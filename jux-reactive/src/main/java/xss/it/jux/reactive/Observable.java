package xss.it.jux.reactive;

/**
 * Root interface for observable objects in the JUX reactive system.
 *
 * An {@code Observable} supports attaching {@link InvalidationListener}s
 * that are notified whenever the {@code Observable} becomes invalid.
 * It also provides a convenient {@link #subscribe} method that returns
 * a {@link Subscription} for easy cleanup.
 *
 * @see InvalidationListener
 * @see xss.it.jux.reactive.value.ObservableValue
 */
public interface Observable {

    void addListener(InvalidationListener listener);

    void removeListener(InvalidationListener listener);

    default Subscription subscribe(InvalidationListener listener) {
        addListener(listener);
        return () -> removeListener(listener);
    }
}

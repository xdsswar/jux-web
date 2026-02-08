package xss.it.jux.reactive;

/**
 * A listener that is notified when an {@link Observable} becomes invalid.
 *
 * Invalidation listeners are lightweight — they are called when the value
 * becomes potentially stale, but the new value is not yet computed (lazy
 * evaluation). Use {@link xss.it.jux.reactive.ChangeListener} if you need
 * both old and new values.
 *
 * @see Observable
 */
@FunctionalInterface
public interface InvalidationListener {

    /**
     * Called when the given {@code observable} becomes invalid.
     *
     * @param observable the {@code Observable} that became invalid
     */
    void invalidated(Observable observable);
}

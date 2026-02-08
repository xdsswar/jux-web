package xss.it.jux.reactive;

import java.util.List;
import java.util.Objects;

/**
 * Represents a cancellation or cleanup operation for an action that can be
 * canceled or that allocated resources. Subscriptions are typically obtained
 * as a result of registering a listener or callback.
 * <p>
 * Subscriptions can be combined using {@link #combine} and {@link #and},
 * allowing multiple subscriptions to be unsubscribed together when they
 * share the same lifecycle.
 */
@FunctionalInterface
public interface Subscription {

    /** An empty subscription. Does nothing when cancelled. */
    Subscription EMPTY = () -> {};

    /**
     * Returns a {@code Subscription} which combines all of the given subscriptions.
     *
     * @param subscriptions an array of subscriptions to combine
     * @return a combined {@code Subscription}
     * @throws NullPointerException if {@code subscriptions} is or contains {@code null}
     */
    static Subscription combine(Subscription... subscriptions) {
        List<Subscription> list = List.of(subscriptions);
        return () -> list.forEach(Subscription::unsubscribe);
    }

    /**
     * Cancels this subscription, or does nothing if already cancelled.
     * Implementations must be idempotent.
     */
    void unsubscribe();

    /**
     * Combines this {@code Subscription} with another, returning a new
     * {@code Subscription} that cancels both when cancelled.
     *
     * @param other another {@code Subscription}
     * @return a combined {@code Subscription}
     * @throws NullPointerException if {@code other} is {@code null}
     */
    default Subscription and(Subscription other) {
        Objects.requireNonNull(other, "other cannot be null");
        return () -> {
            unsubscribe();
            other.unsubscribe();
        };
    }
}

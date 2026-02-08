package xss.it.jux.reactive.property;

import xss.it.jux.reactive.value.ObservableValue;
import xss.it.jux.reactive.value.WritableValue;

/**
 * A read-write property that supports unidirectional and bidirectional binding.
 *
 * @param <T> the type of the wrapped value
 */
public interface Property<T> extends ReadOnlyProperty<T>, WritableValue<T> {

    /**
     * Creates a unidirectional binding to the given observable. While bound,
     * calling {@link #setValue} throws {@link IllegalStateException}.
     *
     * @param observable the observable to bind to
     */
    void bind(ObservableValue<? extends T> observable);

    /**
     * Removes the unidirectional binding, if any.
     */
    void unbind();

    /**
     * Returns whether this property is bound unidirectionally.
     *
     * @return {@code true} if bound
     */
    boolean isBound();

    /**
     * Creates a bidirectional binding between this property and another.
     * Both properties will keep their values synchronized.
     *
     * @param other the other property
     */
    void bindBidirectional(Property<T> other);

    /**
     * Removes a bidirectional binding between this property and another.
     *
     * @param other the other property
     */
    void unbindBidirectional(Property<T> other);
}

package xss.it.jux.reactive.value;

/**
 * An observable reference to a typed object.
 *
 * @param <T> the type of the wrapped value
 * @see ObservableValue
 */
public interface ObservableObjectValue<T> extends ObservableValue<T> {

    /**
     * Returns the current value. Must be identical to {@link #getValue()}.
     *
     * @return the current value
     */
    T get();
}

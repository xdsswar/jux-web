package xss.it.jux.reactive.value;

/**
 * A writable typed reference value.
 *
 * @param <T> the type of the wrapped value
 * @see WritableValue
 */
public interface WritableObjectValue<T> extends WritableValue<T> {

    T get();

    void set(T value);
}

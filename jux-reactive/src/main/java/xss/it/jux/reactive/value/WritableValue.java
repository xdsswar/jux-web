package xss.it.jux.reactive.value;

/**
 * A writable value that can be read and set. This is the base interface
 * for all writable values in the JUX reactive system.
 *
 * @param <T> the type of the wrapped value
 * @see WritableObjectValue
 */
public interface WritableValue<T> {

    T getValue();

    void setValue(T value);
}

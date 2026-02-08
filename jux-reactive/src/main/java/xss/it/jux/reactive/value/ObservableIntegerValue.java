package xss.it.jux.reactive.value;

/**
 * An observable {@code int} value.
 *
 * @see ObservableNumberValue
 */
public interface ObservableIntegerValue extends ObservableNumberValue {

    /**
     * Returns the current {@code int} value.
     *
     * @return the current value
     */
    int get();
}

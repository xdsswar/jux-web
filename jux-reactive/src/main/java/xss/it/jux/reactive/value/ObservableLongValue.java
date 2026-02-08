package xss.it.jux.reactive.value;

/**
 * An observable {@code long} value.
 *
 * @see ObservableNumberValue
 */
public interface ObservableLongValue extends ObservableNumberValue {

    /**
     * Returns the current {@code long} value.
     *
     * @return the current value
     */
    long get();
}

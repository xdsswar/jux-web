package xss.it.jux.reactive.value;

/**
 * An observable {@code double} value.
 *
 * @see ObservableNumberValue
 */
public interface ObservableDoubleValue extends ObservableNumberValue {

    /**
     * Returns the current {@code double} value.
     *
     * @return the current value
     */
    double get();
}

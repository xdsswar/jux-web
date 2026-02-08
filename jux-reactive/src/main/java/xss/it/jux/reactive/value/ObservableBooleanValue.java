package xss.it.jux.reactive.value;

/**
 * An observable {@code boolean} value.
 *
 * @see ObservableValue
 */
public interface ObservableBooleanValue extends ObservableValue<Boolean> {

    /**
     * Returns the current {@code boolean} value.
     *
     * @return the current value
     */
    boolean get();
}

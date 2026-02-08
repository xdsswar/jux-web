package xss.it.jux.reactive.value;

/**
 * A writable {@code boolean} value.
 *
 * @see WritableValue
 */
public interface WritableBooleanValue extends WritableValue<Boolean> {

    boolean get();

    void set(boolean value);

    @Override
    void setValue(Boolean value);
}

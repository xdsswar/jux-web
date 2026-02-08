package xss.it.jux.reactive.value;

/**
 * A writable {@code int} value.
 *
 * @see WritableNumberValue
 */
public interface WritableIntegerValue extends WritableNumberValue {

    int get();

    void set(int value);

    @Override
    void setValue(Number value);
}

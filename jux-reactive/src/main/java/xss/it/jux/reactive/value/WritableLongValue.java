package xss.it.jux.reactive.value;

/**
 * A writable {@code long} value.
 *
 * @see WritableNumberValue
 */
public interface WritableLongValue extends WritableNumberValue {

    long get();

    void set(long value);

    @Override
    void setValue(Number value);
}

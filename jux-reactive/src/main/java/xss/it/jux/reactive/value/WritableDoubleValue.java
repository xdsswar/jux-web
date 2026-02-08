package xss.it.jux.reactive.value;

/**
 * A writable {@code double} value.
 *
 * @see WritableNumberValue
 */
public interface WritableDoubleValue extends WritableNumberValue {

    double get();

    void set(double value);

    @Override
    void setValue(Number value);
}

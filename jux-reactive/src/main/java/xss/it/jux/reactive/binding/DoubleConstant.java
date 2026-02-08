package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableDoubleValue;

/**
 * A simple {@link ObservableDoubleValue} that represents a single constant value.
 * <p>
 * Since the value never changes, listener registration methods are no-ops.
 */
public final class DoubleConstant implements ObservableDoubleValue {

    private final double value;

    private DoubleConstant(double value) {
        this.value = value;
    }

    /**
     * Creates a new {@code DoubleConstant} with the given value.
     *
     * @param value the constant value
     * @return a new {@code DoubleConstant}
     */
    public static DoubleConstant valueOf(double value) {
        return new DoubleConstant(value);
    }

    @Override
    public double get() {
        return value;
    }

    @Override
    public Double getValue() {
        return value;
    }

    @Override
    public void addListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        // no-op
    }

    @Override
    public void removeListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        // no-op
    }

    @Override
    public int intValue() {
        return (int) value;
    }

    @Override
    public long longValue() {
        return (long) value;
    }

    @Override
    public float floatValue() {
        return (float) value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}

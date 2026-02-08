package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableIntegerValue;

/**
 * A simple {@link ObservableIntegerValue} that represents a single constant value.
 * <p>
 * Since the value never changes, listener registration methods are no-ops.
 */
public final class IntegerConstant implements ObservableIntegerValue {

    private final int value;

    private IntegerConstant(int value) {
        this.value = value;
    }

    /**
     * Creates a new {@code IntegerConstant} with the given value.
     *
     * @param value the constant value
     * @return a new {@code IntegerConstant}
     */
    public static IntegerConstant valueOf(int value) {
        return new IntegerConstant(value);
    }

    @Override
    public int get() {
        return value;
    }

    @Override
    public Integer getValue() {
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
        return value;
    }

    @Override
    public long longValue() {
        return value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    @Override
    public double doubleValue() {
        return value;
    }
}

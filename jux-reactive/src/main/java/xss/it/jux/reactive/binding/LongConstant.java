package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableLongValue;

/**
 * A simple {@link ObservableLongValue} that represents a single constant value.
 * <p>
 * Since the value never changes, listener registration methods are no-ops.
 */
public final class LongConstant implements ObservableLongValue {

    private final long value;

    private LongConstant(long value) {
        this.value = value;
    }

    /**
     * Creates a new {@code LongConstant} with the given value.
     *
     * @param value the constant value
     * @return a new {@code LongConstant}
     */
    public static LongConstant valueOf(long value) {
        return new LongConstant(value);
    }

    @Override
    public long get() {
        return value;
    }

    @Override
    public Long getValue() {
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

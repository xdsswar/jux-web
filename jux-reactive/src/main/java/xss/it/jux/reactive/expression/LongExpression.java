package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.value.ObservableLongValue;

/**
 * {@code LongExpression} is an {@link ObservableLongValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableLongValue#get()}.
 */
public abstract class LongExpression extends NumberExpressionBase
        implements ObservableLongValue {

    public LongExpression() {
    }

    @Override
    public int intValue() {
        return (int) get();
    }

    @Override
    public long longValue() {
        return get();
    }

    @Override
    public float floatValue() {
        return (float) get();
    }

    @Override
    public double doubleValue() {
        return (double) get();
    }

    @Override
    public Long getValue() {
        return get();
    }
}

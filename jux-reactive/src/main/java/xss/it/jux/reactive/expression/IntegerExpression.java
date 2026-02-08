package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.value.ObservableIntegerValue;

/**
 * {@code IntegerExpression} is an {@link ObservableIntegerValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableIntegerValue#get()}.
 */
public abstract class IntegerExpression extends NumberExpressionBase
        implements ObservableIntegerValue {

    public IntegerExpression() {
    }

    @Override
    public int intValue() {
        return get();
    }

    @Override
    public long longValue() {
        return (long) get();
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
    public Integer getValue() {
        return get();
    }
}

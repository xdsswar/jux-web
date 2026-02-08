package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.value.ObservableDoubleValue;

/**
 * {@code DoubleExpression} is an {@link ObservableDoubleValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableDoubleValue#get()}.
 */
public abstract class DoubleExpression extends NumberExpressionBase
        implements ObservableDoubleValue {

    public DoubleExpression() {
    }

    @Override
    public int intValue() {
        return (int) get();
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
        return get();
    }

    @Override
    public Double getValue() {
        return get();
    }
}

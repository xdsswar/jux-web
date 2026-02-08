package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.binding.BooleanBinding;
import xss.it.jux.reactive.binding.StringBinding;
import xss.it.jux.reactive.value.ObservableObjectValue;

/**
 * {@code ObjectExpression} is an {@link ObservableObjectValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableObjectValue#get()}.
 *
 * @param <T> the type of the expression value
 */
public abstract class ObjectExpression<T> implements ObservableObjectValue<T> {

    public ObjectExpression() {
    }

    @Override
    public T getValue() {
        return get();
    }

    // ── Equality ─────────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to another observable object (using
     * {@link Object#equals(Object)}).
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(ObservableObjectValue<?> other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to another observable object.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(ObservableObjectValue<?> other) {
        return Bindings.notEqual(this, other);
    }

    // ── Null checks ──────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression's value is {@code null}.
     *
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNull() {
        return Bindings.isNull(this);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression's value is not {@code null}.
     *
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotNull() {
        return Bindings.isNotNull(this);
    }

    // ── Conversion ───────────────────────────────────────────────

    /**
     * Creates a {@link StringBinding} that holds the string representation
     * of this object expression.
     *
     * @return a new {@code StringBinding}
     */
    public StringBinding asString() {
        return Bindings.format("%s", this);
    }
}

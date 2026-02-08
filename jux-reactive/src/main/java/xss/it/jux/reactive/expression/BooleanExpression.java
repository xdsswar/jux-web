package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.binding.BooleanBinding;
import xss.it.jux.reactive.binding.StringBinding;
import xss.it.jux.reactive.value.ObservableBooleanValue;

/**
 * {@code BooleanExpression} is an {@link ObservableBooleanValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableBooleanValue#get()}.
 */
public abstract class BooleanExpression implements ObservableBooleanValue {

    public BooleanExpression() {
    }

    @Override
    public Boolean getValue() {
        return get();
    }

    // ── Logical operations ───────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds the logical AND of this
     * expression and another observable boolean.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding and(ObservableBooleanValue other) {
        return Bindings.and(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds the logical OR of this
     * expression and another observable boolean.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding or(ObservableBooleanValue other) {
        return Bindings.or(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds the logical NOT of this
     * expression.
     *
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding not() {
        return Bindings.not(this);
    }

    // ── Equality ─────────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to another observable boolean.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(ObservableBooleanValue other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to another observable boolean.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(ObservableBooleanValue other) {
        return Bindings.notEqual(this, other);
    }

    // ── Conversion ───────────────────────────────────────────────

    /**
     * Creates a {@link StringBinding} that holds the string representation
     * of this boolean expression.
     *
     * @return a new {@code StringBinding}
     */
    public StringBinding asString() {
        return Bindings.format("%s", this);
    }
}

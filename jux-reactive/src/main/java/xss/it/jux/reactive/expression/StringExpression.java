package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.binding.BooleanBinding;
import xss.it.jux.reactive.binding.IntegerBinding;
import xss.it.jux.reactive.binding.StringBinding;
import xss.it.jux.reactive.value.ObservableStringValue;

/**
 * {@code StringExpression} is an {@link ObservableStringValue} with additional
 * convenience methods for creating bindings in a fluent style.
 *
 * Concrete sub-classes must implement {@link ObservableStringValue#get()}.
 */
public abstract class StringExpression implements ObservableStringValue {

    public StringExpression() {
    }

    @Override
    public String getValue() {
        return get();
    }

    /**
     * Returns the value of this expression, or an empty string if the value
     * is {@code null}.
     *
     * @return the current value or {@code ""} if {@code null}
     */
    public final String getValueSafe() {
        final String value = get();
        return value == null ? "" : value;
    }

    // ── Concatenation ────────────────────────────────────────────

    /**
     * Creates a {@link StringBinding} that holds the concatenation of this
     * string expression and the string representation of another object.
     *
     * <p>If {@code other} is an {@link ObservableValue}, the binding will
     * update when either value changes. Otherwise the constant string
     * representation of {@code other} is appended.</p>
     *
     * @param other the object to concatenate (may be an {@code ObservableValue})
     * @return a new {@code StringBinding}
     */
    public StringBinding concat(Object other) {
        return Bindings.concat(this, other);
    }

    // ── Length / empty ───────────────────────────────────────────

    /**
     * Creates an {@link IntegerBinding} that holds the length of this string
     * expression. If the value is {@code null}, the length is {@code 0}.
     *
     * @return a new {@code IntegerBinding}
     */
    public IntegerBinding length() {
        return Bindings.length(this);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is {@code null} or empty.
     *
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEmpty() {
        return Bindings.isEmpty(this);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is neither {@code null} nor empty.
     *
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEmpty() {
        return Bindings.isNotEmpty(this);
    }

    // ── Equality ─────────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is equal to another observable string (case-sensitive).
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(ObservableStringValue other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is not equal to another observable string
     * (case-sensitive).
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(ObservableStringValue other) {
        return Bindings.notEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is equal to another observable string, ignoring case.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualToIgnoreCase(ObservableStringValue other) {
        return Bindings.equalIgnoreCase(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * string expression is not equal to another observable string, ignoring
     * case.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualToIgnoreCase(ObservableStringValue other) {
        return Bindings.notEqualIgnoreCase(this, other);
    }
}

package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.binding.Bindings;
import xss.it.jux.reactive.binding.BooleanBinding;
import xss.it.jux.reactive.binding.DoubleBinding;
import xss.it.jux.reactive.binding.NumberBinding;
import xss.it.jux.reactive.binding.StringBinding;
import xss.it.jux.reactive.value.ObservableNumberValue;

/**
 * Base class for numeric expression types. Contains convenience methods
 * common to all {@link NumberExpression} subclasses.
 *
 * @see IntegerExpression
 * @see LongExpression
 * @see DoubleExpression
 */
public abstract class NumberExpressionBase implements NumberExpression {

    public NumberExpressionBase() {
    }

    // ── Arithmetic ───────────────────────────────────────────────

    /**
     * Creates a {@link NumberBinding} that calculates the sum of this
     * expression and another observable number.
     *
     * @param other the other operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding add(ObservableNumberValue other) {
        return Bindings.add(this, other);
    }

    /**
     * Creates a {@link DoubleBinding} that calculates the sum of this
     * expression and a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code DoubleBinding}
     */
    public DoubleBinding add(double other) {
        return Bindings.add(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the sum of this
     * expression and a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding add(int other) {
        return Bindings.add(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the sum of this
     * expression and a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding add(long other) {
        return Bindings.add(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the difference of this
     * expression and another observable number.
     *
     * @param other the other operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding subtract(ObservableNumberValue other) {
        return Bindings.subtract(this, other);
    }

    /**
     * Creates a {@link DoubleBinding} that calculates the difference of this
     * expression and a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code DoubleBinding}
     */
    public DoubleBinding subtract(double other) {
        return Bindings.subtract(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the difference of this
     * expression and a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding subtract(int other) {
        return Bindings.subtract(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the difference of this
     * expression and a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding subtract(long other) {
        return Bindings.subtract(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the product of this
     * expression and another observable number.
     *
     * @param other the other operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding multiply(ObservableNumberValue other) {
        return Bindings.multiply(this, other);
    }

    /**
     * Creates a {@link DoubleBinding} that calculates the product of this
     * expression and a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code DoubleBinding}
     */
    public DoubleBinding multiply(double other) {
        return Bindings.multiply(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the product of this
     * expression and a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding multiply(int other) {
        return Bindings.multiply(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the product of this
     * expression and a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code NumberBinding}
     */
    public NumberBinding multiply(long other) {
        return Bindings.multiply(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the quotient of this
     * expression and another observable number.
     *
     * @param other the divisor
     * @return a new {@code NumberBinding}
     */
    public NumberBinding divide(ObservableNumberValue other) {
        return Bindings.divide(this, other);
    }

    /**
     * Creates a {@link DoubleBinding} that calculates the quotient of this
     * expression and a constant {@code double} value.
     *
     * @param other the constant divisor
     * @return a new {@code DoubleBinding}
     */
    public DoubleBinding divide(double other) {
        return Bindings.divide(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the quotient of this
     * expression and a constant {@code int} value.
     *
     * @param other the constant divisor
     * @return a new {@code NumberBinding}
     */
    public NumberBinding divide(int other) {
        return Bindings.divide(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that calculates the quotient of this
     * expression and a constant {@code long} value.
     *
     * @param other the constant divisor
     * @return a new {@code NumberBinding}
     */
    public NumberBinding divide(long other) {
        return Bindings.divide(this, other);
    }

    /**
     * Creates a {@link NumberBinding} that holds the negation of this expression.
     *
     * @return a new {@code NumberBinding}
     */
    public NumberBinding negate() {
        return Bindings.negate(this);
    }

    // ── Comparison ───────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThan(ObservableNumberValue other) {
        return Bindings.greaterThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThan(double other) {
        return Bindings.greaterThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThan(int other) {
        return Bindings.greaterThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThan(long other) {
        return Bindings.greaterThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThan(ObservableNumberValue other) {
        return Bindings.lessThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThan(double other) {
        return Bindings.lessThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThan(int other) {
        return Bindings.lessThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThan(long other) {
        return Bindings.lessThan(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than or equal to another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThanOrEqual(ObservableNumberValue other) {
        return Bindings.greaterThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than or equal to a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThanOrEqual(double other) {
        return Bindings.greaterThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than or equal to a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThanOrEqual(int other) {
        return Bindings.greaterThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is greater than or equal to a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding greaterThanOrEqual(long other) {
        return Bindings.greaterThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than or equal to another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThanOrEqual(ObservableNumberValue other) {
        return Bindings.lessThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than or equal to a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThanOrEqual(double other) {
        return Bindings.lessThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than or equal to a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThanOrEqual(int other) {
        return Bindings.lessThanOrEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is less than or equal to a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding lessThanOrEqual(long other) {
        return Bindings.lessThanOrEqual(this, other);
    }

    // ── Equality ─────────────────────────────────────────────────

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(ObservableNumberValue other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(double other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(int other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is equal to a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isEqualTo(long other) {
        return Bindings.equal(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to another observable number.
     *
     * @param other the other operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(ObservableNumberValue other) {
        return Bindings.notEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to a constant {@code double} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(double other) {
        return Bindings.notEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to a constant {@code int} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(int other) {
        return Bindings.notEqual(this, other);
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} when this
     * expression is not equal to a constant {@code long} value.
     *
     * @param other the constant operand
     * @return a new {@code BooleanBinding}
     */
    public BooleanBinding isNotEqualTo(long other) {
        return Bindings.notEqual(this, other);
    }

    // ── Conversion ───────────────────────────────────────────────

    /**
     * Creates a {@link StringBinding} that holds the string representation
     * of this numeric expression.
     *
     * @return a new {@code StringBinding}
     */
    public StringBinding asString() {
        return Bindings.format("%s", this);
    }
}

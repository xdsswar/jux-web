package xss.it.jux.reactive.expression;

import xss.it.jux.reactive.value.ObservableNumberValue;

/**
 * An expression that wraps a numeric value. Provides the common contract
 * for all numeric expression types.
 *
 * @see IntegerExpression
 * @see LongExpression
 * @see DoubleExpression
 */
public interface NumberExpression extends ObservableNumberValue {
}

package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.expression.NumberExpression;

/**
 * A tagging interface to mark all Bindings that wrap a number-value.
 *
 * @see Binding
 * @see NumberExpression
 */
public interface NumberBinding extends Binding<Number>, NumberExpression {
}

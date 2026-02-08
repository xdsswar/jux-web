package xss.it.jux.reactive.property;

import xss.it.jux.reactive.expression.ObjectExpression;

/**
 * Superclass for all read-only properties wrapping an {@code Object}.
 *
 * @param <T> the type of the wrapped value
 */
public abstract class ReadOnlyObjectProperty<T> extends ObjectExpression<T>
        implements ReadOnlyProperty<T> {

    public ReadOnlyObjectProperty() {
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ReadOnlyObjectProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && !name.isEmpty()) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }
}

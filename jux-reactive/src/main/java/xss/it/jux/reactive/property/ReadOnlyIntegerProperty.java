package xss.it.jux.reactive.property;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.expression.IntegerExpression;

/**
 * Superclass for all read-only properties wrapping an {@code int}.
 */
public abstract class ReadOnlyIntegerProperty extends IntegerExpression
        implements ReadOnlyProperty<Number> {

    public ReadOnlyIntegerProperty() {
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ReadOnlyIntegerProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && !name.isEmpty()) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

    public static <T extends Number> ReadOnlyIntegerProperty readOnlyIntegerProperty(final ReadOnlyProperty<T> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return property instanceof ReadOnlyIntegerProperty ? (ReadOnlyIntegerProperty) property
                : new ReadOnlyIntegerPropertyBase() {
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (valid) {
                    valid = false;
                    fireValueChangedEvent();
                }
            };

            {
                property.addListener(listener);
            }

            @Override
            public int get() {
                valid = true;
                final T value = property.getValue();
                return value == null ? 0 : value.intValue();
            }

            @Override public Object getBean() { return null; }
            @Override public String getName() { return property.getName(); }
        };
    }

    public ReadOnlyObjectProperty<Integer> asObject() {
        return new ReadOnlyObjectPropertyBase<>() {
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (valid) { valid = false; fireValueChangedEvent(); }
            };

            { ReadOnlyIntegerProperty.this.addListener(listener); }

            @Override public Object getBean() { return null; }
            @Override public String getName() { return ReadOnlyIntegerProperty.this.getName(); }
            @Override public Integer get() { valid = true; return ReadOnlyIntegerProperty.this.getValue(); }
        };
    }
}

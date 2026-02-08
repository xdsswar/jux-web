package xss.it.jux.reactive.property;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.expression.BooleanExpression;

/**
 * Superclass for all read-only properties wrapping a {@code boolean}.
 */
public abstract class ReadOnlyBooleanProperty extends BooleanExpression
        implements ReadOnlyProperty<Boolean> {

    public ReadOnlyBooleanProperty() {
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ReadOnlyBooleanProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && !name.isEmpty()) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

    public static ReadOnlyBooleanProperty readOnlyBooleanProperty(final ReadOnlyProperty<Boolean> property) {
        if (property == null) {
            throw new NullPointerException("Property cannot be null");
        }
        return property instanceof ReadOnlyBooleanProperty ? (ReadOnlyBooleanProperty) property
                : new ReadOnlyBooleanPropertyBase() {
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
            public boolean get() {
                valid = true;
                final Boolean value = property.getValue();
                return value == null ? false : value;
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return property.getName();
            }
        };
    }

    public ReadOnlyObjectProperty<Boolean> asObject() {
        return new ReadOnlyObjectPropertyBase<>() {
            private boolean valid = true;
            private final InvalidationListener listener = observable -> {
                if (valid) {
                    valid = false;
                    fireValueChangedEvent();
                }
            };

            {
                ReadOnlyBooleanProperty.this.addListener(listener);
            }

            @Override
            public Object getBean() { return null; }

            @Override
            public String getName() { return ReadOnlyBooleanProperty.this.getName(); }

            @Override
            public Boolean get() {
                valid = true;
                return ReadOnlyBooleanProperty.this.getValue();
            }
        };
    }
}

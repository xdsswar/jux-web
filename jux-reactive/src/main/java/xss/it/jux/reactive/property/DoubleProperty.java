package xss.it.jux.reactive.property;

import java.util.Objects;
import xss.it.jux.reactive.internal.BidirectionalBinding;
import xss.it.jux.reactive.internal.Logging;
import xss.it.jux.reactive.value.WritableDoubleValue;

/**
 * Full implementation of a {@link Property} wrapping a {@code double} value.
 * Setting or binding this property to a null value will set it to {@code 0.0}.
 */
public abstract class DoubleProperty extends ReadOnlyDoubleProperty implements
        Property<Number>, WritableDoubleValue {

    public DoubleProperty() {
    }

    @Override
    public void setValue(Number v) {
        if (v == null) {
            Logging.getLogger().warning(
                "Attempt to set double property to null, using default value instead.",
                new NullPointerException());
            set(0.0);
        } else {
            set(v.doubleValue());
        }
    }

    @Override
    public void bindBidirectional(Property<Number> other) {
        BidirectionalBinding.bind(this, other);
    }

    @Override
    public void unbindBidirectional(Property<Number> other) {
        BidirectionalBinding.unbind(this, other);
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("DoubleProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

    public static DoubleProperty doubleProperty(final Property<Double> property) {
        Objects.requireNonNull(property, "Property cannot be null");
        return new DoublePropertyBase() {
            {
                BidirectionalBinding.bindNumber(this, property);
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

    @Override
    public ObjectProperty<Double> asObject() {
        return new ObjectPropertyBase<>() {
            {
                BidirectionalBinding.bindNumber(this, DoubleProperty.this);
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return DoubleProperty.this.getName();
            }
        };
    }
}

package xss.it.jux.reactive.property;

import java.util.Objects;
import xss.it.jux.reactive.internal.BidirectionalBinding;
import xss.it.jux.reactive.internal.Logging;
import xss.it.jux.reactive.value.WritableBooleanValue;

/**
 * Full implementation of a {@link Property} wrapping a {@code boolean} value.
 *
 * <p>This class provides a full implementation of {@link Property} wrapping a
 * {@code boolean} value.</p>
 *
 * <p>The value of a {@code BooleanProperty} can be obtained with
 * {@link #get()}, and set with {@link #set(boolean)} or {@link #setValue(Boolean)}.
 * Setting or binding this property to a {@code null} value will set it to {@code false}.</p>
 *
 * <p>A property can be bound with {@link #bind(xss.it.jux.reactive.value.ObservableValue)}
 * and unbound with {@link #unbind()}. Bidirectional bindings can be created and removed
 * with {@link #bindBidirectional(Property)} and {@link #unbindBidirectional(Property)}.</p>
 *
 * @see xss.it.jux.reactive.value.ObservableBooleanValue
 * @see WritableBooleanValue
 * @see ReadOnlyBooleanProperty
 * @see Property
 */
public abstract class BooleanProperty extends ReadOnlyBooleanProperty implements
        Property<Boolean>, WritableBooleanValue {

    /**
     * Creates a default {@code BooleanProperty}.
     */
    public BooleanProperty() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the value is {@code null}, this method sets the wrapped value to {@code false}.</p>
     */
    @Override
    public void setValue(Boolean v) {
        if (v == null) {
            Logging.getLogger().warning(
                    "Attempt to set boolean property to null, using default value instead.",
                    new NullPointerException());
            set(false);
        } else {
            set(v);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindBidirectional(Property<Boolean> other) {
        BidirectionalBinding.bind(this, other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindBidirectional(Property<Boolean> other) {
        BidirectionalBinding.unbind(this, other);
    }

    /**
     * Returns a string representation of this {@code BooleanProperty} object.
     *
     * @return a string representation of this {@code BooleanProperty} object
     */
    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("BooleanProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }

    /**
     * Returns a {@code BooleanProperty} that wraps a {@link Property} of type {@code Boolean}.
     * If the source property is already a {@code BooleanProperty}, it is returned directly.
     * Otherwise, a new {@code BooleanProperty} is created that is bidirectionally bound
     * to the source property.
     *
     * @param property the source {@code Property}
     * @return a {@code BooleanProperty} that wraps the source property
     * @throws NullPointerException if {@code property} is {@code null}
     */
    public static BooleanProperty booleanProperty(final Property<Boolean> property) {
        Objects.requireNonNull(property, "Property cannot be null");
        return property instanceof BooleanProperty ? (BooleanProperty) property : new BooleanPropertyBase() {
            {
                BidirectionalBinding.bind(this, property);
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

    /**
     * Creates an {@link ObjectProperty} that bidirectionally bound to this
     * {@code BooleanProperty}. If the value of this {@code BooleanProperty}
     * changes, the value of the {@code ObjectProperty} will be updated
     * automatically and vice-versa.
     *
     * @return the new {@code ObjectProperty}
     */
    @Override
    public ObjectProperty<Boolean> asObject() {
        return new ObjectPropertyBase<>() {
            {
                BidirectionalBinding.bind(this, BooleanProperty.this);
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return BooleanProperty.this.getName();
            }
        };
    }
}

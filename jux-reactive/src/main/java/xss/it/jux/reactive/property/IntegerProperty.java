package xss.it.jux.reactive.property;

import java.util.Objects;
import xss.it.jux.reactive.internal.BidirectionalBinding;
import xss.it.jux.reactive.internal.Logging;
import xss.it.jux.reactive.value.WritableIntegerValue;

/**
 * Full implementation of a {@link Property} wrapping an {@code int} value.
 *
 * <p>This class provides a full implementation of {@link Property} wrapping an
 * {@code int} value.</p>
 *
 * <p>The value of an {@code IntegerProperty} can be obtained with
 * {@link #get()}, and set with {@link #set(int)} or {@link #setValue(Number)}.
 * Setting or binding this property to a {@code null} value will set it to {@code 0}.</p>
 *
 * <p>A property can be bound with {@link #bind(xss.it.jux.reactive.value.ObservableValue)}
 * and unbound with {@link #unbind()}. Bidirectional bindings can be created and removed
 * with {@link #bindBidirectional(Property)} and {@link #unbindBidirectional(Property)}.</p>
 *
 * @see xss.it.jux.reactive.value.ObservableIntegerValue
 * @see WritableIntegerValue
 * @see ReadOnlyIntegerProperty
 * @see Property
 */
public abstract class IntegerProperty extends ReadOnlyIntegerProperty implements
        Property<Number>, WritableIntegerValue {

    /**
     * Creates a default {@code IntegerProperty}.
     */
    public IntegerProperty() {
    }

    /**
     * {@inheritDoc}
     *
     * <p>If the value is {@code null}, this method sets the wrapped value to {@code 0}.</p>
     */
    @Override
    public void setValue(Number v) {
        if (v == null) {
            Logging.getLogger().warning(
                    "Attempt to set integer property to null, using default value instead.",
                    new NullPointerException());
            set(0);
        } else {
            set(v.intValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bindBidirectional(Property<Number> other) {
        BidirectionalBinding.bind(this, other);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbindBidirectional(Property<Number> other) {
        BidirectionalBinding.unbind(this, other);
    }

    /**
     * Returns a string representation of this {@code IntegerProperty} object.
     *
     * @return a string representation of this {@code IntegerProperty} object
     */
    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("IntegerProperty [");
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
     * Returns an {@code IntegerProperty} that wraps a {@link Property} of type {@code Integer}.
     * If the source property is already an {@code IntegerProperty}, it is returned directly.
     * Otherwise, a new {@code IntegerProperty} is created that is bidirectionally bound
     * to the source property using {@link BidirectionalBinding#bindNumber}.
     *
     * @param property the source {@code Property}
     * @return an {@code IntegerProperty} that wraps the source property
     * @throws NullPointerException if {@code property} is {@code null}
     */
    public static IntegerProperty integerProperty(final Property<Integer> property) {
        Objects.requireNonNull(property, "Property cannot be null");
        return new IntegerPropertyBase() {
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

    /**
     * Creates an {@link ObjectProperty} that is bidirectionally bound to this
     * {@code IntegerProperty}. If the value of this {@code IntegerProperty}
     * changes, the value of the {@code ObjectProperty} will be updated
     * automatically and vice-versa.
     *
     * @return the new {@code ObjectProperty}
     */
    @Override
    public ObjectProperty<Integer> asObject() {
        return new ObjectPropertyBase<>() {
            {
                BidirectionalBinding.bindNumber(this, IntegerProperty.this);
            }

            @Override
            public Object getBean() {
                return null;
            }

            @Override
            public String getName() {
                return IntegerProperty.this.getName();
            }
        };
    }
}

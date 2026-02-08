package xss.it.jux.reactive.property;

import xss.it.jux.reactive.internal.BidirectionalBinding;
import xss.it.jux.reactive.value.ObservableValue;
import xss.it.jux.reactive.value.WritableObjectValue;

/**
 * Full implementation of a {@link Property} wrapping an arbitrary {@code Object}.
 *
 * @param <T> the type of the wrapped {@code Object}
 */
public abstract class ObjectProperty<T> extends ReadOnlyObjectProperty<T>
        implements Property<T>, WritableObjectValue<T> {

    public ObjectProperty() {
    }

    @Override
    public void setValue(T v) {
        set(v);
    }

    @Override
    public void bindBidirectional(Property<T> other) {
        BidirectionalBinding.bind(this, other);
    }

    @Override
    public void unbindBidirectional(Property<T> other) {
        BidirectionalBinding.unbind(this, other);
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("ObjectProperty [");
        if (bean != null) {
            result.append("bean: ").append(bean).append(", ");
        }
        if ((name != null) && (!name.equals(""))) {
            result.append("name: ").append(name).append(", ");
        }
        result.append("value: ").append(get()).append("]");
        return result.toString();
    }
}

package xss.it.jux.reactive.property;

import xss.it.jux.reactive.internal.BidirectionalBinding;
import xss.it.jux.reactive.value.ObservableValue;
import xss.it.jux.reactive.value.WritableStringValue;

/**
 * Full implementation of a {@link Property} wrapping a {@code String} value.
 */
public abstract class StringProperty extends ReadOnlyStringProperty implements
        Property<String>, WritableStringValue {

    public StringProperty() {
    }

    @Override
    public void setValue(String v) {
        set(v);
    }

    @Override
    public void bindBidirectional(Property<String> other) {
        BidirectionalBinding.bind(this, other);
    }

    @Override
    public void unbindBidirectional(Property<String> other) {
        BidirectionalBinding.unbind(this, other);
    }

    @Override
    public String toString() {
        final Object bean = getBean();
        final String name = getName();
        final StringBuilder result = new StringBuilder("StringProperty [");
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

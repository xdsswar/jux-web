package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableStringValue;

/**
 * A simple {@link ObservableStringValue} that represents a single constant value.
 * <p>
 * Since the value never changes, listener registration methods are no-ops.
 */
public final class StringConstant implements ObservableStringValue {

    private final String value;

    private StringConstant(String value) {
        this.value = value;
    }

    /**
     * Creates a new {@code StringConstant} with the given value.
     *
     * @param value the constant value
     * @return a new {@code StringConstant}
     */
    public static StringConstant valueOf(String value) {
        return new StringConstant(value);
    }

    @Override
    public String get() {
        return value;
    }

    @Override
    public String getValue() {
        return value;
    }

    @Override
    public void addListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void addListener(ChangeListener<? super String> listener) {
        // no-op
    }

    @Override
    public void removeListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void removeListener(ChangeListener<? super String> listener) {
        // no-op
    }
}

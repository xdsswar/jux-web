package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableObjectValue;

/**
 * A simple {@link ObservableObjectValue} that represents a single constant value.
 * <p>
 * Since the value never changes, listener registration methods are no-ops.
 *
 * @param <T> the type of the wrapped constant value
 */
public final class ObjectConstant<T> implements ObservableObjectValue<T> {

    private final T value;

    private ObjectConstant(T value) {
        this.value = value;
    }

    /**
     * Creates a new {@code ObjectConstant} with the given value.
     *
     * @param <T>   the type of the constant value
     * @param value the constant value
     * @return a new {@code ObjectConstant}
     */
    public static <T> ObjectConstant<T> valueOf(T value) {
        return new ObjectConstant<>(value);
    }

    @Override
    public T get() {
        return value;
    }

    @Override
    public T getValue() {
        return value;
    }

    @Override
    public void addListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        // no-op
    }

    @Override
    public void removeListener(InvalidationListener observer) {
        // no-op
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        // no-op
    }
}

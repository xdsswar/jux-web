package xss.it.jux.reactive.property;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.value.ObservableValue;
import xss.it.jux.reactive.internal.ExpressionHelper;

/**
 * Base class for a property wrapping an {@code int} value.
 *
 * <p>Provides all the functionality required for a property wrapping an
 * {@code int} value except for the {@link #getBean()} and
 * {@link #getName()} methods, which must be implemented by subclasses.</p>
 *
 * @see IntegerProperty
 */
public abstract class IntegerPropertyBase extends IntegerProperty {

    private int value;
    private ObservableValue<? extends Number> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<Number> helper = null;

    /**
     * Creates a new {@code IntegerPropertyBase} with the default value {@code 0}.
     */
    public IntegerPropertyBase() {
    }

    /**
     * Creates a new {@code IntegerPropertyBase} with the specified initial value.
     *
     * @param initialValue the initial value of the wrapped {@code int}
     */
    public IntegerPropertyBase(int initialValue) {
        this.value = initialValue;
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void addListener(ChangeListener<? super Number> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Number> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners} and
     * {@link ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(int)} or in case of a bound property, if the
     * binding becomes invalid.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }

    private void markInvalid() {
        if (valid) {
            valid = false;
            invalidated();
            fireValueChangedEvent();
        }
    }

    /**
     * The method {@code invalidated()} can be overridden to receive
     * invalidation notifications. This is the preferred option in
     * {@code Objects} defining the property, because it requires less memory.
     *
     * <p>The default implementation is empty.</p>
     */
    protected void invalidated() {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int get() {
        valid = true;
        return observable == null ? value : (observable.getValue() == null ? 0 : observable.getValue().intValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(int newValue) {
        if (isBound()) {
            throw new RuntimeException((getBean() != null && getName() != null ?
                    getBean().getClass().getSimpleName() + "." + getName() + " : " : "") + "A bound value cannot be set.");
        }
        if (value != newValue) {
            value = newValue;
            markInvalid();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isBound() {
        return observable != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void bind(final ObservableValue<? extends Number> newObservable) {
        if (newObservable == null) {
            throw new NullPointerException("Cannot bind to null");
        }
        if (!newObservable.equals(observable)) {
            unbind();
            observable = newObservable;
            if (listener == null) {
                listener = new Listener(this);
            }
            observable.addListener(listener);
            markInvalid();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unbind() {
        if (observable != null) {
            Number val = observable.getValue();
            value = val == null ? 0 : val.intValue();
            observable.removeListener(listener);
            observable = null;
        }
    }

    /**
     * Returns a string representation of this {@code IntegerPropertyBase} object.
     *
     * @return a string representation of this {@code IntegerPropertyBase} object
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
        if (isBound()) {
            result.append("bound, ");
            if (valid) {
                result.append("value: ").append(get());
            } else {
                result.append("invalid");
            }
        } else {
            result.append("value: ").append(get());
        }
        result.append("]");
        return result.toString();
    }

    private static class Listener implements InvalidationListener {

        private final IntegerPropertyBase ref;

        public Listener(IntegerPropertyBase ref) {
            this.ref = ref;
        }

        @Override
        public void invalidated(Observable observable) {
            ref.markInvalid();
        }
    }
}

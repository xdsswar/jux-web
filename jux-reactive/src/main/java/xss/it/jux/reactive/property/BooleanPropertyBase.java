package xss.it.jux.reactive.property;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.value.ObservableValue;
import xss.it.jux.reactive.internal.ExpressionHelper;

/**
 * Base class for a property wrapping a {@code boolean} value.
 *
 * <p>Provides all the functionality required for a property wrapping a
 * {@code boolean} value except for the {@link #getBean()} and
 * {@link #getName()} methods, which must be implemented by subclasses.</p>
 *
 * @see BooleanProperty
 */
public abstract class BooleanPropertyBase extends BooleanProperty {

    private boolean value;
    private ObservableValue<? extends Boolean> observable = null;
    private InvalidationListener listener = null;
    private boolean valid = true;
    private ExpressionHelper<Boolean> helper = null;

    /**
     * Creates a new {@code BooleanPropertyBase} with the default value {@code false}.
     */
    public BooleanPropertyBase() {
    }

    /**
     * Creates a new {@code BooleanPropertyBase} with the specified initial value.
     *
     * @param initialValue the initial value of the wrapped {@code boolean}
     */
    public BooleanPropertyBase(boolean initialValue) {
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
    public void addListener(ChangeListener<? super Boolean> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super Boolean> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Sends notifications to all attached
     * {@link InvalidationListener InvalidationListeners} and
     * {@link ChangeListener ChangeListeners}.
     *
     * This method is called when the value is changed, either manually by
     * calling {@link #set(boolean)} or in case of a bound property, if the
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
    public boolean get() {
        valid = true;
        return observable == null ? value : Boolean.TRUE.equals(observable.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void set(boolean newValue) {
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
    public void bind(final ObservableValue<? extends Boolean> newObservable) {
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
            value = Boolean.TRUE.equals(observable.getValue());
            observable.removeListener(listener);
            observable = null;
        }
    }

    /**
     * Returns a string representation of this {@code BooleanPropertyBase} object.
     *
     * @return a string representation of this {@code BooleanPropertyBase} object
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

        private final BooleanPropertyBase ref;

        public Listener(BooleanPropertyBase ref) {
            this.ref = ref;
        }

        @Override
        public void invalidated(Observable observable) {
            ref.markInvalid();
        }
    }
}

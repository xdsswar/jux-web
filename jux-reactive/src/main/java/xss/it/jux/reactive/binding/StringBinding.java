package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.expression.StringExpression;
import xss.it.jux.reactive.internal.ExpressionHelper;

/**
 * Base class that provides most of the functionality needed to implement a
 * {@link Binding} of a {@code String}.
 * <p>
 * {@code StringBinding} provides a simple invalidation-scheme. An extending
 * class can register dependencies by calling {@link #bind(Observable...)}.
 * If one of the registered dependencies becomes invalid, this
 * {@code StringBinding} is marked as invalid. With
 * {@link #unbind(Observable...)} listening to dependencies can be stopped.
 * <p>
 * To provide a concrete implementation of this class, the method
 * {@link #computeValue()} has to be implemented to calculate the value of this
 * binding based on the current state of the dependencies. It is called when
 * {@link #get()} is called for an invalid binding.
 *
 * @see Binding
 * @see StringExpression
 */
public abstract class StringBinding extends StringExpression implements Binding<String> {

    private String value;
    private boolean valid = false;

    /**
     * Invalidation listener used for observing dependencies. This
     * is never cleared once created as there is no way to determine
     * when all dependencies that were previously bound were removed
     * in one or more calls to {@link #unbind(Observable...)}.
     */
    private BindingHelperObserver observer;
    private ExpressionHelper<String> helper = null;

    /**
     * Creates a default {@code StringBinding}.
     */
    public StringBinding() {
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
    public void addListener(ChangeListener<? super String> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super String> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Start observing the dependencies for changes. If the value of one of the
     * dependencies changes, the binding is marked as invalid.
     *
     * @param dependencies the dependencies to observe
     */
    protected final void bind(Observable... dependencies) {
        if ((dependencies != null) && (dependencies.length > 0)) {
            if (observer == null) {
                observer = new BindingHelperObserver(this);
            }
            for (final Observable dep : dependencies) {
                dep.addListener(observer);
            }
        }
    }

    /**
     * Stop observing the dependencies for changes.
     *
     * @param dependencies the dependencies to stop observing
     */
    protected final void unbind(Observable... dependencies) {
        if (observer != null) {
            for (final Observable dep : dependencies) {
                dep.removeListener(observer);
            }
        }
    }

    /**
     * A default implementation of {@code dispose()} that is empty.
     */
    @Override
    public void dispose() {
    }

    /**
     * A default implementation of {@code getDependencies()} that returns an
     * empty {@link ObservableList}.
     *
     * @return an empty {@code ObservableList}
     */
    @Override
    public ObservableList<?> getDependencies() {
        return JuxCollections.emptyObservableList();
    }

    /**
     * Returns the result of {@link #computeValue()}. The method
     * {@code computeValue()} is only called if the binding is invalid. The
     * result is cached and returned if the binding did not become invalid since
     * the last call of {@code get()}.
     *
     * @return the current value
     */
    @Override
    public final String get() {
        if (!valid) {
            value = computeValue();
            valid = true;
        }
        return value;
    }

    @Override
    public String getValue() {
        return get();
    }

    /**
     * Called when this binding becomes invalid. Can be overridden by extending
     * classes to react to the invalidation. The default implementation is empty.
     */
    protected void onInvalidating() {
    }

    @Override
    public final void invalidate() {
        if (valid) {
            valid = false;
            onInvalidating();
            ExpressionHelper.fireValueChangedEvent(helper);
        }
    }

    @Override
    public final boolean isValid() {
        return valid;
    }

    /**
     * Calculates the current value of this binding.
     * <p>
     * Classes extending {@code StringBinding} have to provide an implementation
     * of {@code computeValue}.
     *
     * @return the current value
     */
    protected abstract String computeValue();

    /**
     * Returns a string representation of this {@code StringBinding} object.
     *
     * @return a string representation of this {@code StringBinding} object.
     */
    @Override
    public String toString() {
        return valid ? "StringBinding [value: " + get() + "]"
                : "StringBinding [invalid]";
    }
}

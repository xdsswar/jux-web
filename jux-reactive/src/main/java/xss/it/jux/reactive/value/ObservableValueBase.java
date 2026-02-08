package xss.it.jux.reactive.value;

import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.internal.ExpressionHelper;

/**
 * A convenience base class for {@link ObservableValue} implementations.
 * Provides infrastructure for invalidation and change event notification
 * using {@link ExpressionHelper}.
 *
 * This implementation handles adding and removing listeners while
 * notifications are being dispatched, but is not thread-safe.
 *
 * @param <T> the type of the wrapped value
 */
public abstract class ObservableValueBase<T> implements ObservableValue<T> {

    private ExpressionHelper<T> helper;

    public ObservableValueBase() {
    }

    @Override
    public void addListener(InvalidationListener listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void addListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.addListener(helper, this, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    @Override
    public void removeListener(ChangeListener<? super T> listener) {
        helper = ExpressionHelper.removeListener(helper, listener);
    }

    /**
     * Notifies all registered listeners of a value change.
     */
    protected void fireValueChangedEvent() {
        ExpressionHelper.fireValueChangedEvent(helper);
    }
}

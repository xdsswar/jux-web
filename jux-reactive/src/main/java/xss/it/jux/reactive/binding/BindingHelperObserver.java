package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;

/**
 * Internal invalidation listener that forwards invalidation events to a
 * {@link Binding}. Uses a direct reference to the binding (no
 * {@code WeakReference}, as TeaVM does not support it).
 *
 * <p>When any observed dependency fires an invalidation, this observer
 * calls {@link Binding#invalidate()} on the target binding.</p>
 */
public class BindingHelperObserver implements InvalidationListener {

    private final Binding<?> binding;

    public BindingHelperObserver(Binding<?> binding) {
        if (binding == null) {
            throw new NullPointerException("Binding has to be specified.");
        }
        this.binding = binding;
    }

    @Override
    public void invalidated(Observable observable) {
        binding.invalidate();
    }
}

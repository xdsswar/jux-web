package xss.it.jux.reactive.collections.internal;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.collections.ListChangeListener;

import java.util.Arrays;

/**
 * Helper class for managing invalidation and change listeners attached to an observable list.
 *
 * <p>This class uses a flyweight pattern with three internal implementations to minimize
 * overhead: a single invalidation listener, a single change listener, and a generic
 * multi-listener variant that manages arrays of both listener types.</p>
 *
 * <p>All public entry points are static factory methods. Callers maintain a reference
 * to the current helper instance and replace it on every add/remove call.</p>
 *
 * @param <E> the element type of the observable list
 */
public abstract class ListListenerHelper<E> {

    // -----------------------------------------------------------------------------------------------------------------
    // Static methods

    /**
     * Adds an invalidation listener. If the helper is {@code null}, a new single-invalidation
     * helper is created; otherwise the existing helper is promoted as needed.
     *
     * @param helper   the current helper instance, may be {@code null}
     * @param listener the invalidation listener to add, must not be {@code null}
     * @param <E>      element type
     * @return the (possibly new) helper instance
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public static <E> ListListenerHelper<E> addListener(ListListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleInvalidation<>(listener) : helper.addListener(listener);
    }

    /**
     * Removes an invalidation listener. Returns {@code null} when the last listener is removed.
     *
     * @param helper   the current helper instance, may be {@code null}
     * @param listener the invalidation listener to remove, must not be {@code null}
     * @param <E>      element type
     * @return the (possibly new) helper instance, or {@code null} if empty
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public static <E> ListListenerHelper<E> removeListener(ListListenerHelper<E> helper, InvalidationListener listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    /**
     * Adds a list change listener. If the helper is {@code null}, a new single-change
     * helper is created; otherwise the existing helper is promoted as needed.
     *
     * @param helper   the current helper instance, may be {@code null}
     * @param listener the list change listener to add, must not be {@code null}
     * @param <E>      element type
     * @return the (possibly new) helper instance
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public static <E> ListListenerHelper<E> addListener(ListListenerHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? new SingleChange<>(listener) : helper.addListener(listener);
    }

    /**
     * Removes a list change listener. Returns {@code null} when the last listener is removed.
     *
     * @param helper   the current helper instance, may be {@code null}
     * @param listener the list change listener to remove, must not be {@code null}
     * @param <E>      element type
     * @return the (possibly new) helper instance, or {@code null} if empty
     * @throws NullPointerException if {@code listener} is {@code null}
     */
    public static <E> ListListenerHelper<E> removeListener(ListListenerHelper<E> helper, ListChangeListener<? super E> listener) {
        if (listener == null) {
            throw new NullPointerException();
        }
        return (helper == null) ? null : helper.removeListener(listener);
    }

    /**
     * Fires a value-changed event to all registered listeners. The change is reset before
     * delivery so that listeners can iterate it from the beginning.
     *
     * @param helper the current helper instance, may be {@code null}
     * @param change the change event describing what happened
     * @param <E>    element type
     */
    public static <E> void fireValueChangedEvent(ListListenerHelper<E> helper, ListChangeListener.Change<? extends E> change) {
        if (helper != null) {
            change.reset();
            helper.fireValueChangedEvent(change);
        }
    }

    /**
     * Returns {@code true} if the helper has any listeners registered.
     *
     * @param helper the current helper instance, may be {@code null}
     * @param <E>    element type
     * @return {@code true} if at least one listener is registered
     */
    public static <E> boolean hasListeners(ListListenerHelper<E> helper) {
        return helper != null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // Abstract instance methods

    protected abstract ListListenerHelper<E> addListener(InvalidationListener listener);

    protected abstract ListListenerHelper<E> removeListener(InvalidationListener listener);

    protected abstract ListListenerHelper<E> addListener(ListChangeListener<? super E> listener);

    protected abstract ListListenerHelper<E> removeListener(ListChangeListener<? super E> listener);

    protected abstract void fireValueChangedEvent(ListChangeListener.Change<? extends E> change);

    // -----------------------------------------------------------------------------------------------------------------
    // Implementations

    private static class SingleInvalidation<E> extends ListListenerHelper<E> {

        private final InvalidationListener listener;

        private SingleInvalidation(InvalidationListener listener) {
            this.listener = listener;
        }

        @Override
        protected ListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(InvalidationListener listener) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected ListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            try {
                listener.invalidated(change.getList());
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class SingleChange<E> extends ListListenerHelper<E> {

        private final ListChangeListener<? super E> listener;

        private SingleChange(ListChangeListener<? super E> listener) {
            this.listener = listener;
        }

        @Override
        protected ListListenerHelper<E> addListener(InvalidationListener listener) {
            return new Generic<>(listener, this.listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(InvalidationListener listener) {
            return this;
        }

        @Override
        protected ListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            return new Generic<>(this.listener, listener);
        }

        @Override
        protected ListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            return (listener.equals(this.listener)) ? null : this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            try {
                listener.onChanged(change);
            } catch (Exception e) {
                Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
            }
        }
    }

    private static class Generic<E> extends ListListenerHelper<E> {

        private InvalidationListener[] invalidationListeners;
        private ListChangeListener<? super E>[] changeListeners;
        private int invalidationSize;
        private int changeSize;
        private boolean locked;

        private Generic(InvalidationListener listener0, InvalidationListener listener1) {
            this.invalidationListeners = new InvalidationListener[]{listener0, listener1};
            this.invalidationSize = 2;
        }

        @SuppressWarnings("unchecked")
        private Generic(ListChangeListener<? super E> listener0, ListChangeListener<? super E> listener1) {
            this.changeListeners = new ListChangeListener[]{listener0, listener1};
            this.changeSize = 2;
        }

        @SuppressWarnings("unchecked")
        private Generic(InvalidationListener invalidationListener, ListChangeListener<? super E> changeListener) {
            this.invalidationListeners = new InvalidationListener[]{invalidationListener};
            this.invalidationSize = 1;
            this.changeListeners = new ListChangeListener[]{changeListener};
            this.changeSize = 1;
        }

        @Override
        protected Generic<E> addListener(InvalidationListener listener) {
            if (invalidationListeners == null) {
                invalidationListeners = new InvalidationListener[]{listener};
                invalidationSize = 1;
            } else {
                final int oldCapacity = invalidationListeners.length;
                if (locked) {
                    final int newCapacity = (invalidationSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                } else if (invalidationSize == oldCapacity) {
                    final int newCapacity = (oldCapacity * 3) / 2 + 1;
                    invalidationListeners = Arrays.copyOf(invalidationListeners, newCapacity);
                }
                invalidationListeners[invalidationSize++] = listener;
            }
            return this;
        }

        @Override
        protected ListListenerHelper<E> removeListener(InvalidationListener listener) {
            if (invalidationListeners != null) {
                for (int index = 0; index < invalidationSize; index++) {
                    if (listener.equals(invalidationListeners[index])) {
                        if (invalidationSize == 1) {
                            if (changeSize == 1) {
                                return new SingleChange<>(changeListeners[0]);
                            }
                            invalidationListeners = null;
                            invalidationSize = 0;
                        } else if ((invalidationSize == 2) && (changeSize == 0)) {
                            return new SingleInvalidation<>(invalidationListeners[1 - index]);
                        } else {
                            final int numMoved = invalidationSize - index - 1;
                            final InvalidationListener[] oldListeners = invalidationListeners;
                            if (locked) {
                                invalidationListeners = new InvalidationListener[invalidationListeners.length];
                                System.arraycopy(oldListeners, 0, invalidationListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, invalidationListeners, index, numMoved);
                            }
                            invalidationSize--;
                            if (!locked) {
                                invalidationListeners[invalidationSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        protected ListListenerHelper<E> addListener(ListChangeListener<? super E> listener) {
            if (changeListeners == null) {
                changeListeners = new ListChangeListener[]{listener};
                changeSize = 1;
            } else {
                final int oldCapacity = changeListeners.length;
                if (locked) {
                    final int newCapacity = (changeSize < oldCapacity) ? oldCapacity : (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                } else if (changeSize == oldCapacity) {
                    final int newCapacity = (oldCapacity * 3) / 2 + 1;
                    changeListeners = Arrays.copyOf(changeListeners, newCapacity);
                }
                changeListeners[changeSize++] = listener;
            }
            return this;
        }

        @Override
        protected ListListenerHelper<E> removeListener(ListChangeListener<? super E> listener) {
            if (changeListeners != null) {
                for (int index = 0; index < changeSize; index++) {
                    if (listener.equals(changeListeners[index])) {
                        if (changeSize == 1) {
                            if (invalidationSize == 1) {
                                return new SingleInvalidation<>(invalidationListeners[0]);
                            }
                            changeListeners = null;
                            changeSize = 0;
                        } else if ((changeSize == 2) && (invalidationSize == 0)) {
                            return new SingleChange<>(changeListeners[1 - index]);
                        } else {
                            final int numMoved = changeSize - index - 1;
                            @SuppressWarnings("unchecked")
                            final ListChangeListener<? super E>[] oldListeners = changeListeners;
                            if (locked) {
                                changeListeners = new ListChangeListener[changeListeners.length];
                                System.arraycopy(oldListeners, 0, changeListeners, 0, index);
                            }
                            if (numMoved > 0) {
                                System.arraycopy(oldListeners, index + 1, changeListeners, index, numMoved);
                            }
                            changeSize--;
                            if (!locked) {
                                changeListeners[changeSize] = null; // Let gc do its work
                            }
                        }
                        break;
                    }
                }
            }
            return this;
        }

        @Override
        protected void fireValueChangedEvent(ListChangeListener.Change<? extends E> change) {
            final InvalidationListener[] curInvalidationList = invalidationListeners;
            final int curInvalidationSize = invalidationSize;
            final ListChangeListener<? super E>[] curChangeList = changeListeners;
            final int curChangeSize = changeSize;

            try {
                locked = true;
                for (int i = 0; i < curInvalidationSize; i++) {
                    try {
                        curInvalidationList[i].invalidated(change.getList());
                    } catch (Exception e) {
                        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
                for (int i = 0; i < curChangeSize; i++) {
                    change.reset();
                    try {
                        curChangeList[i].onChanged(change);
                    } catch (Exception e) {
                        Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
                    }
                }
            } finally {
                locked = false;
            }
        }
    }
}

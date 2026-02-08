package xss.it.jux.reactive.collections;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.collections.internal.SetListenerHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

/**
 * A {@link Set} wrapper that implements {@link ObservableSet}.
 *
 * <p>This is a package-private implementation that wraps a standard {@link Set} and adds
 * observability. All mutating operations (add, remove, clear, etc.) fire
 * {@link SetChangeListener.Change} events to registered listeners.</p>
 *
 * <p>Iteration via {@link #iterator()} returns an iterator whose {@code remove()} method
 * also triggers change notifications.</p>
 *
 * @param <E> the type of elements maintained by this set
 * @see ObservableSet
 * @see SetChangeListener
 * @see JuxCollections#observableSet(Object...)
 */
class ObservableSetWrapper<E> implements ObservableSet<E> {

    private final Set<E> backingSet;

    private SetListenerHelper<E> listenerHelper;

    /**
     * Creates a new observable wrapper around the specified backing set.
     *
     * <p>The backing set is used directly (not copied), so any external modifications
     * to it will not trigger change notifications. All modifications should be made
     * through this wrapper to ensure proper listener notification.</p>
     *
     * @param set the backing set to wrap, must not be {@code null}
     */
    public ObservableSetWrapper(Set<E> set) {
        this.backingSet = set;
    }

    /**
     * A change event describing an element addition to the set.
     */
    private class SimpleAddChange extends SetChangeListener.Change<E> {

        private final E added;

        public SimpleAddChange(E added) {
            super(ObservableSetWrapper.this);
            this.added = added;
        }

        @Override
        public boolean wasAdded() {
            return true;
        }

        @Override
        public boolean wasRemoved() {
            return false;
        }

        @Override
        public E getElementAdded() {
            return added;
        }

        @Override
        public E getElementRemoved() {
            return null;
        }

        @Override
        public String toString() {
            return "added " + added;
        }
    }

    /**
     * A change event describing an element removal from the set.
     */
    private class SimpleRemoveChange extends SetChangeListener.Change<E> {

        private final E removed;

        public SimpleRemoveChange(E removed) {
            super(ObservableSetWrapper.this);
            this.removed = removed;
        }

        @Override
        public boolean wasAdded() {
            return false;
        }

        @Override
        public boolean wasRemoved() {
            return true;
        }

        @Override
        public E getElementAdded() {
            return null;
        }

        @Override
        public E getElementRemoved() {
            return removed;
        }

        @Override
        public String toString() {
            return "removed " + removed;
        }
    }

    private void callObservers(SetChangeListener.Change<E> change) {
        SetListenerHelper.fireValueChangedEvent(listenerHelper, change);
    }

    @Override
    public void addListener(InvalidationListener listener) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public void removeListener(InvalidationListener listener) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public void addListener(SetChangeListener<? super E> observer) {
        listenerHelper = SetListenerHelper.addListener(listenerHelper, observer);
    }

    @Override
    public void removeListener(SetChangeListener<? super E> observer) {
        listenerHelper = SetListenerHelper.removeListener(listenerHelper, observer);
    }

    /**
     * Returns the number of elements in this set.
     *
     * @return the number of elements in this set
     */
    @Override
    public int size() {
        return backingSet.size();
    }

    /**
     * Returns {@code true} if this set contains no elements.
     *
     * @return {@code true} if this set contains no elements
     */
    @Override
    public boolean isEmpty() {
        return backingSet.isEmpty();
    }

    /**
     * Returns {@code true} if this set contains the specified element.
     *
     * @param o element whose presence in this set is to be tested
     * @return {@code true} if this set contains the specified element
     */
    @Override
    public boolean contains(Object o) {
        return backingSet.contains(o);
    }

    /**
     * Returns an iterator over the elements in this set.
     * If the iterator's {@code remove()} method is called then the
     * registered observers are notified as well.
     *
     * @return an iterator over the elements in this set
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {

            private final Iterator<E> backingIt = backingSet.iterator();
            private E lastElement;

            @Override
            public boolean hasNext() {
                return backingIt.hasNext();
            }

            @Override
            public E next() {
                lastElement = backingIt.next();
                return lastElement;
            }

            @Override
            public void remove() {
                backingIt.remove();
                callObservers(new SimpleRemoveChange(lastElement));
            }
        };
    }

    /**
     * Returns an array containing all of the elements in this set.
     *
     * @return an array containing all of the elements in this set
     */
    @Override
    public Object[] toArray() {
        return backingSet.toArray();
    }

    /**
     * Returns an array containing all of the elements in this set.
     * The runtime type of the returned array is that of the specified array.
     *
     * @param a the array into which the elements of this set are to be stored, if it is big enough;
     *          otherwise, a new array of the same runtime type is allocated
     * @return an array containing all of the elements in this set
     */
    @Override
    public <T> T[] toArray(T[] a) {
        return backingSet.toArray(a);
    }

    /**
     * Adds the specified element to this set if it is not already present and notifies
     * all registered observers. Returns {@code true} if the element was added.
     *
     * @param o element to be added to this set
     * @return {@code true} if the element was added
     */
    @Override
    public boolean add(E o) {
        boolean ret = backingSet.add(o);
        if (ret) {
            callObservers(new SimpleAddChange(o));
        }
        return ret;
    }

    /**
     * Removes the specified element from this set if it is present and notifies
     * all registered observers. Returns {@code true} if the element was removed.
     *
     * @param o element to be removed from this set
     * @return {@code true} if the element was removed
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        boolean ret = backingSet.remove(o);
        if (ret) {
            callObservers(new SimpleRemoveChange((E) o));
        }
        return ret;
    }

    /**
     * Returns {@code true} if this set contains all of the elements in the specified collection.
     *
     * @param c collection to be checked for containment in this set
     * @return {@code true} if this set contains all of the elements in the specified collection
     */
    @Override
    public boolean containsAll(Collection<?> c) {
        return backingSet.containsAll(c);
    }

    /**
     * Adds all of the elements in the specified collection to this set.
     * Observers are notified for each element that was not already present.
     *
     * @param c collection containing elements to be added to this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = false;
        for (E element : c) {
            ret |= add(element);
        }
        return ret;
    }

    /**
     * Retains only the elements in this set that are contained in the specified collection.
     * All other elements are removed. For each removed element all the observers are notified.
     *
     * @param c collection containing elements to be retained in this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean retainAll(Collection<?> c) {
        // implicit check to ensure c != null
        if (c.isEmpty() && !backingSet.isEmpty()) {
            clear();
            return true;
        }

        if (backingSet.isEmpty()) {
            return false;
        }

        return removeRetain(c, false);
    }

    /**
     * Removes from this set all of its elements that are contained in the specified collection.
     * Observers are notified for each removed element.
     *
     * @param c collection containing elements to be removed from this set
     * @return {@code true} if this set changed as a result of the call
     */
    @Override
    public boolean removeAll(Collection<?> c) {
        // implicit check to ensure c != null
        if (c.isEmpty() || backingSet.isEmpty()) {
            return false;
        }

        return removeRetain(c, true);
    }

    private boolean removeRetain(Collection<?> c, boolean remove) {
        boolean removed = false;
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            if (remove == c.contains(element)) {
                removed = true;
                i.remove();
                callObservers(new SimpleRemoveChange(element));
            }
        }
        return removed;
    }

    /**
     * Removes all of the elements from this set. Observers are notified for each element.
     */
    @Override
    public void clear() {
        for (Iterator<E> i = backingSet.iterator(); i.hasNext(); ) {
            E element = i.next();
            i.remove();
            callObservers(new SimpleRemoveChange(element));
        }
    }

    /**
     * Returns the String representation of the wrapped set.
     *
     * @return the String representation of the wrapped set
     */
    @Override
    public String toString() {
        return backingSet.toString();
    }

    /**
     * Indicates whether some other object is "equal to" the wrapped set.
     *
     * @param obj the reference object with which to compare
     * @return {@code true} if the wrapped set is equal to the obj argument
     */
    @Override
    public boolean equals(Object obj) {
        return backingSet.equals(obj);
    }

    /**
     * Returns the hash code for the wrapped set.
     *
     * @return the hash code for the wrapped set
     */
    @Override
    public int hashCode() {
        return backingSet.hashCode();
    }
}

package xss.it.jux.reactive.collections;

/**
 * Interface that receives notifications of changes to an {@link ObservableSet}.
 *
 * @param <E> the element type
 * @see Change
 */
@FunctionalInterface
public interface SetChangeListener<E> {

    /**
     * An elementary change done to an ObservableSet.
     * Change contains information about an add or remove operation.
     * Note that adding element that is already in the set does not
     * modify the set and hence no change will be generated.
     *
     * @param <E> element type
     */
    public static abstract class Change<E> {

        private final ObservableSet<E> set;

        /**
         * Constructs a change associated with a set.
         * @param set the source of the change
         */
        public Change(ObservableSet<E> set) {
            this.set = set;
        }

        /**
         * An observable set that is associated with the change.
         * @return the source set
         */
        public ObservableSet<E> getSet() {
            return set;
        }

        /**
         * If this change is a result of add operation.
         * @return true if a new element was added to the set
         */
        public abstract boolean wasAdded();

        /**
         * If this change is a result of removal operation.
         * @return true if an old element was removed from the set
         */
        public abstract boolean wasRemoved();

        /**
         * Get the new element. Return null if this is a removal.
         * @return the element that was just added
         */
        public abstract E getElementAdded();

        /**
         * Get the old element. Return null if this is an addition.
         * @return the element that was just removed
         */
        public abstract E getElementRemoved();

    }

    /**
     * Called after a change has been made to an ObservableSet.
     * This method is called on every elementary change (add/remove) once.
     * This means, complex changes like removeAll(Collection) or clear()
     * may result in more than one call of onChanged method.
     *
     * @param change the change that was made
     */
    void onChanged(Change<? extends E> change);
}

package xss.it.jux.reactive.collections.internal;

import java.util.Collections;
import java.util.List;

import xss.it.jux.reactive.collections.ListChangeListener.Change;
import xss.it.jux.reactive.collections.ObservableList;

/**
 * A single-step {@link Change} implementation that does not support iteration
 * over multiple sub-changes.
 *
 * <p>This abstract class provides the scaffolding for concrete change types
 * that represent exactly one atomic mutation: a permutation, an add, a remove,
 * an update, or a combined add/remove. Call {@link #next()} once to advance
 * into the (only) change; subsequent calls return {@code false}.</p>
 *
 * <p>Package-private -- not part of the public JUX API.</p>
 *
 * @param <E> the element type of the list that was changed
 */
public abstract class NonIterableChange<E> extends Change<E> {

    private final int from;
    private final int to;
    private boolean invalid = true;

    protected NonIterableChange(int from, int to, ObservableList<E> list) {
        super(list);
        this.from = from;
        this.to = to;
    }

    @Override
    public int getFrom() {
        checkState();
        return from;
    }

    @Override
    public int getTo() {
        checkState();
        return to;
    }

    private static final int[] EMPTY_PERM = new int[0];

    @Override
    protected int[] getPermutation() {
        checkState();
        return EMPTY_PERM;
    }

    @Override
    public boolean next() {
        if (invalid) {
            invalid = false;
            return true;
        }
        return false;
    }

    @Override
    public void reset() {
        invalid = true;
    }

    /**
     * Verifies that {@link #next()} has been called at least once since
     * construction or the last {@link #reset()}.
     *
     * @throws IllegalStateException if the change is still in its initial state
     */
    public void checkState() {
        if (invalid) {
            throw new IllegalStateException(
                    "Invalid Change state: next() must be called before inspecting the Change.");
        }
    }

    @Override
    public String toString() {
        boolean oldInvalid = invalid;
        invalid = false;
        String ret;
        if (wasPermutated()) {
            ret = ChangeHelper.permChangeToString(getPermutation());
        } else if (wasUpdated()) {
            ret = ChangeHelper.updateChangeToString(from, to);
        } else {
            ret = ChangeHelper.addRemoveChangeToString(from, to, getList(), getRemoved());
        }
        invalid = oldInvalid;
        return "{ " + ret + " }";
    }

    // ------------------------------------------------------------------
    // Concrete implementations for common change types
    // ------------------------------------------------------------------

    /**
     * A change that represents a combined add and/or remove operation with
     * an explicit list of removed elements.
     *
     * @param <E> the element type
     */
    public static class GenericAddRemoveChange<E> extends NonIterableChange<E> {

        private final List<E> removed;

        public GenericAddRemoveChange(int from, int to, List<E> removed, ObservableList<E> list) {
            super(from, to, list);
            this.removed = removed;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return removed;
        }
    }

    /**
     * A change that represents the removal of a single element.
     *
     * @param <E> the element type
     */
    public static class SimpleRemovedChange<E> extends NonIterableChange<E> {

        private final List<E> removed;

        public SimpleRemovedChange(int from, int to, E removed, ObservableList<E> list) {
            super(from, to, list);
            this.removed = Collections.singletonList(removed);
        }

        @Override
        public boolean wasRemoved() {
            checkState();
            return true;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return removed;
        }
    }

    /**
     * A change that represents the addition of one or more contiguous elements.
     *
     * @param <E> the element type
     */
    public static class SimpleAddChange<E> extends NonIterableChange<E> {

        public SimpleAddChange(int from, int to, ObservableList<E> list) {
            super(from, to, list);
        }

        @Override
        public boolean wasRemoved() {
            checkState();
            return false;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return Collections.<E>emptyList();
        }
    }

    /**
     * A change that represents a permutation of elements within a range.
     *
     * @param <E> the element type
     */
    public static class SimplePermutationChange<E> extends NonIterableChange<E> {

        private final int[] permutation;

        public SimplePermutationChange(int from, int to, int[] permutation, ObservableList<E> list) {
            super(from, to, list);
            this.permutation = permutation;
        }

        @Override
        public List<E> getRemoved() {
            checkState();
            return Collections.<E>emptyList();
        }

        @Override
        protected int[] getPermutation() {
            checkState();
            return permutation;
        }
    }

    /**
     * A change that represents an in-place update of one or more elements.
     *
     * @param <E> the element type
     */
    public static class SimpleUpdateChange<E> extends NonIterableChange<E> {

        public SimpleUpdateChange(int position, ObservableList<E> list) {
            this(position, position + 1, list);
        }

        public SimpleUpdateChange(int from, int to, ObservableList<E> list) {
            super(from, to, list);
        }

        @Override
        public List<E> getRemoved() {
            return Collections.<E>emptyList();
        }

        @Override
        public boolean wasUpdated() {
            return true;
        }
    }
}

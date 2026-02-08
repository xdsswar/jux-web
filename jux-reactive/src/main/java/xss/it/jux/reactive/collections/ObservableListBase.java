package xss.it.jux.reactive.collections;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.collections.internal.ListListenerHelper;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Abstract class that serves as a base class for {@link ObservableList} implementations.
 *
 * <p>The base class provides two functionalities for the implementing classes:</p>
 * <ul>
 *   <li>Listener handling by implementing {@code addListener} and {@code removeListener} methods.
 *       The {@link #fireChange(ListChangeListener.Change)} method is provided for notifying the
 *       listeners with a {@code Change} object.</li>
 *   <li>Methods for building up a {@link ListChangeListener.Change} object. There are various
 *       methods called {@code next*}, like {@link #nextAdd(int, int)} for new items in the list
 *       or {@link #nextRemove(int, Object)} for an item being removed from the list.
 *       <p><strong>These methods must always be enclosed in {@link #beginChange()} and
 *       {@link #endChange()} blocks.</strong></p>
 *       <p>See the example below.</p>
 *   </li>
 * </ul>
 *
 * <p>The following example shows how the Change build-up works:</p>
 * <pre>
 *  <strong>public void</strong> removeOddIndexes() {
 *      beginChange();
 *      try {
 *          for (<strong>int</strong> i = 1; i &lt; size(); ++i) {
 *              remove(i);
 *          }
 *      } finally {
 *          endChange();
 *      }
 *  }
 *
 *  <strong>public void</strong> remove(<strong>int</strong> i) {
 *      beginChange();
 *      try {
 *          <strong>E</strong> removed = ... //do some stuff that will actually remove the element at index i
 *          nextRemove(i, removed);
 *      } finally {
 *          endChange();
 *      }
 *  }
 * </pre>
 *
 * <p>The {@code try}/{@code finally} blocks in the example are needed only if there is a
 * possibility for an exception to occur inside a {@code beginChange()} / {@code endChange()}
 * block.</p>
 *
 * <p>Note: If you want to create a modifiable {@link ObservableList} implementation, consider
 * using {@link ModifiableObservableListBase} as a superclass.</p>
 *
 * <p>Note: In order to create a list with sequential access, you should override
 * {@link #listIterator()}, {@link #iterator()} methods and use them in {@link #get},
 * {@link #size()} and other methods accordingly.</p>
 *
 * @param <E> the type of the elements contained in the List
 * @see ObservableList
 * @see ListChangeListener.Change
 * @see ModifiableObservableListBase
 */
public abstract class ObservableListBase<E> extends AbstractList<E> implements ObservableList<E> {

    private ListListenerHelper<E> listenerHelper;
    private ListChangeBuilder<E> changeBuilder;

    /**
     * Creates a default {@code ObservableListBase}.
     */
    public ObservableListBase() {
    }

    /**
     * Adds a new update operation to the change.
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param pos the position in the list where the updated element resides
     */
    protected final void nextUpdate(int pos) {
        getListChangeBuilder().nextUpdate(pos);
    }

    /**
     * Adds a new set operation to the change.
     * Equivalent to {@code nextRemove(idx); nextAdd(idx, idx + 1); }.
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param idx the index of the item that was set
     * @param old the old value at the {@code idx} position
     */
    protected final void nextSet(int idx, E old) {
        getListChangeBuilder().nextSet(idx, old);
    }

    /**
     * Adds a new replace operation to the change.
     * Equivalent to {@code nextRemove(from, removed); nextAdd(from, to); }
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param from    the index where the items were replaced
     * @param to      the end index (exclusive) of the range where the new items reside
     * @param removed the list of items that were removed
     */
    protected final void nextReplace(int from, int to, List<? extends E> removed) {
        getListChangeBuilder().nextReplace(from, to, removed);
    }

    /**
     * Adds a new remove operation to the change with multiple items removed.
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param idx     the index where the items were removed
     * @param removed the list of items that were removed
     */
    protected final void nextRemove(int idx, List<? extends E> removed) {
        getListChangeBuilder().nextRemove(idx, removed);
    }

    /**
     * Adds a new remove operation to the change with a single item removed.
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param idx     the index where the item was removed
     * @param removed the item that was removed
     */
    protected final void nextRemove(int idx, E removed) {
        getListChangeBuilder().nextRemove(idx, removed);
    }

    /**
     * Adds a new permutation operation to the change.
     *
     * <p>The permutation on index {@code "i"} contains the index where the item from
     * the index {@code "i"} was moved.</p>
     *
     * <p>It is not necessary to provide the smallest permutation possible. It is correct
     * to always call this method with {@code nextPermutation(0, size(), permutation); }</p>
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param from marks the beginning (inclusive) of the range that was permutated
     * @param to   marks the end (exclusive) of the range that was permutated
     * @param perm the permutation in that range. Even if {@code from != 0}, the array
     *             should contain the indexes of the list. Therefore, such permutation
     *             would not contain indexes of range {@code (0, from)}
     */
    protected final void nextPermutation(int from, int to, int[] perm) {
        getListChangeBuilder().nextPermutation(from, to, perm);
    }

    /**
     * Adds a new add operation to the change.
     *
     * <p>There is no need to provide the list of added items as they can be found directly
     * in the list under the specified indexes.</p>
     *
     * <p><strong>Note</strong>: needs to be called inside {@code beginChange()} /
     * {@code endChange()} block.</p>
     * <p><strong>Note</strong>: needs to reflect the <em>current</em> state of the list.</p>
     *
     * @param from marks the beginning (inclusive) of the range that was added
     * @param to   marks the end (exclusive) of the range that was added
     */
    protected final void nextAdd(int from, int to) {
        getListChangeBuilder().nextAdd(from, to);
    }

    /**
     * Begins a change block.
     *
     * <p>Must be called before any of the {@code next*} methods is called.
     * For every {@code beginChange()}, there must be a corresponding {@link #endChange()} call.</p>
     *
     * <p>{@code beginChange()} calls can be nested in a {@code beginChange()}/{@code endChange()}
     * block.</p>
     *
     * @see #endChange()
     */
    protected final void beginChange() {
        getListChangeBuilder().beginChange();
    }

    /**
     * Ends the change block.
     *
     * <p>If the block is the outermost block for the {@code ObservableList}, the
     * {@code Change} is constructed and all listeners are notified.</p>
     *
     * <p>Ending a nested block does not fire a notification.</p>
     *
     * @see #beginChange()
     */
    protected final void endChange() {
        getListChangeBuilder().endChange();
    }

    private ListChangeBuilder<E> getListChangeBuilder() {
        if (changeBuilder == null) {
            changeBuilder = new ListChangeBuilder<>(this);
        }

        return changeBuilder;
    }

    @Override
    public final void addListener(InvalidationListener listener) {
        listenerHelper = ListListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public final void removeListener(InvalidationListener listener) {
        listenerHelper = ListListenerHelper.removeListener(listenerHelper, listener);
    }

    @Override
    public final void addListener(ListChangeListener<? super E> listener) {
        listenerHelper = ListListenerHelper.addListener(listenerHelper, listener);
    }

    @Override
    public final void removeListener(ListChangeListener<? super E> listener) {
        listenerHelper = ListListenerHelper.removeListener(listenerHelper, listener);
    }

    /**
     * Notifies all listeners of a change.
     *
     * @param change an object representing the change that was done
     */
    protected final void fireChange(ListChangeListener.Change<? extends E> change) {
        ListListenerHelper.fireValueChangedEvent(listenerHelper, change);
    }

    /**
     * Returns {@code true} if there are listeners registered for this list.
     *
     * @return {@code true} if there is a listener for this list
     */
    protected final boolean hasListeners() {
        return ListListenerHelper.hasListeners(listenerHelper);
    }

    @Override
    public boolean addAll(E... elements) {
        return addAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(E... elements) {
        return setAll(Arrays.asList(elements));
    }

    @Override
    public boolean setAll(Collection<? extends E> col) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(E... elements) {
        return removeAll(Arrays.asList(elements));
    }

    @Override
    public boolean retainAll(E... elements) {
        return retainAll(Arrays.asList(elements));
    }

    @Override
    public void remove(int from, int to) {
        removeRange(from, to);
    }
}

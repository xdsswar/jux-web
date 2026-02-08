package xss.it.jux.reactive.collections.internal;

import java.util.List;

import xss.it.jux.reactive.collections.ListChangeListener;
import xss.it.jux.reactive.collections.ListChangeListener.Change;
import xss.it.jux.reactive.collections.ObservableList;

/**
 * Adapts a {@link Change} from a source list so that it appears to originate
 * from a different (wrapper) list.
 *
 * <p>This is used by {@code TransformationList} to re-broadcast changes from
 * the underlying source list while keeping {@link Change#getList()} pointing
 * at the transformation list itself rather than the source.</p>
 *
 * <p>All query methods delegate to the wrapped change; only
 * {@link #getList()} returns the adapter's target list.</p>
 *
 * <p>Package-private -- not part of the public JUX API.</p>
 *
 * @param <E> the element type of the target (wrapper) list
 */
public class SourceAdapterChange<E> extends ListChangeListener.Change<E> {

    private final Change<? extends E> change;
    private int[] perm;

    /**
     * Creates an adapter that makes the given {@code change} appear to
     * originate from {@code list}.
     *
     * @param list   the list to report as the source of this change
     * @param change the original change from the actual source list
     */
    public SourceAdapterChange(ObservableList<E> list, Change<? extends E> change) {
        super(list);
        this.change = change;
    }

    @Override
    public boolean next() {
        perm = null;
        return change.next();
    }

    @Override
    public void reset() {
        change.reset();
    }

    @Override
    public int getTo() {
        return change.getTo();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<E> getRemoved() {
        return (List<E>) change.getRemoved();
    }

    @Override
    public int getFrom() {
        return change.getFrom();
    }

    @Override
    public boolean wasUpdated() {
        return change.wasUpdated();
    }

    @Override
    protected int[] getPermutation() {
        if (perm == null) {
            if (change.wasPermutated()) {
                final int from = change.getFrom();
                final int n = change.getTo() - from;
                perm = new int[n];
                for (int i = 0; i < n; i++) {
                    perm[i] = change.getPermutation(from + i);
                }
            } else {
                perm = new int[0];
            }
        }
        return perm;
    }

    @Override
    public String toString() {
        return change.toString();
    }
}

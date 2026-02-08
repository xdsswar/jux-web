package xss.it.jux.reactive.collections.internal;

import java.util.Arrays;
import java.util.List;

/**
 * Static utility methods for producing human-readable {@code toString()} representations
 * of observable collection change events.
 *
 * <p>These helpers are used by the concrete {@code Change} implementations in
 * {@code ObservableList}, {@code ObservableMap}, and {@code ObservableSet} to
 * render diagnostic descriptions of add, remove, permutation, and update changes.</p>
 */
public final class ChangeHelper {

    private ChangeHelper() {
        // Non-instantiable utility class
    }

    /**
     * Returns a human-readable description of an add or remove (or replace) change
     * on an observable list.
     *
     * @param from    the inclusive start index of the change
     * @param to      the exclusive end index of the change
     * @param list    the current state of the list after the change
     * @param removed the list of removed elements (empty if this is a pure addition)
     * @return a descriptive string such as {@code "[x, y] added at 2"} or
     *         {@code "[a] replaced by [b] at 0"}
     */
    public static String addRemoveChangeToString(int from, int to, List<?> list, List<?> removed) {
        StringBuilder b = new StringBuilder();

        if (removed.isEmpty()) {
            b.append(list.subList(from, to));
            b.append(" added at ").append(from);
        } else {
            b.append(removed);
            if (from == to) {
                b.append(" removed at ").append(from);
            } else {
                b.append(" replaced by ");
                b.append(list.subList(from, to));
                b.append(" at ").append(from);
            }
        }
        return b.toString();
    }

    /**
     * Returns a human-readable description of a permutation change on an observable list.
     *
     * @param permutation the permutation array mapping old indexes to new indexes
     * @return a descriptive string such as {@code "permutated by [2, 0, 1]"}
     */
    public static String permChangeToString(int[] permutation) {
        return "permutated by " + Arrays.toString(permutation);
    }

    /**
     * Returns a human-readable description of an update change on an observable list.
     *
     * @param from the inclusive start index of the updated range
     * @param to   the exclusive end index of the updated range
     * @return a descriptive string such as {@code "updated at range [1, 3)"}
     */
    public static String updateChangeToString(int from, int to) {
        return "updated at range [" + from + ", " + to + ")";
    }
}

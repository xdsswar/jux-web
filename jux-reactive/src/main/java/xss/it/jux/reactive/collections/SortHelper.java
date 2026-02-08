package xss.it.jux.reactive.collections;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

/**
 * A helper class containing merge-sort algorithms that additionally track
 * the permutations created during the process.
 *
 * <p>Used internally by {@link ObservableListWrapper}, {@link
 * xss.it.jux.reactive.collections.transformation.FilteredList}, and {@link
 * xss.it.jux.reactive.collections.transformation.SortedList} to report sort
 * operations as permutation changes.</p>
 */
public class SortHelper {

    private int[] permutation;
    private int[] reversePermutation;

    private static final int INSERTIONSORT_THRESHOLD = 7;

    /**
     * Sorts the given list in place and returns the permutation array.
     *
     * @param <T>        the element type
     * @param list       the list to sort
     * @param comparator the comparator; must not be {@code null}
     * @return the permutation array
     */
    public <T> int[] sort(List<T> list, Comparator<? super T> comparator) {
        @SuppressWarnings("unchecked")
        T[] a = (T[]) list.toArray();
        int[] result = sort(a, comparator);
        ListIterator<T> i = list.listIterator();
        for (int j = 0; j < a.length; j++) {
            i.next();
            i.set(a[j]);
        }
        return result;
    }

    private <T> int[] sort(T[] items, Comparator<? super T> comparator) {
        T[] aux = items.clone();
        int[] result = initPermutation(items.length);
        mergeSort(aux, items, 0, items.length, 0, comparator);
        reversePermutation = null;
        permutation = null;
        return result;
    }

    /**
     * Sorts a sub-range of the given array and returns the permutation array for that range.
     *
     * @param <T>        the element type
     * @param items      the array to sort
     * @param fromIndex  start index (inclusive)
     * @param toIndex    end index (exclusive)
     * @param comparator the comparator; must not be {@code null}
     * @return the permutation array for the specified range
     */
    public <T> int[] sort(T[] items, int fromIndex, int toIndex, Comparator<? super T> comparator) {
        rangeCheck(items.length, fromIndex, toIndex);
        T[] aux = Arrays.copyOfRange(items, fromIndex, toIndex);
        int[] result = initPermutation(items.length);
        mergeSort(aux, items, fromIndex, toIndex, -fromIndex, comparator);
        reversePermutation = null;
        permutation = null;
        return Arrays.copyOfRange(result, fromIndex, toIndex);
    }

    /**
     * Sorts an int index array and returns the permutation array for that range.
     * Used by {@link xss.it.jux.reactive.collections.transformation.FilteredList}.
     *
     * @param indices   the index array to sort
     * @param fromIndex start index (inclusive)
     * @param toIndex   end index (exclusive)
     * @return the permutation array for the specified range
     */
    public int[] sort(int[] indices, int fromIndex, int toIndex) {
        rangeCheck(indices.length, fromIndex, toIndex);
        int[] aux = Arrays.copyOfRange(indices, fromIndex, toIndex);
        int[] result = initPermutation(indices.length);
        mergeSort(aux, indices, fromIndex, toIndex, -fromIndex);
        reversePermutation = null;
        permutation = null;
        return Arrays.copyOfRange(result, fromIndex, toIndex);
    }

    private static void rangeCheck(int arrayLen, int fromIndex, int toIndex) {
        if (fromIndex > toIndex) {
            throw new IllegalArgumentException("fromIndex(" + fromIndex + ") > toIndex(" + toIndex + ")");
        }
        if (fromIndex < 0) {
            throw new ArrayIndexOutOfBoundsException(fromIndex);
        }
        if (toIndex > arrayLen) {
            throw new ArrayIndexOutOfBoundsException(toIndex);
        }
    }

    private void mergeSort(int[] src, int[] dest, int low, int high, int off) {
        int length = high - low;

        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && Integer.compare(dest[j - 1], dest[j]) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off);
        mergeSort(dest, src, mid, high, -off);

        if (Integer.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && Integer.compare(src[p], src[q]) <= 0) {
                dest[i] = src[p];
                permutation[reversePermutation[p++]] = i;
            } else {
                dest[i] = src[q];
                permutation[reversePermutation[q++]] = i;
            }
        }

        for (int i = destLow; i < destHigh; ++i) {
            reversePermutation[permutation[i]] = i;
        }
    }

    private <T> void mergeSort(T[] src, T[] dest, int low, int high, int off, Comparator<? super T> comparator) {
        int length = high - low;

        if (length < INSERTIONSORT_THRESHOLD) {
            for (int i = low; i < high; i++) {
                for (int j = i; j > low && comparator.compare(dest[j - 1], dest[j]) > 0; j--) {
                    swap(dest, j, j - 1);
                }
            }
            return;
        }

        int destLow = low;
        int destHigh = high;
        low += off;
        high += off;
        int mid = (low + high) >>> 1;
        mergeSort(dest, src, low, mid, -off, comparator);
        mergeSort(dest, src, mid, high, -off, comparator);

        if (comparator.compare(src[mid - 1], src[mid]) <= 0) {
            System.arraycopy(src, low, dest, destLow, length);
            return;
        }

        for (int i = destLow, p = low, q = mid; i < destHigh; i++) {
            if (q >= high || p < mid && comparator.compare(src[p], src[q]) <= 0) {
                dest[i] = src[p];
                permutation[reversePermutation[p++]] = i;
            } else {
                dest[i] = src[q];
                permutation[reversePermutation[q++]] = i;
            }
        }

        for (int i = destLow; i < destHigh; ++i) {
            reversePermutation[permutation[i]] = i;
        }
    }

    private void swap(int[] x, int a, int b) {
        int t = x[a];
        x[a] = x[b];
        x[b] = t;
        permutation[reversePermutation[a]] = b;
        permutation[reversePermutation[b]] = a;
        int tp = reversePermutation[a];
        reversePermutation[a] = reversePermutation[b];
        reversePermutation[b] = tp;
    }

    private void swap(Object[] x, int a, int b) {
        Object t = x[a];
        x[a] = x[b];
        x[b] = t;
        permutation[reversePermutation[a]] = b;
        permutation[reversePermutation[b]] = a;
        int tp = reversePermutation[a];
        reversePermutation[a] = reversePermutation[b];
        reversePermutation[b] = tp;
    }

    private int[] initPermutation(int length) {
        permutation = new int[length];
        reversePermutation = new int[length];
        for (int i = 0; i < length; ++i) {
            permutation[i] = reversePermutation[i] = i;
        }
        return permutation;
    }
}

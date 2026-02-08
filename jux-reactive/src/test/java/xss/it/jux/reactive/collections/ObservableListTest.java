package xss.it.jux.reactive.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.collections.transformation.FilteredList;
import xss.it.jux.reactive.collections.transformation.SortedList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ObservableList")
class ObservableListTest {

    private ObservableList<String> list;

    @BeforeEach
    void setUp() {
        list = JuxCollections.observableArrayList();
    }

    // =====================================================================
    // Helper: captures all Change events from a ListChangeListener
    // =====================================================================

    /**
     * Records describing a single iteration step within a Change.
     * We iterate through each next() call and store the relevant flags.
     */
    record ChangeRecord(
            boolean wasAdded,
            boolean wasRemoved,
            boolean wasReplaced,
            boolean wasPermutated,
            boolean wasUpdated,
            int from,
            int to,
            List<String> addedSubList,
            List<String> removed
    ) {}

    /**
     * Flattens a Change into a list of ChangeRecords by iterating all next() calls.
     */
    private static List<ChangeRecord> toRecords(ListChangeListener.Change<? extends String> c) {
        var records = new ArrayList<ChangeRecord>();
        while (c.next()) {
            records.add(new ChangeRecord(
                    c.wasAdded(),
                    c.wasRemoved(),
                    c.wasReplaced(),
                    c.wasPermutated(),
                    c.wasUpdated(),
                    c.getFrom(),
                    c.getTo(),
                    new ArrayList<>(c.getAddedSubList()),
                    new ArrayList<>(c.getRemoved())
            ));
        }
        return records;
    }

    // =====================================================================
    // Basic List Operations
    // =====================================================================

    @Nested
    @DisplayName("Basic list operations")
    class BasicOperations {

        @Test
        void emptyList_hasSizeZero() {
            assertThat(list).isEmpty();
            assertThat(list.size()).isZero();
        }

        @Test
        void addElement_increasesSize() {
            list.add("alpha");
            assertThat(list).hasSize(1);
            assertThat(list.get(0)).isEqualTo("alpha");
        }

        @Test
        void setElement_replacesAtIndex() {
            list.addAll("alpha", "beta");
            String old = list.set(1, "gamma");
            assertThat(old).isEqualTo("beta");
            assertThat(list.get(1)).isEqualTo("gamma");
        }

        @Test
        void removeByIndex_removesElement() {
            list.addAll("alpha", "beta", "gamma");
            String removed = list.remove(1);
            assertThat(removed).isEqualTo("beta");
            assertThat(list).containsExactly("alpha", "gamma");
        }

        @Test
        void getElement_returnsCorrectElement() {
            list.addAll("alpha", "beta", "gamma");
            assertThat(list.get(0)).isEqualTo("alpha");
            assertThat(list.get(1)).isEqualTo("beta");
            assertThat(list.get(2)).isEqualTo("gamma");
        }

        @Test
        void addAllVarargs_addsAllElements() {
            boolean result = list.addAll("alpha", "beta", "gamma");
            assertThat(result).isTrue();
            assertThat(list).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void clear_removesAllElements() {
            list.addAll("alpha", "beta", "gamma");
            list.clear();
            assertThat(list).isEmpty();
        }

        @Test
        void setAll_replacesAllElements() {
            list.addAll("alpha", "beta");
            list.setAll("gamma", "delta", "epsilon");
            assertThat(list).containsExactly("gamma", "delta", "epsilon");
        }

        @Test
        void setAllCollection_replacesAllElements() {
            list.addAll("alpha", "beta");
            list.setAll(List.of("gamma", "delta"));
            assertThat(list).containsExactly("gamma", "delta");
        }
    }

    // =====================================================================
    // ListChangeListener notifications
    // =====================================================================

    @Nested
    @DisplayName("ListChangeListener notifications")
    class ChangeNotifications {

        private List<List<ChangeRecord>> allChanges;

        @BeforeEach
        void setUpListener() {
            allChanges = new ArrayList<>();
            list.addListener((ListChangeListener<String>) c -> allChanges.add(toRecords(c)));
        }

        @Test
        void addElement_firesChangeWithWasAdded() {
            list.add("alpha");

            assertThat(allChanges).hasSize(1);
            var records = allChanges.getFirst();
            assertThat(records).hasSize(1);
            assertThat(records.getFirst().wasAdded()).isTrue();
            assertThat(records.getFirst().wasRemoved()).isFalse();
            assertThat(records.getFirst().addedSubList()).containsExactly("alpha");
        }

        @Test
        void removeElement_firesChangeWithWasRemoved() {
            list.addAll("alpha", "beta");
            allChanges.clear();

            list.remove("alpha");

            assertThat(allChanges).hasSize(1);
            var records = allChanges.getFirst();
            assertThat(records).hasSize(1);
            assertThat(records.getFirst().wasRemoved()).isTrue();
            assertThat(records.getFirst().removed()).containsExactly("alpha");
        }

        @Test
        void setAtIndex_firesChangeWithWasReplaced() {
            list.addAll("alpha", "beta");
            allChanges.clear();

            list.set(0, "gamma");

            assertThat(allChanges).hasSize(1);
            var records = allChanges.getFirst();
            assertThat(records).hasSize(1);
            assertThat(records.getFirst().wasReplaced()).isTrue();
            assertThat(records.getFirst().wasAdded()).isTrue();
            assertThat(records.getFirst().wasRemoved()).isTrue();
            assertThat(records.getFirst().removed()).containsExactly("alpha");
            assertThat(records.getFirst().addedSubList()).containsExactly("gamma");
        }

        @Test
        void clear_firesChangeWithAllElementsRemoved() {
            list.addAll("alpha", "beta", "gamma");
            allChanges.clear();

            list.clear();

            assertThat(allChanges).hasSize(1);
            // The clear fires a single change with all removed elements
            var records = allChanges.getFirst();
            // Flatten all removed items across all records
            var allRemoved = new ArrayList<String>();
            for (var record : records) {
                allRemoved.addAll(record.removed());
            }
            assertThat(allRemoved).containsExactlyInAnyOrder("alpha", "beta", "gamma");
        }

        @Test
        void setAll_firesSingleChange() {
            list.addAll("alpha", "beta");
            allChanges.clear();

            list.setAll("gamma", "delta", "epsilon");

            // setAll fires a single listener callback (one Change object)
            assertThat(allChanges).hasSize(1);
        }

        @Test
        void addAllVarargs_firesChange() {
            list.addAll("alpha", "beta", "gamma");

            assertThat(allChanges).hasSize(1);
            var records = allChanges.getFirst();
            // All additions reported
            var allAdded = new ArrayList<String>();
            for (var record : records) {
                allAdded.addAll(record.addedSubList());
            }
            assertThat(allAdded).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void addElement_changeFromAndToHaveCorrectIndices() {
            list.addAll("alpha", "beta");
            allChanges.clear();

            list.add("gamma");

            var records = allChanges.getFirst();
            assertThat(records.getFirst().from()).isEqualTo(2);
            assertThat(records.getFirst().to()).isEqualTo(3);
        }

        @Test
        void addAtIndex_changeFromAndToHaveCorrectIndices() {
            list.addAll("alpha", "gamma");
            allChanges.clear();

            list.add(1, "beta");

            var records = allChanges.getFirst();
            assertThat(records.getFirst().from()).isEqualTo(1);
            assertThat(records.getFirst().to()).isEqualTo(2);
        }

        @Test
        void removeByIndex_changeFromIndicatesRemovedPosition() {
            list.addAll("alpha", "beta", "gamma");
            allChanges.clear();

            list.remove(1);

            var records = allChanges.getFirst();
            assertThat(records.getFirst().from()).isEqualTo(1);
            assertThat(records.getFirst().removed()).containsExactly("beta");
        }

        @Test
        void getAddedSubList_containsCorrectElements() {
            list.addAll("alpha");
            allChanges.clear();

            list.addAll(1, List.of("beta", "gamma"));

            var records = allChanges.getFirst();
            var allAdded = new ArrayList<String>();
            for (var record : records) {
                allAdded.addAll(record.addedSubList());
            }
            assertThat(allAdded).containsExactly("beta", "gamma");
        }

        @Test
        void getRemoved_containsCorrectElementsAfterRemoval() {
            list.addAll("alpha", "beta", "gamma");
            allChanges.clear();

            list.remove(0, 2);

            var records = allChanges.getFirst();
            var allRemoved = new ArrayList<String>();
            for (var record : records) {
                allRemoved.addAll(record.removed());
            }
            assertThat(allRemoved).containsExactlyInAnyOrder("alpha", "beta");
        }
    }

    // =====================================================================
    // Multiple listeners
    // =====================================================================

    @Nested
    @DisplayName("Multiple listeners")
    class MultipleListeners {

        @Test
        void multipleListeners_allReceiveChanges() {
            var count1 = new AtomicInteger(0);
            var count2 = new AtomicInteger(0);

            list.addListener((ListChangeListener<String>) c -> count1.incrementAndGet());
            list.addListener((ListChangeListener<String>) c -> count2.incrementAndGet());

            list.add("alpha");

            assertThat(count1.get()).isEqualTo(1);
            assertThat(count2.get()).isEqualTo(1);
        }

        @Test
        void removeListener_stopsNotifications() {
            var fired = new AtomicBoolean(false);
            ListChangeListener<String> listener = c -> fired.set(true);
            list.addListener(listener);
            list.removeListener(listener);

            list.add("alpha");

            assertThat(fired.get()).isFalse();
        }

        @Test
        void removedListener_otherListenerStillReceives() {
            var count = new AtomicInteger(0);
            ListChangeListener<String> stayingListener = c -> count.incrementAndGet();
            ListChangeListener<String> removedListener = c -> {};

            list.addListener(stayingListener);
            list.addListener(removedListener);
            list.removeListener(removedListener);

            list.add("alpha");

            assertThat(count.get()).isEqualTo(1);
        }
    }

    // =====================================================================
    // InvalidationListener
    // =====================================================================

    @Nested
    @DisplayName("InvalidationListener support")
    class InvalidationListenerSupport {

        @Test
        void addElement_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            list.addListener((InvalidationListener) obs -> fired.set(true));

            list.add("alpha");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void removeElement_firesInvalidationListener() {
            list.add("alpha");
            var fired = new AtomicBoolean(false);
            list.addListener((InvalidationListener) obs -> fired.set(true));

            list.remove("alpha");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void setElement_firesInvalidationListener() {
            list.add("alpha");
            var fired = new AtomicBoolean(false);
            list.addListener((InvalidationListener) obs -> fired.set(true));

            list.set(0, "beta");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void clear_firesInvalidationListener() {
            list.addAll("alpha", "beta");
            var fired = new AtomicBoolean(false);
            list.addListener((InvalidationListener) obs -> fired.set(true));

            list.clear();

            assertThat(fired.get()).isTrue();
        }

        @Test
        void removeInvalidationListener_stopsNotifications() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            list.addListener(listener);
            list.removeListener(listener);

            list.add("alpha");

            assertThat(fired.get()).isFalse();
        }

        @Test
        void invalidationListenerReceivesCorrectObservable() {
            var capturedObservable = new AtomicBoolean(false);
            list.addListener((InvalidationListener) obs -> {
                capturedObservable.set(obs == list);
            });

            list.add("alpha");

            assertThat(capturedObservable.get()).isTrue();
        }
    }

    // =====================================================================
    // Transformation views
    // =====================================================================

    @Nested
    @DisplayName("Transformation views")
    class TransformationViews {

        @Test
        void filtered_returnsFilteredView() {
            list.addAll("alpha", "beta", "gamma", "delta");

            FilteredList<String> filtered = list.filtered(s -> s.startsWith("a") || s.startsWith("g"));

            assertThat(filtered).containsExactly("alpha", "gamma");
        }

        @Test
        void filtered_tracksSourceChanges() {
            list.addAll("alpha", "beta");
            FilteredList<String> filtered = list.filtered(s -> s.length() > 4);

            assertThat(filtered).containsExactly("alpha");

            list.add("gamma");
            assertThat(filtered).containsExactly("alpha", "gamma");
        }

        @Test
        void sorted_returnsSortedView() {
            list.addAll("gamma", "alpha", "beta");

            SortedList<String> sorted = list.sorted(Comparator.naturalOrder());

            assertThat(sorted).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void sorted_tracksSourceChanges() {
            list.addAll("gamma", "alpha");
            SortedList<String> sorted = list.sorted(Comparator.naturalOrder());

            assertThat(sorted).containsExactly("alpha", "gamma");

            list.add("beta");
            assertThat(sorted).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void sortedWithNaturalOrder_returnsSortedView() {
            list.addAll("gamma", "alpha", "beta");

            SortedList<String> sorted = list.sorted();

            assertThat(sorted).containsExactly("alpha", "beta", "gamma");
        }
    }

    // =====================================================================
    // Varargs and range operations
    // =====================================================================

    @Nested
    @DisplayName("Varargs and range operations")
    class VarargsAndRange {

        @Test
        void removeAllVarargs_removesSpecifiedElements() {
            list.addAll("alpha", "beta", "gamma", "delta");

            boolean result = list.removeAll("beta", "delta");

            assertThat(result).isTrue();
            assertThat(list).containsExactly("alpha", "gamma");
        }

        @Test
        void retainAllVarargs_keepsOnlySpecifiedElements() {
            list.addAll("alpha", "beta", "gamma", "delta");

            boolean result = list.retainAll("beta", "delta");

            assertThat(result).isTrue();
            assertThat(list).containsExactly("beta", "delta");
        }

        @Test
        void removeRange_removesElementsInRange() {
            list.addAll("alpha", "beta", "gamma", "delta");

            list.remove(1, 3);

            assertThat(list).containsExactly("alpha", "delta");
        }

        @Test
        void setAllOnEmpty_returnsTrueWhenNewElementsProvided() {
            boolean result = list.setAll("alpha", "beta");
            assertThat(result).isTrue();
            assertThat(list).containsExactly("alpha", "beta");
        }

        @Test
        void setAllBothEmpty_returnsFalse() {
            boolean result = list.setAll(List.of());
            assertThat(result).isFalse();
        }
    }
}

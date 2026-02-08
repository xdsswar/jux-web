package xss.it.jux.reactive.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.InvalidationListener;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("JuxCollections")
class JuxCollectionsTest {

    // =====================================================================
    // Observable list factories
    // =====================================================================

    @Nested
    @DisplayName("Observable list factories")
    class ListFactories {

        @Test
        void observableArrayList_createsEmptyList() {
            ObservableList<String> list = JuxCollections.observableArrayList();

            assertThat(list).isEmpty();
            assertThat(list.size()).isZero();
        }

        @Test
        void observableArrayListWithElements_containsElements() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma");

            assertThat(list).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void observableArrayListFromCollection_containsAllElements() {
            var source = List.of("alpha", "beta", "gamma");
            ObservableList<String> list = JuxCollections.observableArrayList(source);

            assertThat(list).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void observableArrayListFromCollection_isIndependentOfSource() {
            var source = new ArrayList<>(List.of("alpha", "beta"));
            ObservableList<String> list = JuxCollections.observableArrayList(source);

            source.add("gamma");

            assertThat(list).containsExactly("alpha", "beta");
        }

        @Test
        void observableList_wrapsBackingList() {
            var backing = new ArrayList<>(List.of("alpha", "beta"));
            ObservableList<String> list = JuxCollections.observableList(backing);

            assertThat(list).containsExactly("alpha", "beta");
        }

        @Test
        void observableList_nullBackingList_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.observableList(null));
        }

        @Test
        void observableArrayListWithElements_isMutable() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha");
            list.add("beta");

            assertThat(list).containsExactly("alpha", "beta");
        }

        @Test
        void observableArrayListWithElements_supportsListeners() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha");
            var fired = new AtomicBoolean(false);
            list.addListener((ListChangeListener<String>) c -> fired.set(true));

            list.add("beta");

            assertThat(fired.get()).isTrue();
        }
    }

    // =====================================================================
    // Observable map factories
    // =====================================================================

    @Nested
    @DisplayName("Observable map factories")
    class MapFactories {

        @Test
        void observableHashMap_createsEmptyMap() {
            ObservableMap<String, Integer> map = JuxCollections.observableHashMap();

            assertThat(map).isEmpty();
        }

        @Test
        void observableMap_wrapsBackingMap() {
            var backing = new java.util.HashMap<String, Integer>();
            backing.put("alpha", 1);
            ObservableMap<String, Integer> map = JuxCollections.observableMap(backing);

            assertThat(map).containsEntry("alpha", 1);
        }

        @Test
        void observableMap_nullBackingMap_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.observableMap(null));
        }

        @Test
        void observableHashMap_isMutable() {
            ObservableMap<String, Integer> map = JuxCollections.observableHashMap();
            map.put("alpha", 1);

            assertThat(map).containsEntry("alpha", 1);
        }

        @Test
        void observableHashMap_supportsListeners() {
            ObservableMap<String, Integer> map = JuxCollections.observableHashMap();
            var fired = new AtomicBoolean(false);
            map.addListener((MapChangeListener<String, Integer>) c -> fired.set(true));

            map.put("alpha", 1);

            assertThat(fired.get()).isTrue();
        }
    }

    // =====================================================================
    // Observable set factories
    // =====================================================================

    @Nested
    @DisplayName("Observable set factories")
    class SetFactories {

        @Test
        void observableSetFromSet_createsObservableSet() {
            var backing = new java.util.LinkedHashSet<String>();
            backing.add("alpha");
            backing.add("beta");
            ObservableSet<String> set = JuxCollections.observableSet(backing);

            assertThat(set).containsExactly("alpha", "beta");
        }

        @Test
        void observableSetVarargs_createsSetWithElements() {
            ObservableSet<String> set = JuxCollections.observableSet("alpha", "beta", "gamma");

            assertThat(set).containsExactlyInAnyOrder("alpha", "beta", "gamma");
        }

        @Test
        void observableSet_nullBackingSet_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.observableSet((java.util.Set<String>) null));
        }
    }

    // =====================================================================
    // Unmodifiable wrappers
    // =====================================================================

    @Nested
    @DisplayName("Unmodifiable observable list")
    class UnmodifiableList {

        private ObservableList<String> backing;
        private ObservableList<String> unmodifiable;

        @BeforeEach
        void setUp() {
            backing = JuxCollections.observableArrayList("alpha", "beta", "gamma");
            unmodifiable = JuxCollections.unmodifiableObservableList(backing);
        }

        @Test
        void unmodifiableList_reflectsBackingListContent() {
            assertThat(unmodifiable).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void add_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.add("delta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void remove_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.remove(0))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void set_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.set(0, "delta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void clear_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void setAll_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.setAll("delta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void addAll_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.addAll("delta", "epsilon"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void removeRange_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.remove(0, 1))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void backingListChange_forwardsToUnmodifiableListeners() {
            var fired = new AtomicBoolean(false);
            unmodifiable.addListener((ListChangeListener<String>) c -> fired.set(true));

            backing.add("delta");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void backingListChange_unmodifiableReflectsNewState() {
            backing.add("delta");

            assertThat(unmodifiable).containsExactly("alpha", "beta", "gamma", "delta");
        }

        @Test
        void backingListRemoval_forwardsChangeWithRemovedElements() {
            var removedItems = new ArrayList<String>();
            unmodifiable.addListener((ListChangeListener<String>) c -> {
                while (c.next()) {
                    removedItems.addAll(c.getRemoved());
                }
            });

            backing.remove("beta");

            assertThat(removedItems).containsExactly("beta");
        }

        @Test
        void nullArgument_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.unmodifiableObservableList(null));
        }
    }

    @Nested
    @DisplayName("Unmodifiable observable map")
    class UnmodifiableMap {

        private ObservableMap<String, Integer> backing;
        private ObservableMap<String, Integer> unmodifiable;

        @BeforeEach
        void setUp() {
            backing = JuxCollections.observableHashMap();
            backing.put("alpha", 1);
            backing.put("beta", 2);
            unmodifiable = JuxCollections.unmodifiableObservableMap(backing);
        }

        @Test
        void unmodifiableMap_reflectsBackingMapContent() {
            assertThat(unmodifiable).containsEntry("alpha", 1).containsEntry("beta", 2);
        }

        @Test
        void put_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.put("gamma", 3))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void remove_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.remove("alpha"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void clear_throwsUnsupportedOperationException() {
            assertThatThrownBy(() -> unmodifiable.clear())
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void backingMapChange_forwardsToUnmodifiableListeners() {
            var fired = new AtomicBoolean(false);
            unmodifiable.addListener((MapChangeListener<String, Integer>) c -> fired.set(true));

            backing.put("gamma", 3);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void nullArgument_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.unmodifiableObservableMap(null));
        }
    }

    // =====================================================================
    // Empty collections
    // =====================================================================

    @Nested
    @DisplayName("Empty observable collections")
    class EmptyCollections {

        @Test
        void emptyObservableList_isEmpty() {
            ObservableList<String> empty = JuxCollections.emptyObservableList();
            assertThat(empty).isEmpty();
            assertThat(empty.size()).isZero();
        }

        @Test
        void emptyObservableList_isImmutable() {
            ObservableList<String> empty = JuxCollections.emptyObservableList();
            assertThatThrownBy(() -> empty.add("alpha"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void emptyObservableList_getThrowsIndexOutOfBounds() {
            ObservableList<String> empty = JuxCollections.emptyObservableList();
            assertThatThrownBy(() -> empty.get(0))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        void emptyObservableList_setAllThrowsUnsupported() {
            ObservableList<String> empty = JuxCollections.emptyObservableList();
            assertThatThrownBy(() -> empty.setAll("alpha"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void emptyObservableList_addAllThrowsUnsupported() {
            ObservableList<String> empty = JuxCollections.emptyObservableList();
            assertThatThrownBy(() -> empty.addAll("alpha", "beta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void emptyObservableList_isSingleton() {
            ObservableList<String> empty1 = JuxCollections.emptyObservableList();
            ObservableList<String> empty2 = JuxCollections.emptyObservableList();
            assertThat(empty1).isSameAs(empty2);
        }

        @Test
        void emptyObservableMap_isEmpty() {
            ObservableMap<String, Integer> empty = JuxCollections.emptyObservableMap();
            assertThat(empty).isEmpty();
        }

        @Test
        void emptyObservableMap_isSingleton() {
            ObservableMap<String, Integer> empty1 = JuxCollections.emptyObservableMap();
            ObservableMap<String, Integer> empty2 = JuxCollections.emptyObservableMap();
            assertThat(empty1).isSameAs(empty2);
        }

        @Test
        void emptyObservableSet_isEmpty() {
            ObservableSet<String> empty = JuxCollections.emptyObservableSet();
            assertThat(empty).isEmpty();
        }

        @Test
        void emptyObservableSet_isSingleton() {
            ObservableSet<String> empty1 = JuxCollections.emptyObservableSet();
            ObservableSet<String> empty2 = JuxCollections.emptyObservableSet();
            assertThat(empty1).isSameAs(empty2);
        }
    }

    // =====================================================================
    // Singleton observable list
    // =====================================================================

    @Nested
    @DisplayName("Singleton observable list")
    class SingletonList {

        @Test
        void singletonList_containsOneElement() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThat(list).containsExactly("alpha");
            assertThat(list.size()).isEqualTo(1);
        }

        @Test
        void singletonList_getReturnsElement() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThat(list.get(0)).isEqualTo("alpha");
        }

        @Test
        void singletonList_getOutOfBounds_throwsException() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThatThrownBy(() -> list.get(1))
                    .isInstanceOf(IndexOutOfBoundsException.class);
        }

        @Test
        void singletonList_isImmutable() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThatThrownBy(() -> list.add("beta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void singletonList_nullElement_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.singletonObservableList(null));
        }

        @Test
        void singletonList_containsReturnsTrueForElement() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThat(list.contains("alpha")).isTrue();
            assertThat(list.contains("beta")).isFalse();
        }

        @Test
        void singletonList_isNotEmpty() {
            ObservableList<String> list = JuxCollections.singletonObservableList("alpha");
            assertThat(list.isEmpty()).isFalse();
        }
    }

    // =====================================================================
    // Sort operation
    // =====================================================================

    @Nested
    @DisplayName("Sort operation")
    class SortOperation {

        private ObservableList<String> list;

        @BeforeEach
        void setUp() {
            list = JuxCollections.observableArrayList("gamma", "alpha", "delta", "beta");
        }

        @Test
        void sortWithComparator_sortsElements() {
            JuxCollections.sort(list, Comparator.naturalOrder());

            assertThat(list).containsExactly("alpha", "beta", "delta", "gamma");
        }

        @Test
        void sortNaturalOrder_sortsElements() {
            JuxCollections.sort(list);

            assertThat(list).containsExactly("alpha", "beta", "delta", "gamma");
        }

        @Test
        void sort_firesSingleChange() {
            var changeCount = new AtomicInteger(0);
            list.addListener((ListChangeListener<String>) c -> changeCount.incrementAndGet());

            JuxCollections.sort(list, Comparator.naturalOrder());

            assertThat(changeCount.get()).isEqualTo(1);
        }

        @Test
        void sortReverseOrder_sortsDescending() {
            JuxCollections.sort(list, Comparator.reverseOrder());

            assertThat(list).containsExactly("gamma", "delta", "beta", "alpha");
        }

        @Test
        void sortAlreadySorted_maintainsOrder() {
            var sorted = JuxCollections.observableArrayList("alpha", "beta", "gamma");
            JuxCollections.sort(sorted, Comparator.naturalOrder());

            assertThat(sorted).containsExactly("alpha", "beta", "gamma");
        }
    }

    // =====================================================================
    // Reverse operation
    // =====================================================================

    @Nested
    @DisplayName("Reverse operation")
    class ReverseOperation {

        @Test
        void reverse_reversesElements() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma");

            JuxCollections.reverse(list);

            assertThat(list).containsExactly("gamma", "beta", "alpha");
        }

        @Test
        void reverse_firesSingleChange() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma");
            var changeCount = new AtomicInteger(0);
            list.addListener((ListChangeListener<String>) c -> changeCount.incrementAndGet());

            JuxCollections.reverse(list);

            assertThat(changeCount.get()).isEqualTo(1);
        }

        @Test
        void reverseSingleElement_noChange() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha");
            JuxCollections.reverse(list);

            assertThat(list).containsExactly("alpha");
        }

        @Test
        void reverseEmpty_noChange() {
            ObservableList<String> list = JuxCollections.observableArrayList();
            JuxCollections.reverse(list);

            assertThat(list).isEmpty();
        }
    }

    // =====================================================================
    // Shuffle operation
    // =====================================================================

    @Nested
    @DisplayName("Shuffle operation")
    class ShuffleOperation {

        @Test
        void shuffle_containsSameElements() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma", "delta");

            JuxCollections.shuffle(list);

            assertThat(list).containsExactlyInAnyOrder("alpha", "beta", "gamma", "delta");
        }

        @Test
        void shuffleWithRandom_containsSameElements() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma");

            JuxCollections.shuffle(list, new java.util.Random(42));

            assertThat(list).containsExactlyInAnyOrder("alpha", "beta", "gamma");
        }

        @Test
        void shuffle_firesSingleChange() {
            ObservableList<String> list = JuxCollections.observableArrayList("alpha", "beta", "gamma", "delta");
            var changeCount = new AtomicInteger(0);
            list.addListener((ListChangeListener<String>) c -> changeCount.incrementAndGet());

            JuxCollections.shuffle(list, new java.util.Random(42));

            assertThat(changeCount.get()).isEqualTo(1);
        }
    }

    // =====================================================================
    // Concatenation
    // =====================================================================

    @Nested
    @DisplayName("Concat operation")
    class ConcatOperation {

        @Test
        void concatTwoLists_combinesElements() {
            ObservableList<String> list1 = JuxCollections.observableArrayList("alpha", "beta");
            ObservableList<String> list2 = JuxCollections.observableArrayList("gamma", "delta");

            ObservableList<String> result = JuxCollections.concat(list1, list2);

            assertThat(result).containsExactly("alpha", "beta", "gamma", "delta");
        }

        @Test
        void concatThreeLists_combinesAllElements() {
            ObservableList<String> list1 = JuxCollections.observableArrayList("alpha");
            ObservableList<String> list2 = JuxCollections.observableArrayList("beta");
            ObservableList<String> list3 = JuxCollections.observableArrayList("gamma");

            ObservableList<String> result = JuxCollections.concat(list1, list2, list3);

            assertThat(result).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void concatEmpty_returnsEmptyList() {
            @SuppressWarnings("unchecked")
            ObservableList<String> result = JuxCollections.concat();

            assertThat(result).isEmpty();
        }

        @Test
        void concatSingleList_returnsCopy() {
            ObservableList<String> source = JuxCollections.observableArrayList("alpha", "beta");
            ObservableList<String> result = JuxCollections.concat(source);

            assertThat(result).containsExactly("alpha", "beta");
            // Result should be independent
            source.add("gamma");
            assertThat(result).containsExactly("alpha", "beta");
        }

        @Test
        void concatResult_isMutable() {
            ObservableList<String> list1 = JuxCollections.observableArrayList("alpha");
            ObservableList<String> list2 = JuxCollections.observableArrayList("beta");
            ObservableList<String> result = JuxCollections.concat(list1, list2);

            result.add("gamma");

            assertThat(result).containsExactly("alpha", "beta", "gamma");
        }

        @Test
        void concatResult_isIndependentOfSources() {
            ObservableList<String> list1 = JuxCollections.observableArrayList("alpha");
            ObservableList<String> list2 = JuxCollections.observableArrayList("beta");
            ObservableList<String> result = JuxCollections.concat(list1, list2);

            list1.add("gamma");

            // concat creates a new list, not a live view
            assertThat(result).containsExactly("alpha", "beta");
        }

        @Test
        void concatWithEmptyLists_returnsNonEmptyResult() {
            ObservableList<String> empty = JuxCollections.observableArrayList();
            ObservableList<String> nonEmpty = JuxCollections.observableArrayList("alpha");

            ObservableList<String> result = JuxCollections.concat(empty, nonEmpty, empty);

            assertThat(result).containsExactly("alpha");
        }
    }

    // =====================================================================
    // Unmodifiable set wrapper
    // =====================================================================

    @Nested
    @DisplayName("Unmodifiable observable set")
    class UnmodifiableSet {

        @Test
        void unmodifiableSet_reflectsBackingContent() {
            ObservableSet<String> backing = JuxCollections.observableSet("alpha", "beta");
            ObservableSet<String> unmodifiable = JuxCollections.unmodifiableObservableSet(backing);

            assertThat(unmodifiable).containsExactlyInAnyOrder("alpha", "beta");
        }

        @Test
        void unmodifiableSet_addThrowsUnsupported() {
            ObservableSet<String> backing = JuxCollections.observableSet("alpha");
            ObservableSet<String> unmodifiable = JuxCollections.unmodifiableObservableSet(backing);

            assertThatThrownBy(() -> unmodifiable.add("beta"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void unmodifiableSet_removeThrowsUnsupported() {
            ObservableSet<String> backing = JuxCollections.observableSet("alpha");
            ObservableSet<String> unmodifiable = JuxCollections.unmodifiableObservableSet(backing);

            assertThatThrownBy(() -> unmodifiable.remove("alpha"))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void nullArgument_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> JuxCollections.unmodifiableObservableSet(null));
        }
    }
}

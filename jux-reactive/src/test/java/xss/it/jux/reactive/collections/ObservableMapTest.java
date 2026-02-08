package xss.it.jux.reactive.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.InvalidationListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@DisplayName("ObservableMap")
class ObservableMapTest {

    private ObservableMap<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = JuxCollections.observableHashMap();
    }

    // =====================================================================
    // Helper: records MapChangeListener.Change events
    // =====================================================================

    record MapChangeRecord<K, V>(
            K key,
            boolean wasAdded,
            boolean wasRemoved,
            V valueAdded,
            V valueRemoved
    ) {}

    private final List<MapChangeRecord<String, Integer>> changes = new ArrayList<>();

    private void attachListener() {
        map.addListener((MapChangeListener<String, Integer>) c ->
                changes.add(new MapChangeRecord<>(
                        c.getKey(),
                        c.wasAdded(),
                        c.wasRemoved(),
                        c.getValueAdded(),
                        c.getValueRemoved()
                ))
        );
    }

    // =====================================================================
    // Basic Map Operations
    // =====================================================================

    @Nested
    @DisplayName("Basic map operations")
    class BasicOperations {

        @Test
        void emptyMap_hasSizeZero() {
            assertThat(map).isEmpty();
            assertThat(map.size()).isZero();
        }

        @Test
        void putElement_increasesSize() {
            map.put("alpha", 1);
            assertThat(map).hasSize(1);
            assertThat(map.get("alpha")).isEqualTo(1);
        }

        @Test
        void putMultipleElements_allRetrievable() {
            map.put("alpha", 1);
            map.put("beta", 2);
            map.put("gamma", 3);

            assertThat(map).hasSize(3);
            assertThat(map.get("alpha")).isEqualTo(1);
            assertThat(map.get("beta")).isEqualTo(2);
            assertThat(map.get("gamma")).isEqualTo(3);
        }

        @Test
        void putReplace_returnsOldValue() {
            map.put("alpha", 1);
            Integer old = map.put("alpha", 2);

            assertThat(old).isEqualTo(1);
            assertThat(map.get("alpha")).isEqualTo(2);
        }

        @Test
        void remove_removesEntry() {
            map.put("alpha", 1);
            Integer removed = map.remove("alpha");

            assertThat(removed).isEqualTo(1);
            assertThat(map).doesNotContainKey("alpha");
        }

        @Test
        void removeNonExistent_returnsNull() {
            Integer removed = map.remove("nonexistent");
            assertThat(removed).isNull();
        }

        @Test
        void clear_removesAllEntries() {
            map.put("alpha", 1);
            map.put("beta", 2);
            map.clear();

            assertThat(map).isEmpty();
        }

        @Test
        void containsKey_returnsTrueForExistingKey() {
            map.put("alpha", 1);
            assertThat(map.containsKey("alpha")).isTrue();
            assertThat(map.containsKey("beta")).isFalse();
        }

        @Test
        void containsValue_returnsTrueForExistingValue() {
            map.put("alpha", 42);
            assertThat(map.containsValue(42)).isTrue();
            assertThat(map.containsValue(99)).isFalse();
        }
    }

    // =====================================================================
    // MapChangeListener notifications
    // =====================================================================

    @Nested
    @DisplayName("MapChangeListener notifications")
    class ChangeNotifications {

        @BeforeEach
        void setUpListener() {
            attachListener();
        }

        @Test
        void putNewKey_firesChangeWithWasAdded() {
            map.put("alpha", 1);

            assertThat(changes).hasSize(1);
            var change = changes.getFirst();
            assertThat(change.wasAdded()).isTrue();
            assertThat(change.wasRemoved()).isFalse();
            assertThat(change.key()).isEqualTo("alpha");
            assertThat(change.valueAdded()).isEqualTo(1);
        }

        @Test
        void putReplacingValue_firesChangeWithWasAddedAndWasRemoved() {
            map.put("alpha", 1);
            changes.clear();

            map.put("alpha", 2);

            assertThat(changes).hasSize(1);
            var change = changes.getFirst();
            assertThat(change.wasAdded()).isTrue();
            assertThat(change.wasRemoved()).isTrue();
            assertThat(change.key()).isEqualTo("alpha");
            assertThat(change.valueAdded()).isEqualTo(2);
            assertThat(change.valueRemoved()).isEqualTo(1);
        }

        @Test
        void putSameValue_doesNotFireChange() {
            map.put("alpha", 1);
            changes.clear();

            map.put("alpha", 1);

            // Same value = no change fired
            assertThat(changes).isEmpty();
        }

        @Test
        void removeExistingKey_firesChangeWithWasRemoved() {
            map.put("alpha", 1);
            changes.clear();

            map.remove("alpha");

            assertThat(changes).hasSize(1);
            var change = changes.getFirst();
            assertThat(change.wasAdded()).isFalse();
            assertThat(change.wasRemoved()).isTrue();
            assertThat(change.key()).isEqualTo("alpha");
            assertThat(change.valueRemoved()).isEqualTo(1);
        }

        @Test
        void removeNonExistentKey_doesNotFireChange() {
            map.remove("nonexistent");

            assertThat(changes).isEmpty();
        }

        @Test
        void clear_firesChangeForEachRemovedEntry() {
            map.put("alpha", 1);
            map.put("beta", 2);
            map.put("gamma", 3);
            changes.clear();

            map.clear();

            assertThat(changes).hasSize(3);
            for (var change : changes) {
                assertThat(change.wasRemoved()).isTrue();
                assertThat(change.wasAdded()).isFalse();
            }
            var removedKeys = changes.stream().map(MapChangeRecord::key).toList();
            assertThat(removedKeys).containsExactlyInAnyOrder("alpha", "beta", "gamma");
        }

        @Test
        void putAll_firesChangeForEachEntry() {
            map.putAll(Map.of("alpha", 1, "beta", 2));

            assertThat(changes).hasSize(2);
            var addedKeys = changes.stream().map(MapChangeRecord::key).toList();
            assertThat(addedKeys).containsExactlyInAnyOrder("alpha", "beta");
        }

        @Test
        void getKey_returnsCorrectKeyOnChange() {
            map.put("alpha", 1);

            assertThat(changes.getFirst().key()).isEqualTo("alpha");
        }

        @Test
        void getValueAdded_returnsCorrectValueOnPut() {
            map.put("alpha", 42);

            assertThat(changes.getFirst().valueAdded()).isEqualTo(42);
        }

        @Test
        void getValueRemoved_returnsCorrectValueOnRemove() {
            map.put("alpha", 42);
            changes.clear();

            map.remove("alpha");

            assertThat(changes.getFirst().valueRemoved()).isEqualTo(42);
        }

        @Test
        void getValueRemoved_returnsNullForNewKey() {
            map.put("alpha", 1);

            assertThat(changes.getFirst().valueRemoved()).isNull();
        }

        @Test
        void getValueAdded_returnsNullForRemoval() {
            map.put("alpha", 1);
            changes.clear();

            map.remove("alpha");

            assertThat(changes.getFirst().valueAdded()).isNull();
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

            map.addListener((MapChangeListener<String, Integer>) c -> count1.incrementAndGet());
            map.addListener((MapChangeListener<String, Integer>) c -> count2.incrementAndGet());

            map.put("alpha", 1);

            assertThat(count1.get()).isEqualTo(1);
            assertThat(count2.get()).isEqualTo(1);
        }

        @Test
        void removeListener_stopsNotifications() {
            var fired = new AtomicBoolean(false);
            MapChangeListener<String, Integer> listener = c -> fired.set(true);
            map.addListener(listener);
            map.removeListener(listener);

            map.put("alpha", 1);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void removedListener_otherListenerStillReceives() {
            var count = new AtomicInteger(0);
            MapChangeListener<String, Integer> stayingListener = c -> count.incrementAndGet();
            MapChangeListener<String, Integer> removedListener = c -> {};

            map.addListener(stayingListener);
            map.addListener(removedListener);
            map.removeListener(removedListener);

            map.put("alpha", 1);

            assertThat(count.get()).isEqualTo(1);
        }
    }

    // =====================================================================
    // InvalidationListener support
    // =====================================================================

    @Nested
    @DisplayName("InvalidationListener support")
    class InvalidationListenerSupport {

        @Test
        void putElement_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            map.addListener((InvalidationListener) obs -> fired.set(true));

            map.put("alpha", 1);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void removeElement_firesInvalidationListener() {
            map.put("alpha", 1);
            var fired = new AtomicBoolean(false);
            map.addListener((InvalidationListener) obs -> fired.set(true));

            map.remove("alpha");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void clear_firesInvalidationListener() {
            map.put("alpha", 1);
            var count = new AtomicInteger(0);
            map.addListener((InvalidationListener) obs -> count.incrementAndGet());

            map.clear();

            assertThat(count.get()).isGreaterThanOrEqualTo(1);
        }

        @Test
        void removeInvalidationListener_stopsNotifications() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            map.addListener(listener);
            map.removeListener(listener);

            map.put("alpha", 1);

            assertThat(fired.get()).isFalse();
        }
    }

    // =====================================================================
    // Change getMap() reference
    // =====================================================================

    @Nested
    @DisplayName("Change getMap() reference")
    class ChangeMapReference {

        @Test
        void changeGetMap_returnsSourceMap() {
            var capturedMap = new ArrayList<ObservableMap<?, ?>>();
            map.addListener((MapChangeListener<String, Integer>) c -> capturedMap.add(c.getMap()));

            map.put("alpha", 1);

            assertThat(capturedMap).hasSize(1);
            assertThat(capturedMap.getFirst()).isSameAs(map);
        }
    }
}

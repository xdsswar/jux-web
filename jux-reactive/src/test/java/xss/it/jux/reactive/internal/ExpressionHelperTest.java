package xss.it.jux.reactive.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleObjectProperty;
import xss.it.jux.reactive.value.ObservableValue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link ExpressionHelper} — the core listener management engine
 * that supports three internal strategies: SingleInvalidation, SingleChange,
 * and Generic.
 */
@DisplayName("ExpressionHelper")
class ExpressionHelperTest {

    private SimpleObjectProperty<String> property;

    @BeforeEach
    void setUp() {
        property = new SimpleObjectProperty<>("initial");
    }

    // ═══════════════════════════════════════════════════════════════
    //  Static method null handling
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Static method null handling")
    class NullHandling {

        @Test
        @DisplayName("addListener with null observable throws NPE for invalidation")
        void addInvalidationListener_nullObservable_throwsNpe() {
            InvalidationListener listener = obs -> {};
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.addListener(null, null, listener));
        }

        @Test
        @DisplayName("addListener with null invalidation listener throws NPE")
        void addInvalidationListener_nullListener_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.addListener(
                            null, property, (InvalidationListener) null));
        }

        @Test
        @DisplayName("addListener with null observable throws NPE for change listener")
        void addChangeListener_nullObservable_throwsNpe() {
            ChangeListener<String> listener = (obs, o, n) -> {};
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.addListener(null, null, listener));
        }

        @Test
        @DisplayName("addListener with null change listener throws NPE")
        void addChangeListener_nullListener_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.addListener(
                            null, property, (ChangeListener<String>) null));
        }

        @Test
        @DisplayName("removeListener on null helper returns null for invalidation")
        void removeInvalidationListener_nullHelper_returnsNull() {
            InvalidationListener listener = obs -> {};
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(null, listener);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("removeListener on null helper returns null for change listener")
        void removeChangeListener_nullHelper_returnsNull() {
            ChangeListener<String> listener = (obs, o, n) -> {};
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(null, listener);
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("removeListener with null invalidation listener throws NPE")
        void removeInvalidationListener_nullListener_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.removeListener(
                            null, (InvalidationListener) null));
        }

        @Test
        @DisplayName("removeListener with null change listener throws NPE")
        void removeChangeListener_nullListener_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> ExpressionHelper.removeListener(
                            null, (ChangeListener<String>) null));
        }

        @Test
        @DisplayName("fireValueChangedEvent on null helper is a no-op")
        void fireValueChangedEvent_nullHelper_isNoOp() {
            assertThatNoException()
                    .isThrownBy(() -> ExpressionHelper.fireValueChangedEvent(null));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SingleInvalidation strategy
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SingleInvalidation strategy")
    class SingleInvalidation {

        @Test
        @DisplayName("first invalidation listener creates helper")
        void firstInvalidationListener_createsHelper() {
            InvalidationListener listener = obs -> {};
            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            assertThat(helper).isNotNull();
        }

        @Test
        @DisplayName("fires invalidation listener on value change")
        void firesInvalidationListener_onValueChange() {
            List<Observable> fired = new ArrayList<>();
            InvalidationListener listener = fired::add;

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            property.set("changed");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(fired).hasSize(1);
            assertThat(fired.getFirst()).isSameAs(property);
        }

        @Test
        @DisplayName("removing the only invalidation listener returns null")
        void removingOnlyListener_returnsNull() {
            InvalidationListener listener = obs -> {};
            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, listener);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("removing non-matching invalidation listener returns same helper")
        void removingNonMatchingListener_returnsSameHelper() {
            InvalidationListener listener = obs -> {};
            InvalidationListener other = obs -> {};
            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, other);

            assertThat(result).isSameAs(helper);
        }

        @Test
        @DisplayName("removing change listener from single-invalidation returns same helper")
        void removingChangeListener_returnsSameHelper() {
            InvalidationListener invListener = obs -> {};
            ChangeListener<String> chgListener = (obs, o, n) -> {};

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, invListener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, chgListener);

            assertThat(result).isSameAs(helper);
        }

        @Test
        @DisplayName("adding second invalidation listener transitions to Generic")
        void addingSecondInvalidationListener_transitionsToGeneric() {
            List<String> fired = new ArrayList<>();
            InvalidationListener listener1 = obs -> fired.add("first");
            InvalidationListener listener2 = obs -> fired.add("second");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener1);
            helper = ExpressionHelper.addListener(helper, property, listener2);

            property.set("updated");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(fired).containsExactly("first", "second");
        }

        @Test
        @DisplayName("adding change listener to single-invalidation transitions to Generic")
        void addingChangeListener_transitionsToGeneric() {
            List<String> log = new ArrayList<>();
            InvalidationListener invListener = obs -> log.add("invalidated");
            ChangeListener<String> chgListener = (obs, o, n) -> log.add("changed:" + o + "->" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, invListener);
            helper = ExpressionHelper.addListener(helper, property, chgListener);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("invalidated", "changed:initial->newval");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SingleChange strategy
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SingleChange strategy")
    class SingleChange {

        @Test
        @DisplayName("first change listener creates helper")
        void firstChangeListener_createsHelper() {
            ChangeListener<String> listener = (obs, o, n) -> {};
            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            assertThat(helper).isNotNull();
        }

        @Test
        @DisplayName("fires change listener when value changes")
        void firesChangeListener_whenValueChanges() {
            List<String> changes = new ArrayList<>();
            ChangeListener<String> listener = (obs, o, n) -> changes.add(o + "->" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            property.set("updated");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(changes).containsExactly("initial->updated");
        }

        @Test
        @DisplayName("does not fire change listener when value unchanged")
        void doesNotFireChangeListener_whenValueUnchanged() {
            List<String> changes = new ArrayList<>();
            ChangeListener<String> listener = (obs, o, n) -> changes.add(o + "->" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            /* Value stays "initial" — no actual change. */
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(changes).isEmpty();
        }

        @Test
        @DisplayName("removing the only change listener returns null")
        void removingOnlyChangeListener_returnsNull() {
            ChangeListener<String> listener = (obs, o, n) -> {};
            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, listener);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("removing non-matching change listener returns same helper")
        void removingNonMatchingChangeListener_returnsSameHelper() {
            ChangeListener<String> listener = (obs, o, n) -> {};
            ChangeListener<String> other = (obs, o, n) -> {};

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, other);

            assertThat(result).isSameAs(helper);
        }

        @Test
        @DisplayName("removing invalidation listener from single-change returns same helper")
        void removingInvalidationListener_returnsSameHelper() {
            ChangeListener<String> chgListener = (obs, o, n) -> {};
            InvalidationListener invListener = obs -> {};

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, chgListener);
            ExpressionHelper<String> result =
                    ExpressionHelper.removeListener(helper, invListener);

            assertThat(result).isSameAs(helper);
        }

        @Test
        @DisplayName("adding second change listener transitions to Generic")
        void addingSecondChangeListener_transitionsToGeneric() {
            List<String> log = new ArrayList<>();
            ChangeListener<String> listener1 = (obs, o, n) -> log.add("first:" + n);
            ChangeListener<String> listener2 = (obs, o, n) -> log.add("second:" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, listener1);
            helper = ExpressionHelper.addListener(helper, property, listener2);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("first:newval", "second:newval");
        }

        @Test
        @DisplayName("adding invalidation listener to single-change transitions to Generic")
        void addingInvalidationListener_transitionsToGeneric() {
            List<String> log = new ArrayList<>();
            ChangeListener<String> chgListener = (obs, o, n) -> log.add("changed:" + n);
            InvalidationListener invListener = obs -> log.add("invalidated");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, chgListener);
            helper = ExpressionHelper.addListener(helper, property, invListener);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("invalidated", "changed:newval");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Generic strategy
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Generic strategy")
    class Generic {

        @Test
        @DisplayName("fires all invalidation listeners")
        void firesAllInvalidationListeners() {
            List<String> log = new ArrayList<>();
            InvalidationListener l1 = obs -> log.add("inv1");
            InvalidationListener l2 = obs -> log.add("inv2");
            InvalidationListener l3 = obs -> log.add("inv3");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, l1);
            helper = ExpressionHelper.addListener(helper, property, l2);
            helper = ExpressionHelper.addListener(helper, property, l3);

            property.set("changed");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("inv1", "inv2", "inv3");
        }

        @Test
        @DisplayName("fires all change listeners when value changes")
        void firesAllChangeListeners_whenValueChanges() {
            List<String> log = new ArrayList<>();
            ChangeListener<String> l1 = (obs, o, n) -> log.add("chg1:" + n);
            ChangeListener<String> l2 = (obs, o, n) -> log.add("chg2:" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, l1);
            helper = ExpressionHelper.addListener(helper, property, l2);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("chg1:newval", "chg2:newval");
        }

        @Test
        @DisplayName("does not fire change listeners when value unchanged")
        void doesNotFireChangeListeners_whenValueUnchanged() {
            List<String> log = new ArrayList<>();
            InvalidationListener inv = obs -> log.add("inv");
            ChangeListener<String> chg = (obs, o, n) -> log.add("chg");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, inv);
            helper = ExpressionHelper.addListener(helper, property, chg);

            /* Fire without changing the value. */
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("inv");
        }

        @Test
        @DisplayName("fires invalidation listeners before change listeners")
        void firesInvalidationBeforeChange() {
            List<String> order = new ArrayList<>();
            InvalidationListener inv = obs -> order.add("inv");
            ChangeListener<String> chg = (obs, o, n) -> order.add("chg");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, inv);
            helper = ExpressionHelper.addListener(helper, property, chg);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(order).containsExactly("inv", "chg");
        }

        @Test
        @DisplayName("exception in one listener does not prevent others from firing")
        void exceptionInListener_doesNotPreventOthers() {
            List<String> log = new ArrayList<>();

            /* Capture uncaught exceptions so they don't pollute test output. */
            AtomicReference<Throwable> caught = new AtomicReference<>();
            Thread.UncaughtExceptionHandler originalHandler =
                    Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> caught.set(e));

            try {
                InvalidationListener throwing = obs -> { throw new RuntimeException("boom"); };
                InvalidationListener healthy = obs -> log.add("survived");

                ExpressionHelper<String> helper =
                        ExpressionHelper.addListener(null, property, throwing);
                helper = ExpressionHelper.addListener(helper, property, healthy);

                property.set("trigger");
                ExpressionHelper.fireValueChangedEvent(helper);

                assertThat(log).containsExactly("survived");
                assertThat(caught.get()).isInstanceOf(RuntimeException.class)
                        .hasMessage("boom");
            } finally {
                Thread.currentThread().setUncaughtExceptionHandler(originalHandler);
            }
        }

        @Test
        @DisplayName("exception in change listener does not prevent others from firing")
        void exceptionInChangeListener_doesNotPreventOthers() {
            List<String> log = new ArrayList<>();

            AtomicReference<Throwable> caught = new AtomicReference<>();
            Thread.UncaughtExceptionHandler originalHandler =
                    Thread.currentThread().getUncaughtExceptionHandler();
            Thread.currentThread().setUncaughtExceptionHandler((t, e) -> caught.set(e));

            try {
                ChangeListener<String> throwing = (obs, o, n) -> {
                    throw new RuntimeException("change-boom");
                };
                ChangeListener<String> healthy = (obs, o, n) -> log.add("survived:" + n);

                ExpressionHelper<String> helper =
                        ExpressionHelper.addListener(null, property, throwing);
                helper = ExpressionHelper.addListener(helper, property, healthy);

                property.set("trigger");
                ExpressionHelper.fireValueChangedEvent(helper);

                assertThat(log).containsExactly("survived:trigger");
                assertThat(caught.get()).isInstanceOf(RuntimeException.class)
                        .hasMessage("change-boom");
            } finally {
                Thread.currentThread().setUncaughtExceptionHandler(originalHandler);
            }
        }

        @Test
        @DisplayName("removing from Generic leaving 1 invalidation + 0 change returns SingleInvalidation")
        void removingToOneInvalidation_returnsSingleInvalidation() {
            List<String> log = new ArrayList<>();
            InvalidationListener l1 = obs -> log.add("l1");
            InvalidationListener l2 = obs -> log.add("l2");

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, l1);
            helper = ExpressionHelper.addListener(helper, property, l2);

            /* Now in Generic with 2 invalidation, 0 change. Remove l1. */
            helper = ExpressionHelper.removeListener(helper, l1);

            /* Should be back to SingleInvalidation — fire and verify only l2 fires. */
            property.set("after-remove");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("l2");
        }

        @Test
        @DisplayName("removing from Generic leaving 0 invalidation + 1 change returns SingleChange")
        void removingToOneChange_returnsSingleChange() {
            List<String> log = new ArrayList<>();
            InvalidationListener inv = obs -> log.add("inv");
            ChangeListener<String> chg = (obs, o, n) -> log.add("chg:" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, inv);
            helper = ExpressionHelper.addListener(helper, property, chg);

            /* Now in Generic with 1 invalidation, 1 change. Remove invalidation. */
            helper = ExpressionHelper.removeListener(helper, inv);

            /* Should be back to SingleChange — fire and verify only chg fires. */
            property.set("after-remove");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("chg:after-remove");
        }

        @Test
        @DisplayName("removing from Generic with multiple of each type stays Generic")
        void removingOneOfMany_staysGeneric() {
            List<String> log = new ArrayList<>();
            InvalidationListener inv1 = obs -> log.add("inv1");
            InvalidationListener inv2 = obs -> log.add("inv2");
            ChangeListener<String> chg = (obs, o, n) -> log.add("chg:" + n);

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, property, inv1);
            helper = ExpressionHelper.addListener(helper, property, inv2);
            helper = ExpressionHelper.addListener(helper, property, chg);

            /* Remove inv1 — still have inv2 + chg, stays Generic. */
            helper = ExpressionHelper.removeListener(helper, inv1);

            property.set("newval");
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("inv2", "chg:newval");
        }

        @Test
        @DisplayName("handles null value transitions correctly for change detection")
        void nullValueTransitions_changeDetection() {
            SimpleObjectProperty<String> prop = new SimpleObjectProperty<>(null);
            List<String> log = new ArrayList<>();
            ChangeListener<String> listener = (obs, o, n) ->
                    log.add(String.valueOf(o) + "->" + String.valueOf(n));

            ExpressionHelper<String> helper =
                    ExpressionHelper.addListener(null, prop, listener);

            /* null -> "hello" should fire. */
            prop.set("hello");
            ExpressionHelper.fireValueChangedEvent(helper);

            /* "hello" -> null should fire. */
            ExpressionHelper.fireValueChangedEvent(helper);
            prop.set(null);
            ExpressionHelper.fireValueChangedEvent(helper);

            assertThat(log).containsExactly("null->hello", "hello->null");
        }
    }
}

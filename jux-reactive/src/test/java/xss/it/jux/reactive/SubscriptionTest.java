package xss.it.jux.reactive;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link Subscription} — the functional interface for cancellable
 * listener registrations including {@link Subscription#EMPTY},
 * {@link Subscription#combine(Subscription...)}, and {@link Subscription#and(Subscription)}.
 */
@DisplayName("Subscription")
class SubscriptionTest {

    // ═══════════════════════════════════════════════════════════════
    //  EMPTY constant
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("EMPTY")
    class Empty {

        @Test
        @DisplayName("unsubscribe does nothing and does not throw")
        void unsubscribe_doesNothingAndDoesNotThrow() {
            assertThatNoException().isThrownBy(Subscription.EMPTY::unsubscribe);
        }

        @Test
        @DisplayName("multiple unsubscribe calls are idempotent")
        void multipleUnsubscribeCalls_areIdempotent() {
            assertThatNoException().isThrownBy(() -> {
                Subscription.EMPTY.unsubscribe();
                Subscription.EMPTY.unsubscribe();
                Subscription.EMPTY.unsubscribe();
            });
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Lambda / functional usage
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Lambda subscription")
    class LambdaSubscription {

        private boolean actionRan;

        @BeforeEach
        void setUp() {
            actionRan = false;
        }

        @Test
        @DisplayName("unsubscribe executes the action")
        void unsubscribe_executesTheAction() {
            Subscription sub = () -> actionRan = true;
            sub.unsubscribe();
            assertThat(actionRan).isTrue();
        }

        @Test
        @DisplayName("unsubscribe can be called multiple times")
        void unsubscribe_canBeCalledMultipleTimes() {
            List<String> calls = new ArrayList<>();
            Subscription sub = () -> calls.add("called");

            sub.unsubscribe();
            sub.unsubscribe();

            assertThat(calls).hasSize(2);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  combine()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("combine()")
    class Combine {

        @Test
        @DisplayName("combines multiple subscriptions and calls all on unsubscribe")
        void multipleSubscriptions_callsAllOnUnsubscribe() {
            List<String> log = new ArrayList<>();
            Subscription a = () -> log.add("a");
            Subscription b = () -> log.add("b");
            Subscription c = () -> log.add("c");

            Subscription combined = Subscription.combine(a, b, c);
            combined.unsubscribe();

            assertThat(log).containsExactly("a", "b", "c");
        }

        @Test
        @DisplayName("with empty array does nothing on unsubscribe")
        void emptyArray_doesNothingOnUnsubscribe() {
            Subscription combined = Subscription.combine();
            assertThatNoException().isThrownBy(combined::unsubscribe);
        }

        @Test
        @DisplayName("with single subscription calls it on unsubscribe")
        void singleSubscription_callsItOnUnsubscribe() {
            List<String> log = new ArrayList<>();
            Subscription combined = Subscription.combine(() -> log.add("only"));
            combined.unsubscribe();

            assertThat(log).containsExactly("only");
        }

        @Test
        @DisplayName("with null element throws NullPointerException from List.of")
        void nullElement_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Subscription.combine((() -> {}), null));
        }

        @Test
        @DisplayName("with null array throws NullPointerException")
        void nullArray_throwsNpe() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Subscription.combine((Subscription[]) null));
        }

        @Test
        @DisplayName("preserves declaration order during unsubscribe")
        void preservesDeclarationOrder_duringUnsubscribe() {
            List<Integer> order = new ArrayList<>();
            Subscription s1 = () -> order.add(1);
            Subscription s2 = () -> order.add(2);
            Subscription s3 = () -> order.add(3);

            Subscription.combine(s1, s2, s3).unsubscribe();

            assertThat(order).containsExactly(1, 2, 3);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  and()
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("and()")
    class And {

        @Test
        @DisplayName("combines this and other subscription")
        void combinesThisAndOther() {
            List<String> log = new ArrayList<>();
            Subscription first = () -> log.add("first");
            Subscription second = () -> log.add("second");

            Subscription combined = first.and(second);
            combined.unsubscribe();

            assertThat(log).containsExactly("first", "second");
        }

        @Test
        @DisplayName("calls this before other")
        void callsThisBeforeOther() {
            List<Integer> order = new ArrayList<>();
            Subscription a = () -> order.add(1);
            Subscription b = () -> order.add(2);

            a.and(b).unsubscribe();

            assertThat(order).containsExactly(1, 2);
        }

        @Test
        @DisplayName("with null other throws NullPointerException")
        void nullOther_throwsNpe() {
            Subscription sub = () -> {};
            assertThatNullPointerException()
                    .isThrownBy(() -> sub.and(null))
                    .withMessageContaining("other cannot be null");
        }

        @Test
        @DisplayName("chaining multiple and() calls preserves order")
        void chainingMultipleAndCalls_preservesOrder() {
            List<Integer> order = new ArrayList<>();
            Subscription a = () -> order.add(1);
            Subscription b = () -> order.add(2);
            Subscription c = () -> order.add(3);

            a.and(b).and(c).unsubscribe();

            assertThat(order).containsExactly(1, 2, 3);
        }

        @Test
        @DisplayName("and with EMPTY does not interfere")
        void andWithEmpty_doesNotInterfere() {
            List<String> log = new ArrayList<>();
            Subscription sub = () -> log.add("action");

            sub.and(Subscription.EMPTY).unsubscribe();

            assertThat(log).containsExactly("action");
        }
    }
}

package xss.it.jux.reactive.internal;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleDoubleProperty;
import xss.it.jux.reactive.property.SimpleIntegerProperty;
import xss.it.jux.reactive.property.SimpleLongProperty;
import xss.it.jux.reactive.property.SimpleObjectProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link BidirectionalBinding} — bidirectional property synchronization
 * including generic, boolean, integer, double, and long specializations,
 * as well as equals/hashCode symmetry and circular update prevention.
 */
@DisplayName("BidirectionalBinding")
class BidirectionalBindingTest {

    // ═══════════════════════════════════════════════════════════════
    //  Parameter validation
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Parameter validation")
    class ParameterValidation {

        @Test
        @DisplayName("bind with null p1 throws NullPointerException")
        void bindNullP1_throwsNpe() {
            SimpleObjectProperty<String> p2 = new SimpleObjectProperty<>("value");
            assertThatNullPointerException()
                    .isThrownBy(() -> BidirectionalBinding.bind(null, p2))
                    .withMessageContaining("Both properties must be specified");
        }

        @Test
        @DisplayName("bind with null p2 throws NullPointerException")
        void bindNullP2_throwsNpe() {
            SimpleObjectProperty<String> p1 = new SimpleObjectProperty<>("value");
            assertThatNullPointerException()
                    .isThrownBy(() -> BidirectionalBinding.bind(p1, null))
                    .withMessageContaining("Both properties must be specified");
        }

        @Test
        @DisplayName("bind with same property throws IllegalArgumentException")
        void bindSameProperty_throwsIllegalArgument() {
            SimpleObjectProperty<String> p = new SimpleObjectProperty<>("value");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> BidirectionalBinding.bind(p, p))
                    .withMessageContaining("Cannot bind property to itself");
        }

        @Test
        @DisplayName("unbind with null p1 throws NullPointerException")
        void unbindNullP1_throwsNpe() {
            SimpleObjectProperty<String> p2 = new SimpleObjectProperty<>("value");
            assertThatNullPointerException()
                    .isThrownBy(() -> BidirectionalBinding.unbind(null, p2));
        }

        @Test
        @DisplayName("unbind with null p2 throws NullPointerException")
        void unbindNullP2_throwsNpe() {
            SimpleObjectProperty<String> p1 = new SimpleObjectProperty<>("value");
            assertThatNullPointerException()
                    .isThrownBy(() -> BidirectionalBinding.unbind(p1, null));
        }

        @Test
        @DisplayName("unbind with same property throws IllegalArgumentException")
        void unbindSameProperty_throwsIllegalArgument() {
            SimpleObjectProperty<String> p = new SimpleObjectProperty<>("value");
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> BidirectionalBinding.unbind(p, p));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Generic (Object) bidirectional binding
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Generic Object binding")
    class GenericBinding {

        private SimpleObjectProperty<String> p1;
        private SimpleObjectProperty<String> p2;

        @BeforeEach
        void setUp() {
            p1 = new SimpleObjectProperty<>("alpha");
            p2 = new SimpleObjectProperty<>("beta");
        }

        @Test
        @DisplayName("bind syncs value from p2 to p1")
        void bind_syncsValueFromP2ToP1() {
            BidirectionalBinding.bind(p1, p2);
            assertThat(p1.get()).isEqualTo("beta");
            assertThat(p2.get()).isEqualTo("beta");
        }

        @Test
        @DisplayName("after bind, changing p1 updates p2")
        void afterBind_changingP1UpdatesP2() {
            BidirectionalBinding.bind(p1, p2);
            p1.set("gamma");
            assertThat(p2.get()).isEqualTo("gamma");
        }

        @Test
        @DisplayName("after bind, changing p2 updates p1")
        void afterBind_changingP2UpdatesP1() {
            BidirectionalBinding.bind(p1, p2);
            p2.set("delta");
            assertThat(p1.get()).isEqualTo("delta");
        }

        @Test
        @DisplayName("unbind stops synchronization")
        void unbind_stopsSynchronization() {
            BidirectionalBinding.bind(p1, p2);
            BidirectionalBinding.unbind(p1, p2);

            p1.set("independent");
            assertThat(p2.get()).isEqualTo("beta");

            p2.set("also-independent");
            assertThat(p1.get()).isEqualTo("independent");
        }

        @Test
        @DisplayName("circular update prevention does not cause infinite loop")
        void circularUpdatePrevention_noInfiniteLoop() {
            BidirectionalBinding.bind(p1, p2);

            /* This must complete without StackOverflowError. */
            p1.set("round-trip");
            assertThat(p1.get()).isEqualTo("round-trip");
            assertThat(p2.get()).isEqualTo("round-trip");

            p2.set("back-again");
            assertThat(p1.get()).isEqualTo("back-again");
            assertThat(p2.get()).isEqualTo("back-again");
        }

        @Test
        @DisplayName("null value synchronization works correctly")
        void nullValueSynchronization_worksCorrectly() {
            BidirectionalBinding.bind(p1, p2);
            p1.set(null);
            assertThat(p2.get()).isNull();

            p2.set("restored");
            assertThat(p1.get()).isEqualTo("restored");
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Boolean bidirectional binding
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Boolean binding")
    class BooleanBinding {

        private SimpleBooleanProperty p1;
        private SimpleBooleanProperty p2;

        @BeforeEach
        void setUp() {
            p1 = new SimpleBooleanProperty(false);
            p2 = new SimpleBooleanProperty(true);
        }

        @Test
        @DisplayName("bind syncs value from p2 to p1")
        void bind_syncsValueFromP2ToP1() {
            BidirectionalBinding.bind(p1, p2);
            assertThat(p1.get()).isTrue();
        }

        @Test
        @DisplayName("after bind, changing p1 updates p2")
        void afterBind_changingP1UpdatesP2() {
            BidirectionalBinding.bind(p1, p2);
            p1.set(false);
            assertThat(p2.get()).isFalse();
        }

        @Test
        @DisplayName("after bind, changing p2 updates p1")
        void afterBind_changingP2UpdatesP1() {
            BidirectionalBinding.bind(p1, p2);
            p2.set(false);
            assertThat(p1.get()).isFalse();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Integer bidirectional binding
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Integer binding")
    class IntegerBinding {

        private SimpleIntegerProperty p1;
        private SimpleIntegerProperty p2;

        @BeforeEach
        void setUp() {
            p1 = new SimpleIntegerProperty(10);
            p2 = new SimpleIntegerProperty(20);
        }

        @Test
        @DisplayName("bind syncs value from p2 to p1")
        void bind_syncsValueFromP2ToP1() {
            BidirectionalBinding.bind(p1, p2);
            assertThat(p1.get()).isEqualTo(20);
        }

        @Test
        @DisplayName("after bind, changing p1 updates p2")
        void afterBind_changingP1UpdatesP2() {
            BidirectionalBinding.bind(p1, p2);
            p1.set(42);
            assertThat(p2.get()).isEqualTo(42);
        }

        @Test
        @DisplayName("after bind, changing p2 updates p1")
        void afterBind_changingP2UpdatesP1() {
            BidirectionalBinding.bind(p1, p2);
            p2.set(99);
            assertThat(p1.get()).isEqualTo(99);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Double bidirectional binding
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Double binding")
    class DoubleBinding {

        private SimpleDoubleProperty p1;
        private SimpleDoubleProperty p2;

        @BeforeEach
        void setUp() {
            p1 = new SimpleDoubleProperty(1.5);
            p2 = new SimpleDoubleProperty(3.14);
        }

        @Test
        @DisplayName("bind syncs value from p2 to p1")
        void bind_syncsValueFromP2ToP1() {
            BidirectionalBinding.bind(p1, p2);
            assertThat(p1.get()).isEqualTo(3.14);
        }

        @Test
        @DisplayName("after bind, changing p1 updates p2")
        void afterBind_changingP1UpdatesP2() {
            BidirectionalBinding.bind(p1, p2);
            p1.set(2.718);
            assertThat(p2.get()).isEqualTo(2.718);
        }

        @Test
        @DisplayName("after bind, changing p2 updates p1")
        void afterBind_changingP2UpdatesP1() {
            BidirectionalBinding.bind(p1, p2);
            p2.set(0.0);
            assertThat(p1.get()).isEqualTo(0.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Long bidirectional binding
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Long binding")
    class LongBinding {

        private SimpleLongProperty p1;
        private SimpleLongProperty p2;

        @BeforeEach
        void setUp() {
            p1 = new SimpleLongProperty(100L);
            p2 = new SimpleLongProperty(200L);
        }

        @Test
        @DisplayName("bind syncs value from p2 to p1")
        void bind_syncsValueFromP2ToP1() {
            BidirectionalBinding.bind(p1, p2);
            assertThat(p1.get()).isEqualTo(200L);
        }

        @Test
        @DisplayName("after bind, changing p1 updates p2")
        void afterBind_changingP1UpdatesP2() {
            BidirectionalBinding.bind(p1, p2);
            p1.set(999L);
            assertThat(p2.get()).isEqualTo(999L);
        }

        @Test
        @DisplayName("after bind, changing p2 updates p1")
        void afterBind_changingP2UpdatesP1() {
            BidirectionalBinding.bind(p1, p2);
            p2.set(Long.MAX_VALUE);
            assertThat(p1.get()).isEqualTo(Long.MAX_VALUE);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  equals / hashCode symmetry
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        private SimpleObjectProperty<String> a;
        private SimpleObjectProperty<String> b;
        private SimpleObjectProperty<String> c;

        @BeforeEach
        void setUp() {
            a = new SimpleObjectProperty<>("a");
            b = new SimpleObjectProperty<>("b");
            c = new SimpleObjectProperty<>("c");
        }

        @Test
        @DisplayName("binding(a,b) equals binding(b,a) — symmetric")
        void bindingAB_equalsBindingBA_symmetric() {
            BidirectionalBinding bindingAB = BidirectionalBinding.bind(a, b);
            BidirectionalBinding.unbind(a, b);

            SimpleObjectProperty<String> a2 = a;
            SimpleObjectProperty<String> b2 = b;
            a2.set("a");
            b2.set("b");
            BidirectionalBinding bindingBA = BidirectionalBinding.bind(b2, a2);

            assertThat(bindingAB).isEqualTo(bindingBA);
            assertThat(bindingBA).isEqualTo(bindingAB);
        }

        @Test
        @DisplayName("hashCode is symmetric — binding(a,b) has same hash as binding(b,a)")
        void hashCode_isSymmetric() {
            BidirectionalBinding bindingAB = BidirectionalBinding.bind(a, b);
            BidirectionalBinding.unbind(a, b);

            a.set("a");
            b.set("b");
            BidirectionalBinding bindingBA = BidirectionalBinding.bind(b, a);

            assertThat(bindingAB.hashCode()).isEqualTo(bindingBA.hashCode());
        }

        @Test
        @DisplayName("equals returns false for different property pairs")
        void equalsReturnsFalse_forDifferentPropertyPairs() {
            BidirectionalBinding bindingAB = BidirectionalBinding.bind(a, b);
            BidirectionalBinding.unbind(a, b);

            a.set("a");
            BidirectionalBinding bindingAC = BidirectionalBinding.bind(a, c);

            assertThat(bindingAB).isNotEqualTo(bindingAC);
        }

        @Test
        @DisplayName("equals returns false for non-BidirectionalBinding objects")
        void equalsReturnsFalse_forNonBindingObjects() {
            BidirectionalBinding binding = BidirectionalBinding.bind(a, b);
            assertThat(binding).isNotEqualTo("not a binding");
            assertThat(binding).isNotEqualTo(null);
        }

        @Test
        @DisplayName("equals is reflexive")
        void equals_isReflexive() {
            BidirectionalBinding binding = BidirectionalBinding.bind(a, b);
            assertThat(binding).isEqualTo(binding);
        }
    }
}

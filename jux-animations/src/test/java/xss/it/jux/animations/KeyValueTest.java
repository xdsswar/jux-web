package xss.it.jux.animations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import xss.it.jux.reactive.property.SimpleDoubleProperty;
import xss.it.jux.reactive.property.SimpleObjectProperty;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link KeyValue} -- associates a property target with an end
 * value and an interpolator for timeline-based animation.
 */
@DisplayName("KeyValue")
class KeyValueTest {

    // ── Full constructor (target, endValue, interpolator) ────────

    @Nested
    @DisplayName("Full constructor (target, endValue, interpolator)")
    class FullConstructor {

        @Test
        @DisplayName("stores target, end value, and interpolator")
        void fullConstructor_storesAllFields() {
            var prop = new SimpleDoubleProperty(0.0);
            Interpolator interp = Interpolator.EASE_OUT;

            var kv = new KeyValue<>(prop, 100.0, interp);

            assertThat(kv.getTarget()).isSameAs(prop);
            assertThat(kv.getEndValue()).isEqualTo(100.0);
            assertThat(kv.getInterpolator()).isSameAs(interp);
        }

        @Test
        @DisplayName("null target throws NullPointerException")
        void nullTarget_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new KeyValue<>(null, 1.0, Interpolator.LINEAR))
                    .withMessageContaining("target");
        }

        @Test
        @DisplayName("null interpolator throws NullPointerException")
        void nullInterpolator_throwsNPE() {
            var prop = new SimpleDoubleProperty(0.0);
            assertThatNullPointerException()
                    .isThrownBy(() -> new KeyValue<>(prop, 1.0, null))
                    .withMessageContaining("interpolator");
        }

        @Test
        @DisplayName("null end value is allowed")
        void nullEndValue_isAllowed() {
            var prop = new SimpleObjectProperty<String>("hello");
            var kv = new KeyValue<>(prop, null, Interpolator.LINEAR);
            assertThat(kv.getEndValue()).isNull();
        }
    }

    // ── Two-arg constructor (target, endValue) ───────────────────

    @Nested
    @DisplayName("Two-arg constructor (target, endValue)")
    class TwoArgConstructor {

        @Test
        @DisplayName("defaults to LINEAR interpolator")
        void twoArgConstructor_defaultsToLinear() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 50.0);
            assertThat(kv.getInterpolator()).isSameAs(Interpolator.LINEAR);
        }

        @Test
        @DisplayName("stores target and end value")
        void twoArgConstructor_storesTargetAndEndValue() {
            var prop = new SimpleDoubleProperty(5.0);
            var kv = new KeyValue<>(prop, 99.0);
            assertThat(kv.getTarget()).isSameAs(prop);
            assertThat(kv.getEndValue()).isEqualTo(99.0);
        }

        @Test
        @DisplayName("null target throws NullPointerException")
        void twoArgNullTarget_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new KeyValue<>(null, 1.0));
        }
    }

    // ── Getters ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Getters")
    class Getters {

        @Test
        @DisplayName("getTarget returns the property reference")
        void getTarget_returnsProperty() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 10.0);
            assertThat(kv.getTarget()).isSameAs(prop);
        }

        @Test
        @DisplayName("getEndValue returns the end value")
        void getEndValue_returnsEndValue() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 42.0);
            assertThat(kv.getEndValue()).isEqualTo(42.0);
        }

        @Test
        @DisplayName("getInterpolator returns the assigned interpolator")
        void getInterpolator_returnsInterpolator() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 1.0, Interpolator.EASE_BOTH);
            assertThat(kv.getInterpolator()).isSameAs(Interpolator.EASE_BOTH);
        }
    }

    // ── toString ─────────────────────────────────────────────────

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString contains target, endValue, and interpolator")
        void toString_containsAllFields() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 7.5, Interpolator.EASE_IN);
            String str = kv.toString();
            assertThat(str).contains("KeyValue");
            assertThat(str).contains("7.5");
            assertThat(str).contains("EASE_IN");
        }
    }

    // ── Generic type usage ───────────────────────────────────────

    @Nested
    @DisplayName("Generic type usage")
    class GenericTypeTests {

        @Test
        @DisplayName("works with SimpleObjectProperty<String>")
        void worksWithStringProperty() {
            var prop = new SimpleObjectProperty<>("initial");
            var kv = new KeyValue<>(prop, "final", Interpolator.DISCRETE);
            assertThat(kv.getTarget()).isSameAs(prop);
            assertThat(kv.getEndValue()).isEqualTo("final");
            assertThat(kv.getInterpolator()).isSameAs(Interpolator.DISCRETE);
        }
    }
}

package xss.it.jux.animations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import xss.it.jux.reactive.property.SimpleDoubleProperty;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link KeyFrame} -- defines a set of key values at a specific
 * point in time within a timeline.
 */
@DisplayName("KeyFrame")
class KeyFrameTest {

    // ── Constructor: full (time, values, onFinished) ─────────────

    @Nested
    @DisplayName("Full constructor (time, values, onFinished)")
    class FullConstructor {

        @Test
        @DisplayName("stores time, values, and onFinished callback")
        void fullConstructor_storesAllFields() {
            var time = Duration.seconds(1);
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 1.0);
            Runnable callback = () -> {};
            List<KeyValue<?>> values = List.of(kv);

            var kf = new KeyFrame(time, values, callback);

            assertThat(kf.getTime()).isSameAs(time);
            assertThat(kf.getValues()).containsExactly(kv);
            assertThat(kf.getOnFinished()).isSameAs(callback);
        }

        @Test
        @DisplayName("null time throws NullPointerException")
        void nullTime_throwsNPE() {
            var values = List.<KeyValue<?>>of();
            assertThatNullPointerException()
                    .isThrownBy(() -> new KeyFrame(null, values, null))
                    .withMessageContaining("time");
        }

        @Test
        @DisplayName("null values list throws NullPointerException")
        void nullValues_throwsNPE() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new KeyFrame(Duration.millis(100), null, null))
                    .withMessageContaining("values");
        }

        @Test
        @DisplayName("onFinished may be null without error")
        void nullOnFinished_doesNotThrow() {
            var kf = new KeyFrame(Duration.millis(100), List.of(), null);
            assertThat(kf.getOnFinished()).isNull();
        }
    }

    // ── Constructor: (time, values) ──────────────────────────────

    @Nested
    @DisplayName("Two-arg constructor (time, values)")
    class TwoArgValuesConstructor {

        @Test
        @DisplayName("sets onFinished to null")
        void twoArgConstructor_onFinishedIsNull() {
            var kf = new KeyFrame(Duration.millis(500), List.of());
            assertThat(kf.getOnFinished()).isNull();
        }

        @Test
        @DisplayName("stores time and values correctly")
        void twoArgConstructor_storesTimeAndValues() {
            var time = Duration.millis(250);
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 5.0);
            List<KeyValue<?>> values = List.of(kv);

            var kf = new KeyFrame(time, values);

            assertThat(kf.getTime()).isSameAs(time);
            assertThat(kf.getValues()).containsExactly(kv);
        }
    }

    // ── Constructor: (time, onFinished) ──────────────────────────

    @Nested
    @DisplayName("Two-arg constructor (time, onFinished)")
    class TwoArgCallbackConstructor {

        @Test
        @DisplayName("stores callback with empty values list")
        void callbackConstructor_hasEmptyValues() {
            Runnable cb = () -> {};
            var kf = new KeyFrame(Duration.millis(100), cb);
            assertThat(kf.getValues()).isEmpty();
            assertThat(kf.getOnFinished()).isSameAs(cb);
        }

        @Test
        @DisplayName("stores time correctly")
        void callbackConstructor_storesTime() {
            var time = Duration.seconds(2);
            var kf = new KeyFrame(time, () -> {});
            assertThat(kf.getTime()).isSameAs(time);
        }
    }

    // ── Unmodifiable values list ─────────────────────────────────

    @Nested
    @DisplayName("Unmodifiable values list")
    class UnmodifiableValues {

        @Test
        @DisplayName("getValues returns unmodifiable list")
        void getValues_returnsUnmodifiableList() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv = new KeyValue<>(prop, 1.0);
            List<KeyValue<?>> values = List.of(kv);
            var kf = new KeyFrame(Duration.millis(100), values);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> kf.getValues().add(new KeyValue<>(prop, 2.0)));
        }

        @Test
        @DisplayName("getValues backed by original list reflects mutations (unmodifiable view)")
        void getValues_backedByOriginalList_reflectsMutations() {
            var prop = new SimpleDoubleProperty(0.0);
            var kv1 = new KeyValue<>(prop, 1.0);
            var mutableList = new ArrayList<KeyValue<?>>();
            mutableList.add(kv1);

            var kf = new KeyFrame(Duration.millis(100), mutableList);
            mutableList.add(new KeyValue<>(prop, 2.0));

            // Collections.unmodifiableList wraps the original -- mutations propagate
            assertThat(kf.getValues()).hasSize(2);
        }
    }

    // ── toString ─────────────────────────────────────────────────

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString contains time and values count")
        void toString_containsTimeAndValuesCount() {
            var prop = new SimpleDoubleProperty(0.0);
            List<KeyValue<?>> values = List.of(new KeyValue<>(prop, 1.0));
            var kf = new KeyFrame(
                    Duration.millis(500),
                    values,
                    () -> {}
            );
            String str = kf.toString();
            assertThat(str).contains("500.0ms");
            assertThat(str).contains("values=1");
            assertThat(str).contains("onFinished=true");
        }

        @Test
        @DisplayName("toString shows onFinished=false when no callback")
        void toString_noCallback_showsFalse() {
            var kf = new KeyFrame(Duration.millis(100), List.of());
            assertThat(kf.toString()).contains("onFinished=false");
        }
    }
}

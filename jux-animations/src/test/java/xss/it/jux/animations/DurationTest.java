package xss.it.jux.animations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Duration} -- immutable time value used throughout
 * the animation system.
 */
@DisplayName("Duration")
class DurationTest {

    // ── Factory Methods ───────────────────────────────────────────

    @Nested
    @DisplayName("Factory methods")
    class FactoryMethods {

        @Test
        @DisplayName("millis(100) creates a 100ms duration")
        void millis_createsCorrectDuration() {
            Duration d = Duration.millis(100);
            assertThat(d.toMillis()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("seconds(2) creates a 2000ms duration")
        void seconds_convertsToMillisCorrectly() {
            Duration d = Duration.seconds(2);
            assertThat(d.toMillis()).isEqualTo(2000.0);
        }

        @Test
        @DisplayName("minutes(1) creates a 60000ms duration")
        void minutes_convertsToMillisCorrectly() {
            Duration d = Duration.minutes(1);
            assertThat(d.toMillis()).isEqualTo(60_000.0);
        }

        @Test
        @DisplayName("millis(0) creates a zero-length duration")
        void millis_zeroValue_createsZeroDuration() {
            Duration d = Duration.millis(0);
            assertThat(d.toMillis()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("seconds(0.5) creates a 500ms duration")
        void seconds_fractionalValue_convertsCorrectly() {
            Duration d = Duration.seconds(0.5);
            assertThat(d.toMillis()).isEqualTo(500.0);
        }
    }

    // ── Constants ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Constants")
    class Constants {

        @Test
        @DisplayName("ZERO is 0ms")
        void zero_isZeroMillis() {
            assertThat(Duration.ZERO.toMillis()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("ONE is 1ms")
        void one_isOneMillis() {
            assertThat(Duration.ONE.toMillis()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("INDEFINITE is positive infinity")
        void indefinite_isPositiveInfinity() {
            assertThat(Duration.INDEFINITE.toMillis()).isEqualTo(Double.POSITIVE_INFINITY);
            assertThat(Duration.INDEFINITE.isIndefinite()).isTrue();
        }
    }

    // ── Conversion ────────────────────────────────────────────────

    @Nested
    @DisplayName("Conversion")
    class Conversion {

        @Test
        @DisplayName("toMillis returns raw millisecond value")
        void toMillis_returnsRawValue() {
            Duration d = Duration.millis(1500);
            assertThat(d.toMillis()).isEqualTo(1500.0);
        }

        @Test
        @DisplayName("toSeconds converts milliseconds to seconds")
        void toSeconds_convertsMillisToSeconds() {
            Duration d = Duration.millis(2500);
            assertThat(d.toSeconds()).isEqualTo(2.5);
        }

        @Test
        @DisplayName("toSeconds for seconds factory round-trips correctly")
        void toSeconds_roundTripsFromSecondsFactory() {
            Duration d = Duration.seconds(3.7);
            assertThat(d.toSeconds()).isCloseTo(3.7, within(1e-9));
        }
    }

    // ── Arithmetic ────────────────────────────────────────────────

    @Nested
    @DisplayName("Arithmetic operations")
    class Arithmetic {

        @Test
        @DisplayName("add combines two durations")
        void addTwoDurations_returnsSumDuration() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(200);
            Duration result = a.add(b);
            assertThat(result.toMillis()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("subtract returns the difference")
        void subtractDurations_returnsDifference() {
            Duration a = Duration.millis(500);
            Duration b = Duration.millis(200);
            Duration result = a.subtract(b);
            assertThat(result.toMillis()).isEqualTo(300.0);
        }

        @Test
        @DisplayName("subtract can produce negative duration")
        void subtractLargerFromSmaller_producesNegative() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(300);
            Duration result = a.subtract(b);
            assertThat(result.toMillis()).isEqualTo(-200.0);
        }

        @Test
        @DisplayName("multiply scales by factor")
        void multiplyByFactor_scalesDuration() {
            Duration d = Duration.millis(200);
            Duration result = d.multiply(3.0);
            assertThat(result.toMillis()).isEqualTo(600.0);
        }

        @Test
        @DisplayName("divide divides by divisor")
        void divideByDivisor_dividesDuration() {
            Duration d = Duration.millis(600);
            Duration result = d.divide(3.0);
            assertThat(result.toMillis()).isEqualTo(200.0);
        }

        @Test
        @DisplayName("divide by zero produces infinity")
        void divideByZero_producesInfinity() {
            Duration d = Duration.millis(100);
            Duration result = d.divide(0.0);
            assertThat(result.isIndefinite()).isTrue();
        }

        @Test
        @DisplayName("negate flips the sign")
        void negate_flipsSign() {
            Duration d = Duration.millis(250);
            Duration result = d.negate();
            assertThat(result.toMillis()).isEqualTo(-250.0);
        }

        @Test
        @DisplayName("negate of negative yields positive")
        void negateOfNegative_yieldsPositive() {
            Duration d = Duration.millis(-100);
            Duration result = d.negate();
            assertThat(result.toMillis()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("arithmetic does not mutate original")
        void arithmetic_doesNotMutateOriginal() {
            Duration d = Duration.millis(100);
            d.add(Duration.millis(50));
            d.subtract(Duration.millis(10));
            d.multiply(5);
            d.divide(2);
            d.negate();
            assertThat(d.toMillis()).isEqualTo(100.0);
        }
    }

    // ── Comparison ────────────────────────────────────────────────

    @Nested
    @DisplayName("Comparison operators")
    class Comparison {

        @Test
        @DisplayName("lessThan returns true when smaller")
        void lessThan_smallerDuration_returnsTrue() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(200);
            assertThat(a.lessThan(b)).isTrue();
            assertThat(b.lessThan(a)).isFalse();
        }

        @Test
        @DisplayName("lessThan returns false for equal durations")
        void lessThan_equalDurations_returnsFalse() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(100);
            assertThat(a.lessThan(b)).isFalse();
        }

        @Test
        @DisplayName("lessThanOrEqualTo returns true for equal")
        void lessThanOrEqualTo_equalDurations_returnsTrue() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(100);
            assertThat(a.lessThanOrEqualTo(b)).isTrue();
        }

        @Test
        @DisplayName("greaterThan returns true when larger")
        void greaterThan_largerDuration_returnsTrue() {
            Duration a = Duration.millis(300);
            Duration b = Duration.millis(100);
            assertThat(a.greaterThan(b)).isTrue();
            assertThat(b.greaterThan(a)).isFalse();
        }

        @Test
        @DisplayName("greaterThanOrEqualTo returns true for equal")
        void greaterThanOrEqualTo_equalDurations_returnsTrue() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(100);
            assertThat(a.greaterThanOrEqualTo(b)).isTrue();
        }

        @Test
        @DisplayName("compareTo orders durations correctly")
        void compareTo_ordersCorrectly() {
            Duration small = Duration.millis(50);
            Duration medium = Duration.millis(100);
            Duration large = Duration.millis(200);

            assertThat(small.compareTo(medium)).isNegative();
            assertThat(medium.compareTo(medium)).isZero();
            assertThat(large.compareTo(medium)).isPositive();
        }

        @Test
        @DisplayName("INDEFINITE is greater than any finite duration")
        void indefinite_isGreaterThanFinite() {
            Duration finite = Duration.millis(999_999);
            assertThat(Duration.INDEFINITE.greaterThan(finite)).isTrue();
            assertThat(finite.lessThan(Duration.INDEFINITE)).isTrue();
        }
    }

    // ── Sentinel checks ──────────────────────────────────────────

    @Nested
    @DisplayName("Sentinel value checks")
    class SentinelChecks {

        @Test
        @DisplayName("isIndefinite is true for INDEFINITE")
        void isIndefinite_forIndefiniteConstant_returnsTrue() {
            assertThat(Duration.INDEFINITE.isIndefinite()).isTrue();
        }

        @Test
        @DisplayName("isIndefinite is false for finite duration")
        void isIndefinite_forFiniteDuration_returnsFalse() {
            assertThat(Duration.millis(1000).isIndefinite()).isFalse();
        }

        @Test
        @DisplayName("isUnknown is true for NaN-based duration")
        void isUnknown_forNaN_returnsTrue() {
            Duration nan = Duration.millis(Double.NaN);
            assertThat(nan.isUnknown()).isTrue();
        }

        @Test
        @DisplayName("isUnknown is false for finite duration")
        void isUnknown_forFiniteDuration_returnsFalse() {
            assertThat(Duration.millis(100).isUnknown()).isFalse();
        }

        @Test
        @DisplayName("isUnknown is false for INDEFINITE")
        void isUnknown_forIndefinite_returnsFalse() {
            assertThat(Duration.INDEFINITE.isUnknown()).isFalse();
        }
    }

    // ── equals / hashCode ────────────────────────────────────────

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsHashCode {

        @Test
        @DisplayName("equal durations are equal")
        void equalDurations_areEqual() {
            Duration a = Duration.millis(500);
            Duration b = Duration.millis(500);
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different durations are not equal")
        void differentDurations_areNotEqual() {
            Duration a = Duration.millis(100);
            Duration b = Duration.millis(200);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("ZERO equals millis(0)")
        void zeroConstant_equalsMillisZero() {
            assertThat(Duration.ZERO).isEqualTo(Duration.millis(0));
        }

        @Test
        @DisplayName("duration is not equal to null")
        void duration_notEqualToNull() {
            assertThat(Duration.millis(100)).isNotEqualTo(null);
        }

        @Test
        @DisplayName("duration is not equal to non-Duration object")
        void duration_notEqualToOtherType() {
            assertThat(Duration.millis(100)).isNotEqualTo("100ms");
        }
    }

    // ── toString ─────────────────────────────────────────────────

    @Nested
    @DisplayName("toString")
    class ToStringTests {

        @Test
        @DisplayName("toString shows milliseconds with ms suffix")
        void toString_showsMillisWithSuffix() {
            Duration d = Duration.millis(250);
            assertThat(d.toString()).isEqualTo("250.0ms");
        }

        @Test
        @DisplayName("toString for ZERO shows 0.0ms")
        void toString_forZero_shows0ms() {
            assertThat(Duration.ZERO.toString()).isEqualTo("0.0ms");
        }

        @Test
        @DisplayName("toString for INDEFINITE shows Infinityms")
        void toString_forIndefinite_showsInfinityMs() {
            assertThat(Duration.INDEFINITE.toString()).isEqualTo("Infinityms");
        }
    }
}

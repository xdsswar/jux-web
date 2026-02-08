package xss.it.jux.animations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link Interpolator} -- abstract easing curve with built-in
 * instances and factory methods.
 */
@DisplayName("Interpolator")
class InterpolatorTest {

    private static final double EPSILON = 1e-6;

    // ── LINEAR ────────────────────────────────────────────────────

    @Nested
    @DisplayName("LINEAR")
    class LinearTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.LINEAR.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.LINEAR.curve(1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("curve(0.5) returns 0.5 -- constant speed")
        void curveAtHalf_returnsHalf() {
            assertThat(Interpolator.LINEAR.curve(0.5)).isEqualTo(0.5);
        }

        @Test
        @DisplayName("curve(0.25) returns 0.25")
        void curveAtQuarter_returnsQuarter() {
            assertThat(Interpolator.LINEAR.curve(0.25)).isEqualTo(0.25);
        }

        @Test
        @DisplayName("toString contains LINEAR")
        void toString_containsLinear() {
            assertThat(Interpolator.LINEAR.toString()).contains("LINEAR");
        }
    }

    // ── DISCRETE ──────────────────────────────────────────────────

    @Nested
    @DisplayName("DISCRETE")
    class DiscreteTests {

        @Test
        @DisplayName("curve(0) returns 0 -- stays at start")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.DISCRETE.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(0.5) returns 0 -- still at start before t=1")
        void curveAtHalf_returnsZero() {
            assertThat(Interpolator.DISCRETE.curve(0.5)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(0.99) returns 0 -- still at start just before t=1")
        void curveJustBeforeOne_returnsZero() {
            assertThat(Interpolator.DISCRETE.curve(0.99)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1.0) returns 1 -- snaps to end at t=1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.DISCRETE.curve(1.0)).isEqualTo(1.0);
        }
    }

    // ── EASE_IN ───────────────────────────────────────────────────

    @Nested
    @DisplayName("EASE_IN")
    class EaseInTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.EASE_IN.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.EASE_IN.curve(1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("starts slow -- curve(0.5) is less than 0.5")
        void curveAtHalf_isLessThanLinear() {
            double value = Interpolator.EASE_IN.curve(0.5);
            assertThat(value).isLessThan(0.5);
            // t^3 = 0.125
            assertThat(value).isCloseTo(0.125, within(EPSILON));
        }

        @Test
        @DisplayName("monotonically increasing between 0 and 1")
        void monotonicIncrease_betweenZeroAndOne() {
            double prev = 0.0;
            for (double t = 0.1; t <= 1.0; t += 0.1) {
                double curr = Interpolator.EASE_IN.curve(t);
                assertThat(curr).isGreaterThanOrEqualTo(prev);
                prev = curr;
            }
        }
    }

    // ── EASE_OUT ──────────────────────────────────────────────────

    @Nested
    @DisplayName("EASE_OUT")
    class EaseOutTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.EASE_OUT.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.EASE_OUT.curve(1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("ends slow -- curve(0.5) is greater than 0.5")
        void curveAtHalf_isGreaterThanLinear() {
            double value = Interpolator.EASE_OUT.curve(0.5);
            assertThat(value).isGreaterThan(0.5);
            // 1 - (1-0.5)^3 = 1 - 0.125 = 0.875
            assertThat(value).isCloseTo(0.875, within(EPSILON));
        }
    }

    // ── EASE_BOTH ─────────────────────────────────────────────────

    @Nested
    @DisplayName("EASE_BOTH")
    class EaseBothTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.EASE_BOTH.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.EASE_BOTH.curve(1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("curve(0.5) returns 0.5 -- symmetric midpoint")
        void curveAtHalf_returnsHalf() {
            // smoothstep: t^2 * (3 - 2t) at t=0.5 => 0.25 * 2 = 0.5
            double value = Interpolator.EASE_BOTH.curve(0.5);
            assertThat(value).isCloseTo(0.5, within(EPSILON));
        }

        @Test
        @DisplayName("symmetric around midpoint -- curve(0.25) + curve(0.75) == 1")
        void symmetricAroundMidpoint() {
            double low = Interpolator.EASE_BOTH.curve(0.25);
            double high = Interpolator.EASE_BOTH.curve(0.75);
            assertThat(low + high).isCloseTo(1.0, within(EPSILON));
        }
    }

    // ── BOUNCE ────────────────────────────────────────────────────

    @Nested
    @DisplayName("BOUNCE")
    class BounceTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.BOUNCE.curve(0.0)).isCloseTo(0.0, within(EPSILON));
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.BOUNCE.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("all values in [0, 1] range for t in [0, 1]")
        void curveValues_stayInUnitRange() {
            for (double t = 0.0; t <= 1.0; t += 0.01) {
                double v = Interpolator.BOUNCE.curve(t);
                assertThat(v).isBetween(0.0 - EPSILON, 1.0 + EPSILON);
            }
        }
    }

    // ── ELASTIC ───────────────────────────────────────────────────

    @Nested
    @DisplayName("ELASTIC")
    class ElasticTests {

        @Test
        @DisplayName("curve(0) returns 0")
        void curveAtZero_returnsZero() {
            assertThat(Interpolator.ELASTIC.curve(0.0)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("curve(1) returns 1")
        void curveAtOne_returnsOne() {
            assertThat(Interpolator.ELASTIC.curve(1.0)).isEqualTo(1.0);
        }

        @Test
        @DisplayName("overshoots past 1.0 during transition")
        void curveOvershoots_duringTransition() {
            boolean overshoots = false;
            for (double t = 0.0; t <= 1.0; t += 0.01) {
                if (Interpolator.ELASTIC.curve(t) > 1.0) {
                    overshoots = true;
                    break;
                }
            }
            assertThat(overshoots).isTrue();
        }
    }

    // ── OVERSHOOT ─────────────────────────────────────────────────

    @Nested
    @DisplayName("OVERSHOOT")
    class OvershootTests {

        @Test
        @DisplayName("curve(0) returns approximately 0")
        void curveAtZero_returnsApproxZero() {
            // overshoot formula: (0-1)^2 * ((s+1)*(0-1) + s) + 1
            // = 1 * (-(s+1) + s) + 1 = 1 * (-1) + 1 = 0
            assertThat(Interpolator.OVERSHOOT.curve(0.0)).isCloseTo(0.0, within(EPSILON));
        }

        @Test
        @DisplayName("curve(1) returns approximately 1")
        void curveAtOne_returnsApproxOne() {
            assertThat(Interpolator.OVERSHOOT.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("overshoot factory with custom tension")
        void overshootFactory_customTension_producesInterpolator() {
            Interpolator custom = Interpolator.overshoot(2.5);
            assertThat(custom.curve(0.0)).isCloseTo(0.0, within(EPSILON));
            assertThat(custom.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }
    }

    // ── spline factory ────────────────────────────────────────────

    @Nested
    @DisplayName("spline factory")
    class SplineTests {

        @Test
        @DisplayName("spline(0,0,1,1) behaves like linear")
        void splineLinear_behavesLikeLinear() {
            Interpolator s = Interpolator.spline(0, 0, 1, 1);
            assertThat(s.curve(0.0)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(0.5)).isCloseTo(0.5, within(0.01));
            assertThat(s.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("spline with CSS ease-in cubic-bezier values")
        void splineCssEaseIn_startsSlowly() {
            Interpolator s = Interpolator.spline(0.42, 0.0, 1.0, 1.0);
            double midValue = s.curve(0.5);
            // Ease-in: value at 0.5 should be less than 0.5
            assertThat(midValue).isLessThan(0.5);
        }

        @Test
        @DisplayName("spline endpoints are 0 and 1")
        void spline_endpointsAreCorrect() {
            Interpolator s = Interpolator.spline(0.25, 0.1, 0.25, 1.0);
            assertThat(s.curve(0.0)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("spline with x1 out of range throws")
        void spline_x1OutOfRange_throws() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Interpolator.spline(-0.1, 0, 1, 1));
        }

        @Test
        @DisplayName("spline with x2 out of range throws")
        void spline_x2OutOfRange_throws() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Interpolator.spline(0, 0, 1.5, 1));
        }
    }

    // ── steps factory ─────────────────────────────────────────────

    @Nested
    @DisplayName("steps factory")
    class StepsTests {

        @Test
        @DisplayName("steps(3) divides into 3 discrete levels")
        void steps3_producesThreeDiscreteLevels() {
            Interpolator s = Interpolator.steps(3);
            // t in [0, 1/3) -> 0/3 = 0
            assertThat(s.curve(0.0)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(0.1)).isCloseTo(0.0, within(EPSILON));
            // t in [1/3, 2/3) -> 1/3
            assertThat(s.curve(0.34)).isCloseTo(1.0 / 3.0, within(EPSILON));
            // t in [2/3, 1) -> 2/3
            assertThat(s.curve(0.67)).isCloseTo(2.0 / 3.0, within(EPSILON));
            // t == 1 -> 1
            assertThat(s.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("steps(1) returns 0 until t=1, then 1")
        void steps1_behavesLikeDiscrete() {
            Interpolator s = Interpolator.steps(1);
            assertThat(s.curve(0.0)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(0.5)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(0.99)).isCloseTo(0.0, within(EPSILON));
            assertThat(s.curve(1.0)).isCloseTo(1.0, within(EPSILON));
        }

        @Test
        @DisplayName("steps(0) throws IllegalArgumentException")
        void stepsZero_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Interpolator.steps(0))
                    .withMessageContaining("1");
        }

        @Test
        @DisplayName("steps with negative count throws IllegalArgumentException")
        void stepsNegative_throwsIllegalArgument() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> Interpolator.steps(-1));
        }
    }

    // ── spring factory ────────────────────────────────────────────

    @Nested
    @DisplayName("spring factory")
    class SpringTests {

        @Test
        @DisplayName("spring produces non-null interpolator")
        void springFactory_producesInterpolator() {
            Interpolator s = Interpolator.spring(0.5, 1.0);
            assertThat(s).isNotNull();
        }

        @Test
        @DisplayName("spring at t=0 returns 0")
        void springAtZero_returnsZero() {
            Interpolator s = Interpolator.spring(0.5, 1.0);
            assertThat(s.curve(0.0)).isCloseTo(0.0, within(EPSILON));
        }

        @Test
        @DisplayName("spring toString contains damping and stiffness")
        void springToString_containsParams() {
            Interpolator s = Interpolator.spring(0.7, 2.0);
            assertThat(s.toString()).contains("spring");
            assertThat(s.toString()).contains("0.7");
            assertThat(s.toString()).contains("2.0");
        }
    }

    // ── interpolate(Object, Object, double) ──────────────────────

    @Nested
    @DisplayName("interpolate()")
    class InterpolateTests {

        @Test
        @DisplayName("interpolate Double values with linear at 0.5")
        void interpolateDouble_linear_atHalf() {
            Object result = Interpolator.LINEAR.interpolate(0.0, 100.0, 0.5);
            assertThat(result).isInstanceOf(Double.class);
            assertThat((Double) result).isCloseTo(50.0, within(EPSILON));
        }

        @Test
        @DisplayName("interpolate Double at fraction 0 returns start")
        void interpolateDouble_atZero_returnsStart() {
            Object result = Interpolator.LINEAR.interpolate(10.0, 90.0, 0.0);
            assertThat((Double) result).isCloseTo(10.0, within(EPSILON));
        }

        @Test
        @DisplayName("interpolate Double at fraction 1 returns end")
        void interpolateDouble_atOne_returnsEnd() {
            Object result = Interpolator.LINEAR.interpolate(10.0, 90.0, 1.0);
            assertThat((Double) result).isCloseTo(90.0, within(EPSILON));
        }

        @Test
        @DisplayName("interpolate Integer values rounds correctly")
        void interpolateInteger_roundsCorrectly() {
            Object result = Interpolator.LINEAR.interpolate(0, 10, 0.5);
            assertThat(result).isInstanceOf(Integer.class);
            assertThat((Integer) result).isEqualTo(5);
        }

        @Test
        @DisplayName("interpolate Integer rounds to nearest int")
        void interpolateInteger_roundsToNearest() {
            Object result = Interpolator.LINEAR.interpolate(0, 10, 0.33);
            assertThat(result).isInstanceOf(Integer.class);
            // 0 + (10-0) * 0.33 = 3.3, rounded = 3
            assertThat((Integer) result).isEqualTo(3);
        }

        @Test
        @DisplayName("interpolate Long values rounds correctly")
        void interpolateLong_roundsCorrectly() {
            Object result = Interpolator.LINEAR.interpolate(0L, 1000L, 0.5);
            assertThat(result).isInstanceOf(Long.class);
            assertThat((Long) result).isEqualTo(500L);
        }

        @Test
        @DisplayName("interpolate non-numeric snaps at fraction 1")
        void interpolateNonNumeric_snapsAtCompletion() {
            Object result0 = Interpolator.LINEAR.interpolate("start", "end", 0.0);
            assertThat(result0).isEqualTo("start");

            Object result5 = Interpolator.LINEAR.interpolate("start", "end", 0.5);
            assertThat(result5).isEqualTo("start");

            Object result99 = Interpolator.LINEAR.interpolate("start", "end", 0.99);
            assertThat(result99).isEqualTo("start");

            Object result1 = Interpolator.LINEAR.interpolate("start", "end", 1.0);
            assertThat(result1).isEqualTo("end");
        }

        @Test
        @DisplayName("interpolate with EASE_IN curve applies easing to Double")
        void interpolateWithEaseIn_appliesCurve() {
            Object result = Interpolator.EASE_IN.interpolate(0.0, 100.0, 0.5);
            // EASE_IN curve(0.5) = 0.125 (clamped input); result = 0 + 100*0.125 = 12.5
            assertThat((Double) result).isCloseTo(12.5, within(EPSILON));
        }

        @Test
        @DisplayName("interpolate clamps negative fraction to 0")
        void interpolate_negativeFraction_clampsToZero() {
            Object result = Interpolator.LINEAR.interpolate(0.0, 100.0, -0.5);
            // clamp(-0.5) = 0.0, curve(0) = 0.0, interpolation = 0.0
            assertThat((Double) result).isCloseTo(0.0, within(EPSILON));
        }

        @Test
        @DisplayName("interpolate clamps fraction above 1 to 1")
        void interpolate_fractionAboveOne_clampsToOne() {
            Object result = Interpolator.LINEAR.interpolate(0.0, 100.0, 1.5);
            // clamp(1.5) = 1.0, curve(1.0) = 1.0, interpolation = 100.0
            assertThat((Double) result).isCloseTo(100.0, within(EPSILON));
        }
    }

    // ── Boundary values across all built-ins ─────────────────────

    @Nested
    @DisplayName("Boundary values for all built-in interpolators")
    class BoundaryTests {

        @Test
        @DisplayName("all built-ins return approximately 0 at t=0")
        void allBuiltIns_atZero_returnApproxZero() {
            Interpolator[] all = {
                    Interpolator.LINEAR, Interpolator.DISCRETE,
                    Interpolator.EASE_IN, Interpolator.EASE_OUT,
                    Interpolator.EASE_BOTH, Interpolator.BOUNCE,
                    Interpolator.ELASTIC
            };
            for (Interpolator interp : all) {
                assertThat(interp.curve(0.0))
                        .as("curve(0) for %s", interp)
                        .isCloseTo(0.0, within(EPSILON));
            }
        }

        @Test
        @DisplayName("all built-ins return approximately 1 at t=1")
        void allBuiltIns_atOne_returnApproxOne() {
            Interpolator[] all = {
                    Interpolator.LINEAR, Interpolator.DISCRETE,
                    Interpolator.EASE_IN, Interpolator.EASE_OUT,
                    Interpolator.EASE_BOTH, Interpolator.BOUNCE,
                    Interpolator.ELASTIC
            };
            for (Interpolator interp : all) {
                assertThat(interp.curve(1.0))
                        .as("curve(1) for %s", interp)
                        .isCloseTo(1.0, within(EPSILON));
            }
        }
    }
}

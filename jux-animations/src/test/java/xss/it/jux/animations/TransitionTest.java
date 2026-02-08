package xss.it.jux.animations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.animations.transition.FadeTransition;
import xss.it.jux.animations.transition.PauseTransition;
import xss.it.jux.animations.transition.RotateTransition;
import xss.it.jux.animations.transition.ScaleTransition;
import xss.it.jux.animations.transition.SlideTransition;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for single-property transitions: {@link FadeTransition},
 * {@link ScaleTransition}, {@link RotateTransition},
 * {@link SlideTransition}, and {@link PauseTransition}.
 */
@DisplayName("Single-property transitions")
class TransitionTest {

    private TestFrameScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TestFrameScheduler();
        FrameScheduler.setDefault(scheduler);
    }

    // ═══════════════════════════════════════════════════════════════
    //  FadeTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("FadeTransition")
    class FadeTransitionTests {

        @Test
        @DisplayName("property reaches end value after full duration")
        void fromZeroToOne_propertyReachesEndValue() {
            var opacity = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), opacity)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            fade.play();
            scheduler.runUntilIdle(1000);

            assertThat(opacity.get()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("NaN fromValue uses current property value as start")
        void nanFromValue_usesCurrentPropertyValue() {
            var opacity = new SimpleDoubleProperty(0.5);
            var fade = new FadeTransition(scheduler, Duration.millis(500), opacity)
                    .setToValue(1.0);
            // fromValue is NaN by default

            assertThat(fade.getFromValue()).isNaN();

            fade.play();
            scheduler.runUntilIdle(1000);

            // Should animate from 0.5 to 1.0
            assertThat(opacity.get()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("setInterpolator changes easing curve")
        void setInterpolator_changesEasingCurve() {
            var opacity = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(1000), opacity)
                    .setFromValue(0.0)
                    .setToValue(1.0)
                    .setInterpolator(Interpolator.EASE_IN);

            fade.play();
            scheduler.tick(0);   // capture start timestamp
            scheduler.tick(500); // halfway through

            // With EASE_IN (t^3), at t=0.5 the curve value is 0.125
            // So property should be less than 0.5 (which LINEAR would give)
            double valueAtHalfway = opacity.get();
            assertThat(valueAtHalfway).isLessThan(0.4);
        }

        @Test
        @DisplayName("null property throws NullPointerException on play")
        void nullProperty_throwsOnPlay() {
            var fade = new FadeTransition(scheduler, Duration.millis(500), null);

            assertThatThrownBy(fade::play)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fluent setters return same instance")
        void fluentSetters_returnSameInstance() {
            var opacity = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), opacity);

            FadeTransition returned = fade.setFromValue(0.0);
            assertThat(returned).isSameAs(fade);

            returned = fade.setToValue(1.0);
            assertThat(returned).isSameAs(fade);

            returned = fade.setInterpolator(Interpolator.EASE_OUT);
            assertThat(returned).isSameAs(fade);
        }

        @Test
        @DisplayName("getToValue returns NaN when not set")
        void getToValue_returnsNanWhenNotSet() {
            var opacity = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), opacity);

            assertThat(fade.getToValue()).isNaN();
        }

        @Test
        @DisplayName("getTotalDuration returns configured duration")
        void getTotalDuration_returnsConfiguredDuration() {
            var fade = new FadeTransition(scheduler, Duration.millis(750), new SimpleDoubleProperty(0.0));
            assertThat(fade.getTotalDuration().toMillis()).isEqualTo(750.0);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  ScaleTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ScaleTransition")
    class ScaleTransitionTests {

        @Test
        @DisplayName("property reaches end value after full duration")
        void scaleFromOneToTwo_propertyReachesEndValue() {
            var scale = new SimpleDoubleProperty(1.0);
            var transition = new ScaleTransition(scheduler, Duration.millis(500), scale)
                    .setFromValue(1.0)
                    .setToValue(2.0);

            transition.play();
            scheduler.runUntilIdle(1000);

            assertThat(scale.get()).isCloseTo(2.0, within(0.01));
        }

        @Test
        @DisplayName("NaN fromValue uses current property value as start")
        void nanFromValue_usesCurrentPropertyValue() {
            var scale = new SimpleDoubleProperty(1.5);
            var transition = new ScaleTransition(scheduler, Duration.millis(500), scale)
                    .setToValue(3.0);

            transition.play();
            scheduler.runUntilIdle(1000);

            assertThat(scale.get()).isCloseTo(3.0, within(0.01));
        }

        @Test
        @DisplayName("null property throws NullPointerException on play")
        void nullProperty_throwsOnPlay() {
            var transition = new ScaleTransition(scheduler, Duration.millis(500), null);

            assertThatThrownBy(transition::play)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fluent setters return same instance")
        void fluentSetters_returnSameInstance() {
            var scale = new SimpleDoubleProperty(1.0);
            var transition = new ScaleTransition(scheduler, Duration.millis(500), scale);

            ScaleTransition returned = transition.setFromValue(1.0);
            assertThat(returned).isSameAs(transition);

            returned = transition.setToValue(2.0);
            assertThat(returned).isSameAs(transition);

            returned = transition.setInterpolator(Interpolator.EASE_BOTH);
            assertThat(returned).isSameAs(transition);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RotateTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("RotateTransition")
    class RotateTransitionTests {

        @Test
        @DisplayName("property rotates from 0 to 360 degrees after full duration")
        void rotateZeroTo360_propertyReachesEndValue() {
            var rotation = new SimpleDoubleProperty(0.0);
            var transition = new RotateTransition(scheduler, Duration.millis(1000), rotation)
                    .setFromValue(0.0)
                    .setToValue(360.0);

            transition.play();
            scheduler.runUntilIdle(2000);

            assertThat(rotation.get()).isCloseTo(360.0, within(0.1));
        }

        @Test
        @DisplayName("NaN fromValue uses current property value as start")
        void nanFromValue_usesCurrentPropertyValue() {
            var rotation = new SimpleDoubleProperty(45.0);
            var transition = new RotateTransition(scheduler, Duration.millis(500), rotation)
                    .setToValue(180.0);

            transition.play();
            scheduler.runUntilIdle(1000);

            assertThat(rotation.get()).isCloseTo(180.0, within(0.1));
        }

        @Test
        @DisplayName("null property throws NullPointerException on play")
        void nullProperty_throwsOnPlay() {
            var transition = new RotateTransition(scheduler, Duration.millis(500), null);

            assertThatThrownBy(transition::play)
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("fluent setters return same instance")
        void fluentSetters_returnSameInstance() {
            var rotation = new SimpleDoubleProperty(0.0);
            var transition = new RotateTransition(scheduler, Duration.millis(500), rotation);

            RotateTransition returned = transition.setFromValue(0.0);
            assertThat(returned).isSameAs(transition);

            returned = transition.setToValue(360.0);
            assertThat(returned).isSameAs(transition);

            returned = transition.setInterpolator(Interpolator.LINEAR);
            assertThat(returned).isSameAs(transition);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SlideTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SlideTransition")
    class SlideTransitionTests {

        @Test
        @DisplayName("dual-axis slide updates both X and Y properties")
        void dualAxisSlide_updatesBothXAndY() {
            var tx = new SimpleDoubleProperty(0.0);
            var ty = new SimpleDoubleProperty(0.0);
            var slide = new SlideTransition(scheduler, Duration.millis(500), tx, ty)
                    .setToX(200.0)
                    .setToY(100.0);

            slide.play();
            scheduler.runUntilIdle(1000);

            assertThat(tx.get()).isCloseTo(200.0, within(0.5));
            assertThat(ty.get()).isCloseTo(100.0, within(0.5));
        }

        @Test
        @DisplayName("null translateX property is skipped during interpolation")
        void nullTranslateX_isSkippedDuringInterpolation() {
            var ty = new SimpleDoubleProperty(0.0);
            var slide = new SlideTransition(scheduler, Duration.millis(500), null, ty)
                    .setToY(100.0);

            slide.play();
            scheduler.runUntilIdle(1000);

            assertThat(ty.get()).isCloseTo(100.0, within(0.5));
        }

        @Test
        @DisplayName("null translateY property is skipped during interpolation")
        void nullTranslateY_isSkippedDuringInterpolation() {
            var tx = new SimpleDoubleProperty(0.0);
            var slide = new SlideTransition(scheduler, Duration.millis(500), tx, null)
                    .setToX(200.0);

            slide.play();
            scheduler.runUntilIdle(1000);

            assertThat(tx.get()).isCloseTo(200.0, within(0.5));
        }

        @Test
        @DisplayName("NaN toX means X property is not modified")
        void nanToX_propertyNotModified() {
            var tx = new SimpleDoubleProperty(50.0);
            var ty = new SimpleDoubleProperty(0.0);
            var slide = new SlideTransition(scheduler, Duration.millis(500), tx, ty)
                    .setToY(100.0);
            // toX is NaN by default

            slide.play();
            scheduler.runUntilIdle(1000);

            // X should remain at its initial value since toX was NaN
            assertThat(tx.get()).isCloseTo(50.0, within(0.01));
            assertThat(ty.get()).isCloseTo(100.0, within(0.5));
        }

        @Test
        @DisplayName("NaN toY means Y property is not modified")
        void nanToY_propertyNotModified() {
            var tx = new SimpleDoubleProperty(0.0);
            var ty = new SimpleDoubleProperty(75.0);
            var slide = new SlideTransition(scheduler, Duration.millis(500), tx, ty)
                    .setToX(200.0);
            // toY is NaN by default

            slide.play();
            scheduler.runUntilIdle(1000);

            assertThat(tx.get()).isCloseTo(200.0, within(0.5));
            // Y should remain at its initial value since toY was NaN
            assertThat(ty.get()).isCloseTo(75.0, within(0.01));
        }

        @Test
        @DisplayName("fluent setters return same instance")
        void fluentSetters_returnSameInstance() {
            var slide = new SlideTransition(scheduler, Duration.millis(500), null, null);

            SlideTransition returned = slide.setToX(100.0);
            assertThat(returned).isSameAs(slide);

            returned = slide.setToY(50.0);
            assertThat(returned).isSameAs(slide);

            returned = slide.setFromX(0.0);
            assertThat(returned).isSameAs(slide);

            returned = slide.setFromY(0.0);
            assertThat(returned).isSameAs(slide);

            returned = slide.setInterpolator(Interpolator.LINEAR);
            assertThat(returned).isSameAs(slide);
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  PauseTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("PauseTransition")
    class PauseTransitionTests {

        @Test
        @DisplayName("does not modify any property during playback")
        void doesNotModifyAnyProperty_duringPlayback() {
            var prop = new SimpleDoubleProperty(42.0);
            var pause = new PauseTransition(scheduler, Duration.millis(500));

            pause.play();
            scheduler.runUntilIdle(1000);

            // Property should remain unchanged
            assertThat(prop.get()).isCloseTo(42.0, within(0.001));
        }

        @Test
        @DisplayName("getTotalDuration returns configured duration")
        void getTotalDuration_returnsConfiguredDuration() {
            var pause = new PauseTransition(scheduler, Duration.millis(1500));

            assertThat(pause.getTotalDuration().toMillis()).isEqualTo(1500.0);
        }

        @Test
        @DisplayName("completes naturally and fires onFinished callback")
        void completesNaturally_firesOnFinished() {
            var pause = new PauseTransition(scheduler, Duration.millis(500));
            var finished = new boolean[]{false};
            pause.setOnFinished(() -> finished[0] = true);

            pause.play();
            scheduler.runUntilIdle(1000);

            assertThat(pause.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
        }

        @Test
        @DisplayName("default scheduler constructor uses FrameScheduler.getDefault()")
        void defaultSchedulerConstructor_usesDefault() {
            var pause = new PauseTransition(Duration.millis(200));
            var finished = new boolean[]{false};
            pause.setOnFinished(() -> finished[0] = true);

            pause.play();
            scheduler.runUntilIdle(500);

            assertThat(finished[0]).isTrue();
        }
    }
}

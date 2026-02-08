package xss.it.jux.animations;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for {@link Timeline} -- keyframe-based animation driving properties
 * through a sequence of {@link KeyFrame}s with {@link KeyValue}s.
 */
@DisplayName("Timeline")
class TimelineTest {

    private TestFrameScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TestFrameScheduler();
        FrameScheduler.setDefault(scheduler);
    }

    // ── Single keyframe ──────────────────────────────────────────

    @Nested
    @DisplayName("Single keyframe at 1000ms")
    class SingleKeyframe {

        @Test
        @DisplayName("property transitions from start value to end value after full duration")
        void singleKeyframeProperty_reachesEndValue() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop.get()).isCloseTo(100.0, within(0.01));
        }

        @Test
        @DisplayName("property is at intermediate value at 50% elapsed time")
        void singleKeyframeProperty_intermediateValueAtHalfway() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();
            // First tick sets the start timestamp; advance exactly 500ms total
            scheduler.tick(0);    // first tick: startTimestamp captured at currentTime
            scheduler.tick(500);  // 500ms elapsed

            // With LINEAR interpolation, property should be around 50
            assertThat(prop.get()).isCloseTo(50.0, within(5.0));
        }
    }

    // ── Multi-keyframe ───────────────────────────────────────────

    @Nested
    @DisplayName("Multiple keyframes")
    class MultiKeyframe {

        @Test
        @DisplayName("property steps through multiple values across keyframes")
        void multiKeyframe_propertyStepsThroughValues() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 50.0))),
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 200.0))));

            timeline.play();
            scheduler.runUntilIdle(2000);

            // After full completion, should be at final keyframe value
            assertThat(prop.get()).isCloseTo(200.0, within(0.01));
        }

        @Test
        @DisplayName("multiple properties animated independently")
        void multiplePropertiesAnimatedIndependently_reachTheirEndValues() {
            var opacity = new SimpleDoubleProperty(1.0);
            var translateX = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(opacity, 0.0),
                                    new KeyValue<>(translateX, 200.0))));

            timeline.play();
            scheduler.runUntilIdle(2000);

            assertThat(opacity.get()).isCloseTo(0.0, within(0.01));
            assertThat(translateX.get()).isCloseTo(200.0, within(0.01));
        }
    }

    // ── Start value capture ──────────────────────────────────────

    @Nested
    @DisplayName("Start value capture")
    class StartValueCapture {

        @Test
        @DisplayName("start values captured at play time, not at construction time")
        void startValuesCapturedAtPlayTime_notAtConstruction() {
            var prop = new SimpleDoubleProperty(10.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            // Change the property value before play
            prop.set(50.0);
            timeline.play();
            scheduler.runUntilIdle(2000);

            // Should animate from 50 to 100, ending at 100
            assertThat(prop.get()).isCloseTo(100.0, within(0.01));
        }
    }

    // ── Duration ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Total duration")
    class TotalDuration {

        @Test
        @DisplayName("getTotalDuration returns max keyframe time")
        void getTotalDuration_returnsMaxKeyframeTime() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 50.0))),
                    new KeyFrame(Duration.millis(1500),
                            List.of(new KeyValue<>(prop, 150.0))),
                    new KeyFrame(Duration.millis(800),
                            List.of(new KeyValue<>(prop, 80.0))));

            assertThat(timeline.getTotalDuration().toMillis()).isEqualTo(1500.0);
        }

        @Test
        @DisplayName("zero duration timeline with empty keyframes")
        void zeroDurationTimeline_emptyKeyframes() {
            var timeline = new Timeline(scheduler);

            assertThat(timeline.getTotalDuration().toMillis()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("zero duration timeline finishes immediately on play")
        void zeroDurationTimeline_finishesImmediatelyOnPlay() {
            var timeline = new Timeline(scheduler);
            var finished = new boolean[]{false};
            timeline.setOnFinished(() -> finished[0] = true);

            timeline.play();
            scheduler.tick(0);

            assertThat(timeline.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
        }
    }

    // ── KeyFrames access ─────────────────────────────────────────

    @Nested
    @DisplayName("KeyFrames list")
    class KeyFramesList {

        @Test
        @DisplayName("getKeyFrames returns unmodifiable list")
        void getKeyFrames_returnsUnmodifiableList() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 50.0))));

            List<KeyFrame> keyFrames = timeline.getKeyFrames();

            assertThatThrownBy(() -> keyFrames.add(
                    new KeyFrame(Duration.millis(1000), List.of())))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    // ── getEndValue ──────────────────────────────────────────────

    @Nested
    @DisplayName("getEndValue")
    class GetEndValue {

        @Test
        @DisplayName("returns correct end value for animated property")
        void getEndValue_returnsCorrectEndValue() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 50.0))),
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 200.0))));

            Number endValue = timeline.getEndValue(prop);
            assertThat(endValue).isNotNull();
            assertThat(endValue.doubleValue()).isCloseTo(200.0, within(0.001));
        }

        @Test
        @DisplayName("returns null for non-animated property")
        void getEndValue_returnsNullForNonAnimatedProperty() {
            var animated = new SimpleDoubleProperty(0.0);
            var notAnimated = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(animated, 100.0))));

            assertThat(timeline.getEndValue(notAnimated)).isNull();
        }
    }

    // ── Lifecycle with TestFrameScheduler ─────────────────────────

    @Nested
    @DisplayName("Play/Pause/Stop lifecycle")
    class Lifecycle {

        @Test
        @DisplayName("play sets status to RUNNING")
        void play_setsStatusToRunning() {
            var prop = new SimpleDoubleProperty(0.0);
            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();

            assertThat(timeline.getStatus()).isEqualTo(Animation.Status.RUNNING);
        }

        @Test
        @DisplayName("pause sets status to PAUSED and resumes from same position")
        void pauseAndResume_resumesFromSamePosition() {
            var prop = new SimpleDoubleProperty(0.0);
            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();
            scheduler.tick(0);   // first tick captures start timestamp
            scheduler.tick(300); // advance 300ms

            double valueBeforePause = prop.get();
            timeline.pause();

            assertThat(timeline.getStatus()).isEqualTo(Animation.Status.PAUSED);

            // Resume
            timeline.play();
            scheduler.tick(0);   // captures new start timestamp
            scheduler.tick(200); // advance 200ms more

            // Property should have advanced further from the paused value
            assertThat(prop.get()).isGreaterThan(valueBeforePause);
        }

        @Test
        @DisplayName("stop sets status to STOPPED and fires onFinished")
        void stop_setsStatusToStoppedAndFiresOnFinished() {
            var prop = new SimpleDoubleProperty(0.0);
            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            var finished = new boolean[]{false};
            timeline.setOnFinished(() -> finished[0] = true);

            timeline.play();
            scheduler.tick(0);
            scheduler.tick(200);
            timeline.stop();

            assertThat(timeline.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
        }

        @Test
        @DisplayName("animation completes naturally and fires onFinished")
        void animationCompletesNaturally_firesOnFinished() {
            var prop = new SimpleDoubleProperty(0.0);
            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 100.0))));

            var finished = new boolean[]{false};
            timeline.setOnFinished(() -> finished[0] = true);

            timeline.play();
            scheduler.runUntilIdle(2000);

            assertThat(timeline.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
            assertThat(prop.get()).isCloseTo(100.0, within(0.01));
        }
    }

    // ── Interpolation boundaries ─────────────────────────────────

    @Nested
    @DisplayName("Interpolation at fraction boundaries")
    class InterpolationBoundaries {

        @Test
        @DisplayName("interpolate at frac 0.0 keeps property at start value")
        void interpolateAtFracZero_keepsStartValue() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            // Play to capture start values, then manually check first frame
            timeline.play();
            scheduler.tick(0);  // first frame, frac = 0.0

            // At frac 0.0, property should be at or very near start value
            assertThat(prop.get()).isCloseTo(0.0, within(1.0));
        }

        @Test
        @DisplayName("interpolate at frac 1.0 sets property to end value")
        void interpolateAtFracOne_setsEndValue() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(scheduler,
                    new KeyFrame(Duration.millis(1000),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop.get()).isCloseTo(100.0, within(0.01));
        }
    }

    // ── Constructor variants ─────────────────────────────────────

    @Nested
    @DisplayName("Constructor variants")
    class Constructors {

        @Test
        @DisplayName("varargs constructor creates timeline with correct keyframes")
        void varargsConstructor_createsTimelineWithCorrectKeyframes() {
            var prop = new SimpleDoubleProperty(0.0);

            var kf1 = new KeyFrame(Duration.millis(500), List.of(new KeyValue<>(prop, 50.0)));
            var kf2 = new KeyFrame(Duration.millis(1000), List.of(new KeyValue<>(prop, 100.0)));

            var timeline = new Timeline(scheduler, kf1, kf2);

            assertThat(timeline.getKeyFrames()).hasSize(2);
            assertThat(timeline.getTotalDuration().toMillis()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("list constructor creates timeline with correct keyframes")
        void listConstructor_createsTimelineWithCorrectKeyframes() {
            var prop = new SimpleDoubleProperty(0.0);

            var kf1 = new KeyFrame(Duration.millis(500), List.of(new KeyValue<>(prop, 50.0)));
            var kf2 = new KeyFrame(Duration.millis(1000), List.of(new KeyValue<>(prop, 100.0)));

            var timeline = new Timeline(scheduler, List.of(kf1, kf2));

            assertThat(timeline.getKeyFrames()).hasSize(2);
            assertThat(timeline.getTotalDuration().toMillis()).isEqualTo(1000.0);
        }

        @Test
        @DisplayName("default scheduler constructor uses FrameScheduler.getDefault()")
        void defaultSchedulerConstructor_usesFrameSchedulerDefault() {
            var prop = new SimpleDoubleProperty(0.0);

            var timeline = new Timeline(
                    new KeyFrame(Duration.millis(500),
                            List.of(new KeyValue<>(prop, 100.0))));

            timeline.play();
            scheduler.runUntilIdle(1000);

            // Since we set the default scheduler to our TestFrameScheduler,
            // the timeline should have run and updated the property
            assertThat(prop.get()).isCloseTo(100.0, within(0.01));
        }
    }
}

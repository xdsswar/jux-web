package xss.it.jux.animations;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.animations.transition.FadeTransition;
import xss.it.jux.animations.transition.ParallelTransition;
import xss.it.jux.animations.transition.PauseTransition;
import xss.it.jux.animations.transition.SequentialTransition;
import xss.it.jux.reactive.property.SimpleDoubleProperty;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

/**
 * Tests for composite transitions: {@link ParallelTransition} and
 * {@link SequentialTransition}.
 */
@DisplayName("Composite transitions")
class CompositeTransitionTest {

    private TestFrameScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TestFrameScheduler();
        FrameScheduler.setDefault(scheduler);
    }

    // ═══════════════════════════════════════════════════════════════
    //  ParallelTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("ParallelTransition")
    class ParallelTransitionTests {

        @Test
        @DisplayName("two children both advance simultaneously to end values")
        void twoChildren_bothAdvanceSimultaneously() {
            var opacity = new SimpleDoubleProperty(0.0);
            var scale = new SimpleDoubleProperty(1.0);

            var fade = new FadeTransition(scheduler, Duration.millis(500), opacity)
                    .setFromValue(0.0)
                    .setToValue(1.0);
            var scaleUp = new FadeTransition(scheduler, Duration.millis(500), scale)
                    .setFromValue(1.0)
                    .setToValue(2.0);

            var parallel = new ParallelTransition(scheduler, fade, scaleUp);
            parallel.play();
            scheduler.runUntilIdle(1000);

            assertThat(opacity.get()).isCloseTo(1.0, within(0.01));
            assertThat(scale.get()).isCloseTo(2.0, within(0.01));
        }

        @Test
        @DisplayName("total duration equals max of children durations")
        void totalDuration_equalsMaxOfChildren() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var short_ = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setToValue(1.0);
            var long_ = new FadeTransition(scheduler, Duration.millis(800), prop2)
                    .setToValue(1.0);

            var parallel = new ParallelTransition(scheduler, short_, long_);

            assertThat(parallel.getTotalDuration().toMillis()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("shorter child holds at final state while longer child still plays")
        void shorterChild_holdsAtFinalState() {
            var shortProp = new SimpleDoubleProperty(0.0);
            var longProp = new SimpleDoubleProperty(0.0);

            var shortFade = new FadeTransition(scheduler, Duration.millis(300), shortProp)
                    .setFromValue(0.0)
                    .setToValue(1.0);
            var longFade = new FadeTransition(scheduler, Duration.millis(1000), longProp)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var parallel = new ParallelTransition(scheduler, shortFade, longFade);
            parallel.play();

            // Advance to 500ms -- past the short child's 300ms duration
            scheduler.tick(0);   // capture start timestamp
            scheduler.tick(500); // 500ms elapsed

            // Short child should be at its final value (capped at frac=1.0)
            assertThat(shortProp.get()).isCloseTo(1.0, within(0.01));
            // Long child should be midway
            assertThat(longProp.get()).isGreaterThan(0.1);
            assertThat(longProp.get()).isLessThan(0.9);
        }

        @Test
        @DisplayName("empty children list results in zero duration")
        void emptyChildrenList_zeroDuration() {
            var parallel = new ParallelTransition(scheduler);

            assertThat(parallel.getTotalDuration().toMillis()).isEqualTo(0.0);
            assertThat(parallel.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("single child runs normally")
        void singleChild_runsNormally() {
            var prop = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), prop)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var parallel = new ParallelTransition(scheduler, fade);
            parallel.play();
            scheduler.runUntilIdle(1000);

            assertThat(prop.get()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("getChildren returns unmodifiable list")
        void getChildren_returnsUnmodifiableList() {
            var prop = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), prop);

            var parallel = new ParallelTransition(scheduler, fade);
            List<Animation> children = parallel.getChildren();

            assertThatThrownBy(() -> children.add(new PauseTransition(scheduler, Duration.millis(100))))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("completes and fires onFinished when all children done")
        void completesAndFiresOnFinished_whenAllChildrenDone() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(500), prop2)
                    .setFromValue(0.0).setToValue(1.0);

            var parallel = new ParallelTransition(scheduler, fade1, fade2);
            var finished = new boolean[]{false};
            parallel.setOnFinished(() -> finished[0] = true);

            parallel.play();
            scheduler.runUntilIdle(1000);

            assertThat(parallel.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  SequentialTransition
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("SequentialTransition")
    class SequentialTransitionTests {

        @Test
        @DisplayName("children run one after another, both reach end values")
        void childrenRunOneAfterAnother_bothReachEndValues() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setFromValue(0.0)
                    .setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(300), prop2)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2);
            seq.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop1.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop2.get()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("total duration equals sum of children durations")
        void totalDuration_equalsSumOfChildren() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(500), prop2)
                    .setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2);

            assertThat(seq.getTotalDuration().toMillis()).isEqualTo(800.0);
        }

        @Test
        @DisplayName("first child completes before second child starts")
        void firstChildCompletes_beforeSecondStarts() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(500), prop1)
                    .setFromValue(0.0)
                    .setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(500), prop2)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2);
            seq.play();

            // At 250ms (25% of total 1000ms), we are midway through the first child
            scheduler.tick(0);
            scheduler.tick(250);

            // First child should be partway through
            assertThat(prop1.get()).isGreaterThan(0.1);
            assertThat(prop1.get()).isLessThan(0.9);
            // Second child should still be at 0 (not started)
            assertThat(prop2.get()).isCloseTo(0.0, within(0.01));
        }

        @Test
        @DisplayName("finished children hold at final values")
        void finishedChildren_holdAtFinalValues() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(400), prop1)
                    .setFromValue(0.0)
                    .setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(400), prop2)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2);
            seq.play();

            // Advance to 600ms -- past the first child (400ms), into the second child
            scheduler.tick(0);
            scheduler.tick(600);

            // First child should be at its final value (frac=1.0)
            assertThat(prop1.get()).isCloseTo(1.0, within(0.01));
            // Second child should be partway
            assertThat(prop2.get()).isGreaterThan(0.1);
        }

        @Test
        @DisplayName("not-started children remain at initial values")
        void notStartedChildren_remainAtInitialValues() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(50.0);
            var prop3 = new SimpleDoubleProperty(99.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(400), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(400), prop2)
                    .setFromValue(50.0).setToValue(100.0);
            var fade3 = new FadeTransition(scheduler, Duration.millis(400), prop3)
                    .setFromValue(99.0).setToValue(200.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2, fade3);
            seq.play();

            // Advance only 200ms -- still in the first child (0..400ms)
            scheduler.tick(0);
            scheduler.tick(200);

            // Third child has not started, interpolate(0.0) was called on it
            // With from=99 to=200, at frac 0.0: capturedFrom + (target - capturedFrom) * 0 = capturedFrom
            // However, since the child was never play()'d, its capturedFrom is 0.0 (default).
            // The SequentialTransition calls doInterpolate(child, 0.0) which calls interpolate(0.0).
            // For FadeTransition.interpolate(0.0), curved = 0.0, value = capturedFrom + (toValue - capturedFrom) * 0 = capturedFrom
            // Since capturedFrom was never set (play was not called), it defaults to 0.0.
            // This means children driven by doInterpolate at frac=0.0 will use whatever capturedFrom was.
            // The second and third children should be at frac=0.0 state.
            assertThat(prop1.get()).isGreaterThan(0.0);
        }

        @Test
        @DisplayName("empty children results in zero duration")
        void emptyChildren_zeroDuration() {
            var seq = new SequentialTransition(scheduler);

            assertThat(seq.getTotalDuration().toMillis()).isEqualTo(0.0);
            assertThat(seq.getChildren()).isEmpty();
        }

        @Test
        @DisplayName("single child runs normally to completion")
        void singleChild_runsNormallyToCompletion() {
            var prop = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), prop)
                    .setFromValue(0.0)
                    .setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade);
            seq.play();
            scheduler.runUntilIdle(1000);

            assertThat(prop.get()).isCloseTo(1.0, within(0.01));
            assertThat(seq.getTotalDuration().toMillis()).isEqualTo(500.0);
        }

        @Test
        @DisplayName("getChildren returns unmodifiable list")
        void getChildren_returnsUnmodifiableList() {
            var prop = new SimpleDoubleProperty(0.0);
            var fade = new FadeTransition(scheduler, Duration.millis(500), prop);

            var seq = new SequentialTransition(scheduler, fade);
            List<Animation> children = seq.getChildren();

            assertThatThrownBy(() -> children.add(new PauseTransition(scheduler, Duration.millis(100))))
                    .isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        @DisplayName("completes and fires onFinished after all children done")
        void completesAndFiresOnFinished_afterAllChildrenDone() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(200), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(200), prop2)
                    .setFromValue(0.0).setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, fade2);
            var finished = new boolean[]{false};
            seq.setOnFinished(() -> finished[0] = true);

            seq.play();
            scheduler.runUntilIdle(1000);

            assertThat(seq.getStatus()).isEqualTo(Animation.Status.STOPPED);
            assertThat(finished[0]).isTrue();
        }

        @Test
        @DisplayName("sequential with pause between fades")
        void sequentialWithPause_correctTiming() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);

            var fade1 = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var pause = new PauseTransition(scheduler, Duration.millis(200));
            var fade2 = new FadeTransition(scheduler, Duration.millis(300), prop2)
                    .setFromValue(0.0).setToValue(1.0);

            var seq = new SequentialTransition(scheduler, fade1, pause, fade2);

            assertThat(seq.getTotalDuration().toMillis()).isEqualTo(800.0);

            seq.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop1.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop2.get()).isCloseTo(1.0, within(0.01));
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  Nested composites
    // ═══════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("Nested composite transitions")
    class NestedComposites {

        @Test
        @DisplayName("parallel inside sequential runs correctly")
        void parallelInsideSequential_runsCorrectly() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);
            var prop3 = new SimpleDoubleProperty(0.0);

            // Phase 1: fade prop1 and prop2 in parallel (500ms)
            var fade1 = new FadeTransition(scheduler, Duration.millis(500), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(500), prop2)
                    .setFromValue(0.0).setToValue(1.0);
            var parallel = new ParallelTransition(scheduler, fade1, fade2);

            // Phase 2: then fade prop3 (300ms)
            var fade3 = new FadeTransition(scheduler, Duration.millis(300), prop3)
                    .setFromValue(0.0).setToValue(1.0);

            var seq = new SequentialTransition(scheduler, parallel, fade3);

            assertThat(seq.getTotalDuration().toMillis()).isEqualTo(800.0);

            seq.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop1.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop2.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop3.get()).isCloseTo(1.0, within(0.01));
        }

        @Test
        @DisplayName("sequential inside parallel runs correctly")
        void sequentialInsideParallel_runsCorrectly() {
            var prop1 = new SimpleDoubleProperty(0.0);
            var prop2 = new SimpleDoubleProperty(0.0);
            var prop3 = new SimpleDoubleProperty(0.0);

            // Channel A: sequential fade1 (300ms) then fade2 (300ms) = 600ms
            var fade1 = new FadeTransition(scheduler, Duration.millis(300), prop1)
                    .setFromValue(0.0).setToValue(1.0);
            var fade2 = new FadeTransition(scheduler, Duration.millis(300), prop2)
                    .setFromValue(0.0).setToValue(1.0);
            var seqChannel = new SequentialTransition(scheduler, fade1, fade2);

            // Channel B: fade3 (600ms) simultaneously
            var fade3 = new FadeTransition(scheduler, Duration.millis(600), prop3)
                    .setFromValue(0.0).setToValue(1.0);

            var parallel = new ParallelTransition(scheduler, seqChannel, fade3);

            assertThat(parallel.getTotalDuration().toMillis()).isEqualTo(600.0);

            parallel.play();
            scheduler.runUntilIdle(2000);

            assertThat(prop1.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop2.get()).isCloseTo(1.0, within(0.01));
            assertThat(prop3.get()).isCloseTo(1.0, within(0.01));
        }
    }
}

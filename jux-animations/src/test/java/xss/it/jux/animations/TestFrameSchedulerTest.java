package xss.it.jux.animations;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for {@link TestFrameScheduler} -- deterministic scheduler for
 * testing animation logic without a real frame loop.
 */
@DisplayName("TestFrameScheduler")
class TestFrameSchedulerTest {

    private TestFrameScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new TestFrameScheduler();
    }

    // ── Initial state ────────────────────────────────────────────

    @Nested
    @DisplayName("Initial state")
    class InitialState {

        @Test
        @DisplayName("initial current time is 0")
        void initialTime_isZero() {
            assertThat(scheduler.getCurrentTime()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("tick with no pending callback does not throw")
        void tickWithNoPendingCallback_doesNotThrow() {
            assertThatCode(() -> scheduler.tick(16.67))
                    .doesNotThrowAnyException();
        }
    }

    // ── tick() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("tick()")
    class TickTests {

        @Test
        @DisplayName("tick advances current time by delta")
        void tick_advancesCurrentTime() {
            scheduler.tick(100.0);
            assertThat(scheduler.getCurrentTime()).isEqualTo(100.0);
        }

        @Test
        @DisplayName("multiple ticks accumulate time")
        void multipleTicks_accumulateTime() {
            scheduler.tick(10.0);
            scheduler.tick(20.0);
            scheduler.tick(30.0);
            assertThat(scheduler.getCurrentTime()).isEqualTo(60.0);
        }

        @Test
        @DisplayName("tick fires pending callback with current timestamp")
        void tick_firesPendingCallback_withTimestamp() {
            var receivedTimestamp = new AtomicReference<Double>();
            scheduler.requestFrame(receivedTimestamp::set);

            scheduler.tick(16.67);

            assertThat(receivedTimestamp.get()).isEqualTo(16.67);
        }

        @Test
        @DisplayName("callback is cleared after tick -- not called again")
        void tick_clearsCallbackAfterFiring() {
            var callCount = new AtomicInteger(0);
            scheduler.requestFrame(ts -> callCount.incrementAndGet());

            scheduler.tick(16.67);
            scheduler.tick(16.67);

            assertThat(callCount.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("callback can re-request frame during tick")
        void callback_canReRequestFrame() {
            var callCount = new AtomicInteger(0);

            scheduler.requestFrame(new FrameScheduler.FrameCallback() {
                @Override
                public void onFrame(double timestampMs) {
                    int count = callCount.incrementAndGet();
                    if (count < 3) {
                        scheduler.requestFrame(this);
                    }
                }
            });

            scheduler.tick(16.67);
            assertThat(callCount.get()).isEqualTo(1);

            scheduler.tick(16.67);
            assertThat(callCount.get()).isEqualTo(2);

            scheduler.tick(16.67);
            assertThat(callCount.get()).isEqualTo(3);

            // No more re-request after 3 calls
            scheduler.tick(16.67);
            assertThat(callCount.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("tick advances time even with no callback")
        void tick_advancesTime_evenWithNoCallback() {
            scheduler.tick(50.0);
            assertThat(scheduler.getCurrentTime()).isEqualTo(50.0);
        }
    }

    // ── cancelFrame() ────────────────────────────────────────────

    @Nested
    @DisplayName("cancelFrame()")
    class CancelFrameTests {

        @Test
        @DisplayName("cancelFrame prevents pending callback from firing")
        void cancelFrame_preventsCallbackFromFiring() {
            var called = new AtomicInteger(0);
            scheduler.requestFrame(ts -> called.incrementAndGet());

            scheduler.cancelFrame();
            scheduler.tick(16.67);

            assertThat(called.get()).isEqualTo(0);
        }

        @Test
        @DisplayName("cancelFrame when no callback pending does not throw")
        void cancelFrame_noPending_doesNotThrow() {
            assertThatCode(() -> scheduler.cancelFrame())
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("cancelFrame then re-request fires new callback")
        void cancelFrame_thenReRequest_firesNewCallback() {
            var firstCalled = new AtomicInteger(0);
            var secondCalled = new AtomicInteger(0);

            scheduler.requestFrame(ts -> firstCalled.incrementAndGet());
            scheduler.cancelFrame();
            scheduler.requestFrame(ts -> secondCalled.incrementAndGet());

            scheduler.tick(16.67);

            assertThat(firstCalled.get()).isEqualTo(0);
            assertThat(secondCalled.get()).isEqualTo(1);
        }
    }

    // ── runUntilIdle() ───────────────────────────────────────────

    @Nested
    @DisplayName("runUntilIdle()")
    class RunUntilIdleTests {

        @Test
        @DisplayName("runUntilIdle stops when no more callbacks are pending")
        void runUntilIdle_stopsWhenNoPendingCallbacks() {
            var callCount = new AtomicInteger(0);

            scheduler.requestFrame(new FrameScheduler.FrameCallback() {
                @Override
                public void onFrame(double timestampMs) {
                    int count = callCount.incrementAndGet();
                    if (count < 5) {
                        scheduler.requestFrame(this);
                    }
                }
            });

            scheduler.runUntilIdle(5000);

            assertThat(callCount.get()).isEqualTo(5);
        }

        @Test
        @DisplayName("runUntilIdle respects max time budget")
        void runUntilIdle_respectsMaxTimeBudget() {
            // Callback always re-requests, so it will never become idle
            scheduler.requestFrame(new FrameScheduler.FrameCallback() {
                @Override
                public void onFrame(double timestampMs) {
                    scheduler.requestFrame(this);
                }
            });

            double startTime = scheduler.getCurrentTime();
            scheduler.runUntilIdle(100.0);
            double elapsed = scheduler.getCurrentTime() - startTime;

            // Should have advanced approximately maxMs
            // Each tick is ~16.67ms, so 100/16.67 ~ 6 ticks = ~100ms
            assertThat(elapsed).isGreaterThanOrEqualTo(100.0);
        }

        @Test
        @DisplayName("runUntilIdle with no pending callback does nothing")
        void runUntilIdle_noPending_doesNotAdvanceTime() {
            double before = scheduler.getCurrentTime();
            scheduler.runUntilIdle(1000.0);
            assertThat(scheduler.getCurrentTime()).isEqualTo(before);
        }

        @Test
        @DisplayName("runUntilIdle advances time in ~16.67ms increments")
        void runUntilIdle_advancesInFrameIncrements() {
            var timestamps = new java.util.ArrayList<Double>();

            scheduler.requestFrame(new FrameScheduler.FrameCallback() {
                @Override
                public void onFrame(double timestampMs) {
                    timestamps.add(timestampMs);
                    if (timestamps.size() < 3) {
                        scheduler.requestFrame(this);
                    }
                }
            });

            scheduler.runUntilIdle(1000);

            assertThat(timestamps).hasSize(3);
            // Each tick should advance by approximately 16.67ms
            assertThat(timestamps.get(0)).isCloseTo(16.67, within(0.01));
            assertThat(timestamps.get(1)).isCloseTo(33.34, within(0.01));
            assertThat(timestamps.get(2)).isCloseTo(50.01, within(0.01));
        }
    }

    // ── getCurrentTime() ─────────────────────────────────────────

    @Nested
    @DisplayName("getCurrentTime()")
    class GetCurrentTimeTests {

        @Test
        @DisplayName("reflects accumulated tick deltas")
        void getCurrentTime_reflectsAccumulatedTicks() {
            scheduler.tick(10.0);
            assertThat(scheduler.getCurrentTime()).isEqualTo(10.0);

            scheduler.tick(5.5);
            assertThat(scheduler.getCurrentTime()).isEqualTo(15.5);

            scheduler.tick(0.5);
            assertThat(scheduler.getCurrentTime()).isEqualTo(16.0);
        }

        @Test
        @DisplayName("reflects time after runUntilIdle")
        void getCurrentTime_reflectsRunUntilIdleAdvance() {
            var count = new AtomicInteger(0);
            scheduler.requestFrame(new FrameScheduler.FrameCallback() {
                @Override
                public void onFrame(double timestampMs) {
                    if (count.incrementAndGet() < 10) {
                        scheduler.requestFrame(this);
                    }
                }
            });

            scheduler.runUntilIdle(5000);

            // 10 ticks * 16.67ms = ~166.7ms
            assertThat(scheduler.getCurrentTime()).isCloseTo(166.7, within(0.1));
        }
    }

    // ── requestFrame() ───────────────────────────────────────────

    @Nested
    @DisplayName("requestFrame()")
    class RequestFrameTests {

        @Test
        @DisplayName("latest requestFrame replaces previous pending callback")
        void requestFrame_replacedByLatestRequest() {
            var firstCalled = new AtomicInteger(0);
            var secondCalled = new AtomicInteger(0);

            scheduler.requestFrame(ts -> firstCalled.incrementAndGet());
            scheduler.requestFrame(ts -> secondCalled.incrementAndGet());

            scheduler.tick(16.67);

            assertThat(firstCalled.get()).isEqualTo(0);
            assertThat(secondCalled.get()).isEqualTo(1);
        }
    }
}

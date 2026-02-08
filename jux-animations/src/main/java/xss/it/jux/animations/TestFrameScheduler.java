package xss.it.jux.animations;

/**
 * A deterministic {@link FrameScheduler} for unit and integration tests.
 *
 * <p>Rather than relying on a real frame loop, tests call {@link #tick(double)}
 * to advance time by a specified delta and fire any pending frame callback.
 * The convenience method {@link #runUntilIdle(double)} repeatedly ticks at
 * ~60 fps until no more callbacks are pending or the time budget is exhausted.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * var scheduler = new TestFrameScheduler();
 * FrameScheduler.setDefault(scheduler);
 *
 * var timeline = new Timeline(scheduler, ...);
 * timeline.play();
 *
 * scheduler.tick(16.67);  // advance one frame
 * scheduler.runUntilIdle(2000); // run up to 2 seconds of animation
 * }</pre>
 */
public class TestFrameScheduler implements FrameScheduler {

    private FrameCallback pending;
    private double currentTime = 0;

    @Override
    public void requestFrame(FrameCallback callback) {
        pending = callback;
    }

    @Override
    public void cancelFrame() {
        pending = null;
    }

    /**
     * Advances time by {@code deltaMs} milliseconds and fires the pending
     * frame callback (if any). After the callback executes, the pending
     * slot is cleared; the callback must re-request a frame to receive
     * subsequent ticks.
     *
     * @param deltaMs the number of milliseconds to advance
     */
    public void tick(double deltaMs) {
        currentTime += deltaMs;
        if (pending != null) {
            FrameCallback cb = pending;
            pending = null;
            cb.onFrame(currentTime);
        }
    }

    /**
     * Repeatedly ticks at ~60 fps (16.67 ms increments) until either
     * no callback is pending or the total elapsed time since this method
     * was called reaches {@code maxMs}.
     *
     * @param maxMs the maximum number of milliseconds to simulate
     */
    public void runUntilIdle(double maxMs) {
        double end = currentTime + maxMs;
        while (pending != null && currentTime < end) {
            tick(16.67);
        }
    }

    /**
     * Returns the current simulated timestamp.
     *
     * @return the timestamp in milliseconds
     */
    public double getCurrentTime() {
        return currentTime;
    }
}

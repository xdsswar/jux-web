package xss.it.jux.animations;

/**
 * Abstraction over the platform frame loop.
 *
 * <p>On a real browser runtime the implementation would delegate to
 * {@code requestAnimationFrame}; in tests the {@link TestFrameScheduler}
 * provides deterministic manual ticking.</p>
 *
 * <p>A global default instance is accessible via {@link #getDefault()} and
 * can be replaced with {@link #setDefault(FrameScheduler)} &mdash; typically
 * done once at application startup or in test setup.</p>
 */
public interface FrameScheduler {

    /**
     * Requests a single animation frame. The callback will be invoked
     * with a monotonically increasing timestamp in milliseconds.
     *
     * @param callback the callback to invoke on the next frame
     */
    void requestFrame(FrameCallback callback);

    /**
     * Cancels a previously requested frame, if any.
     */
    void cancelFrame();

    /**
     * Callback interface for frame ticks.
     */
    @FunctionalInterface
    interface FrameCallback {

        /**
         * Called on each animation frame.
         *
         * @param timestampMs the current timestamp in milliseconds
         */
        void onFrame(double timestampMs);
    }

    /**
     * Returns the current default {@code FrameScheduler}.
     *
     * @return the default scheduler
     */
    static FrameScheduler getDefault() {
        return DefaultFrameSchedulerHolder.INSTANCE;
    }

    /**
     * Replaces the global default {@code FrameScheduler}.
     *
     * @param scheduler the new default scheduler
     */
    static void setDefault(FrameScheduler scheduler) {
        DefaultFrameSchedulerHolder.INSTANCE = scheduler;
    }
}

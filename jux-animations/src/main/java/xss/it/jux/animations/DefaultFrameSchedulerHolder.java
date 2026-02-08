package xss.it.jux.animations;

/**
 * Package-private holder for the global default {@link FrameScheduler} instance.
 *
 * <p>The initial value is a no-op scheduler that silently ignores frame
 * requests. Consumers should call {@link FrameScheduler#setDefault} at
 * startup to install a real or test implementation.</p>
 */
final class DefaultFrameSchedulerHolder {

    /** Mutable singleton &mdash; swapped via {@link FrameScheduler#setDefault}. */
    static volatile FrameScheduler INSTANCE = new NoOpFrameScheduler();

    private DefaultFrameSchedulerHolder() {
    }

    /**
     * A scheduler that does nothing. Serves as a safe default so that
     * code which obtains {@code FrameScheduler.getDefault()} before a
     * real scheduler has been installed will not throw.
     */
    private static final class NoOpFrameScheduler implements FrameScheduler {

        @Override
        public void requestFrame(FrameCallback callback) {
            // intentionally empty
        }

        @Override
        public void cancelFrame() {
            // intentionally empty
        }
    }
}

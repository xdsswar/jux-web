package xss.it.jux.animations;

import java.util.Objects;
import xss.it.jux.reactive.property.DoubleProperty;
import xss.it.jux.reactive.property.IntegerProperty;
import xss.it.jux.reactive.property.ObjectProperty;
import xss.it.jux.reactive.property.ReadOnlyDoubleProperty;
import xss.it.jux.reactive.property.ReadOnlyIntegerProperty;
import xss.it.jux.reactive.property.ReadOnlyObjectProperty;
import xss.it.jux.reactive.property.SimpleDoubleProperty;
import xss.it.jux.reactive.property.SimpleIntegerProperty;
import xss.it.jux.reactive.property.SimpleObjectProperty;

/**
 * Abstract base class for all animations in the JUX animation system.
 *
 * <p>An animation drives one or more property values over time using a
 * {@link FrameScheduler} to receive periodic frame callbacks. Subclasses
 * implement {@link #interpolate(double)} to update their target properties
 * at each fraction, and {@link #getTotalDuration()} to report the natural
 * length of a single cycle.</p>
 *
 * <p><b>Lifecycle:</b></p>
 * <ol>
 *   <li>{@link #play()} or {@link #playFromStart()} &rarr; status becomes {@link Status#RUNNING}</li>
 *   <li>The frame scheduler fires {@link #tick(double)} on each frame</li>
 *   <li>The animation computes elapsed time, handles rate, auto-reverse,
 *       and cycle counting, then calls {@link #interpolate(double)}</li>
 *   <li>{@link #pause()} &rarr; status becomes {@link Status#PAUSED}</li>
 *   <li>{@link #stop()} &rarr; status becomes {@link Status#STOPPED},
 *       {@code onFinished} callback is invoked</li>
 * </ol>
 *
 * <p>Observable properties allow listeners to react to status changes,
 * current time, and cycle progression.</p>
 */
public abstract class Animation {

    // ── Constants ────────────────────────────────────────────────

    /** Sentinel cycle count meaning "repeat forever". */
    public static final int INDEFINITE = -1;

    // ── Status enum ─────────────────────────────────────────────

    /** The playback state of an animation. */
    public enum Status {
        /** The animation is not running. */
        STOPPED,
        /** The animation is temporarily suspended. */
        PAUSED,
        /** The animation is actively playing. */
        RUNNING
    }

    // ── Fields ──────────────────────────────────────────────────

    private final FrameScheduler scheduler;

    private final ObjectProperty<Status> status =
            new SimpleObjectProperty<>(this, "status", Status.STOPPED);

    private final DoubleProperty rate =
            new SimpleDoubleProperty(this, "rate", 1.0);

    private final IntegerProperty cycleCount =
            new SimpleIntegerProperty(this, "cycleCount", 1);

    private final ObjectProperty<Boolean> autoReverse =
            new SimpleObjectProperty<>(this, "autoReverse", Boolean.FALSE);

    private final ObjectProperty<Runnable> onFinished =
            new SimpleObjectProperty<>(this, "onFinished", null);

    // Read-only wrappers for current time and current cycle
    private final SimpleDoubleProperty currentTime =
            new SimpleDoubleProperty(this, "currentTime", 0.0);

    private final SimpleIntegerProperty currentCycle =
            new SimpleIntegerProperty(this, "currentCycle", 0);

    // Internal frame-loop state
    private double startTimestamp = -1;
    private double pausedElapsed = 0;
    private double elapsedSinceStart = 0;

    // ── Constructor ─────────────────────────────────────────────

    /**
     * Creates an animation that uses the given frame scheduler.
     *
     * @param scheduler the scheduler supplying frame callbacks
     * @throws NullPointerException if scheduler is null
     */
    protected Animation(FrameScheduler scheduler) {
        this.scheduler = Objects.requireNonNull(scheduler, "scheduler");
    }

    // ── Abstract methods ────────────────────────────────────────

    /**
     * Updates target properties for the given cycle fraction.
     *
     * <p>Called on every frame while the animation is running. The
     * fraction is always in the range {@code [0..1]} and accounts
     * for rate, auto-reverse, and cycle counting.</p>
     *
     * @param frac the current fraction within the cycle, 0..1
     */
    protected abstract void interpolate(double frac);

    /**
     * Returns the natural duration of a single cycle.
     *
     * @return the total duration of one cycle
     */
    public abstract Duration getTotalDuration();

    // ── Playback control ────────────────────────────────────────

    /**
     * Starts (or resumes) the animation from its current position.
     *
     * <p>If the animation is currently {@link Status#PAUSED}, it resumes
     * from where it left off. If {@link Status#STOPPED}, it starts from
     * the beginning.</p>
     */
    public void play() {
        if (status.get() == Status.RUNNING) {
            return;
        }

        if (status.get() == Status.STOPPED) {
            elapsedSinceStart = 0;
            pausedElapsed = 0;
            currentCycle.set(0);
            currentTime.set(0.0);
        }

        status.set(Status.RUNNING);
        startTimestamp = -1; // will be set on first tick

        scheduler.requestFrame(this::tick);
    }

    /**
     * Stops the animation (if running), resets to the beginning, and
     * starts playing from time zero.
     */
    public void playFromStart() {
        stop();
        elapsedSinceStart = 0;
        pausedElapsed = 0;
        currentCycle.set(0);
        currentTime.set(0.0);
        play();
    }

    /**
     * Pauses the animation. Calling {@link #play()} afterwards will
     * resume from the paused position.
     */
    public void pause() {
        if (status.get() != Status.RUNNING) {
            return;
        }
        scheduler.cancelFrame();
        status.set(Status.PAUSED);
        pausedElapsed = elapsedSinceStart;
    }

    /**
     * Stops the animation and fires the {@code onFinished} callback
     * (if one is set).
     */
    public void stop() {
        if (status.get() == Status.STOPPED) {
            return;
        }
        scheduler.cancelFrame();
        status.set(Status.STOPPED);
        startTimestamp = -1;

        Runnable callback = onFinished.get();
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * Jumps the animation to the specified time offset within the
     * current cycle. Does not change the running status.
     *
     * @param time the target time offset
     */
    public void jumpTo(Duration time) {
        double totalMs = getTotalDuration().toMillis();
        if (totalMs <= 0) return;

        double targetMs = Math.max(0, Math.min(time.toMillis(), totalMs));
        double frac = targetMs / totalMs;

        currentTime.set(targetMs);
        interpolate(frac);

        // Adjust internal elapsed tracking so play() continues from here
        int cycle = currentCycle.get();
        elapsedSinceStart = cycle * totalMs + targetMs;
        pausedElapsed = elapsedSinceStart;
    }

    // ── Frame loop ──────────────────────────────────────────────

    /**
     * Called by the frame scheduler on each animation frame.
     *
     * <p>Computes elapsed time, handles rate, auto-reverse, and cycle
     * counting, then delegates to {@link #interpolate(double)}.</p>
     *
     * @param timestampMs the current frame timestamp in milliseconds
     */
    private void tick(double timestampMs) {
        if (status.get() != Status.RUNNING) {
            return;
        }

        if (startTimestamp < 0) {
            startTimestamp = timestampMs;
        }

        double currentRate = rate.get();
        double rawElapsed = (timestampMs - startTimestamp) * Math.abs(currentRate);
        elapsedSinceStart = pausedElapsed + rawElapsed;

        double totalMs = getTotalDuration().toMillis();
        if (totalMs <= 0) {
            // Zero-duration animation: snap to end
            interpolate(1.0);
            finishAnimation();
            return;
        }

        int maxCycles = cycleCount.get();
        boolean indefinite = (maxCycles == INDEFINITE);

        // Determine which cycle we are in
        int cycle = (int) (elapsedSinceStart / totalMs);
        double cycleElapsed = elapsedSinceStart - (cycle * totalMs);

        // Check if we have exceeded the total number of cycles
        if (!indefinite && cycle >= maxCycles) {
            cycle = maxCycles - 1;
            cycleElapsed = totalMs;

            currentCycle.set(cycle);
            currentTime.set(cycleElapsed);

            // Final fraction depends on auto-reverse
            double frac;
            boolean reversed = autoReverse.get() && (cycle % 2 == 1);
            frac = reversed ? 0.0 : 1.0;

            interpolate(frac);
            finishAnimation();
            return;
        }

        currentCycle.set(cycle);
        currentTime.set(cycleElapsed);

        // Compute fraction within the current cycle
        double frac = cycleElapsed / totalMs;
        frac = Math.max(0.0, Math.min(1.0, frac));

        // Handle auto-reverse: odd cycles play backwards
        boolean reversed = autoReverse.get() && (cycle % 2 == 1);
        if (reversed) {
            frac = 1.0 - frac;
        }

        // Handle negative rate (play backwards)
        if (currentRate < 0) {
            frac = 1.0 - frac;
        }

        interpolate(frac);

        // Request next frame
        scheduler.requestFrame(this::tick);
    }

    /**
     * Terminates the animation cleanly: cancels the frame, sets status
     * to STOPPED, and invokes the onFinished callback.
     */
    private void finishAnimation() {
        scheduler.cancelFrame();
        status.set(Status.STOPPED);
        startTimestamp = -1;

        Runnable callback = onFinished.get();
        if (callback != null) {
            callback.run();
        }
    }

    // ── Property accessors ──────────────────────────────────────

    /**
     * Returns the status property.
     *
     * @return the read-only status property
     */
    public ReadOnlyObjectProperty<Status> statusProperty() {
        return status;
    }

    /**
     * Returns the current status.
     *
     * @return the current status
     */
    public Status getStatus() {
        return status.get();
    }

    /**
     * Returns the rate property. The rate controls playback speed:
     * 1.0 = normal, 2.0 = double speed, -1.0 = reverse.
     *
     * @return the rate property
     */
    public DoubleProperty rateProperty() {
        return rate;
    }

    /**
     * Returns the current playback rate.
     *
     * @return the rate
     */
    public double getRate() {
        return rate.get();
    }

    /**
     * Sets the playback rate.
     *
     * @param value the new rate
     * @return this animation for fluent chaining
     */
    public Animation setRate(double value) {
        rate.set(value);
        return this;
    }

    /**
     * Returns the cycle count property.
     *
     * @return the cycle count property
     */
    public IntegerProperty cycleCountProperty() {
        return cycleCount;
    }

    /**
     * Returns the number of cycles this animation will play.
     *
     * @return the cycle count, or {@link #INDEFINITE}
     */
    public int getCycleCount() {
        return cycleCount.get();
    }

    /**
     * Sets the number of cycles to play.
     *
     * @param value the cycle count, or {@link #INDEFINITE}
     * @return this animation for fluent chaining
     */
    public Animation setCycleCount(int value) {
        cycleCount.set(value);
        return this;
    }

    /**
     * Returns the auto-reverse property.
     *
     * @return the auto-reverse property
     */
    public ObjectProperty<Boolean> autoReverseProperty() {
        return autoReverse;
    }

    /**
     * Returns whether the animation reverses direction on alternate cycles.
     *
     * @return {@code true} if auto-reverse is enabled
     */
    public boolean isAutoReverse() {
        return autoReverse.get();
    }

    /**
     * Sets whether the animation reverses direction on alternate cycles.
     *
     * @param value {@code true} to enable auto-reverse
     * @return this animation for fluent chaining
     */
    public Animation setAutoReverse(boolean value) {
        autoReverse.set(value);
        return this;
    }

    /**
     * Returns the onFinished property.
     *
     * @return the onFinished callback property
     */
    public ObjectProperty<Runnable> onFinishedProperty() {
        return onFinished;
    }

    /**
     * Returns the callback invoked when the animation finishes or is stopped.
     *
     * @return the callback, or {@code null}
     */
    public Runnable getOnFinished() {
        return onFinished.get();
    }

    /**
     * Sets a callback to be invoked when the animation finishes or is stopped.
     *
     * @param value the callback
     * @return this animation for fluent chaining
     */
    public Animation setOnFinished(Runnable value) {
        onFinished.set(value);
        return this;
    }

    /**
     * Returns the current time property (read-only). The value represents
     * the elapsed time within the current cycle, in milliseconds.
     *
     * @return the read-only current time property
     */
    public ReadOnlyDoubleProperty currentTimeProperty() {
        return currentTime;
    }

    /**
     * Returns the current elapsed time within the current cycle, in milliseconds.
     *
     * @return the current time in milliseconds
     */
    public double getCurrentTime() {
        return currentTime.get();
    }

    /**
     * Returns the current cycle property (read-only). Zero-based cycle
     * index that increments each time the animation completes one cycle.
     *
     * @return the read-only current cycle property
     */
    public ReadOnlyIntegerProperty currentCycleProperty() {
        return currentCycle;
    }

    /**
     * Returns the zero-based index of the current cycle.
     *
     * @return the current cycle index
     */
    public int getCurrentCycle() {
        return currentCycle.get();
    }

    /**
     * Returns the frame scheduler used by this animation.
     *
     * @return the frame scheduler
     */
    protected FrameScheduler getScheduler() {
        return scheduler;
    }

    /**
     * Drives a child animation to the given fraction.
     *
     * <p>This method exists so that composite animations (e.g.
     * {@code ParallelTransition}, {@code SequentialTransition}) in
     * subpackages can invoke {@link #interpolate(double)} on their
     * children, which would otherwise be inaccessible due to Java's
     * {@code protected} visibility rules across packages.</p>
     *
     * @param child the child animation to drive
     * @param frac  the cycle fraction, normally {@code [0..1]}
     */
    protected static void doInterpolate(Animation child, double frac) {
        child.interpolate(frac);
    }
}

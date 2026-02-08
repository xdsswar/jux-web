package xss.it.jux.animations.transition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;

/**
 * Runs multiple {@link Animation}s simultaneously.
 *
 * <p>The total duration equals the longest child animation's duration.
 * Each child receives a fraction scaled to its own duration relative to
 * the overall duration. Shorter children complete and hold their final
 * values while longer children continue.</p>
 *
 * <pre>{@code
 * var parallel = new ParallelTransition(
 *     new FadeTransition(Duration.millis(300), opacity).setToValue(0),
 *     new SlideTransition(Duration.millis(500), tx, null).setToX(200)
 * );
 * parallel.play();
 * }</pre>
 */
public class ParallelTransition extends Animation {

    private final List<Animation> children;
    private final Duration totalDuration;

    /**
     * Creates a parallel transition from the given children using a specific scheduler.
     *
     * @param scheduler the frame scheduler
     * @param children  the animations to run in parallel
     */
    public ParallelTransition(FrameScheduler scheduler, Animation... children) {
        super(scheduler);
        this.children = Collections.unmodifiableList(
                Arrays.asList(Objects.requireNonNull(children, "children")));
        this.totalDuration = computeMaxDuration(this.children);
    }

    /**
     * Creates a parallel transition from the given children using a specific scheduler.
     *
     * @param scheduler the frame scheduler
     * @param children  the animations to run in parallel
     */
    public ParallelTransition(FrameScheduler scheduler, List<Animation> children) {
        super(scheduler);
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
        this.totalDuration = computeMaxDuration(this.children);
    }

    /**
     * Creates a parallel transition using the default scheduler.
     *
     * @param children the animations to run in parallel
     */
    public ParallelTransition(Animation... children) {
        this(FrameScheduler.getDefault(), children);
    }

    /**
     * Creates a parallel transition using the default scheduler.
     *
     * @param children the animations to run in parallel
     */
    public ParallelTransition(List<Animation> children) {
        this(FrameScheduler.getDefault(), children);
    }

    @Override
    public Duration getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void interpolate(double frac) {
        double parentMs = totalDuration.toMillis();
        double currentMs = frac * parentMs;

        for (Animation child : children) {
            double childMs = child.getTotalDuration().toMillis();
            if (childMs <= 0) {
                doInterpolate(child, 1.0);
            } else {
                double childFrac = Math.min(1.0, currentMs / childMs);
                doInterpolate(child, childFrac);
            }
        }
    }

    /**
     * Returns the child animations.
     *
     * @return an unmodifiable list of children
     */
    public List<Animation> getChildren() {
        return children;
    }

    // ── Internal ────────────────────────────────────────────────

    private static Duration computeMaxDuration(List<Animation> children) {
        double maxMs = 0;
        for (Animation child : children) {
            double ms = child.getTotalDuration().toMillis();
            if (ms > maxMs) {
                maxMs = ms;
            }
        }
        return Duration.millis(maxMs);
    }
}

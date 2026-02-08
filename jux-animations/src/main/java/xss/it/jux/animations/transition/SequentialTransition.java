package xss.it.jux.animations.transition;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;

/**
 * Runs multiple {@link Animation}s one after another.
 *
 * <p>The total duration equals the sum of all child durations. At each
 * frame the sequential transition determines which child is active,
 * computes a local fraction within that child, and delegates to it.
 * Children that have already completed are held at their final state
 * (fraction&nbsp;=&nbsp;1.0).</p>
 *
 * <pre>{@code
 * var seq = new SequentialTransition(
 *     new FadeTransition(Duration.millis(300), opacity).setToValue(0),
 *     new PauseTransition(Duration.millis(200)),
 *     new FadeTransition(Duration.millis(300), opacity).setToValue(1)
 * );
 * seq.play();
 * }</pre>
 */
public class SequentialTransition extends Animation {

    private final List<Animation> children;
    private final Duration totalDuration;

    /** Precomputed cumulative end times in milliseconds for each child. */
    private final double[] cumulativeEndMs;

    /**
     * Creates a sequential transition from the given children using a specific scheduler.
     *
     * @param scheduler the frame scheduler
     * @param children  the animations to run in sequence
     */
    public SequentialTransition(FrameScheduler scheduler, Animation... children) {
        super(scheduler);
        this.children = Collections.unmodifiableList(
                Arrays.asList(Objects.requireNonNull(children, "children")));
        this.cumulativeEndMs = computeCumulativeEnd(this.children);
        this.totalDuration = computeTotalDuration(this.cumulativeEndMs);
    }

    /**
     * Creates a sequential transition from the given children using a specific scheduler.
     *
     * @param scheduler the frame scheduler
     * @param children  the animations to run in sequence
     */
    public SequentialTransition(FrameScheduler scheduler, List<Animation> children) {
        super(scheduler);
        this.children = List.copyOf(Objects.requireNonNull(children, "children"));
        this.cumulativeEndMs = computeCumulativeEnd(this.children);
        this.totalDuration = computeTotalDuration(this.cumulativeEndMs);
    }

    /**
     * Creates a sequential transition using the default scheduler.
     *
     * @param children the animations to run in sequence
     */
    public SequentialTransition(Animation... children) {
        this(FrameScheduler.getDefault(), children);
    }

    /**
     * Creates a sequential transition using the default scheduler.
     *
     * @param children the animations to run in sequence
     */
    public SequentialTransition(List<Animation> children) {
        this(FrameScheduler.getDefault(), children);
    }

    @Override
    public Duration getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void interpolate(double frac) {
        double totalMs = totalDuration.toMillis();
        double currentMs = frac * totalMs;

        for (int i = 0; i < children.size(); i++) {
            Animation child = children.get(i);
            double childStartMs = (i == 0) ? 0 : cumulativeEndMs[i - 1];
            double childEndMs = cumulativeEndMs[i];
            double childDurationMs = childEndMs - childStartMs;

            if (currentMs >= childEndMs) {
                // This child has completed -- hold at final state
                doInterpolate(child, 1.0);
            } else if (currentMs >= childStartMs) {
                // This child is active
                double childFrac;
                if (childDurationMs <= 0) {
                    childFrac = 1.0;
                } else {
                    childFrac = (currentMs - childStartMs) / childDurationMs;
                    childFrac = Math.max(0.0, Math.min(1.0, childFrac));
                }
                doInterpolate(child, childFrac);
                // Children after this one have not started, so stop here.
                // However, we should still ensure they are at frac=0.
                for (int j = i + 1; j < children.size(); j++) {
                    doInterpolate(children.get(j), 0.0);
                }
                return;
            } else {
                // This child has not started -- hold at initial state
                doInterpolate(child, 0.0);
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

    private static double[] computeCumulativeEnd(List<Animation> children) {
        double[] result = new double[children.size()];
        double sum = 0;
        for (int i = 0; i < children.size(); i++) {
            sum += children.get(i).getTotalDuration().toMillis();
            result[i] = sum;
        }
        return result;
    }

    private static Duration computeTotalDuration(double[] cumulativeEndMs) {
        if (cumulativeEndMs.length == 0) return Duration.ZERO;
        return Duration.millis(cumulativeEndMs[cumulativeEndMs.length - 1]);
    }
}

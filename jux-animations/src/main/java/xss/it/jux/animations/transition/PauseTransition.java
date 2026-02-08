package xss.it.jux.animations.transition;

import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;

/**
 * An animation that does nothing for a specified duration.
 *
 * <p>This is useful as a spacing element inside a
 * {@link SequentialTransition}, allowing a delay between two other
 * animations without introducing sleep or timer logic.</p>
 *
 * <pre>{@code
 * var seq = new SequentialTransition(
 *     fadeIn,
 *     new PauseTransition(Duration.seconds(2)),
 *     fadeOut
 * );
 * seq.play();
 * }</pre>
 */
public class PauseTransition extends Animation {

    private final Duration duration;

    /**
     * Creates a pause transition with the given scheduler.
     *
     * @param scheduler the frame scheduler
     * @param duration  the pause duration
     */
    public PauseTransition(FrameScheduler scheduler, Duration duration) {
        super(scheduler);
        this.duration = Objects.requireNonNull(duration, "duration");
    }

    /**
     * Creates a pause transition using the default scheduler.
     *
     * @param duration the pause duration
     */
    public PauseTransition(Duration duration) {
        this(FrameScheduler.getDefault(), duration);
    }

    @Override
    public Duration getTotalDuration() {
        return duration;
    }

    @Override
    protected void interpolate(double frac) {
        // intentionally empty -- this animation does not modify any property
    }
}

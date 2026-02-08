package xss.it.jux.animations.transition;

import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.reactive.property.DoubleProperty;

/**
 * Animates a scale {@link DoubleProperty} from a start value to an
 * end value over a configurable duration.
 *
 * <pre>{@code
 * var scale = new SimpleDoubleProperty(1.0);
 * var transition = new ScaleTransition(Duration.millis(300), scale)
 *     .setFromValue(1.0)
 *     .setToValue(1.5)
 *     .setInterpolator(Interpolator.EASE_BOTH);
 * transition.play();
 * }</pre>
 */
public class ScaleTransition extends Animation {

    private final Duration duration;
    private DoubleProperty property;
    private double fromValue = Double.NaN;
    private double toValue = Double.NaN;
    private Interpolator interpolator = Interpolator.LINEAR;

    private double capturedFrom;

    /**
     * Creates a scale transition with the given scheduler.
     *
     * @param scheduler the frame scheduler
     * @param duration  the animation duration
     * @param property  the scale property to animate
     */
    public ScaleTransition(FrameScheduler scheduler, Duration duration, DoubleProperty property) {
        super(scheduler);
        this.duration = Objects.requireNonNull(duration, "duration");
        this.property = property;
    }

    /**
     * Creates a scale transition using the default scheduler.
     *
     * @param duration the animation duration
     * @param property the scale property to animate
     */
    public ScaleTransition(Duration duration, DoubleProperty property) {
        this(FrameScheduler.getDefault(), duration, property);
    }

    /**
     * Creates a scale transition with just a duration using the default scheduler.
     *
     * @param duration the animation duration
     */
    public ScaleTransition(Duration duration) {
        this(FrameScheduler.getDefault(), duration, null);
    }

    @Override
    public void play() {
        Objects.requireNonNull(property, "property must be set before playing");
        capturedFrom = Double.isNaN(fromValue) ? property.get() : fromValue;
        super.play();
    }

    @Override
    public Duration getTotalDuration() {
        return duration;
    }

    @Override
    protected void interpolate(double frac) {
        double target = Double.isNaN(toValue) ? property.get() : toValue;
        double curved = interpolator.curve(frac);
        double value = capturedFrom + (target - capturedFrom) * curved;
        property.set(value);
    }

    // ── Fluent setters ──────────────────────────────────────────

    /**
     * Sets the target property.
     *
     * @param property the scale property
     * @return this transition for chaining
     */
    public ScaleTransition setProperty(DoubleProperty property) {
        this.property = Objects.requireNonNull(property, "property");
        return this;
    }

    /**
     * Sets the starting scale value. If not set, uses the property's current value.
     *
     * @param value the from value
     * @return this transition for chaining
     */
    public ScaleTransition setFromValue(double value) {
        this.fromValue = value;
        return this;
    }

    /**
     * Sets the ending scale value.
     *
     * @param value the to value
     * @return this transition for chaining
     */
    public ScaleTransition setToValue(double value) {
        this.toValue = value;
        return this;
    }

    /**
     * Sets the interpolator.
     *
     * @param interpolator the interpolator
     * @return this transition for chaining
     */
    public ScaleTransition setInterpolator(Interpolator interpolator) {
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator");
        return this;
    }

    /** Returns the target property. */
    public DoubleProperty getProperty() { return property; }

    /** Returns the configured from value, or {@code NaN}. */
    public double getFromValue() { return fromValue; }

    /** Returns the configured to value, or {@code NaN}. */
    public double getToValue() { return toValue; }

    /** Returns the interpolator. */
    public Interpolator getInterpolator() { return interpolator; }
}

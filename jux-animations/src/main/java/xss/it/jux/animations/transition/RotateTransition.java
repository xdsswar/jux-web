package xss.it.jux.animations.transition;

import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.reactive.property.DoubleProperty;

/**
 * Animates a rotation {@link DoubleProperty} (in degrees) from a start
 * value to an end value over a configurable duration.
 *
 * <pre>{@code
 * var rotation = new SimpleDoubleProperty(0);
 * var spin = new RotateTransition(Duration.seconds(1), rotation)
 *     .setFromValue(0)
 *     .setToValue(360)
 *     .setInterpolator(Interpolator.EASE_BOTH);
 * spin.play();
 * }</pre>
 */
public class RotateTransition extends Animation {

    private final Duration duration;
    private DoubleProperty property;
    private double fromValue = Double.NaN;
    private double toValue = Double.NaN;
    private Interpolator interpolator = Interpolator.LINEAR;

    private double capturedFrom;

    /**
     * Creates a rotate transition with the given scheduler.
     *
     * @param scheduler the frame scheduler
     * @param duration  the animation duration
     * @param property  the rotation property (degrees)
     */
    public RotateTransition(FrameScheduler scheduler, Duration duration, DoubleProperty property) {
        super(scheduler);
        this.duration = Objects.requireNonNull(duration, "duration");
        this.property = property;
    }

    /**
     * Creates a rotate transition using the default scheduler.
     *
     * @param duration the animation duration
     * @param property the rotation property (degrees)
     */
    public RotateTransition(Duration duration, DoubleProperty property) {
        this(FrameScheduler.getDefault(), duration, property);
    }

    /**
     * Creates a rotate transition with just a duration using the default scheduler.
     *
     * @param duration the animation duration
     */
    public RotateTransition(Duration duration) {
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
     * @param property the rotation property
     * @return this transition for chaining
     */
    public RotateTransition setProperty(DoubleProperty property) {
        this.property = Objects.requireNonNull(property, "property");
        return this;
    }

    /**
     * Sets the starting rotation (degrees). If not set, uses the property's current value.
     *
     * @param value the from value in degrees
     * @return this transition for chaining
     */
    public RotateTransition setFromValue(double value) {
        this.fromValue = value;
        return this;
    }

    /**
     * Sets the ending rotation (degrees).
     *
     * @param value the to value in degrees
     * @return this transition for chaining
     */
    public RotateTransition setToValue(double value) {
        this.toValue = value;
        return this;
    }

    /**
     * Sets the interpolator.
     *
     * @param interpolator the interpolator
     * @return this transition for chaining
     */
    public RotateTransition setInterpolator(Interpolator interpolator) {
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

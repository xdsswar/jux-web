package xss.it.jux.animations.transition;

import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.reactive.property.DoubleProperty;

/**
 * Animates a {@link DoubleProperty} representing opacity from a start
 * value to an end value over a configurable duration.
 *
 * <p>Typical usage fades an element in or out by targeting an opacity
 * property bound to the element's rendering:</p>
 * <pre>{@code
 * var opacity = new SimpleDoubleProperty(1.0);
 * var fade = new FadeTransition(Duration.seconds(0.3), opacity)
 *     .setFromValue(1.0)
 *     .setToValue(0.0)
 *     .setInterpolator(Interpolator.EASE_OUT);
 * fade.play();
 * }</pre>
 */
public class FadeTransition extends Animation {

    private final Duration duration;
    private DoubleProperty property;
    private double fromValue = Double.NaN;
    private double toValue = Double.NaN;
    private Interpolator interpolator = Interpolator.LINEAR;

    /** Captured start value at play time. */
    private double capturedFrom;

    /**
     * Creates a fade transition for the given property using a specific scheduler.
     *
     * @param scheduler the frame scheduler
     * @param duration  the animation duration
     * @param property  the property to animate
     */
    public FadeTransition(FrameScheduler scheduler, Duration duration, DoubleProperty property) {
        super(scheduler);
        this.duration = Objects.requireNonNull(duration, "duration");
        this.property = property;
    }

    /**
     * Creates a fade transition for the given property using the default scheduler.
     *
     * @param duration the animation duration
     * @param property the property to animate
     */
    public FadeTransition(Duration duration, DoubleProperty property) {
        this(FrameScheduler.getDefault(), duration, property);
    }

    /**
     * Creates a fade transition with just a duration, using the default scheduler.
     * The property must be set via {@link #setProperty(DoubleProperty)} before playing.
     *
     * @param duration the animation duration
     */
    public FadeTransition(Duration duration) {
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
     * @param property the property to animate
     * @return this transition for chaining
     */
    public FadeTransition setProperty(DoubleProperty property) {
        this.property = Objects.requireNonNull(property, "property");
        return this;
    }

    /**
     * Sets the starting opacity value. If not set, the property's
     * current value at play time is used.
     *
     * @param value the from value
     * @return this transition for chaining
     */
    public FadeTransition setFromValue(double value) {
        this.fromValue = value;
        return this;
    }

    /**
     * Sets the ending opacity value.
     *
     * @param value the to value
     * @return this transition for chaining
     */
    public FadeTransition setToValue(double value) {
        this.toValue = value;
        return this;
    }

    /**
     * Sets the interpolator for this transition.
     *
     * @param interpolator the interpolator
     * @return this transition for chaining
     */
    public FadeTransition setInterpolator(Interpolator interpolator) {
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator");
        return this;
    }

    /**
     * Returns the target property.
     *
     * @return the animated property
     */
    public DoubleProperty getProperty() {
        return property;
    }

    /**
     * Returns the configured from value, or {@code NaN} if not set.
     *
     * @return the from value
     */
    public double getFromValue() {
        return fromValue;
    }

    /**
     * Returns the configured to value, or {@code NaN} if not set.
     *
     * @return the to value
     */
    public double getToValue() {
        return toValue;
    }

    /**
     * Returns the interpolator.
     *
     * @return the interpolator
     */
    public Interpolator getInterpolator() {
        return interpolator;
    }
}

package xss.it.jux.animations.transition;

import java.util.Objects;
import xss.it.jux.animations.Animation;
import xss.it.jux.animations.Duration;
import xss.it.jux.animations.FrameScheduler;
import xss.it.jux.animations.Interpolator;
import xss.it.jux.reactive.property.DoubleProperty;

/**
 * Animates {@code translateX} and/or {@code translateY}
 * {@link DoubleProperty} values to slide an element across the screen.
 *
 * <p>Either or both properties may be set. Properties that are not set
 * are left untouched during playback.</p>
 *
 * <pre>{@code
 * var tx = new SimpleDoubleProperty(0);
 * var ty = new SimpleDoubleProperty(0);
 * var slide = new SlideTransition(Duration.millis(400), tx, ty)
 *     .setToX(200).setToY(50)
 *     .setInterpolator(Interpolator.EASE_BOTH);
 * slide.play();
 * }</pre>
 */
public class SlideTransition extends Animation {

    private final Duration duration;
    private DoubleProperty translateX;
    private DoubleProperty translateY;

    private double fromX = Double.NaN;
    private double fromY = Double.NaN;
    private double toX = Double.NaN;
    private double toY = Double.NaN;

    private Interpolator interpolator = Interpolator.LINEAR;

    private double capturedFromX;
    private double capturedFromY;

    /**
     * Creates a slide transition with the given scheduler and properties.
     *
     * @param scheduler  the frame scheduler
     * @param duration   the animation duration
     * @param translateX the horizontal translation property (may be {@code null})
     * @param translateY the vertical translation property (may be {@code null})
     */
    public SlideTransition(FrameScheduler scheduler, Duration duration,
                           DoubleProperty translateX, DoubleProperty translateY) {
        super(scheduler);
        this.duration = Objects.requireNonNull(duration, "duration");
        this.translateX = translateX;
        this.translateY = translateY;
    }

    /**
     * Creates a slide transition using the default scheduler.
     *
     * @param duration   the animation duration
     * @param translateX the horizontal translation property (may be {@code null})
     * @param translateY the vertical translation property (may be {@code null})
     */
    public SlideTransition(Duration duration, DoubleProperty translateX,
                           DoubleProperty translateY) {
        this(FrameScheduler.getDefault(), duration, translateX, translateY);
    }

    /**
     * Creates a slide transition with just a duration using the default scheduler.
     *
     * @param duration the animation duration
     */
    public SlideTransition(Duration duration) {
        this(FrameScheduler.getDefault(), duration, null, null);
    }

    @Override
    public void play() {
        if (translateX != null) {
            capturedFromX = Double.isNaN(fromX) ? translateX.get() : fromX;
        }
        if (translateY != null) {
            capturedFromY = Double.isNaN(fromY) ? translateY.get() : fromY;
        }
        super.play();
    }

    @Override
    public Duration getTotalDuration() {
        return duration;
    }

    @Override
    protected void interpolate(double frac) {
        double curved = interpolator.curve(frac);

        if (translateX != null && !Double.isNaN(toX)) {
            translateX.set(capturedFromX + (toX - capturedFromX) * curved);
        }
        if (translateY != null && !Double.isNaN(toY)) {
            translateY.set(capturedFromY + (toY - capturedFromY) * curved);
        }
    }

    // ── Fluent setters ──────────────────────────────────────────

    /**
     * Sets the translateX property.
     *
     * @param translateX the horizontal translation property
     * @return this transition for chaining
     */
    public SlideTransition setTranslateX(DoubleProperty translateX) {
        this.translateX = translateX;
        return this;
    }

    /**
     * Sets the translateY property.
     *
     * @param translateY the vertical translation property
     * @return this transition for chaining
     */
    public SlideTransition setTranslateY(DoubleProperty translateY) {
        this.translateY = translateY;
        return this;
    }

    /**
     * Sets the starting X value. If not set, uses the property's current value.
     *
     * @param value the from X value
     * @return this transition for chaining
     */
    public SlideTransition setFromX(double value) {
        this.fromX = value;
        return this;
    }

    /**
     * Sets the starting Y value. If not set, uses the property's current value.
     *
     * @param value the from Y value
     * @return this transition for chaining
     */
    public SlideTransition setFromY(double value) {
        this.fromY = value;
        return this;
    }

    /**
     * Sets the ending X value.
     *
     * @param value the target X value
     * @return this transition for chaining
     */
    public SlideTransition setToX(double value) {
        this.toX = value;
        return this;
    }

    /**
     * Sets the ending Y value.
     *
     * @param value the target Y value
     * @return this transition for chaining
     */
    public SlideTransition setToY(double value) {
        this.toY = value;
        return this;
    }

    /**
     * Sets the interpolator for this transition.
     *
     * @param interpolator the interpolator
     * @return this transition for chaining
     */
    public SlideTransition setInterpolator(Interpolator interpolator) {
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator");
        return this;
    }

    /** Returns the translateX property. */
    public DoubleProperty getTranslateX() { return translateX; }

    /** Returns the translateY property. */
    public DoubleProperty getTranslateY() { return translateY; }

    /** Returns the configured from X, or {@code NaN}. */
    public double getFromX() { return fromX; }

    /** Returns the configured from Y, or {@code NaN}. */
    public double getFromY() { return fromY; }

    /** Returns the configured to X, or {@code NaN}. */
    public double getToX() { return toX; }

    /** Returns the configured to Y, or {@code NaN}. */
    public double getToY() { return toY; }

    /** Returns the interpolator. */
    public Interpolator getInterpolator() { return interpolator; }
}

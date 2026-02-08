package xss.it.jux.animations;

import java.util.Objects;
import xss.it.jux.reactive.property.Property;

/**
 * Associates a {@link Property} target with a desired end value and
 * an optional {@link Interpolator} that controls how the value
 * transitions over time.
 *
 * <p>Key values are collected into {@link KeyFrame}s, which define
 * the animation at specific points along a {@link Timeline}.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * var opacity = new SimpleDoubleProperty(1.0);
 * var kv = new KeyValue<>(opacity, 0.0, Interpolator.EASE_OUT);
 * }</pre>
 *
 * @param <T> the type of the property value
 */
public final class KeyValue<T> {

    private final Property<T> target;
    private final T endValue;
    private final Interpolator interpolator;

    /**
     * Creates a key value with the specified interpolator.
     *
     * @param target       the property to animate
     * @param endValue     the desired value at the key frame time
     * @param interpolator the easing curve to apply
     * @throws NullPointerException if target or interpolator is null
     */
    public KeyValue(Property<T> target, T endValue, Interpolator interpolator) {
        this.target = Objects.requireNonNull(target, "target");
        this.endValue = endValue;
        this.interpolator = Objects.requireNonNull(interpolator, "interpolator");
    }

    /**
     * Creates a key value using {@link Interpolator#LINEAR}.
     *
     * @param target   the property to animate
     * @param endValue the desired value at the key frame time
     * @throws NullPointerException if target is null
     */
    public KeyValue(Property<T> target, T endValue) {
        this(target, endValue, Interpolator.LINEAR);
    }

    /**
     * Returns the target property.
     *
     * @return the animated property
     */
    public Property<T> getTarget() {
        return target;
    }

    /**
     * Returns the end value for the target property at the key frame time.
     *
     * @return the end value
     */
    public T getEndValue() {
        return endValue;
    }

    /**
     * Returns the interpolator that controls the easing curve.
     *
     * @return the interpolator
     */
    public Interpolator getInterpolator() {
        return interpolator;
    }

    @Override
    public String toString() {
        return "KeyValue[target=" + target + ", endValue=" + endValue
                + ", interpolator=" + interpolator + "]";
    }
}

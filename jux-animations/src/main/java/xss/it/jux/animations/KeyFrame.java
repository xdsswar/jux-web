package xss.it.jux.animations;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Defines a set of {@link KeyValue}s at a specific point in time within
 * a {@link Timeline}.
 *
 * <p>A key frame optionally carries a {@link Runnable} callback that is
 * invoked when the timeline reaches (or passes through) the frame's
 * time during playback.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * var kf = new KeyFrame(
 *     Duration.seconds(1),
 *     List.of(
 *         new KeyValue<>(opacity, 0.0, Interpolator.EASE_OUT),
 *         new KeyValue<>(translateX, 200.0)
 *     ),
 *     () -> System.out.println("Reached 1s")
 * );
 * }</pre>
 */
public final class KeyFrame {

    private final Duration time;
    private final List<KeyValue<?>> values;
    private final Runnable onFinished;

    /**
     * Creates a key frame with key values and an optional callback.
     *
     * @param time       the time offset within the timeline
     * @param values     the key values active at this time
     * @param onFinished an optional callback invoked when this frame is reached;
     *                   may be {@code null}
     * @throws NullPointerException if time or values is null
     */
    public KeyFrame(Duration time, List<KeyValue<?>> values, Runnable onFinished) {
        this.time = Objects.requireNonNull(time, "time");
        this.values = Collections.unmodifiableList(
                Objects.requireNonNull(values, "values"));
        this.onFinished = onFinished;
    }

    /**
     * Creates a key frame with key values and no callback.
     *
     * @param time   the time offset within the timeline
     * @param values the key values active at this time
     * @throws NullPointerException if time or values is null
     */
    public KeyFrame(Duration time, List<KeyValue<?>> values) {
        this(time, values, null);
    }

    /**
     * Creates a key frame with a single callback and no key values.
     * Useful for scheduling side-effects at specific timeline offsets.
     *
     * @param time       the time offset within the timeline
     * @param onFinished the callback to invoke
     * @throws NullPointerException if time is null
     */
    public KeyFrame(Duration time, Runnable onFinished) {
        this(time, List.of(), onFinished);
    }

    /**
     * Returns the time offset of this key frame within the timeline.
     *
     * @return the time offset
     */
    public Duration getTime() {
        return time;
    }

    /**
     * Returns the key values defined at this key frame.
     *
     * @return an unmodifiable list of key values
     */
    public List<KeyValue<?>> getValues() {
        return values;
    }

    /**
     * Returns the optional callback, or {@code null}.
     *
     * @return the callback, or {@code null}
     */
    public Runnable getOnFinished() {
        return onFinished;
    }

    @Override
    public String toString() {
        return "KeyFrame[time=" + time + ", values=" + values.size()
                + ", onFinished=" + (onFinished != null) + "]";
    }
}

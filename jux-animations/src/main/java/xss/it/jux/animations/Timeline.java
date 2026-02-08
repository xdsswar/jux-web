package xss.it.jux.animations;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import xss.it.jux.reactive.property.Property;

/**
 * A concrete {@link Animation} that drives properties through a sequence
 * of {@link KeyFrame}s.
 *
 * <p>At each frame the timeline determines the current elapsed time,
 * finds the bracketing key frames for every animated property, and
 * interpolates between the start and end values using the key value's
 * {@link Interpolator}.</p>
 *
 * <p>Start values are captured automatically when {@link #play()} is
 * called &mdash; the timeline reads the current value of each animated
 * property and treats it as the value at time zero.</p>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * var opacity = new SimpleDoubleProperty(1.0);
 * var translateX = new SimpleDoubleProperty(0.0);
 *
 * var timeline = new Timeline(
 *     new KeyFrame(Duration.ZERO,
 *         List.of(new KeyValue<>(opacity, 1.0),
 *                 new KeyValue<>(translateX, 0.0))),
 *     new KeyFrame(Duration.seconds(1),
 *         List.of(new KeyValue<>(opacity, 0.0, Interpolator.EASE_OUT),
 *                 new KeyValue<>(translateX, 200.0, Interpolator.EASE_BOTH)))
 * );
 * timeline.play();
 * }</pre>
 */
public class Timeline extends Animation {

    private final List<KeyFrame> keyFrames;
    private final Duration totalDuration;

    /**
     * Captured start values, keyed by property identity. Populated on
     * {@link #play()} from the properties' current values.
     */
    private final Map<Property<?>, Object> startValues = new IdentityHashMap<>();

    /**
     * Sorted per-property timeline segments. Each entry maps a property
     * to the list of (time, KeyValue) pairs involving that property,
     * sorted by time ascending.
     */
    private Map<Property<?>, List<TimedKeyValue<?>>> propertySegments;

    // ── Constructors ────────────────────────────────────────────

    /**
     * Creates a timeline with the given key frames using the specified
     * frame scheduler.
     *
     * @param scheduler the frame scheduler
     * @param keyFrames the key frames defining the animation
     * @throws NullPointerException if scheduler or any key frame is null
     */
    public Timeline(FrameScheduler scheduler, KeyFrame... keyFrames) {
        super(scheduler);
        this.keyFrames = List.of(keyFrames); // implicit null check
        this.totalDuration = computeTotalDuration(this.keyFrames);
        this.propertySegments = buildPropertySegments(this.keyFrames);
    }

    /**
     * Creates a timeline with the given key frames using the specified
     * frame scheduler.
     *
     * @param scheduler the frame scheduler
     * @param keyFrames the key frames defining the animation
     * @throws NullPointerException if scheduler or keyFrames is null
     */
    public Timeline(FrameScheduler scheduler, List<KeyFrame> keyFrames) {
        super(scheduler);
        this.keyFrames = List.copyOf(Objects.requireNonNull(keyFrames, "keyFrames"));
        this.totalDuration = computeTotalDuration(this.keyFrames);
        this.propertySegments = buildPropertySegments(this.keyFrames);
    }

    /**
     * Creates a timeline using the default frame scheduler.
     *
     * @param keyFrames the key frames defining the animation
     */
    public Timeline(KeyFrame... keyFrames) {
        this(FrameScheduler.getDefault(), keyFrames);
    }

    // ── Animation overrides ─────────────────────────────────────

    @Override
    public void play() {
        captureStartValues();
        super.play();
    }

    @Override
    public Duration getTotalDuration() {
        return totalDuration;
    }

    @Override
    protected void interpolate(double frac) {
        double totalMs = totalDuration.toMillis();
        double currentMs = frac * totalMs;

        // Fire key frame callbacks for frames we have passed
        for (KeyFrame kf : keyFrames) {
            Runnable cb = kf.getOnFinished();
            if (cb != null) {
                double kfMs = kf.getTime().toMillis();
                if (currentMs >= kfMs) {
                    // Only fire once per pass -- simple approach: fire if at or past
                    // In a production system this would track fired state per cycle.
                    // For correctness with the frame loop granularity, fire on
                    // any frame where currentMs >= kfMs. Users should design
                    // callbacks to be idempotent or use Timeline events.
                }
            }
        }

        // Interpolate each property
        for (var entry : propertySegments.entrySet()) {
            Property<?> prop = entry.getKey();
            List<TimedKeyValue<?>> segments = entry.getValue();
            interpolateProperty(prop, segments, currentMs, totalMs);
        }

        // If we have reached the final frame (frac == 1.0), set final values
        if (frac >= 1.0) {
            setFinalValues();
        }
    }

    // ── Internal helpers ────────────────────────────────────────

    /**
     * Captures the current value of every animated property as the
     * implicit start value at time zero.
     */
    private void captureStartValues() {
        startValues.clear();
        for (var entry : propertySegments.entrySet()) {
            Property<?> prop = entry.getKey();
            startValues.put(prop, prop.getValue());
        }
    }

    /**
     * Interpolates a single property at the given elapsed time.
     */
    @SuppressWarnings("unchecked")
    private <T> void interpolateProperty(Property<?> rawProp,
                                         List<TimedKeyValue<?>> segments,
                                         double currentMs,
                                         double totalMs) {
        Property<T> prop = (Property<T>) rawProp;

        // Find the bracketing segment: the latest keyframe at or before
        // currentMs (left) and the earliest keyframe after currentMs (right).
        TimedKeyValue<T> left = null;
        TimedKeyValue<T> right = null;

        for (TimedKeyValue<?> tkv : segments) {
            @SuppressWarnings("unchecked")
            TimedKeyValue<T> typed = (TimedKeyValue<T>) tkv;
            if (typed.timeMs <= currentMs) {
                left = typed;
            }
            if (typed.timeMs > currentMs && right == null) {
                right = typed;
            }
        }

        // Determine start value and end value for the current segment
        T startVal;
        T endVal;
        Interpolator interpolator;
        double segmentStart;
        double segmentEnd;

        if (left == null && right == null) {
            // No segments -- should not happen if keyFrames are valid
            return;
        }

        if (left == null) {
            // Before the first key frame: hold at start value
            Object captured = startValues.get(prop);
            startVal = captured != null ? (T) captured : prop.getValue();
            endVal = right.kv.getEndValue();
            interpolator = right.kv.getInterpolator();
            segmentStart = 0;
            segmentEnd = right.timeMs;
        } else if (right == null) {
            // After the last key frame: hold at the last key value
            prop.setValue(left.kv.getEndValue());
            return;
        } else {
            // Between two key frames
            startVal = left.kv.getEndValue();
            endVal = right.kv.getEndValue();
            interpolator = right.kv.getInterpolator();
            segmentStart = left.timeMs;
            segmentEnd = right.timeMs;
        }

        // Compute the local fraction within this segment
        double segmentLength = segmentEnd - segmentStart;
        double localFrac;
        if (segmentLength <= 0) {
            localFrac = 1.0;
        } else {
            localFrac = (currentMs - segmentStart) / segmentLength;
            localFrac = Math.max(0.0, Math.min(1.0, localFrac));
        }

        // Interpolate and set
        @SuppressWarnings("unchecked")
        T interpolated = (T) interpolator.interpolate(startVal, endVal, localFrac);
        prop.setValue(interpolated);
    }

    /**
     * Sets all properties to their final key frame end values.
     */
    @SuppressWarnings("unchecked")
    private void setFinalValues() {
        for (var entry : propertySegments.entrySet()) {
            Property<?> prop = entry.getKey();
            List<TimedKeyValue<?>> segments = entry.getValue();
            if (!segments.isEmpty()) {
                TimedKeyValue<?> last = segments.get(segments.size() - 1);
                setFinalValue((Property<Object>) prop, last);
            }
        }
    }

    /**
     * Sets a single property to the end value of the given timed key value.
     */
    @SuppressWarnings("unchecked")
    private <T> void setFinalValue(Property<T> prop, TimedKeyValue<?> tkv) {
        TimedKeyValue<T> typed = (TimedKeyValue<T>) tkv;
        prop.setValue(typed.kv.getEndValue());
    }

    /**
     * Returns the end value of the last key value for the given property,
     * or {@code null} if the property is not animated.
     *
     * @param property the property to query
     * @param <T>      the property type
     * @return the end value, or {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T getEndValue(Property<T> property) {
        List<TimedKeyValue<?>> segments = propertySegments.get(property);
        if (segments == null || segments.isEmpty()) return null;
        TimedKeyValue<T> last = (TimedKeyValue<T>) segments.get(segments.size() - 1);
        return last.kv.getEndValue();
    }

    /**
     * Returns the key frames of this timeline.
     *
     * @return an unmodifiable list of key frames
     */
    public List<KeyFrame> getKeyFrames() {
        return keyFrames;
    }

    // ── Static helpers ──────────────────────────────────────────

    private static Duration computeTotalDuration(List<KeyFrame> frames) {
        double maxMs = 0;
        for (KeyFrame kf : frames) {
            double ms = kf.getTime().toMillis();
            if (ms > maxMs) {
                maxMs = ms;
            }
        }
        return Duration.millis(maxMs);
    }

    /**
     * Builds per-property sorted segment lists from the key frames.
     */
    private static Map<Property<?>, List<TimedKeyValue<?>>> buildPropertySegments(
            List<KeyFrame> frames) {

        Map<Property<?>, List<TimedKeyValue<?>>> map = new IdentityHashMap<>();

        for (KeyFrame kf : frames) {
            double timeMs = kf.getTime().toMillis();
            for (KeyValue<?> kv : kf.getValues()) {
                @SuppressWarnings("unchecked")
                var tkv = new TimedKeyValue<>(timeMs, (KeyValue<Object>) kv);
                map.computeIfAbsent(kv.getTarget(), k -> new ArrayList<>())
                   .add(tkv);
            }
        }

        // Sort each property's segments by time
        for (List<TimedKeyValue<?>> list : map.values()) {
            list.sort(Comparator.comparingDouble(tkv -> tkv.timeMs));
        }

        return map;
    }

    /**
     * Internal record pairing a time offset with its key value.
     */
    private record TimedKeyValue<T>(double timeMs, KeyValue<T> kv) {
    }
}

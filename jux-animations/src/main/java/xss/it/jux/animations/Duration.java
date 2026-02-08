package xss.it.jux.animations;

/**
 * Immutable time value used throughout the animation system.
 *
 * <p>Durations represent a length of time in milliseconds and provide
 * convenient factory methods for creating values from milliseconds,
 * seconds, and minutes. Arithmetic operations return new instances;
 * {@code Duration} objects are never mutated.</p>
 *
 * <p>Special sentinel values:</p>
 * <ul>
 *   <li>{@link #ZERO} &mdash; zero-length duration</li>
 *   <li>{@link #ONE} &mdash; one millisecond</li>
 *   <li>{@link #INDEFINITE} &mdash; infinite duration (for indefinite animations)</li>
 * </ul>
 */
public final class Duration implements Comparable<Duration> {

    private final double millis;

    /** A duration of zero milliseconds. */
    public static final Duration ZERO = new Duration(0);

    /** A duration of one millisecond. */
    public static final Duration ONE = new Duration(1);

    /** An infinite duration, used for animations that repeat indefinitely. */
    public static final Duration INDEFINITE = new Duration(Double.POSITIVE_INFINITY);

    private Duration(double millis) {
        this.millis = millis;
    }

    /**
     * Creates a duration from the given number of milliseconds.
     *
     * @param ms the duration in milliseconds
     * @return a new {@code Duration}
     */
    public static Duration millis(double ms) {
        return new Duration(ms);
    }

    /**
     * Creates a duration from the given number of seconds.
     *
     * @param s the duration in seconds
     * @return a new {@code Duration}
     */
    public static Duration seconds(double s) {
        return new Duration(s * 1000);
    }

    /**
     * Creates a duration from the given number of minutes.
     *
     * @param m the duration in minutes
     * @return a new {@code Duration}
     */
    public static Duration minutes(double m) {
        return new Duration(m * 60_000);
    }

    /**
     * Returns this duration expressed in milliseconds.
     *
     * @return the duration in milliseconds
     */
    public double toMillis() {
        return millis;
    }

    /**
     * Returns this duration expressed in seconds.
     *
     * @return the duration in seconds
     */
    public double toSeconds() {
        return millis / 1000;
    }

    /**
     * Returns a new duration that is the sum of this duration and the other.
     *
     * @param other the duration to add
     * @return a new {@code Duration} representing the sum
     */
    public Duration add(Duration other) {
        return millis(this.millis + other.millis);
    }

    /**
     * Returns a new duration that is this duration minus the other.
     *
     * @param other the duration to subtract
     * @return a new {@code Duration} representing the difference
     */
    public Duration subtract(Duration other) {
        return millis(this.millis - other.millis);
    }

    /**
     * Returns a new duration scaled by the given factor.
     *
     * @param factor the multiplication factor
     * @return a new {@code Duration} representing the scaled value
     */
    public Duration multiply(double factor) {
        return millis(this.millis * factor);
    }

    /**
     * Returns a new duration divided by the given divisor.
     *
     * @param divisor the divisor
     * @return a new {@code Duration} representing the divided value
     */
    public Duration divide(double divisor) {
        return millis(this.millis / divisor);
    }

    /**
     * Returns a new duration with the negated value.
     *
     * @return a new {@code Duration} with the opposite sign
     */
    public Duration negate() {
        return millis(-this.millis);
    }

    /**
     * Returns {@code true} if this duration is strictly less than the other.
     *
     * @param other the duration to compare against
     * @return {@code true} if this &lt; other
     */
    public boolean lessThan(Duration other) {
        return millis < other.millis;
    }

    /**
     * Returns {@code true} if this duration is less than or equal to the other.
     *
     * @param other the duration to compare against
     * @return {@code true} if this &le; other
     */
    public boolean lessThanOrEqualTo(Duration other) {
        return millis <= other.millis;
    }

    /**
     * Returns {@code true} if this duration is strictly greater than the other.
     *
     * @param other the duration to compare against
     * @return {@code true} if this &gt; other
     */
    public boolean greaterThan(Duration other) {
        return millis > other.millis;
    }

    /**
     * Returns {@code true} if this duration is greater than or equal to the other.
     *
     * @param other the duration to compare against
     * @return {@code true} if this &ge; other
     */
    public boolean greaterThanOrEqualTo(Duration other) {
        return millis >= other.millis;
    }

    /**
     * Returns {@code true} if this duration represents positive infinity.
     *
     * @return {@code true} if indefinite
     */
    public boolean isIndefinite() {
        return Double.isInfinite(millis);
    }

    /**
     * Returns {@code true} if the underlying value is {@code NaN}.
     *
     * @return {@code true} if unknown
     */
    public boolean isUnknown() {
        return Double.isNaN(millis);
    }

    @Override
    public int compareTo(Duration other) {
        return Double.compare(millis, other.millis);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Duration d && Double.compare(millis, d.millis) == 0;
    }

    @Override
    public int hashCode() {
        return Double.hashCode(millis);
    }

    @Override
    public String toString() {
        return millis + "ms";
    }
}

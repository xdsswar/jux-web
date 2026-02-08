package xss.it.jux.animations;

/**
 * Maps a normalized time fraction {@code [0..1]} to a curve value, and
 * interpolates between a start and end value using that curve.
 *
 * <p>Built-in instances cover the most common easing curves:</p>
 * <ul>
 *   <li>{@link #LINEAR} &mdash; constant speed</li>
 *   <li>{@link #DISCRETE} &mdash; snaps to end value at {@code t == 1}</li>
 *   <li>{@link #EASE_IN}, {@link #EASE_OUT}, {@link #EASE_BOTH} &mdash; cubic easing</li>
 *   <li>{@link #OVERSHOOT}, {@link #BOUNCE}, {@link #ELASTIC} &mdash; physics-based</li>
 * </ul>
 *
 * <p>Custom curves can be created with the static factory methods
 * {@link #spline(double, double, double, double)},
 * {@link #spring(double, double)},
 * {@link #steps(int)}, and
 * {@link #overshoot(double)}.</p>
 */
public abstract class Interpolator {

    // ── Built-in curve instances ─────────────────────────────────

    /** Constant speed &mdash; output equals input. */
    public static final Interpolator LINEAR = new Interpolator() {
        @Override
        public double curve(double t) {
            return t;
        }

        @Override
        public String toString() {
            return "Interpolator.LINEAR";
        }
    };

    /** Holds the start value until {@code t == 1}, then jumps to the end value. */
    public static final Interpolator DISCRETE = new Interpolator() {
        @Override
        public double curve(double t) {
            return (t >= 1.0) ? 1.0 : 0.0;
        }

        @Override
        public String toString() {
            return "Interpolator.DISCRETE";
        }
    };

    /** Starts slow, accelerates. Cubic {@code t^3}. */
    public static final Interpolator EASE_IN = new Interpolator() {
        @Override
        public double curve(double t) {
            return t * t * t;
        }

        @Override
        public String toString() {
            return "Interpolator.EASE_IN";
        }
    };

    /** Starts fast, decelerates. Cubic {@code 1 - (1-t)^3}. */
    public static final Interpolator EASE_OUT = new Interpolator() {
        @Override
        public double curve(double t) {
            double inv = 1.0 - t;
            return 1.0 - inv * inv * inv;
        }

        @Override
        public String toString() {
            return "Interpolator.EASE_OUT";
        }
    };

    /** Starts slow, speeds up, then decelerates. Smooth-step cubic. */
    public static final Interpolator EASE_BOTH = new Interpolator() {
        @Override
        public double curve(double t) {
            return t * t * (3.0 - 2.0 * t);
        }

        @Override
        public String toString() {
            return "Interpolator.EASE_BOTH";
        }
    };

    /** Overshoots the target slightly before settling. Default tension = 1.70158. */
    public static final Interpolator OVERSHOOT = overshoot(1.70158);

    /** Bounces at the end like a ball dropping onto a hard surface. */
    public static final Interpolator BOUNCE = new Interpolator() {
        @Override
        public double curve(double t) {
            return bounceOut(t);
        }

        @Override
        public String toString() {
            return "Interpolator.BOUNCE";
        }
    };

    /** Elastic snap-back effect, like a rubber band releasing. */
    public static final Interpolator ELASTIC = new Interpolator() {
        @Override
        public double curve(double t) {
            if (t <= 0.0) return 0.0;
            if (t >= 1.0) return 1.0;
            double p = 0.3;
            double s = p / 4.0;
            return Math.pow(2.0, -10.0 * t) * Math.sin((t - s) * (2.0 * Math.PI) / p) + 1.0;
        }

        @Override
        public String toString() {
            return "Interpolator.ELASTIC";
        }
    };

    // ── Factory methods ──────────────────────────────────────────

    /**
     * Creates a cubic-bezier interpolator defined by two control points.
     *
     * <p>The curve starts at (0,0) and ends at (1,1). The two control
     * points {@code (x1,y1)} and {@code (x2,y2)} shape the curve,
     * matching the CSS {@code cubic-bezier()} function.</p>
     *
     * @param x1 x of the first control point (0..1)
     * @param y1 y of the first control point
     * @param x2 x of the second control point (0..1)
     * @param y2 y of the second control point
     * @return a new spline interpolator
     * @throws IllegalArgumentException if x1 or x2 is outside [0,1]
     */
    public static Interpolator spline(double x1, double y1, double x2, double y2) {
        return new SplineInterpolator(x1, y1, x2, y2);
    }

    /**
     * Creates a spring-dynamics interpolator.
     *
     * <p>Simulates a damped spring settling towards the target value.
     * Higher stiffness produces a snappier motion; lower damping
     * produces more oscillation.</p>
     *
     * @param damping   the damping ratio (0 = no damping, 1 = critically damped)
     * @param stiffness the spring stiffness (higher = faster)
     * @return a new spring interpolator
     */
    public static Interpolator spring(double damping, double stiffness) {
        return new Interpolator() {
            @Override
            public double curve(double t) {
                // Damped spring: x(t) = 1 - e^(-damping*stiffness*t) * cos(omega*t)
                double omega = Math.sqrt(stiffness) * (1.0 - damping * 0.5);
                double decay = Math.exp(-damping * stiffness * t);
                return 1.0 - decay * Math.cos(omega * t * Math.PI * 2.0);
            }

            @Override
            public String toString() {
                return "Interpolator.spring(damping=" + damping + ", stiffness=" + stiffness + ")";
            }
        };
    }

    /**
     * Creates a stepped interpolator that divides the animation into
     * {@code count} discrete steps with instant jumps.
     *
     * @param count the number of steps (must be &ge; 1)
     * @return a new stepped interpolator
     * @throws IllegalArgumentException if count &lt; 1
     */
    public static Interpolator steps(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("Step count must be >= 1, got: " + count);
        }
        return new Interpolator() {
            @Override
            public double curve(double t) {
                if (t >= 1.0) return 1.0;
                return Math.floor(t * count) / count;
            }

            @Override
            public String toString() {
                return "Interpolator.steps(" + count + ")";
            }
        };
    }

    /**
     * Creates a back/overshoot interpolator with the specified tension.
     *
     * <p>Higher tension values produce a more pronounced overshoot.
     * A tension of 0 is equivalent to {@link #EASE_OUT}.</p>
     *
     * @param tension the overshoot tension (typically 1.0 to 3.0)
     * @return a new overshoot interpolator
     */
    public static Interpolator overshoot(double tension) {
        return new Interpolator() {
            @Override
            public double curve(double t) {
                double t1 = t - 1.0;
                return t1 * t1 * ((tension + 1.0) * t1 + tension) + 1.0;
            }

            @Override
            public String toString() {
                return "Interpolator.overshoot(" + tension + ")";
            }
        };
    }

    // ── Abstract curve method ────────────────────────────────────

    /**
     * Maps a linear time fraction to a curved value.
     *
     * <p>Input and output are nominally in the range {@code [0..1]}, but
     * physics-based curves (overshoot, elastic, spring) may produce
     * values outside that range.</p>
     *
     * @param t the input fraction, normally in {@code [0..1]}
     * @return the curved output value
     */
    public abstract double curve(double t);

    // ── Value interpolation ──────────────────────────────────────

    /**
     * Interpolates between two values using this curve.
     *
     * <p>Supported types:</p>
     * <ul>
     *   <li>{@link Double} &mdash; linear blend</li>
     *   <li>{@link Integer} &mdash; linear blend, rounded</li>
     *   <li>{@link Long} &mdash; linear blend, rounded</li>
     *   <li>All other types &mdash; returns {@code start} until
     *       {@code fraction >= 1.0}, then returns {@code end}</li>
     * </ul>
     *
     * @param start    the starting value
     * @param end      the ending value
     * @param fraction the raw linear fraction {@code [0..1]}
     * @return the interpolated value
     */
    public Object interpolate(Object start, Object end, double fraction) {
        double curved = curve(clamp(fraction));

        if (start instanceof Double s && end instanceof Double e) {
            return s + (e - s) * curved;
        }
        if (start instanceof Integer s && end instanceof Integer e) {
            return (int) Math.round(s + (e - s) * curved);
        }
        if (start instanceof Long s && end instanceof Long e) {
            return Math.round(s + (e - s) * curved);
        }

        // Non-numeric types: snap at completion
        return (fraction >= 1.0) ? end : start;
    }

    // ── Internal helpers ─────────────────────────────────────────

    private static double clamp(double t) {
        if (t < 0.0) return 0.0;
        if (t > 1.0) return 1.0;
        return t;
    }

    /** Bounce-out helper used by {@link #BOUNCE}. */
    private static double bounceOut(double t) {
        if (t < 1.0 / 2.75) {
            return 7.5625 * t * t;
        } else if (t < 2.0 / 2.75) {
            double t2 = t - 1.5 / 2.75;
            return 7.5625 * t2 * t2 + 0.75;
        } else if (t < 2.5 / 2.75) {
            double t2 = t - 2.25 / 2.75;
            return 7.5625 * t2 * t2 + 0.9375;
        } else {
            double t2 = t - 2.625 / 2.75;
            return 7.5625 * t2 * t2 + 0.984375;
        }
    }

    // ── SplineInterpolator (cubic bezier) ────────────────────────

    /**
     * Cubic bezier interpolator matching the CSS {@code cubic-bezier()} function.
     *
     * <p>Uses Newton-Raphson iteration to invert the X(t) parametric
     * equation, then evaluates Y at the resulting parameter.</p>
     */
    private static final class SplineInterpolator extends Interpolator {

        private final double x1, y1, x2, y2;

        SplineInterpolator(double x1, double y1, double x2, double y2) {
            if (x1 < 0 || x1 > 1) {
                throw new IllegalArgumentException("x1 must be in [0,1], got: " + x1);
            }
            if (x2 < 0 || x2 > 1) {
                throw new IllegalArgumentException("x2 must be in [0,1], got: " + x2);
            }
            this.x1 = x1;
            this.y1 = y1;
            this.x2 = x2;
            this.y2 = y2;
        }

        @Override
        public double curve(double t) {
            if (t <= 0.0) return 0.0;
            if (t >= 1.0) return 1.0;

            // Find the parametric value 'p' such that bezierX(p) == t
            double p = t; // initial guess
            for (int i = 0; i < 20; i++) {
                double x = bezierX(p) - t;
                if (Math.abs(x) < 1e-7) break;
                double dx = bezierDx(p);
                if (Math.abs(dx) < 1e-12) break;
                p -= x / dx;
            }

            return bezierY(p);
        }

        /** Evaluate the X coordinate of the cubic bezier at parameter p. */
        private double bezierX(double p) {
            double oneMinusP = 1.0 - p;
            return 3.0 * oneMinusP * oneMinusP * p * x1
                 + 3.0 * oneMinusP * p * p * x2
                 + p * p * p;
        }

        /** Derivative of bezierX with respect to p. */
        private double bezierDx(double p) {
            double oneMinusP = 1.0 - p;
            return 3.0 * oneMinusP * oneMinusP * x1
                 + 6.0 * oneMinusP * p * (x2 - x1)
                 + 3.0 * p * p * (1.0 - x2);
        }

        /** Evaluate the Y coordinate of the cubic bezier at parameter p. */
        private double bezierY(double p) {
            double oneMinusP = 1.0 - p;
            return 3.0 * oneMinusP * oneMinusP * p * y1
                 + 3.0 * oneMinusP * p * p * y2
                 + p * p * p;
        }

        @Override
        public String toString() {
            return "Interpolator.spline(" + x1 + ", " + y1 + ", " + x2 + ", " + y2 + ")";
        }
    }
}

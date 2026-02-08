package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.internal.Logging;
import xss.it.jux.reactive.value.ObservableBooleanValue;
import xss.it.jux.reactive.value.ObservableDoubleValue;
import xss.it.jux.reactive.value.ObservableIntegerValue;
import xss.it.jux.reactive.value.ObservableLongValue;
import xss.it.jux.reactive.value.ObservableNumberValue;
import xss.it.jux.reactive.value.ObservableObjectValue;
import xss.it.jux.reactive.value.ObservableStringValue;
import xss.it.jux.reactive.value.ObservableValue;

import java.util.concurrent.Callable;

/**
 * Bindings is a helper class with a lot of utility functions to create simple
 * bindings.
 * <p>
 * Usually there are two variants of each binding function. One takes all values
 * as {@link Observable} parameters and computes the binding based on the
 * type of these observables. The other variant takes some of the values as
 * constant values (e.g. int, String). The constant values are then wrapped
 * in constant observable implementations (e.g. {@link IntegerConstant},
 * {@link StringConstant}).
 * <p>
 * There are no Float variants in this class. The type dispatch for number
 * bindings checks Double > Long > Integer.
 * <p>
 * All binding factory methods that accept variable dependencies use direct
 * {@link InvalidationListener} references (no {@code WeakReference}).
 *
 * @see Binding
 * @see NumberBinding
 */
public final class Bindings {

    private Bindings() {
    }

    // =================================================================================================================
    // Helper: getDependencies from varargs
    // =================================================================================================================

    private static ObservableList<?> makeDependencies(Observable... dependencies) {
        if (dependencies == null || dependencies.length == 0) {
            return JuxCollections.emptyObservableList();
        }
        if (dependencies.length == 1) {
            return JuxCollections.singletonObservableList(dependencies[0]);
        }
        return JuxCollections.unmodifiableObservableList(
                JuxCollections.observableArrayList(dependencies));
    }

    // =================================================================================================================
    // Custom binding factories
    // =================================================================================================================

    /**
     * Helper function to create a custom {@link BooleanBinding}.
     *
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static BooleanBinding createBooleanBinding(final Callable<Boolean> func,
                                                       final Observable... dependencies) {
        return new BooleanBinding() {
            {
                bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return false;
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    /**
     * Helper function to create a custom {@link DoubleBinding}.
     *
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static DoubleBinding createDoubleBinding(final Callable<Double> func,
                                                     final Observable... dependencies) {
        return new DoubleBinding() {
            {
                bind(dependencies);
            }

            @Override
            protected double computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return 0.0;
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    /**
     * Helper function to create a custom {@link IntegerBinding}.
     *
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static IntegerBinding createIntegerBinding(final Callable<Integer> func,
                                                       final Observable... dependencies) {
        return new IntegerBinding() {
            {
                bind(dependencies);
            }

            @Override
            protected int computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return 0;
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    /**
     * Helper function to create a custom {@link LongBinding}.
     *
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static LongBinding createLongBinding(final Callable<Long> func,
                                                 final Observable... dependencies) {
        return new LongBinding() {
            {
                bind(dependencies);
            }

            @Override
            protected long computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return 0L;
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    /**
     * Helper function to create a custom {@link StringBinding}.
     *
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static StringBinding createStringBinding(final Callable<String> func,
                                                     final Observable... dependencies) {
        return new StringBinding() {
            {
                bind(dependencies);
            }

            @Override
            protected String computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return "";
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    /**
     * Helper function to create a custom {@link ObjectBinding}.
     *
     * @param <T>          the type of the bound value
     * @param func         a function that calculates the value of this binding
     * @param dependencies the dependencies of this binding
     * @return the generated binding
     */
    public static <T> ObjectBinding<T> createObjectBinding(final Callable<T> func,
                                                            final Observable... dependencies) {
        return new ObjectBinding<>() {
            {
                bind(dependencies);
            }

            @Override
            protected T computeValue() {
                try {
                    return func.call();
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while evaluating binding", e);
                    return null;
                }
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }
        };
    }

    // =================================================================================================================
    // When (ternary expression)
    // =================================================================================================================

    /**
     * Creates a binding that calculates a ternary expression.
     *
     * @param condition the condition of the ternary expression
     * @return an intermediate class to build the complete binding
     * @see When
     */
    public static When when(final ObservableBooleanValue condition) {
        return new When(condition);
    }

    // =================================================================================================================
    // Negation
    // =================================================================================================================

    /**
     * Creates a new {@link NumberBinding} that calculates the negation of an
     * {@link ObservableNumberValue}.
     * <p>
     * Type dispatch: Double > Long > Integer (no Float).
     *
     * @param value the operand
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if the value is {@code null}
     */
    public static NumberBinding negate(final ObservableNumberValue value) {
        if (value == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        if (value instanceof ObservableDoubleValue) {
            return new DoubleBinding() {
                {
                    super.bind(value);
                }

                @Override
                protected double computeValue() {
                    return -value.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.singletonObservableList(value);
                }

                @Override
                public void dispose() {
                    super.unbind(value);
                }
            };
        } else if (value instanceof ObservableLongValue) {
            return new LongBinding() {
                {
                    super.bind(value);
                }

                @Override
                protected long computeValue() {
                    return -value.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.singletonObservableList(value);
                }

                @Override
                public void dispose() {
                    super.unbind(value);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(value);
                }

                @Override
                protected int computeValue() {
                    return -value.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.singletonObservableList(value);
                }

                @Override
                public void dispose() {
                    super.unbind(value);
                }
            };
        }
    }

    // =================================================================================================================
    // Add
    // =================================================================================================================

    private static NumberBinding add(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                     final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return op1.doubleValue() + op2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return op1.longValue() + op2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return op1.intValue() + op2.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the sum of the
     * values of two instances of {@link ObservableNumberValue}.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static NumberBinding add(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return add(op1, op2, op1, op2);
    }

    /**
     * Creates a new {@link DoubleBinding} that calculates the sum of the
     * value of an {@link ObservableNumberValue} and a constant value.
     *
     * @param op1 the {@code ObservableNumberValue}
     * @param op2 the constant value
     * @return the new {@code DoubleBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static DoubleBinding add(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) add(op1, DoubleConstant.valueOf(op2), op1);
    }

    /**
     * Creates a new {@link DoubleBinding} that calculates the sum of a
     * constant value and the value of an {@link ObservableNumberValue}.
     *
     * @param op1 the constant value
     * @param op2 the {@code ObservableNumberValue}
     * @return the new {@code DoubleBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static DoubleBinding add(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) add(DoubleConstant.valueOf(op1), op2, op2);
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the sum of the
     * value of an {@link ObservableNumberValue} and a constant value.
     *
     * @param op1 the {@code ObservableNumberValue}
     * @param op2 the constant value
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static NumberBinding add(final ObservableNumberValue op1, long op2) {
        return add(op1, LongConstant.valueOf(op2), op1);
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the sum of a
     * constant value and the value of an {@link ObservableNumberValue}.
     *
     * @param op1 the constant value
     * @param op2 the {@code ObservableNumberValue}
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static NumberBinding add(long op1, final ObservableNumberValue op2) {
        return add(LongConstant.valueOf(op1), op2, op2);
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the sum of the
     * value of an {@link ObservableNumberValue} and a constant value.
     *
     * @param op1 the {@code ObservableNumberValue}
     * @param op2 the constant value
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static NumberBinding add(final ObservableNumberValue op1, int op2) {
        return add(op1, IntegerConstant.valueOf(op2), op1);
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the sum of a
     * constant value and the value of an {@link ObservableNumberValue}.
     *
     * @param op1 the constant value
     * @param op2 the {@code ObservableNumberValue}
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if the {@code ObservableNumberValue} is {@code null}
     */
    public static NumberBinding add(int op1, final ObservableNumberValue op2) {
        return add(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Subtract
    // =================================================================================================================

    private static NumberBinding subtract(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                           final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return op1.doubleValue() - op2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return op1.longValue() - op2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return op1.intValue() - op2.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the difference of the
     * values of two instances of {@link ObservableNumberValue}.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static NumberBinding subtract(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return subtract(op1, op2, op1, op2);
    }

    public static DoubleBinding subtract(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) subtract(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static DoubleBinding subtract(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) subtract(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding subtract(final ObservableNumberValue op1, long op2) {
        return subtract(op1, LongConstant.valueOf(op2), op1);
    }

    public static NumberBinding subtract(long op1, final ObservableNumberValue op2) {
        return subtract(LongConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding subtract(final ObservableNumberValue op1, int op2) {
        return subtract(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static NumberBinding subtract(int op1, final ObservableNumberValue op2) {
        return subtract(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Multiply
    // =================================================================================================================

    private static NumberBinding multiply(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                           final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return op1.doubleValue() * op2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return op1.longValue() * op2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return op1.intValue() * op2.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the product of the
     * values of two instances of {@link ObservableNumberValue}.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static NumberBinding multiply(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return multiply(op1, op2, op1, op2);
    }

    public static DoubleBinding multiply(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) multiply(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static DoubleBinding multiply(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) multiply(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding multiply(final ObservableNumberValue op1, long op2) {
        return multiply(op1, LongConstant.valueOf(op2), op1);
    }

    public static NumberBinding multiply(long op1, final ObservableNumberValue op2) {
        return multiply(LongConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding multiply(final ObservableNumberValue op1, int op2) {
        return multiply(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static NumberBinding multiply(int op1, final ObservableNumberValue op2) {
        return multiply(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Divide
    // =================================================================================================================

    private static NumberBinding divide(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                         final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return op1.doubleValue() / op2.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return op1.longValue() / op2.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return op1.intValue() / op2.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    /**
     * Creates a new {@link NumberBinding} that calculates the division of the
     * values of two instances of {@link ObservableNumberValue}.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code NumberBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static NumberBinding divide(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return divide(op1, op2, op1, op2);
    }

    public static DoubleBinding divide(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) divide(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static DoubleBinding divide(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) divide(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding divide(final ObservableNumberValue op1, long op2) {
        return divide(op1, LongConstant.valueOf(op2), op1);
    }

    public static NumberBinding divide(long op1, final ObservableNumberValue op2) {
        return divide(LongConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding divide(final ObservableNumberValue op1, int op2) {
        return divide(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static NumberBinding divide(int op1, final ObservableNumberValue op2) {
        return divide(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Equals (numbers)
    // =================================================================================================================

    private static BooleanBinding equal(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                         final double epsilon, final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
                    return Math.abs(op1.doubleValue() - op2.doubleValue()) <= epsilon;
                } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
                    return Math.abs(op1.longValue() - op2.longValue()) <= epsilon;
                } else {
                    return Math.abs(op1.intValue() - op2.intValue()) <= epsilon;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the values
     * of two instances of {@link ObservableNumberValue} are equal (with a
     * tolerance).
     *
     * @param op1     the first operand
     * @param op2     the second operand
     * @param epsilon the permitted tolerance
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding equal(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                        final double epsilon) {
        return equal(op1, op2, epsilon, op1, op2);
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the values
     * of two instances of {@link ObservableNumberValue} are equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding equal(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return equal(op1, op2, 0.0, op1, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, double op2, double epsilon) {
        return equal(op1, DoubleConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding equal(double op1, final ObservableNumberValue op2, double epsilon) {
        return equal(DoubleConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, double op2) {
        return equal(op1, DoubleConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding equal(double op1, final ObservableNumberValue op2) {
        return equal(DoubleConstant.valueOf(op1), op2, 0.0, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, long op2, double epsilon) {
        return equal(op1, LongConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding equal(long op1, final ObservableNumberValue op2, double epsilon) {
        return equal(LongConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, long op2) {
        return equal(op1, LongConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding equal(long op1, final ObservableNumberValue op2) {
        return equal(LongConstant.valueOf(op1), op2, 0.0, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, int op2, double epsilon) {
        return equal(op1, IntegerConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding equal(int op1, final ObservableNumberValue op2, double epsilon) {
        return equal(IntegerConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding equal(final ObservableNumberValue op1, int op2) {
        return equal(op1, IntegerConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding equal(int op1, final ObservableNumberValue op2) {
        return equal(IntegerConstant.valueOf(op1), op2, 0.0, op2);
    }

    // =================================================================================================================
    // NotEqual (numbers)
    // =================================================================================================================

    private static BooleanBinding notEqual(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                            final double epsilon, final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
                    return Math.abs(op1.doubleValue() - op2.doubleValue()) > epsilon;
                } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
                    return Math.abs(op1.longValue() - op2.longValue()) > epsilon;
                } else {
                    return Math.abs(op1.intValue() - op2.intValue()) > epsilon;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                           double epsilon) {
        return notEqual(op1, op2, epsilon, op1, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return notEqual(op1, op2, 0.0, op1, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, double op2, double epsilon) {
        return notEqual(op1, DoubleConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding notEqual(double op1, final ObservableNumberValue op2, double epsilon) {
        return notEqual(DoubleConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, double op2) {
        return notEqual(op1, DoubleConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding notEqual(double op1, final ObservableNumberValue op2) {
        return notEqual(DoubleConstant.valueOf(op1), op2, 0.0, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, long op2, double epsilon) {
        return notEqual(op1, LongConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding notEqual(long op1, final ObservableNumberValue op2, double epsilon) {
        return notEqual(LongConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, long op2) {
        return notEqual(op1, LongConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding notEqual(long op1, final ObservableNumberValue op2) {
        return notEqual(LongConstant.valueOf(op1), op2, 0.0, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, int op2, double epsilon) {
        return notEqual(op1, IntegerConstant.valueOf(op2), epsilon, op1);
    }

    public static BooleanBinding notEqual(int op1, final ObservableNumberValue op2, double epsilon) {
        return notEqual(IntegerConstant.valueOf(op1), op2, epsilon, op2);
    }

    public static BooleanBinding notEqual(final ObservableNumberValue op1, int op2) {
        return notEqual(op1, IntegerConstant.valueOf(op2), 0.0, op1);
    }

    public static BooleanBinding notEqual(int op1, final ObservableNumberValue op2) {
        return notEqual(IntegerConstant.valueOf(op1), op2, 0.0, op2);
    }

    // =================================================================================================================
    // GreaterThan (numbers)
    // =================================================================================================================

    private static BooleanBinding greaterThan(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                               final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
                    return op1.doubleValue() > op2.doubleValue();
                } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
                    return op1.longValue() > op2.longValue();
                } else {
                    return op1.intValue() > op2.intValue();
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding greaterThan(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return greaterThan(op1, op2, op1, op2);
    }

    public static BooleanBinding greaterThan(final ObservableNumberValue op1, double op2) {
        return greaterThan(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThan(double op1, final ObservableNumberValue op2) {
        return greaterThan(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding greaterThan(final ObservableNumberValue op1, long op2) {
        return greaterThan(op1, LongConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThan(long op1, final ObservableNumberValue op2) {
        return greaterThan(LongConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding greaterThan(final ObservableNumberValue op1, int op2) {
        return greaterThan(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThan(int op1, final ObservableNumberValue op2) {
        return greaterThan(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // LessThan (numbers) - delegates to greaterThan with swapped operands
    // =================================================================================================================

    private static BooleanBinding lessThan(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                            final Observable... dependencies) {
        return greaterThan(op2, op1, dependencies);
    }

    public static BooleanBinding lessThan(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return lessThan(op1, op2, op1, op2);
    }

    public static BooleanBinding lessThan(final ObservableNumberValue op1, double op2) {
        return lessThan(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThan(double op1, final ObservableNumberValue op2) {
        return lessThan(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding lessThan(final ObservableNumberValue op1, long op2) {
        return lessThan(op1, LongConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThan(long op1, final ObservableNumberValue op2) {
        return lessThan(LongConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding lessThan(final ObservableNumberValue op1, int op2) {
        return lessThan(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThan(int op1, final ObservableNumberValue op2) {
        return lessThan(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // GreaterThanOrEqual (numbers)
    // =================================================================================================================

    private static BooleanBinding greaterThanOrEqual(final ObservableNumberValue op1,
                                                      final ObservableNumberValue op2,
                                                      final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
                    return op1.doubleValue() >= op2.doubleValue();
                } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
                    return op1.longValue() >= op2.longValue();
                } else {
                    return op1.intValue() >= op2.intValue();
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableNumberValue op1,
                                                     final ObservableNumberValue op2) {
        return greaterThanOrEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableNumberValue op1, double op2) {
        return greaterThanOrEqual(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThanOrEqual(double op1, final ObservableNumberValue op2) {
        return greaterThanOrEqual(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableNumberValue op1, long op2) {
        return greaterThanOrEqual(op1, LongConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThanOrEqual(long op1, final ObservableNumberValue op2) {
        return greaterThanOrEqual(LongConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableNumberValue op1, int op2) {
        return greaterThanOrEqual(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThanOrEqual(int op1, final ObservableNumberValue op2) {
        return greaterThanOrEqual(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // LessThanOrEqual (numbers) - delegates to greaterThanOrEqual with swapped operands
    // =================================================================================================================

    private static BooleanBinding lessThanOrEqual(final ObservableNumberValue op1,
                                                   final ObservableNumberValue op2,
                                                   final Observable... dependencies) {
        return greaterThanOrEqual(op2, op1, dependencies);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableNumberValue op1,
                                                  final ObservableNumberValue op2) {
        return lessThanOrEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableNumberValue op1, double op2) {
        return lessThanOrEqual(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThanOrEqual(double op1, final ObservableNumberValue op2) {
        return lessThanOrEqual(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableNumberValue op1, long op2) {
        return lessThanOrEqual(op1, LongConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThanOrEqual(long op1, final ObservableNumberValue op2) {
        return lessThanOrEqual(LongConstant.valueOf(op1), op2, op2);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableNumberValue op1, int op2) {
        return lessThanOrEqual(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThanOrEqual(int op1, final ObservableNumberValue op2) {
        return lessThanOrEqual(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Minimum (numbers)
    // =================================================================================================================

    private static NumberBinding min(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                      final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return Math.min(op1.doubleValue(), op2.doubleValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return Math.min(op1.longValue(), op2.longValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return Math.min(op1.intValue(), op2.intValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    public static NumberBinding min(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return min(op1, op2, op1, op2);
    }

    public static DoubleBinding min(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) min(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static DoubleBinding min(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) min(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding min(final ObservableNumberValue op1, long op2) {
        return min(op1, LongConstant.valueOf(op2), op1);
    }

    public static NumberBinding min(long op1, final ObservableNumberValue op2) {
        return min(LongConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding min(final ObservableNumberValue op1, int op2) {
        return min(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static NumberBinding min(int op1, final ObservableNumberValue op2) {
        return min(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Maximum (numbers)
    // =================================================================================================================

    private static NumberBinding max(final ObservableNumberValue op1, final ObservableNumberValue op2,
                                      final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        if ((op1 instanceof ObservableDoubleValue) || (op2 instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected double computeValue() {
                    return Math.max(op1.doubleValue(), op2.doubleValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else if ((op1 instanceof ObservableLongValue) || (op2 instanceof ObservableLongValue)) {
            return new LongBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected long computeValue() {
                    return Math.max(op1.longValue(), op2.longValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        } else {
            return new IntegerBinding() {
                {
                    super.bind(dependencies);
                }

                @Override
                protected int computeValue() {
                    return Math.max(op1.intValue(), op2.intValue());
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return makeDependencies(dependencies);
                }

                @Override
                public void dispose() {
                    super.unbind(dependencies);
                }
            };
        }
    }

    public static NumberBinding max(final ObservableNumberValue op1, final ObservableNumberValue op2) {
        return max(op1, op2, op1, op2);
    }

    public static DoubleBinding max(final ObservableNumberValue op1, double op2) {
        return (DoubleBinding) max(op1, DoubleConstant.valueOf(op2), op1);
    }

    public static DoubleBinding max(double op1, final ObservableNumberValue op2) {
        return (DoubleBinding) max(DoubleConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding max(final ObservableNumberValue op1, long op2) {
        return max(op1, LongConstant.valueOf(op2), op1);
    }

    public static NumberBinding max(long op1, final ObservableNumberValue op2) {
        return max(LongConstant.valueOf(op1), op2, op2);
    }

    public static NumberBinding max(final ObservableNumberValue op1, int op2) {
        return max(op1, IntegerConstant.valueOf(op2), op1);
    }

    public static NumberBinding max(int op1, final ObservableNumberValue op2) {
        return max(IntegerConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Boolean And (with short-circuit invalidation)
    // =================================================================================================================

    /**
     * Creates a {@link BooleanBinding} that calculates the conditional-AND
     * of two {@link ObservableBooleanValue} instances.
     * <p>
     * Uses short-circuit invalidation: if op1 is {@code false}, changes to
     * op2 do not invalidate the binding.
     *
     * @param op1 first operand
     * @param op2 second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding and(final ObservableBooleanValue op1, final ObservableBooleanValue op2) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanAndBinding(op1, op2);
    }

    /**
     * Internal binding for boolean AND with short-circuit invalidation.
     * Uses a direct reference (no WeakReference).
     */
    private static class BooleanAndBinding extends BooleanBinding {
        private final ObservableBooleanValue op1;
        private final ObservableBooleanValue op2;
        private final InvalidationListener observer;

        public BooleanAndBinding(ObservableBooleanValue op1, ObservableBooleanValue op2) {
            this.op1 = op1;
            this.op2 = op2;
            this.observer = new ShortCircuitAndInvalidator(this);
            op1.addListener(observer);
            op2.addListener(observer);
        }

        @Override
        protected boolean computeValue() {
            return op1.get() && op2.get();
        }

        @Override
        public ObservableList<?> getDependencies() {
            return JuxCollections.unmodifiableObservableList(
                    JuxCollections.observableArrayList(op1, op2));
        }

        @Override
        public void dispose() {
            op1.removeListener(observer);
            op2.removeListener(observer);
        }
    }

    /**
     * Short-circuit invalidator for AND: only invalidates if the source is
     * op1, or if the binding is currently valid and op1 is true (meaning
     * op2 can actually affect the result).
     */
    private static class ShortCircuitAndInvalidator implements InvalidationListener {
        private final BooleanAndBinding binding;

        ShortCircuitAndInvalidator(BooleanAndBinding binding) {
            this.binding = binding;
        }

        @Override
        public void invalidated(Observable observable) {
            if (observable == binding.op1) {
                binding.invalidate();
            } else {
                // op2 changed; only invalidate if binding is valid and op1 is true
                if (binding.isValid() && binding.op1.get()) {
                    binding.invalidate();
                }
            }
        }
    }

    // =================================================================================================================
    // Boolean Or (with short-circuit invalidation)
    // =================================================================================================================

    /**
     * Creates a {@link BooleanBinding} that calculates the conditional-OR
     * of two {@link ObservableBooleanValue} instances.
     * <p>
     * Uses short-circuit invalidation: if op1 is {@code true}, changes to
     * op2 do not invalidate the binding.
     *
     * @param op1 first operand
     * @param op2 second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding or(final ObservableBooleanValue op1, final ObservableBooleanValue op2) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        return new BooleanOrBinding(op1, op2);
    }

    /**
     * Internal binding for boolean OR with short-circuit invalidation.
     * Uses a direct reference (no WeakReference).
     */
    private static class BooleanOrBinding extends BooleanBinding {
        private final ObservableBooleanValue op1;
        private final ObservableBooleanValue op2;
        private final InvalidationListener observer;

        public BooleanOrBinding(ObservableBooleanValue op1, ObservableBooleanValue op2) {
            this.op1 = op1;
            this.op2 = op2;
            this.observer = new ShortCircuitOrInvalidator(this);
            op1.addListener(observer);
            op2.addListener(observer);
        }

        @Override
        protected boolean computeValue() {
            return op1.get() || op2.get();
        }

        @Override
        public ObservableList<?> getDependencies() {
            return JuxCollections.unmodifiableObservableList(
                    JuxCollections.observableArrayList(op1, op2));
        }

        @Override
        public void dispose() {
            op1.removeListener(observer);
            op2.removeListener(observer);
        }
    }

    /**
     * Short-circuit invalidator for OR: only invalidates if the source is
     * op1, or if the binding is currently valid and op1 is false (meaning
     * op2 can actually affect the result).
     */
    private static class ShortCircuitOrInvalidator implements InvalidationListener {
        private final BooleanOrBinding binding;

        ShortCircuitOrInvalidator(BooleanOrBinding binding) {
            this.binding = binding;
        }

        @Override
        public void invalidated(Observable observable) {
            if (observable == binding.op1) {
                binding.invalidate();
            } else {
                // op2 changed; only invalidate if binding is valid and op1 is false
                if (binding.isValid() && !binding.op1.get()) {
                    binding.invalidate();
                }
            }
        }
    }

    // =================================================================================================================
    // Boolean Not
    // =================================================================================================================

    /**
     * Creates a {@link BooleanBinding} that calculates the inverse of an
     * {@link ObservableBooleanValue}.
     *
     * @param op the operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static BooleanBinding not(final ObservableBooleanValue op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return !op.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    // =================================================================================================================
    // Boolean Equal / NotEqual
    // =================================================================================================================

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableBooleanValue} instances are equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding equal(final ObservableBooleanValue op1, final ObservableBooleanValue op2) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op1, op2);
            }

            @Override
            protected boolean computeValue() {
                return op1.get() == op2.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op1, op2));
            }

            @Override
            public void dispose() {
                super.unbind(op1, op2);
            }
        };
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableBooleanValue} instances are not equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding notEqual(final ObservableBooleanValue op1, final ObservableBooleanValue op2) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op1, op2);
            }

            @Override
            protected boolean computeValue() {
                return op1.get() != op2.get();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op1, op2));
            }

            @Override
            public void dispose() {
                super.unbind(op1, op2);
            }
        };
    }

    // =================================================================================================================
    // String convert / concat / format
    // =================================================================================================================

    /**
     * Creates a {@link StringBinding} that holds the value of an
     * {@link ObservableValue} converted to a {@code String}. If the value
     * is {@code null}, the result is {@code "null"}.
     *
     * @param observableValue the source value
     * @return the new {@code StringBinding}
     * @throws NullPointerException if the value is {@code null}
     */
    public static StringBinding convert(final ObservableValue<?> observableValue) {
        if (observableValue == null) {
            throw new NullPointerException("ObservableValue cannot be null.");
        }

        return new StringBinding() {
            {
                super.bind(observableValue);
            }

            @Override
            protected String computeValue() {
                final Object value = observableValue.getValue();
                return (value == null) ? "null" : value.toString();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(observableValue);
            }

            @Override
            public void dispose() {
                super.unbind(observableValue);
            }
        };
    }

    /**
     * Creates a {@link StringBinding} that holds the concatenation of the
     * string representations of multiple {@link Object} values. Each value
     * may be a constant or an {@link ObservableValue}. Observable values
     * are tracked for changes.
     *
     * @param args the values to concatenate (constants or ObservableValues)
     * @return the new {@code StringBinding}
     */
    public static StringBinding concat(final Object... args) {
        if ((args == null) || (args.length == 0)) {
            return new StringBinding() {
                @Override
                protected String computeValue() {
                    return "";
                }
            };
        }
        if (args.length == 1) {
            final Object arg = args[0];
            if (arg instanceof ObservableValue<?> obs) {
                return convert(obs);
            } else {
                return new StringBinding() {
                    @Override
                    protected String computeValue() {
                        return arg == null ? "null" : arg.toString();
                    }
                };
            }
        }

        // Collect all observable dependencies
        final Observable[] dependencies = extractDependencies(args);

        return new StringBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected String computeValue() {
                final StringBuilder sb = new StringBuilder();
                for (final Object obj : args) {
                    if (obj instanceof ObservableValue<?> obs) {
                        final Object value = obs.getValue();
                        sb.append((value == null) ? "null" : value.toString());
                    } else {
                        sb.append((obj == null) ? "null" : obj.toString());
                    }
                }
                return sb.toString();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a {@link StringBinding} that holds the value of a
     * {@link java.util.Formatter#format(String, Object...)} call. Observable
     * values among the arguments are tracked for changes.
     *
     * @param format the format string
     * @param args   the arguments (constants or ObservableValues)
     * @return the new {@code StringBinding}
     */
    public static StringBinding format(final String format, final Object... args) {
        if (format == null) {
            throw new NullPointerException("Format string cannot be null.");
        }

        final Observable[] dependencies = extractDependencies(args);

        return new StringBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected String computeValue() {
                final Object[] resolved = new Object[args.length];
                for (int i = 0; i < args.length; i++) {
                    if (args[i] instanceof ObservableValue<?> obs) {
                        resolved[i] = obs.getValue();
                    } else {
                        resolved[i] = args[i];
                    }
                }
                try {
                    return String.format(format, resolved);
                } catch (Exception e) {
                    Logging.getLogger().warning("Exception while formatting binding", e);
                    return "";
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Extracts all {@link Observable} instances from a mixed array of
     * constants and observables.
     */
    private static Observable[] extractDependencies(Object... args) {
        int count = 0;
        for (final Object obj : args) {
            if (obj instanceof Observable) {
                count++;
            }
        }
        final Observable[] deps = new Observable[count];
        int idx = 0;
        for (final Object obj : args) {
            if (obj instanceof Observable o) {
                deps[idx++] = o;
            }
        }
        return deps;
    }

    // =================================================================================================================
    // String Equal / NotEqual
    // =================================================================================================================

    private static String getStringSafe(String value) {
        return value == null ? "" : value;
    }

    private static BooleanBinding equal(final ObservableStringValue op1, final ObservableStringValue op2,
                                         final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return s1.equals(s2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableStringValue} instances are equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding equal(final ObservableStringValue op1, final ObservableStringValue op2) {
        return equal(op1, op2, op1, op2);
    }

    public static BooleanBinding equal(final ObservableStringValue op1, String op2) {
        return equal(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding equal(String op1, final ObservableStringValue op2) {
        return equal(StringConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding notEqual(final ObservableStringValue op1, final ObservableStringValue op2,
                                            final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return !s1.equals(s2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableStringValue} instances are not equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding notEqual(final ObservableStringValue op1, final ObservableStringValue op2) {
        return notEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding notEqual(final ObservableStringValue op1, String op2) {
        return notEqual(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding notEqual(String op1, final ObservableStringValue op2) {
        return notEqual(StringConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // String EqualIgnoreCase / NotEqualIgnoreCase
    // =================================================================================================================

    private static BooleanBinding equalIgnoreCase(final ObservableStringValue op1,
                                                   final ObservableStringValue op2,
                                                   final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return s1.equalsIgnoreCase(s2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding equalIgnoreCase(final ObservableStringValue op1,
                                                  final ObservableStringValue op2) {
        return equalIgnoreCase(op1, op2, op1, op2);
    }

    public static BooleanBinding equalIgnoreCase(final ObservableStringValue op1, String op2) {
        return equalIgnoreCase(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding equalIgnoreCase(String op1, final ObservableStringValue op2) {
        return equalIgnoreCase(StringConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding notEqualIgnoreCase(final ObservableStringValue op1,
                                                      final ObservableStringValue op2,
                                                      final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return !s1.equalsIgnoreCase(s2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding notEqualIgnoreCase(final ObservableStringValue op1,
                                                     final ObservableStringValue op2) {
        return notEqualIgnoreCase(op1, op2, op1, op2);
    }

    public static BooleanBinding notEqualIgnoreCase(final ObservableStringValue op1, String op2) {
        return notEqualIgnoreCase(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding notEqualIgnoreCase(String op1, final ObservableStringValue op2) {
        return notEqualIgnoreCase(StringConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // String GreaterThan / LessThan / GreaterThanOrEqual / LessThanOrEqual
    // =================================================================================================================

    private static BooleanBinding greaterThan(final ObservableStringValue op1, final ObservableStringValue op2,
                                               final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return s1.compareTo(s2) > 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding greaterThan(final ObservableStringValue op1, final ObservableStringValue op2) {
        return greaterThan(op1, op2, op1, op2);
    }

    public static BooleanBinding greaterThan(final ObservableStringValue op1, String op2) {
        return greaterThan(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThan(String op1, final ObservableStringValue op2) {
        return greaterThan(StringConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding lessThan(final ObservableStringValue op1, final ObservableStringValue op2,
                                            final Observable... dependencies) {
        return greaterThan(op2, op1, dependencies);
    }

    public static BooleanBinding lessThan(final ObservableStringValue op1, final ObservableStringValue op2) {
        return lessThan(op1, op2, op1, op2);
    }

    public static BooleanBinding lessThan(final ObservableStringValue op1, String op2) {
        return lessThan(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThan(String op1, final ObservableStringValue op2) {
        return lessThan(StringConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding greaterThanOrEqual(final ObservableStringValue op1,
                                                      final ObservableStringValue op2,
                                                      final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final String s1 = getStringSafe(op1.get());
                final String s2 = getStringSafe(op2.get());
                return s1.compareTo(s2) >= 0;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableStringValue op1,
                                                     final ObservableStringValue op2) {
        return greaterThanOrEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding greaterThanOrEqual(final ObservableStringValue op1, String op2) {
        return greaterThanOrEqual(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding greaterThanOrEqual(String op1, final ObservableStringValue op2) {
        return greaterThanOrEqual(StringConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding lessThanOrEqual(final ObservableStringValue op1,
                                                   final ObservableStringValue op2,
                                                   final Observable... dependencies) {
        return greaterThanOrEqual(op2, op1, dependencies);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableStringValue op1,
                                                  final ObservableStringValue op2) {
        return lessThanOrEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding lessThanOrEqual(final ObservableStringValue op1, String op2) {
        return lessThanOrEqual(op1, StringConstant.valueOf(op2), op1);
    }

    public static BooleanBinding lessThanOrEqual(String op1, final ObservableStringValue op2) {
        return lessThanOrEqual(StringConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // String length / isEmpty / isNotEmpty
    // =================================================================================================================

    /**
     * Creates a new {@link IntegerBinding} that holds the length of an
     * {@link ObservableStringValue}.
     *
     * @param op the operand
     * @return the new {@code IntegerBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static IntegerBinding length(final ObservableStringValue op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new IntegerBinding() {
            {
                super.bind(op);
            }

            @Override
            protected int computeValue() {
                return getStringSafe(op.get()).length();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the
     * given {@link ObservableStringValue} is empty.
     *
     * @param op the operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static BooleanBinding isEmpty(final ObservableStringValue op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return getStringSafe(op.get()).isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the
     * given {@link ObservableStringValue} is not empty.
     *
     * @param op the operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static BooleanBinding isNotEmpty(final ObservableStringValue op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return !getStringSafe(op.get()).isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    // =================================================================================================================
    // Object Equal / NotEqual
    // =================================================================================================================

    private static BooleanBinding equal(final ObservableObjectValue<?> op1, final ObservableObjectValue<?> op2,
                                         final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final Object obj1 = op1.get();
                final Object obj2 = op2.get();
                return obj1 == null ? obj2 == null : obj1.equals(obj2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableObjectValue} instances are equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding equal(final ObservableObjectValue<?> op1, final ObservableObjectValue<?> op2) {
        return equal(op1, op2, op1, op2);
    }

    public static BooleanBinding equal(final ObservableObjectValue<?> op1, Object op2) {
        return equal(op1, ObjectConstant.valueOf(op2), op1);
    }

    public static BooleanBinding equal(Object op1, final ObservableObjectValue<?> op2) {
        return equal(ObjectConstant.valueOf(op1), op2, op2);
    }

    private static BooleanBinding notEqual(final ObservableObjectValue<?> op1, final ObservableObjectValue<?> op2,
                                            final Observable... dependencies) {
        if ((op1 == null) || (op2 == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }
        assert (dependencies != null) && (dependencies.length > 0);

        return new BooleanBinding() {
            {
                super.bind(dependencies);
            }

            @Override
            protected boolean computeValue() {
                final Object obj1 = op1.get();
                final Object obj2 = op2.get();
                return obj1 == null ? obj2 != null : !obj1.equals(obj2);
            }

            @Override
            public ObservableList<?> getDependencies() {
                return makeDependencies(dependencies);
            }

            @Override
            public void dispose() {
                super.unbind(dependencies);
            }
        };
    }

    /**
     * Creates a {@link BooleanBinding} that holds {@code true} if the values of two
     * {@link ObservableObjectValue} instances are not equal.
     *
     * @param op1 the first operand
     * @param op2 the second operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if one of the operands is {@code null}
     */
    public static BooleanBinding notEqual(final ObservableObjectValue<?> op1, final ObservableObjectValue<?> op2) {
        return notEqual(op1, op2, op1, op2);
    }

    public static BooleanBinding notEqual(final ObservableObjectValue<?> op1, Object op2) {
        return notEqual(op1, ObjectConstant.valueOf(op2), op1);
    }

    public static BooleanBinding notEqual(Object op1, final ObservableObjectValue<?> op2) {
        return notEqual(ObjectConstant.valueOf(op1), op2, op2);
    }

    // =================================================================================================================
    // Object isNull / isNotNull
    // =================================================================================================================

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the value of an
     * {@link ObservableObjectValue} is {@code null}.
     *
     * @param op the operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static BooleanBinding isNull(final ObservableObjectValue<?> op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return op.get() == null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the value of an
     * {@link ObservableObjectValue} is not {@code null}.
     *
     * @param op the operand
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the operand is {@code null}
     */
    public static BooleanBinding isNotNull(final ObservableObjectValue<?> op) {
        if (op == null) {
            throw new NullPointerException("Operand cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return op.get() != null;
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    // =================================================================================================================
    // List size / isEmpty / isNotEmpty
    // =================================================================================================================

    /**
     * Creates a new {@link IntegerBinding} that contains the size of an
     * {@link ObservableList}.
     *
     * @param op the observable list
     * @return the new {@code IntegerBinding}
     * @throws NullPointerException if the list is {@code null}
     */
    public static IntegerBinding size(final ObservableList<?> op) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }

        return new IntegerBinding() {
            {
                super.bind(op);
            }

            @Override
            protected int computeValue() {
                return op.size();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the
     * given {@link ObservableList} is empty.
     *
     * @param op the observable list
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the list is {@code null}
     */
    public static BooleanBinding isEmpty(final ObservableList<?> op) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return op.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that holds {@code true} if the
     * given {@link ObservableList} is not empty.
     *
     * @param op the observable list
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the list is {@code null}
     */
    public static BooleanBinding isNotEmpty(final ObservableList<?> op) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                return !op.isEmpty();
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    // =================================================================================================================
    // List valueAt
    // =================================================================================================================

    /**
     * Creates a new {@link ObjectBinding} that contains the element of an
     * {@link ObservableList} at the specified position. The binding will
     * return {@code null} if the index is out of range.
     *
     * @param <E>  the type of elements in the list
     * @param op   the observable list
     * @param index the index
     * @return the new {@code ObjectBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static <E> ObjectBinding<E> valueAt(final ObservableList<E> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new ObjectBinding<>() {
            {
                super.bind(op);
            }

            @Override
            protected E computeValue() {
                try {
                    return op.get(index);
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in valueAt binding, returning null.", ex);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link ObjectBinding} that contains the element of an
     * {@link ObservableList} at the specified position. The binding will
     * return {@code null} if the index is out of range.
     *
     * @param <E>   the type of elements in the list
     * @param op    the observable list
     * @param index the observable index
     * @return the new {@code ObjectBinding}
     * @throws NullPointerException if the list or the index is {@code null}
     */
    public static <E> ObjectBinding<E> valueAt(final ObservableList<E> op,
                                                final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ObjectBinding<>() {
            {
                super.bind(op, index);
            }

            @Override
            protected E computeValue() {
                try {
                    return op.get(index.get());
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in valueAt binding, returning null.", ex);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link ObjectBinding} that contains the element of an
     * {@link ObservableList} at the specified position. The binding will
     * return {@code null} if the index is out of range.
     *
     * @param <E>   the type of elements in the list
     * @param op    the observable list
     * @param index the observable number index (converted to int)
     * @return the new {@code ObjectBinding}
     * @throws NullPointerException if the list or the index is {@code null}
     */
    public static <E> ObjectBinding<E> valueAt(final ObservableList<E> op,
                                                final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new ObjectBinding<>() {
            {
                super.bind(op, index);
            }

            @Override
            protected E computeValue() {
                try {
                    return op.get(index.intValue());
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in valueAt binding, returning null.", ex);
                    return null;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that contains the boolean element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code false} if the index is out of range.
     *
     * @param op    the observable list of booleans
     * @param index the index
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static BooleanBinding booleanValueAt(final ObservableList<Boolean> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new BooleanBinding() {
            {
                super.bind(op);
            }

            @Override
            protected boolean computeValue() {
                try {
                    final Boolean value = op.get(index);
                    return value != null && value;
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in booleanValueAt binding, returning false.", ex);
                    return false;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that contains the boolean element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code false} if the index is out of range.
     *
     * @param op    the observable list of booleans
     * @param index the observable index
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static BooleanBinding booleanValueAt(final ObservableList<Boolean> op,
                                                 final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected boolean computeValue() {
                try {
                    final Boolean value = op.get(index.get());
                    return value != null && value;
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in booleanValueAt binding, returning false.", ex);
                    return false;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link BooleanBinding} that contains the boolean element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code false} if the index is out of range.
     *
     * @param op    the observable list of booleans
     * @param index the observable number index (converted to int)
     * @return the new {@code BooleanBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static BooleanBinding booleanValueAt(final ObservableList<Boolean> op,
                                                 final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new BooleanBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected boolean computeValue() {
                try {
                    final Boolean value = op.get(index.intValue());
                    return value != null && value;
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in booleanValueAt binding, returning false.", ex);
                    return false;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link DoubleBinding} that contains the double element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0.0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the index
     * @return the new {@code DoubleBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static DoubleBinding doubleValueAt(final ObservableList<? extends Number> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new DoubleBinding() {
            {
                super.bind(op);
            }

            @Override
            protected double computeValue() {
                try {
                    final Number value = op.get(index);
                    return (value == null) ? 0.0 : value.doubleValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in doubleValueAt binding, returning 0.0.", ex);
                    return 0.0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link DoubleBinding} that contains the double element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0.0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable index
     * @return the new {@code DoubleBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static DoubleBinding doubleValueAt(final ObservableList<? extends Number> op,
                                               final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new DoubleBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected double computeValue() {
                try {
                    final Number value = op.get(index.get());
                    return (value == null) ? 0.0 : value.doubleValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in doubleValueAt binding, returning 0.0.", ex);
                    return 0.0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link DoubleBinding} that contains the double element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0.0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable number index (converted to int)
     * @return the new {@code DoubleBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static DoubleBinding doubleValueAt(final ObservableList<? extends Number> op,
                                               final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new DoubleBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected double computeValue() {
                try {
                    final Number value = op.get(index.intValue());
                    return (value == null) ? 0.0 : value.doubleValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in doubleValueAt binding, returning 0.0.", ex);
                    return 0.0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link IntegerBinding} that contains the integer element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the index
     * @return the new {@code IntegerBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static IntegerBinding integerValueAt(final ObservableList<? extends Number> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new IntegerBinding() {
            {
                super.bind(op);
            }

            @Override
            protected int computeValue() {
                try {
                    final Number value = op.get(index);
                    return (value == null) ? 0 : value.intValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in integerValueAt binding, returning 0.", ex);
                    return 0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link IntegerBinding} that contains the integer element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable index
     * @return the new {@code IntegerBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static IntegerBinding integerValueAt(final ObservableList<? extends Number> op,
                                                 final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new IntegerBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected int computeValue() {
                try {
                    final Number value = op.get(index.get());
                    return (value == null) ? 0 : value.intValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in integerValueAt binding, returning 0.", ex);
                    return 0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link IntegerBinding} that contains the integer element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable number index (converted to int)
     * @return the new {@code IntegerBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static IntegerBinding integerValueAt(final ObservableList<? extends Number> op,
                                                 final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new IntegerBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected int computeValue() {
                try {
                    final Number value = op.get(index.intValue());
                    return (value == null) ? 0 : value.intValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in integerValueAt binding, returning 0.", ex);
                    return 0;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link LongBinding} that contains the long element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0L} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the index
     * @return the new {@code LongBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static LongBinding longValueAt(final ObservableList<? extends Number> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new LongBinding() {
            {
                super.bind(op);
            }

            @Override
            protected long computeValue() {
                try {
                    final Number value = op.get(index);
                    return (value == null) ? 0L : value.longValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in longValueAt binding, returning 0L.", ex);
                    return 0L;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link LongBinding} that contains the long element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0L} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable index
     * @return the new {@code LongBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static LongBinding longValueAt(final ObservableList<? extends Number> op,
                                           final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new LongBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected long computeValue() {
                try {
                    final Number value = op.get(index.get());
                    return (value == null) ? 0L : value.longValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in longValueAt binding, returning 0L.", ex);
                    return 0L;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link LongBinding} that contains the long element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code 0L} if the index is out of range.
     *
     * @param op    the observable list of numbers
     * @param index the observable number index (converted to int)
     * @return the new {@code LongBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static LongBinding longValueAt(final ObservableList<? extends Number> op,
                                           final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new LongBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected long computeValue() {
                try {
                    final Number value = op.get(index.intValue());
                    return (value == null) ? 0L : value.longValue();
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in longValueAt binding, returning 0L.", ex);
                    return 0L;
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link StringBinding} that contains the string element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code ""} if the index is out of range.
     *
     * @param op    the observable list of strings
     * @param index the index
     * @return the new {@code StringBinding}
     * @throws NullPointerException     if the list is {@code null}
     * @throws IllegalArgumentException if the index is negative
     */
    public static StringBinding stringValueAt(final ObservableList<String> op, final int index) {
        if (op == null) {
            throw new NullPointerException("List cannot be null.");
        }
        if (index < 0) {
            throw new IllegalArgumentException("Index cannot be negative.");
        }

        return new StringBinding() {
            {
                super.bind(op);
            }

            @Override
            protected String computeValue() {
                try {
                    return op.get(index);
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in stringValueAt binding, returning empty.", ex);
                    return "";
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.singletonObservableList(op);
            }

            @Override
            public void dispose() {
                super.unbind(op);
            }
        };
    }

    /**
     * Creates a new {@link StringBinding} that contains the string element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code ""} if the index is out of range.
     *
     * @param op    the observable list of strings
     * @param index the observable index
     * @return the new {@code StringBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static StringBinding stringValueAt(final ObservableList<String> op,
                                               final ObservableIntegerValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new StringBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected String computeValue() {
                try {
                    return op.get(index.get());
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in stringValueAt binding, returning empty.", ex);
                    return "";
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }

    /**
     * Creates a new {@link StringBinding} that contains the string element
     * at the specified position of an {@link ObservableList}. The binding
     * will return {@code ""} if the index is out of range.
     *
     * @param op    the observable list of strings
     * @param index the observable number index (converted to int)
     * @return the new {@code StringBinding}
     * @throws NullPointerException if the list or index is {@code null}
     */
    public static StringBinding stringValueAt(final ObservableList<String> op,
                                               final ObservableNumberValue index) {
        if ((op == null) || (index == null)) {
            throw new NullPointerException("Operands cannot be null.");
        }

        return new StringBinding() {
            {
                super.bind(op, index);
            }

            @Override
            protected String computeValue() {
                try {
                    return op.get(index.intValue());
                } catch (IndexOutOfBoundsException ex) {
                    Logging.getLogger().finest("Index out of range in stringValueAt binding, returning empty.", ex);
                    return "";
                }
            }

            @Override
            public ObservableList<?> getDependencies() {
                return JuxCollections.unmodifiableObservableList(
                        JuxCollections.observableArrayList(op, index));
            }

            @Override
            public void dispose() {
                super.unbind(op, index);
            }
        };
    }
}

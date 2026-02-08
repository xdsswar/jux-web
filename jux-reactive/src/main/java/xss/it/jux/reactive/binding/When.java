package xss.it.jux.reactive.binding;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.collections.JuxCollections;
import xss.it.jux.reactive.collections.ObservableList;
import xss.it.jux.reactive.internal.Logging;
import xss.it.jux.reactive.value.ObservableBooleanValue;
import xss.it.jux.reactive.value.ObservableDoubleValue;
import xss.it.jux.reactive.value.ObservableLongValue;
import xss.it.jux.reactive.value.ObservableNumberValue;
import xss.it.jux.reactive.value.ObservableObjectValue;
import xss.it.jux.reactive.value.ObservableStringValue;
import xss.it.jux.reactive.value.ObservableValue;

/**
 * Starting point for a binding that calculates a ternary expression.
 * <p>
 * A ternary expression has the basic form
 * {@code new When(cond).then(value1).otherwise(value2);}. The expression
 * {@code cond} needs to be a {@link ObservableBooleanValue}.
 * Based on the value of {@code cond}, the binding contains the value of
 * {@code value1} (if {@code cond.getValue() == true}) or {@code value2} (if
 * {@code cond.getValue() == false}). The values {@code value1} and
 * {@code value2} have to be of the same type. They can be constant values or
 * implementations of {@link ObservableValue}.
 */
public class When {
    private final ObservableBooleanValue condition;

    /**
     * The constructor of {@code When}.
     *
     * @param condition the condition of the ternary expression
     */
    public When(final ObservableBooleanValue condition) {
        if (condition == null) {
            throw new NullPointerException("Condition must be specified.");
        }
        this.condition = condition;
    }

    private static class WhenListener implements InvalidationListener {

        private final ObservableBooleanValue condition;
        private final ObservableValue<?> thenValue;
        private final ObservableValue<?> otherwiseValue;
        private final Binding<?> binding;

        private WhenListener(Binding<?> binding, ObservableBooleanValue condition,
                             ObservableValue<?> thenValue, ObservableValue<?> otherwiseValue) {
            this.binding = binding;
            this.condition = condition;
            this.thenValue = thenValue;
            this.otherwiseValue = otherwiseValue;
        }

        @Override
        public void invalidated(Observable observable) {
            // short-circuit invalidation. This Binding becomes
            // only invalid if the condition changes or the
            // active branch.
            if (condition.equals(observable) || (binding.isValid() && (condition.get() == observable.equals(thenValue)))) {
                binding.invalidate();
            }
        }
    }

    private static NumberBinding createNumberCondition(
            final ObservableBooleanValue condition,
            final ObservableNumberValue thenValue,
            final ObservableNumberValue otherwiseValue) {
        if ((thenValue instanceof ObservableDoubleValue) || (otherwiseValue instanceof ObservableDoubleValue)) {
            return new DoubleBinding() {
                final InvalidationListener observer = new WhenListener(this, condition, thenValue, otherwiseValue);
                {
                    condition.addListener(observer);
                    thenValue.addListener(observer);
                    otherwiseValue.addListener(observer);
                }

                @Override
                public void dispose() {
                    condition.removeListener(observer);
                    thenValue.removeListener(observer);
                    otherwiseValue.removeListener(observer);
                }

                @Override
                protected double computeValue() {
                    final boolean conditionValue = condition.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
                    return conditionValue ? thenValue.doubleValue() : otherwiseValue.doubleValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.unmodifiableObservableList(
                            JuxCollections.<ObservableValue<?>>observableArrayList(condition, thenValue, otherwiseValue));
                }
            };
        } else if ((thenValue instanceof ObservableLongValue) || (otherwiseValue instanceof ObservableLongValue)) {
            return new LongBinding() {
                final InvalidationListener observer = new WhenListener(this, condition, thenValue, otherwiseValue);
                {
                    condition.addListener(observer);
                    thenValue.addListener(observer);
                    otherwiseValue.addListener(observer);
                }

                @Override
                public void dispose() {
                    condition.removeListener(observer);
                    thenValue.removeListener(observer);
                    otherwiseValue.removeListener(observer);
                }

                @Override
                protected long computeValue() {
                    final boolean conditionValue = condition.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
                    return conditionValue ? thenValue.longValue() : otherwiseValue.longValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.unmodifiableObservableList(
                            JuxCollections.<ObservableValue<?>>observableArrayList(condition, thenValue, otherwiseValue));
                }
            };
        } else {
            return new IntegerBinding() {
                final InvalidationListener observer = new WhenListener(this, condition, thenValue, otherwiseValue);
                {
                    condition.addListener(observer);
                    thenValue.addListener(observer);
                    otherwiseValue.addListener(observer);
                }

                @Override
                public void dispose() {
                    condition.removeListener(observer);
                    thenValue.removeListener(observer);
                    otherwiseValue.removeListener(observer);
                }

                @Override
                protected int computeValue() {
                    final boolean conditionValue = condition.get();
                    Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
                    return conditionValue ? thenValue.intValue() : otherwiseValue.intValue();
                }

                @Override
                public ObservableList<?> getDependencies() {
                    return JuxCollections.unmodifiableObservableList(
                            JuxCollections.<ObservableValue<?>>observableArrayList(condition, thenValue, otherwiseValue));
                }
            };
        }
    }

    /**
     * If-then-else expression returning a number.
     */
    public class NumberConditionBuilder {

        private final ObservableNumberValue thenValue;

        private NumberConditionBuilder(final ObservableNumberValue thenValue) {
            this.thenValue = thenValue;
        }

        /**
         * Defines the {@link ObservableNumberValue} which
         * value is returned by the ternary expression if the condition is
         * {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link NumberBinding}
         */
        public NumberBinding otherwise(ObservableNumberValue otherwiseValue) {
            if (otherwiseValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            return When.createNumberCondition(condition, thenValue, otherwiseValue);
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link DoubleBinding}
         */
        public DoubleBinding otherwise(double otherwiseValue) {
            return (DoubleBinding) otherwise(DoubleConstant.valueOf(otherwiseValue));
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link NumberBinding}
         */
        public NumberBinding otherwise(long otherwiseValue) {
            return otherwise(LongConstant.valueOf(otherwiseValue));
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link NumberBinding}
         */
        public NumberBinding otherwise(int otherwiseValue) {
            return otherwise(IntegerConstant.valueOf(otherwiseValue));
        }
    }

    /**
     * Defines the {@link ObservableNumberValue} which value
     * is returned by the ternary expression if the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public NumberConditionBuilder then(final ObservableNumberValue thenValue) {
        if (thenValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new NumberConditionBuilder(thenValue);
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public NumberConditionBuilder then(double thenValue) {
        return new NumberConditionBuilder(DoubleConstant.valueOf(thenValue));
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public NumberConditionBuilder then(long thenValue) {
        return new NumberConditionBuilder(LongConstant.valueOf(thenValue));
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public NumberConditionBuilder then(int thenValue) {
        return new NumberConditionBuilder(IntegerConstant.valueOf(thenValue));
    }

    /**
     * If-then-else expression returning Boolean.
     */
    private class BooleanCondition extends BooleanBinding {
        private final ObservableBooleanValue trueResult;
        private final boolean trueResultValue;

        private final ObservableBooleanValue falseResult;
        private final boolean falseResultValue;

        private final InvalidationListener observer;

        private BooleanCondition(final ObservableBooleanValue then, final ObservableBooleanValue otherwise) {
            this.trueResult = then;
            this.trueResultValue = false;
            this.falseResult = otherwise;
            this.falseResultValue = false;
            this.observer = new WhenListener(this, condition, then, otherwise);
            condition.addListener(observer);
            then.addListener(observer);
            otherwise.addListener(observer);
        }

        private BooleanCondition(final boolean then, final ObservableBooleanValue otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = otherwise;
            this.falseResultValue = false;
            this.observer = new WhenListener(this, condition, null, otherwise);
            condition.addListener(observer);
            otherwise.addListener(observer);
        }

        private BooleanCondition(final ObservableBooleanValue then, final boolean otherwise) {
            this.trueResult = then;
            this.trueResultValue = false;
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = new WhenListener(this, condition, then, null);
            condition.addListener(observer);
            then.addListener(observer);
        }

        private BooleanCondition(final boolean then, final boolean otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = null;
            super.bind(condition);
        }

        @Override
        protected boolean computeValue() {
            final boolean conditionValue = condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
            return conditionValue ? (trueResult != null ? trueResult.get() : trueResultValue)
                    : (falseResult != null ? falseResult.get() : falseResultValue);
        }

        @Override
        public void dispose() {
            if (observer == null) {
                super.unbind(condition);
            } else {
                condition.removeListener(observer);
                if (trueResult != null) {
                    trueResult.removeListener(observer);
                }
                if (falseResult != null) {
                    falseResult.removeListener(observer);
                }
            }
        }

        @Override
        public ObservableList<?> getDependencies() {
            assert condition != null;
            final ObservableList<ObservableValue<?>> seq = JuxCollections.<ObservableValue<?>>observableArrayList(condition);
            if (trueResult != null) {
                seq.add(trueResult);
            }
            if (falseResult != null) {
                seq.add(falseResult);
            }
            return JuxCollections.unmodifiableObservableList(seq);
        }
    }

    /**
     * An intermediate class needed while assembling the ternary expression. It
     * should not be used in another context.
     */
    public class BooleanConditionBuilder {

        private ObservableBooleanValue trueResult;
        private boolean trueResultValue;

        private BooleanConditionBuilder(final ObservableBooleanValue thenValue) {
            this.trueResult = thenValue;
        }

        private BooleanConditionBuilder(final boolean thenValue) {
            this.trueResultValue = thenValue;
        }

        /**
         * Defines the {@link ObservableBooleanValue} which
         * value is returned by the ternary expression if the condition is
         * {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link BooleanBinding}
         */
        public BooleanBinding otherwise(final ObservableBooleanValue otherwiseValue) {
            if (otherwiseValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            if (trueResult != null)
                return new BooleanCondition(trueResult, otherwiseValue);
            else
                return new BooleanCondition(trueResultValue, otherwiseValue);
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link BooleanBinding}
         */
        public BooleanBinding otherwise(final boolean otherwiseValue) {
            if (trueResult != null)
                return new BooleanCondition(trueResult, otherwiseValue);
            else
                return new BooleanCondition(trueResultValue, otherwiseValue);
        }
    }

    /**
     * Defines the {@link ObservableBooleanValue} which value
     * is returned by the ternary expression if the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public BooleanConditionBuilder then(final ObservableBooleanValue thenValue) {
        if (thenValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new BooleanConditionBuilder(thenValue);
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public BooleanConditionBuilder then(final boolean thenValue) {
        return new BooleanConditionBuilder(thenValue);
    }

    /**
     * If-then-else expression returning String.
     */
    private class StringCondition extends StringBinding {

        private final ObservableStringValue trueResult;
        private final String trueResultValue;

        private final ObservableStringValue falseResult;
        private final String falseResultValue;

        private final InvalidationListener observer;

        private StringCondition(final ObservableStringValue then, final ObservableStringValue otherwise) {
            this.trueResult = then;
            this.trueResultValue = "";
            this.falseResult = otherwise;
            this.falseResultValue = "";
            this.observer = new WhenListener(this, condition, then, otherwise);
            condition.addListener(observer);
            then.addListener(observer);
            otherwise.addListener(observer);
        }

        private StringCondition(final String then, final ObservableStringValue otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = otherwise;
            this.falseResultValue = "";
            this.observer = new WhenListener(this, condition, null, otherwise);
            condition.addListener(observer);
            otherwise.addListener(observer);
        }

        private StringCondition(final ObservableStringValue then, final String otherwise) {
            this.trueResult = then;
            this.trueResultValue = "";
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = new WhenListener(this, condition, then, null);
            condition.addListener(observer);
            then.addListener(observer);
        }

        private StringCondition(final String then, final String otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = null;
            super.bind(condition);
        }

        @Override
        protected String computeValue() {
            final boolean conditionValue = condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
            return conditionValue ? (trueResult != null ? trueResult.get() : trueResultValue)
                    : (falseResult != null ? falseResult.get() : falseResultValue);
        }

        @Override
        public void dispose() {
            if (observer == null) {
                super.unbind(condition);
            } else {
                condition.removeListener(observer);
                if (trueResult != null) {
                    trueResult.removeListener(observer);
                }
                if (falseResult != null) {
                    falseResult.removeListener(observer);
                }
            }
        }

        @Override
        public ObservableList<?> getDependencies() {
            assert condition != null;
            final ObservableList<ObservableValue<?>> seq = JuxCollections.<ObservableValue<?>>observableArrayList(condition);
            if (trueResult != null) {
                seq.add(trueResult);
            }
            if (falseResult != null) {
                seq.add(falseResult);
            }
            return JuxCollections.unmodifiableObservableList(seq);
        }
    }

    /**
     * An intermediate class needed while assembling the ternary expression. It
     * should not be used in another context.
     */
    public class StringConditionBuilder {

        private ObservableStringValue trueResult;
        private String trueResultValue;

        private StringConditionBuilder(final ObservableStringValue thenValue) {
            this.trueResult = thenValue;
        }

        private StringConditionBuilder(final String thenValue) {
            this.trueResultValue = thenValue;
        }

        /**
         * Defines the {@link ObservableStringValue} which
         * value is returned by the ternary expression if the condition is
         * {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link StringBinding}
         */
        public StringBinding otherwise(final ObservableStringValue otherwiseValue) {
            if (trueResult != null)
                return new StringCondition(trueResult, otherwiseValue);
            else
                return new StringCondition(trueResultValue, otherwiseValue);
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link StringBinding}
         */
        public StringBinding otherwise(final String otherwiseValue) {
            if (trueResult != null)
                return new StringCondition(trueResult, otherwiseValue);
            else
                return new StringCondition(trueResultValue, otherwiseValue);
        }
    }

    /**
     * Defines the {@link ObservableStringValue} which value
     * is returned by the ternary expression if the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public StringConditionBuilder then(final ObservableStringValue thenValue) {
        if (thenValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new StringConditionBuilder(thenValue);
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public StringConditionBuilder then(final String thenValue) {
        return new StringConditionBuilder(thenValue);
    }

    /**
     * If-then-else expression returning general objects.
     */
    private class ObjectCondition<T> extends ObjectBinding<T> {

        private final ObservableObjectValue<T> trueResult;
        private final T trueResultValue;

        private final ObservableObjectValue<T> falseResult;
        private final T falseResultValue;

        private final InvalidationListener observer;

        private ObjectCondition(final ObservableObjectValue<T> then, final ObservableObjectValue<T> otherwise) {
            this.trueResult = then;
            this.trueResultValue = null;
            this.falseResult = otherwise;
            this.falseResultValue = null;
            this.observer = new WhenListener(this, condition, then, otherwise);
            condition.addListener(observer);
            then.addListener(observer);
            otherwise.addListener(observer);
        }

        private ObjectCondition(final T then, final ObservableObjectValue<T> otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = otherwise;
            this.falseResultValue = null;
            this.observer = new WhenListener(this, condition, null, otherwise);
            condition.addListener(observer);
            otherwise.addListener(observer);
        }

        private ObjectCondition(final ObservableObjectValue<T> then, final T otherwise) {
            this.trueResult = then;
            this.trueResultValue = null;
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = new WhenListener(this, condition, then, null);
            condition.addListener(observer);
            then.addListener(observer);
        }

        private ObjectCondition(final T then, final T otherwise) {
            this.trueResult = null;
            this.trueResultValue = then;
            this.falseResult = null;
            this.falseResultValue = otherwise;
            this.observer = null;
            super.bind(condition);
        }

        @Override
        protected T computeValue() {
            final boolean conditionValue = condition.get();
            Logging.getLogger().finest("Condition of ternary binding expression was evaluated: {0}", conditionValue);
            return conditionValue ? (trueResult != null ? trueResult.get() : trueResultValue)
                    : (falseResult != null ? falseResult.get() : falseResultValue);
        }

        @Override
        public void dispose() {
            if (observer == null) {
                super.unbind(condition);
            } else {
                condition.removeListener(observer);
                if (trueResult != null) {
                    trueResult.removeListener(observer);
                }
                if (falseResult != null) {
                    falseResult.removeListener(observer);
                }
            }
        }

        @Override
        public ObservableList<?> getDependencies() {
            assert condition != null;
            final ObservableList<ObservableValue<?>> seq = JuxCollections.<ObservableValue<?>>observableArrayList(condition);
            if (trueResult != null) {
                seq.add(trueResult);
            }
            if (falseResult != null) {
                seq.add(falseResult);
            }
            return JuxCollections.unmodifiableObservableList(seq);
        }
    }

    /**
     * An intermediate class needed while assembling the ternary expression. It
     * should not be used in another context.
     *
     * @param <T> the type of the object being built
     */
    public class ObjectConditionBuilder<T> {

        private ObservableObjectValue<T> trueResult;
        private T trueResultValue;

        private ObjectConditionBuilder(final ObservableObjectValue<T> thenValue) {
            this.trueResult = thenValue;
        }

        private ObjectConditionBuilder(final T thenValue) {
            this.trueResultValue = thenValue;
        }

        /**
         * Defines the {@link ObservableObjectValue} which
         * value is returned by the ternary expression if the condition is
         * {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link ObjectBinding}
         */
        public ObjectBinding<T> otherwise(final ObservableObjectValue<T> otherwiseValue) {
            if (otherwiseValue == null) {
                throw new NullPointerException("Value needs to be specified");
            }
            if (trueResult != null)
                return new ObjectCondition<>(trueResult, otherwiseValue);
            else
                return new ObjectCondition<>(trueResultValue, otherwiseValue);
        }

        /**
         * Defines a constant value of the ternary expression, that is returned
         * if the condition is {@code false}.
         *
         * @param otherwiseValue the value
         * @return the complete {@link ObjectBinding}
         */
        public ObjectBinding<T> otherwise(final T otherwiseValue) {
            if (trueResult != null)
                return new ObjectCondition<>(trueResult, otherwiseValue);
            else
                return new ObjectCondition<>(trueResultValue, otherwiseValue);
        }
    }

    /**
     * Defines the {@link ObservableObjectValue} which value
     * is returned by the ternary expression if the condition is {@code true}.
     *
     * @param <T>       the type of the intermediate result
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public <T> ObjectConditionBuilder<T> then(final ObservableObjectValue<T> thenValue) {
        if (thenValue == null) {
            throw new NullPointerException("Value needs to be specified");
        }
        return new ObjectConditionBuilder<>(thenValue);
    }

    /**
     * Defines a constant value of the ternary expression, that is returned if
     * the condition is {@code true}.
     *
     * @param <T>       the type of the intermediate result
     * @param thenValue the value
     * @return the intermediate result which still requires the otherwise-branch
     */
    public <T> ObjectConditionBuilder<T> then(final T thenValue) {
        return new ObjectConditionBuilder<>(thenValue);
    }
}

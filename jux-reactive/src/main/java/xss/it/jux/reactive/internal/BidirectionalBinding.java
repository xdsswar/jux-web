package xss.it.jux.reactive.internal;

import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.*;
import xss.it.jux.reactive.value.ObservableValue;

import java.util.Objects;

/**
 * Implements bidirectional bindings between two properties.
 *
 * Uses {@link InvalidationListener}s with rollback on failure.
 */
public abstract class BidirectionalBinding implements InvalidationListener {

    private static void checkParameters(Object property1, Object property2) {
        Objects.requireNonNull(property1, "Both properties must be specified.");
        Objects.requireNonNull(property2, "Both properties must be specified.");
        if (property1 == property2) {
            throw new IllegalArgumentException("Cannot bind property to itself");
        }
    }

    public static <T> BidirectionalBinding bind(Property<T> property1, Property<T> property2) {
        checkParameters(property1, property2);
        final BidirectionalBinding binding =
                ((property1 instanceof DoubleProperty) && (property2 instanceof DoubleProperty)) ?
                        new BidirectionalDoubleBinding((DoubleProperty) property1, (DoubleProperty) property2)
                : ((property1 instanceof IntegerProperty) && (property2 instanceof IntegerProperty)) ?
                        new BidirectionalIntegerBinding((IntegerProperty) property1, (IntegerProperty) property2)
                : ((property1 instanceof LongProperty) && (property2 instanceof LongProperty)) ?
                        new BidirectionalLongBinding((LongProperty) property1, (LongProperty) property2)
                : ((property1 instanceof BooleanProperty) && (property2 instanceof BooleanProperty)) ?
                        new BidirectionalBooleanBinding((BooleanProperty) property1, (BooleanProperty) property2)
                : new TypedGenericBidirectionalBinding<>(property1, property2);
        property1.setValue(property2.getValue());
        property1.getValue();
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }

    public static <T> void unbind(Property<T> property1, Property<T> property2) {
        checkParameters(property1, property2);
        final BidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
        property1.removeListener(binding);
        property2.removeListener(binding);
    }

    public static void unbind(Object property1, Object property2) {
        checkParameters(property1, property2);
        final BidirectionalBinding binding = new UntypedGenericBidirectionalBinding(property1, property2);
        if (property1 instanceof ObservableValue) {
            ((ObservableValue<?>) property1).removeListener(binding);
        }
        if (property2 instanceof ObservableValue) {
            ((ObservableValue<?>) property2).removeListener(binding);
        }
    }

    public static BidirectionalBinding bindNumber(Property<Integer> property1, IntegerProperty property2) {
        return bindNumber(property1, (Property<Number>) property2);
    }

    public static BidirectionalBinding bindNumber(Property<Long> property1, LongProperty property2) {
        return bindNumber(property1, (Property<Number>) property2);
    }

    public static BidirectionalBinding bindNumber(Property<Double> property1, DoubleProperty property2) {
        return bindNumber(property1, (Property<Number>) property2);
    }

    public static BidirectionalBinding bindNumber(IntegerProperty property1, Property<Integer> property2) {
        return bindNumberObject(property1, property2);
    }

    public static BidirectionalBinding bindNumber(LongProperty property1, Property<Long> property2) {
        return bindNumberObject(property1, property2);
    }

    public static BidirectionalBinding bindNumber(DoubleProperty property1, Property<Double> property2) {
        return bindNumberObject(property1, property2);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> BidirectionalBinding bindNumberObject(Property<Number> property1, Property<T> property2) {
        checkParameters(property1, property2);
        final BidirectionalBinding binding = new TypedNumberBidirectionalBinding<>(property2, property1);
        property1.setValue(property2.getValue());
        property1.getValue();
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Number> BidirectionalBinding bindNumber(Property<T> property1, Property<Number> property2) {
        checkParameters(property1, property2);
        final BidirectionalBinding binding = new TypedNumberBidirectionalBinding<>(property1, property2);
        property1.setValue((T) property2.getValue());
        property1.getValue();
        property1.addListener(binding);
        property2.addListener(binding);
        return binding;
    }

    private final int cachedHashCode;

    private BidirectionalBinding(Object property1, Object property2) {
        cachedHashCode = property1.hashCode() * property2.hashCode();
    }

    protected abstract Object getProperty1();
    protected abstract Object getProperty2();

    @Override
    public int hashCode() {
        return cachedHashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        final Object propertyA1 = getProperty1();
        final Object propertyA2 = getProperty2();

        if (obj instanceof BidirectionalBinding otherBinding) {
            final Object propertyB1 = otherBinding.getProperty1();
            final Object propertyB2 = otherBinding.getProperty2();
            if (propertyA1 == propertyB1 && propertyA2 == propertyB2) return true;
            if (propertyA1 == propertyB2 && propertyA2 == propertyB1) return true;
        }
        return false;
    }

    private static class BidirectionalBooleanBinding extends BidirectionalBinding {
        private final BooleanProperty property1;
        private final BooleanProperty property2;
        private boolean oldValue;
        private boolean updating;

        private BidirectionalBooleanBinding(BooleanProperty property1, BooleanProperty property2) {
            super(property1, property2);
            oldValue = property1.get();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        boolean newValue = property1.get();
                        property2.set(newValue);
                        property2.get();
                        oldValue = newValue;
                    } else {
                        boolean newValue = property2.get();
                        property1.set(newValue);
                        property1.get();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.set(oldValue); property1.get(); }
                        else { property2.set(oldValue); property2.get(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    private static class BidirectionalDoubleBinding extends BidirectionalBinding {
        private final DoubleProperty property1;
        private final DoubleProperty property2;
        private double oldValue;
        private boolean updating;

        private BidirectionalDoubleBinding(DoubleProperty property1, DoubleProperty property2) {
            super(property1, property2);
            oldValue = property1.get();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        double newValue = property1.get();
                        property2.set(newValue);
                        property2.get();
                        oldValue = newValue;
                    } else {
                        double newValue = property2.get();
                        property1.set(newValue);
                        property1.get();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.set(oldValue); property1.get(); }
                        else { property2.set(oldValue); property2.get(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    private static class BidirectionalIntegerBinding extends BidirectionalBinding {
        private final IntegerProperty property1;
        private final IntegerProperty property2;
        private int oldValue;
        private boolean updating;

        private BidirectionalIntegerBinding(IntegerProperty property1, IntegerProperty property2) {
            super(property1, property2);
            oldValue = property1.get();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        int newValue = property1.get();
                        property2.set(newValue);
                        property2.get();
                        oldValue = newValue;
                    } else {
                        int newValue = property2.get();
                        property1.set(newValue);
                        property1.get();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.set(oldValue); property1.get(); }
                        else { property2.set(oldValue); property2.get(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    private static class BidirectionalLongBinding extends BidirectionalBinding {
        private final LongProperty property1;
        private final LongProperty property2;
        private long oldValue;
        private boolean updating;

        private BidirectionalLongBinding(LongProperty property1, LongProperty property2) {
            super(property1, property2);
            oldValue = property1.get();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        long newValue = property1.get();
                        property2.set(newValue);
                        property2.get();
                        oldValue = newValue;
                    } else {
                        long newValue = property2.get();
                        property1.set(newValue);
                        property1.get();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.set(oldValue); property1.get(); }
                        else { property2.set(oldValue); property2.get(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    private static class TypedGenericBidirectionalBinding<T> extends BidirectionalBinding {
        private final Property<T> property1;
        private final Property<T> property2;
        private T oldValue;
        private boolean updating;

        private TypedGenericBidirectionalBinding(Property<T> property1, Property<T> property2) {
            super(property1, property2);
            oldValue = property1.getValue();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        T newValue = property1.getValue();
                        property2.setValue(newValue);
                        property2.getValue();
                        oldValue = newValue;
                    } else {
                        T newValue = property2.getValue();
                        property1.setValue(newValue);
                        property1.getValue();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.setValue(oldValue); property1.getValue(); }
                        else { property2.setValue(oldValue); property2.getValue(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static class TypedNumberBidirectionalBinding<T extends Number> extends BidirectionalBinding {
        private final Property<T> property1;
        private final Property<Number> property2;
        private T oldValue;
        private boolean updating;

        private TypedNumberBidirectionalBinding(Property<T> property1, Property<Number> property2) {
            super(property1, property2);
            oldValue = property1.getValue();
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            if (!updating) {
                try {
                    updating = true;
                    if (property1 == sourceProperty) {
                        T newValue = property1.getValue();
                        property2.setValue(newValue);
                        property2.getValue();
                        oldValue = newValue;
                    } else {
                        T newValue = (T) property2.getValue();
                        property1.setValue(newValue);
                        property1.getValue();
                        oldValue = newValue;
                    }
                } catch (RuntimeException e) {
                    try {
                        if (property1 == sourceProperty) { property1.setValue(oldValue); property1.getValue(); }
                        else { property2.setValue(oldValue); property2.getValue(); }
                    } catch (Exception e2) {
                        e2.addSuppressed(e);
                        unbind(property1, property2);
                        throw new RuntimeException("Bidirectional binding failed together with an attempt to restore the source property to the previous value. Removing the bidirectional binding from properties " + property1 + " and " + property2, e2);
                    }
                    throw new RuntimeException("Bidirectional binding failed, setting to the previous value", e);
                } finally {
                    updating = false;
                }
            }
        }
    }

    private static class UntypedGenericBidirectionalBinding extends BidirectionalBinding {
        private final Object property1;
        private final Object property2;

        public UntypedGenericBidirectionalBinding(Object property1, Object property2) {
            super(property1, property2);
            this.property1 = property1;
            this.property2 = property2;
        }

        @Override protected Object getProperty1() { return property1; }
        @Override protected Object getProperty2() { return property2; }

        @Override
        public void invalidated(Observable sourceProperty) {
            throw new RuntimeException("Should not reach here");
        }
    }
}

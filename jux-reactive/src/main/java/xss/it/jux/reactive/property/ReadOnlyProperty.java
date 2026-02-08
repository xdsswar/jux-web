package xss.it.jux.reactive.property;

import xss.it.jux.reactive.value.ObservableValue;

/**
 * A named, read-only property. Extends {@link ObservableValue} with
 * {@link #getBean()} and {@link #getName()} for debugging and tooling.
 *
 * @param <T> the type of the wrapped value
 */
public interface ReadOnlyProperty<T> extends ObservableValue<T> {

    /**
     * Returns the bean this property belongs to, or {@code null} if standalone.
     *
     * @return the containing bean, or {@code null}
     */
    Object getBean();

    /**
     * Returns the name of this property, or an empty string if unnamed.
     *
     * @return the property name
     */
    String getName();
}

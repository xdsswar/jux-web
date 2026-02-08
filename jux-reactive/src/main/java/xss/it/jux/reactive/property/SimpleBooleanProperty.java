package xss.it.jux.reactive.property;

/**
 * A fully implemented {@link BooleanProperty} with bean and name fields.
 *
 * <p>This class provides a convenient implementation that combines
 * the functionality of {@link BooleanPropertyBase} with bean and name
 * metadata returned by {@link #getBean()} and {@link #getName()}.</p>
 *
 * @see BooleanPropertyBase
 */
public class SimpleBooleanProperty extends BooleanPropertyBase {

    private static final Object DEFAULT_BEAN = null;
    private static final String DEFAULT_NAME = "";

    private final Object bean;
    private final String name;

    @Override
    public Object getBean() {
        return bean;
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Creates a new {@code SimpleBooleanProperty} with an initial value of {@code false}.
     */
    public SimpleBooleanProperty() {
        this(DEFAULT_BEAN, DEFAULT_NAME);
    }

    /**
     * Creates a new {@code SimpleBooleanProperty} with the specified initial value.
     *
     * @param initialValue the initial value of the wrapped {@code boolean}
     */
    public SimpleBooleanProperty(boolean initialValue) {
        this(DEFAULT_BEAN, DEFAULT_NAME, initialValue);
    }

    /**
     * Creates a new {@code SimpleBooleanProperty} with the specified bean and name.
     *
     * @param bean the bean of this property
     * @param name the name of this property
     */
    public SimpleBooleanProperty(Object bean, String name) {
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }

    /**
     * Creates a new {@code SimpleBooleanProperty} with the specified bean, name,
     * and initial value.
     *
     * @param bean         the bean of this property
     * @param name         the name of this property
     * @param initialValue the initial value of the wrapped {@code boolean}
     */
    public SimpleBooleanProperty(Object bean, String name, boolean initialValue) {
        super(initialValue);
        this.bean = bean;
        this.name = (name == null) ? DEFAULT_NAME : name;
    }
}

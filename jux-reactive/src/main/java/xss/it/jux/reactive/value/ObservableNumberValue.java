package xss.it.jux.reactive.value;

/**
 * A common base interface for all observable numeric values.
 *
 * @see ObservableIntegerValue
 * @see ObservableLongValue
 * @see ObservableDoubleValue
 */
public interface ObservableNumberValue extends ObservableValue<Number> {

    int intValue();

    long longValue();

    float floatValue();

    double doubleValue();
}

package xss.it.jux.reactive.binding;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleBooleanProperty;
import xss.it.jux.reactive.property.SimpleDoubleProperty;
import xss.it.jux.reactive.property.SimpleIntegerProperty;
import xss.it.jux.reactive.property.SimpleObjectProperty;
import xss.it.jux.reactive.property.SimpleStringProperty;

import java.util.concurrent.atomic.AtomicBoolean;

@DisplayName("Bindings Utility")
class BindingsUtilityTest {

    @Nested
    @DisplayName("Arithmetic: Add")
    class Add {

        @Test
        void addTwoIntegerProperties_returnsSumBinding() {
            var a = new SimpleIntegerProperty(3);
            var b = new SimpleIntegerProperty(7);

            NumberBinding sum = Bindings.add(a, b);

            assertThat(sum.intValue()).isEqualTo(10);
        }

        @Test
        void addTwoIntegerProperties_updatesWhenDependencyChanges() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(10);

            NumberBinding sum = Bindings.add(a, b);
            assertThat(sum.intValue()).isEqualTo(15);

            a.set(20);
            assertThat(sum.intValue()).isEqualTo(30);

            b.set(1);
            assertThat(sum.intValue()).isEqualTo(21);
        }

        @Test
        void addIntegerPropertyAndDoubleConstant_returnsDoubleBinding() {
            var a = new SimpleIntegerProperty(10);

            DoubleBinding sum = Bindings.add(a, 2.5);

            assertThat(sum.doubleValue()).isEqualTo(12.5);
        }

        @Test
        void addDoubleConstantAndIntegerProperty_returnsDoubleBinding() {
            var b = new SimpleIntegerProperty(3);

            DoubleBinding sum = Bindings.add(1.5, b);

            assertThat(sum.doubleValue()).isEqualTo(4.5);
        }

        @Test
        void addIntegerPropertyAndIntConstant_returnsCorrectSum() {
            var a = new SimpleIntegerProperty(10);

            NumberBinding sum = Bindings.add(a, 5);

            assertThat(sum.intValue()).isEqualTo(15);
        }

        @Test
        void addTwoDoubleProperties_returnsDoubleSum() {
            var a = new SimpleDoubleProperty(1.1);
            var b = new SimpleDoubleProperty(2.2);

            NumberBinding sum = Bindings.add(a, b);

            assertThat(sum.doubleValue()).isCloseTo(3.3, within(0.0001));
        }
    }

    @Nested
    @DisplayName("Arithmetic: Subtract")
    class Subtract {

        @Test
        void subtractTwoIntegerProperties_returnsDifferenceBinding() {
            var a = new SimpleIntegerProperty(10);
            var b = new SimpleIntegerProperty(3);

            NumberBinding diff = Bindings.subtract(a, b);

            assertThat(diff.intValue()).isEqualTo(7);
        }

        @Test
        void subtractUpdatesOnDependencyChange_returnsNewDifference() {
            var a = new SimpleIntegerProperty(20);
            var b = new SimpleIntegerProperty(5);

            NumberBinding diff = Bindings.subtract(a, b);
            assertThat(diff.intValue()).isEqualTo(15);

            a.set(10);
            assertThat(diff.intValue()).isEqualTo(5);

            b.set(12);
            assertThat(diff.intValue()).isEqualTo(-2);
        }

        @Test
        void subtractWithDoubleConstant_returnsDoubleBinding() {
            var a = new SimpleIntegerProperty(10);

            DoubleBinding diff = Bindings.subtract(a, 3.5);

            assertThat(diff.doubleValue()).isEqualTo(6.5);
        }
    }

    @Nested
    @DisplayName("Arithmetic: Multiply")
    class Multiply {

        @Test
        void multiplyTwoIntegerProperties_returnsProductBinding() {
            var a = new SimpleIntegerProperty(4);
            var b = new SimpleIntegerProperty(5);

            NumberBinding product = Bindings.multiply(a, b);

            assertThat(product.intValue()).isEqualTo(20);
        }

        @Test
        void multiplyUpdatesOnDependencyChange_returnsNewProduct() {
            var a = new SimpleIntegerProperty(3);
            var b = new SimpleIntegerProperty(7);

            NumberBinding product = Bindings.multiply(a, b);
            assertThat(product.intValue()).isEqualTo(21);

            a.set(10);
            assertThat(product.intValue()).isEqualTo(70);
        }

        @Test
        void multiplyByZero_returnsZero() {
            var a = new SimpleIntegerProperty(999);
            var b = new SimpleIntegerProperty(0);

            NumberBinding product = Bindings.multiply(a, b);

            assertThat(product.intValue()).isEqualTo(0);
        }

        @Test
        void multiplyIntegerByDoubleConstant_returnsDoubleBinding() {
            var a = new SimpleIntegerProperty(5);

            DoubleBinding product = Bindings.multiply(a, 2.5);

            assertThat(product.doubleValue()).isEqualTo(12.5);
        }
    }

    @Nested
    @DisplayName("Arithmetic: Divide")
    class Divide {

        @Test
        void divideTwoIntegerProperties_returnsQuotientBinding() {
            var a = new SimpleIntegerProperty(20);
            var b = new SimpleIntegerProperty(4);

            NumberBinding quotient = Bindings.divide(a, b);

            assertThat(quotient.intValue()).isEqualTo(5);
        }

        @Test
        void divideUpdatesOnDependencyChange_returnsNewQuotient() {
            var a = new SimpleIntegerProperty(100);
            var b = new SimpleIntegerProperty(10);

            NumberBinding quotient = Bindings.divide(a, b);
            assertThat(quotient.intValue()).isEqualTo(10);

            a.set(50);
            assertThat(quotient.intValue()).isEqualTo(5);
        }

        @Test
        void divideDoubleByZero_returnsInfinity() {
            var a = new SimpleDoubleProperty(10.0);
            var b = new SimpleDoubleProperty(0.0);

            NumberBinding quotient = Bindings.divide(a, b);

            assertThat(quotient.doubleValue()).isInfinite();
        }

        @Test
        void integerDivisionTruncates_returnsFlooredResult() {
            var a = new SimpleIntegerProperty(7);
            var b = new SimpleIntegerProperty(2);

            NumberBinding quotient = Bindings.divide(a, b);

            assertThat(quotient.intValue()).isEqualTo(3);
        }

        @Test
        void divideIntegerPropertyByDoubleConstant_returnsDoubleBinding() {
            var a = new SimpleIntegerProperty(10);

            DoubleBinding quotient = Bindings.divide(a, 3.0);

            assertThat(quotient.doubleValue()).isCloseTo(3.3333, within(0.001));
        }
    }

    @Nested
    @DisplayName("Negation")
    class Negation {

        @Test
        void negateIntegerProperty_returnsNegatedValue() {
            var a = new SimpleIntegerProperty(5);

            NumberBinding neg = Bindings.negate(a);

            assertThat(neg.intValue()).isEqualTo(-5);
        }

        @Test
        void negateDoubleProperty_returnsNegatedValue() {
            var a = new SimpleDoubleProperty(3.14);

            NumberBinding neg = Bindings.negate(a);

            assertThat(neg.doubleValue()).isCloseTo(-3.14, within(0.0001));
        }

        @Test
        void negateUpdatesOnChange_reflectsNewNegation() {
            var a = new SimpleIntegerProperty(10);

            NumberBinding neg = Bindings.negate(a);
            assertThat(neg.intValue()).isEqualTo(-10);

            a.set(-7);
            assertThat(neg.intValue()).isEqualTo(7);
        }

        @Test
        void negateNullOperand_throwsNullPointerException() {
            assertThatThrownBy(() -> Bindings.negate(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Numeric Equality and Inequality")
    class NumericEqualityAndInequality {

        @Test
        void equalTwoEqualIntegerProperties_returnsTrue() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalTwoDifferentIntegerProperties_returnsFalse() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(10);

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isFalse();
        }

        @Test
        void equalUpdatesOnDependencyChange_reflectsNewEquality() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(10);

            BooleanBinding eq = Bindings.equal(a, b);
            assertThat(eq.get()).isFalse();

            b.set(5);
            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalWithEpsilon_withinToleranceReturnsTrue() {
            var a = new SimpleDoubleProperty(1.0);
            var b = new SimpleDoubleProperty(1.0001);

            BooleanBinding eq = Bindings.equal(a, b, 0.001);

            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalWithEpsilon_outsideToleranceReturnsFalse() {
            var a = new SimpleDoubleProperty(1.0);
            var b = new SimpleDoubleProperty(1.01);

            BooleanBinding eq = Bindings.equal(a, b, 0.001);

            assertThat(eq.get()).isFalse();
        }

        @Test
        void notEqualTwoDifferentProperties_returnsTrue() {
            var a = new SimpleIntegerProperty(3);
            var b = new SimpleIntegerProperty(7);

            BooleanBinding neq = Bindings.notEqual(a, b);

            assertThat(neq.get()).isTrue();
        }

        @Test
        void notEqualTwoEqualProperties_returnsFalse() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding neq = Bindings.notEqual(a, b);

            assertThat(neq.get()).isFalse();
        }

        @Test
        void equalIntegerPropertyAndIntConstant_returnsCorrectResult() {
            var a = new SimpleIntegerProperty(42);

            BooleanBinding eq = Bindings.equal(a, 42);

            assertThat(eq.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Numeric Comparisons")
    class NumericComparisons {

        @Test
        void greaterThanWhenFirstIsLarger_returnsTrue() {
            var a = new SimpleIntegerProperty(10);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding gt = Bindings.greaterThan(a, b);

            assertThat(gt.get()).isTrue();
        }

        @Test
        void greaterThanWhenEqual_returnsFalse() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding gt = Bindings.greaterThan(a, b);

            assertThat(gt.get()).isFalse();
        }

        @Test
        void lessThanWhenFirstIsSmaller_returnsTrue() {
            var a = new SimpleIntegerProperty(3);
            var b = new SimpleIntegerProperty(10);

            BooleanBinding lt = Bindings.lessThan(a, b);

            assertThat(lt.get()).isTrue();
        }

        @Test
        void lessThanWhenEqual_returnsFalse() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding lt = Bindings.lessThan(a, b);

            assertThat(lt.get()).isFalse();
        }

        @Test
        void greaterThanOrEqualWhenEqual_returnsTrue() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding gte = Bindings.greaterThanOrEqual(a, b);

            assertThat(gte.get()).isTrue();
        }

        @Test
        void greaterThanOrEqualWhenGreater_returnsTrue() {
            var a = new SimpleIntegerProperty(10);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding gte = Bindings.greaterThanOrEqual(a, b);

            assertThat(gte.get()).isTrue();
        }

        @Test
        void lessThanOrEqualWhenEqual_returnsTrue() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(5);

            BooleanBinding lte = Bindings.lessThanOrEqual(a, b);

            assertThat(lte.get()).isTrue();
        }

        @Test
        void lessThanOrEqualWhenLess_returnsTrue() {
            var a = new SimpleIntegerProperty(3);
            var b = new SimpleIntegerProperty(7);

            BooleanBinding lte = Bindings.lessThanOrEqual(a, b);

            assertThat(lte.get()).isTrue();
        }

        @Test
        void comparisonUpdatesOnDependencyChange_reflectsNewRelation() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(10);

            BooleanBinding gt = Bindings.greaterThan(a, b);
            assertThat(gt.get()).isFalse();

            a.set(15);
            assertThat(gt.get()).isTrue();
        }

        @Test
        void greaterThanWithIntConstant_comparesCorrectly() {
            var a = new SimpleIntegerProperty(10);

            BooleanBinding gt = Bindings.greaterThan(a, 5);

            assertThat(gt.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("String Concat")
    class StringConcat {

        @Test
        void concatTwoStringProperties_returnsConcatenation() {
            var a = new SimpleStringProperty("Hello");
            var b = new SimpleStringProperty(" World");

            StringBinding result = Bindings.concat(a, b);

            assertThat(result.get()).isEqualTo("Hello World");
        }

        @Test
        void concatUpdatesOnDependencyChange_reflectsNewConcatenation() {
            var a = new SimpleStringProperty("Foo");
            var b = new SimpleStringProperty("Bar");

            StringBinding result = Bindings.concat(a, b);
            assertThat(result.get()).isEqualTo("FooBar");

            a.set("Baz");
            assertThat(result.get()).isEqualTo("BazBar");
        }

        @Test
        void concatMixedObservableAndConstant_concatenatesAll() {
            var a = new SimpleStringProperty("Hello");

            StringBinding result = Bindings.concat(a, " ", "World");

            assertThat(result.get()).isEqualTo("Hello World");
        }

        @Test
        void concatWithNullObservableValue_printsNull() {
            var a = new SimpleObjectProperty<String>(null);
            var b = new SimpleStringProperty("text");

            StringBinding result = Bindings.concat(a, b);

            assertThat(result.get()).isEqualTo("nulltext");
        }

        @Test
        void concatNoArgs_returnsEmptyString() {
            StringBinding result = Bindings.concat();

            assertThat(result.get()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Boolean: And")
    class BooleanAnd {

        @Test
        void andBothTrue_returnsTrue() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(true);

            BooleanBinding result = Bindings.and(a, b);

            assertThat(result.get()).isTrue();
        }

        @Test
        void andOneFalse_returnsFalse() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.and(a, b);

            assertThat(result.get()).isFalse();
        }

        @Test
        void andBothFalse_returnsFalse() {
            var a = new SimpleBooleanProperty(false);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.and(a, b);

            assertThat(result.get()).isFalse();
        }

        @Test
        void andUpdatesOnChange_reflectsNewState() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.and(a, b);
            assertThat(result.get()).isFalse();

            b.set(true);
            assertThat(result.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Boolean: Or")
    class BooleanOr {

        @Test
        void orOneTrue_returnsTrue() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.or(a, b);

            assertThat(result.get()).isTrue();
        }

        @Test
        void orBothFalse_returnsFalse() {
            var a = new SimpleBooleanProperty(false);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.or(a, b);

            assertThat(result.get()).isFalse();
        }

        @Test
        void orBothTrue_returnsTrue() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(true);

            BooleanBinding result = Bindings.or(a, b);

            assertThat(result.get()).isTrue();
        }

        @Test
        void orUpdatesOnChange_reflectsNewState() {
            var a = new SimpleBooleanProperty(false);
            var b = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.or(a, b);
            assertThat(result.get()).isFalse();

            a.set(true);
            assertThat(result.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Boolean: Not")
    class BooleanNot {

        @Test
        void notTrue_returnsFalse() {
            var a = new SimpleBooleanProperty(true);

            BooleanBinding result = Bindings.not(a);

            assertThat(result.get()).isFalse();
        }

        @Test
        void notFalse_returnsTrue() {
            var a = new SimpleBooleanProperty(false);

            BooleanBinding result = Bindings.not(a);

            assertThat(result.get()).isTrue();
        }

        @Test
        void notUpdatesOnChange_reflectsNegation() {
            var a = new SimpleBooleanProperty(true);

            BooleanBinding result = Bindings.not(a);
            assertThat(result.get()).isFalse();

            a.set(false);
            assertThat(result.get()).isTrue();
        }

        @Test
        void notNullOperand_throwsNullPointerException() {
            assertThatThrownBy(() -> Bindings.not(null))
                    .isInstanceOf(NullPointerException.class);
        }
    }

    @Nested
    @DisplayName("Object Equality")
    class ObjectEquality {

        @Test
        void equalTwoObjectPropertiesWithSameValue_returnsTrue() {
            var a = new SimpleObjectProperty<>("hello");
            var b = new SimpleObjectProperty<>("hello");

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalTwoObjectPropertiesWithDifferentValues_returnsFalse() {
            var a = new SimpleObjectProperty<>("hello");
            var b = new SimpleObjectProperty<>("world");

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isFalse();
        }

        @Test
        void equalObjectPropertyAndConstant_returnsCorrectResult() {
            var a = new SimpleObjectProperty<>("test");

            BooleanBinding eq = Bindings.equal(a, "test");

            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalBothNull_returnsTrue() {
            var a = new SimpleObjectProperty<String>(null);
            var b = new SimpleObjectProperty<String>(null);

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isTrue();
        }

        @Test
        void equalOneNull_returnsFalse() {
            var a = new SimpleObjectProperty<>("value");
            var b = new SimpleObjectProperty<String>(null);

            BooleanBinding eq = Bindings.equal(a, b);

            assertThat(eq.get()).isFalse();
        }

        @Test
        void notEqualTwoDifferentObjects_returnsTrue() {
            var a = new SimpleObjectProperty<>("alpha");
            var b = new SimpleObjectProperty<>("beta");

            BooleanBinding neq = Bindings.notEqual(a, b);

            assertThat(neq.get()).isTrue();
        }

        @Test
        void notEqualTwoEqualObjects_returnsFalse() {
            var a = new SimpleObjectProperty<>("same");
            var b = new SimpleObjectProperty<>("same");

            BooleanBinding neq = Bindings.notEqual(a, b);

            assertThat(neq.get()).isFalse();
        }

        @Test
        void objectEqualityUpdatesOnChange_reflectsNewComparison() {
            var a = new SimpleObjectProperty<>("x");
            var b = new SimpleObjectProperty<>("y");

            BooleanBinding eq = Bindings.equal(a, b);
            assertThat(eq.get()).isFalse();

            b.set("x");
            assertThat(eq.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("When / Then / Otherwise (Ternary)")
    class WhenThenOtherwise {

        @Test
        void whenConditionTrue_returnsThenValue() {
            var condition = new SimpleBooleanProperty(true);

            NumberBinding result = Bindings.when(condition).then(10).otherwise(20);

            assertThat(result.intValue()).isEqualTo(10);
        }

        @Test
        void whenConditionFalse_returnsOtherwiseValue() {
            var condition = new SimpleBooleanProperty(false);

            NumberBinding result = Bindings.when(condition).then(10).otherwise(20);

            assertThat(result.intValue()).isEqualTo(20);
        }

        @Test
        void whenConditionChanges_resultUpdates() {
            var condition = new SimpleBooleanProperty(true);

            NumberBinding result = Bindings.when(condition).then(100).otherwise(0);
            assertThat(result.intValue()).isEqualTo(100);

            condition.set(false);
            assertThat(result.intValue()).isEqualTo(0);

            condition.set(true);
            assertThat(result.intValue()).isEqualTo(100);
        }

        @Test
        void whenWithBooleanConstants_thenTrueOtherwiseFalse() {
            var condition = new SimpleBooleanProperty(true);

            BooleanBinding result = Bindings.when(condition).then(true).otherwise(false);

            assertThat(result.get()).isTrue();

            condition.set(false);
            assertThat(result.get()).isFalse();
        }

        @Test
        void whenWithStringConstants_returnsThenOrOtherwiseString() {
            var condition = new SimpleBooleanProperty(true);

            StringBinding result = Bindings.when(condition).then("yes").otherwise("no");

            assertThat(result.get()).isEqualTo("yes");

            condition.set(false);
            assertThat(result.get()).isEqualTo("no");
        }

        @Test
        void whenWithObservableNumberValues_thenBranchUpdatesResult() {
            var condition = new SimpleBooleanProperty(true);
            var thenVal = new SimpleIntegerProperty(50);
            var otherwiseVal = new SimpleIntegerProperty(100);

            NumberBinding result = Bindings.when(condition)
                    .then(thenVal)
                    .otherwise(otherwiseVal);

            assertThat(result.intValue()).isEqualTo(50);

            thenVal.set(75);
            assertThat(result.intValue()).isEqualTo(75);

            condition.set(false);
            assertThat(result.intValue()).isEqualTo(100);
        }

        @Test
        void whenWithDoubleValues_returnsDoubleBinding() {
            var condition = new SimpleBooleanProperty(true);

            DoubleBinding result = Bindings.when(condition).then(3.14).otherwise(2.72);

            assertThat(result.doubleValue()).isCloseTo(3.14, within(0.001));

            condition.set(false);
            assertThat(result.doubleValue()).isCloseTo(2.72, within(0.001));
        }
    }

    @Nested
    @DisplayName("Reactive Propagation")
    class ReactivePropagation {

        @Test
        void arithmeticBindingReactsToPropertyChanges_chainedComputation() {
            var price = new SimpleDoubleProperty(100.0);
            var taxRate = new SimpleDoubleProperty(0.08);

            NumberBinding tax = Bindings.multiply(price, taxRate);
            NumberBinding total = Bindings.add(price, tax);

            assertThat(total.doubleValue()).isCloseTo(108.0, within(0.001));

            price.set(200.0);
            assertThat(total.doubleValue()).isCloseTo(216.0, within(0.001));

            taxRate.set(0.10);
            assertThat(total.doubleValue()).isCloseTo(220.0, within(0.001));
        }

        @Test
        void booleanBindingReactsToPropertyChanges_compositeLogic() {
            var loggedIn = new SimpleBooleanProperty(true);
            var isAdmin = new SimpleBooleanProperty(false);

            BooleanBinding canAccess = Bindings.and(loggedIn, isAdmin);
            assertThat(canAccess.get()).isFalse();

            isAdmin.set(true);
            assertThat(canAccess.get()).isTrue();

            loggedIn.set(false);
            assertThat(canAccess.get()).isFalse();
        }

        @Test
        void bindingInvalidationListenerFires_onPropertyChange() {
            var a = new SimpleIntegerProperty(1);
            var b = new SimpleIntegerProperty(2);

            NumberBinding sum = Bindings.add(a, b);
            sum.intValue(); // Force validation

            var fired = new AtomicBoolean(false);
            sum.addListener((InvalidationListener) obs -> fired.set(true));

            a.set(10);

            assertThat(fired.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Custom Binding Factories")
    class CustomBindingFactories {

        @Test
        void createBooleanBinding_computesFromCallable() {
            var a = new SimpleIntegerProperty(5);
            var b = new SimpleIntegerProperty(3);

            BooleanBinding isGreater = Bindings.createBooleanBinding(
                    () -> a.get() > b.get(), a, b);

            assertThat(isGreater.get()).isTrue();

            a.set(1);
            assertThat(isGreater.get()).isFalse();
        }

        @Test
        void createIntegerBinding_computesFromCallable() {
            var a = new SimpleIntegerProperty(10);
            var b = new SimpleIntegerProperty(20);

            IntegerBinding max = Bindings.createIntegerBinding(
                    () -> Math.max(a.get(), b.get()), a, b);

            assertThat(max.get()).isEqualTo(20);

            a.set(30);
            assertThat(max.get()).isEqualTo(30);
        }

        @Test
        void createStringBinding_computesFromCallable() {
            var name = new SimpleStringProperty("World");

            StringBinding greeting = Bindings.createStringBinding(
                    () -> "Hello, " + name.get() + "!", name);

            assertThat(greeting.get()).isEqualTo("Hello, World!");

            name.set("JUX");
            assertThat(greeting.get()).isEqualTo("Hello, JUX!");
        }

        @Test
        void createDoubleBinding_computesFromCallable() {
            var radius = new SimpleDoubleProperty(5.0);

            DoubleBinding area = Bindings.createDoubleBinding(
                    () -> Math.PI * radius.get() * radius.get(), radius);

            assertThat(area.get()).isCloseTo(78.5398, within(0.001));

            radius.set(10.0);
            assertThat(area.get()).isCloseTo(314.1592, within(0.001));
        }

        @Test
        void createObjectBinding_computesFromCallable() {
            var firstName = new SimpleStringProperty("John");
            var lastName = new SimpleStringProperty("Doe");

            ObjectBinding<String> fullName = Bindings.createObjectBinding(
                    () -> firstName.get() + " " + lastName.get(), firstName, lastName);

            assertThat(fullName.get()).isEqualTo("John Doe");

            lastName.set("Smith");
            assertThat(fullName.get()).isEqualTo("John Smith");
        }
    }
}

package xss.it.jux.reactive.property;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimpleDoubleProperty")
class SimpleDoublePropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToZero() {
            var property = new SimpleDoubleProperty();
            assertThat(property.get()).isEqualTo(0.0);
        }

        @Test
        void initialValueConstructor_storesValue() {
            var property = new SimpleDoubleProperty(3.14);
            assertThat(property.get()).isEqualTo(3.14);
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleDoubleProperty(bean, "ratio");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("ratio");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleDoubleProperty(bean, "ratio", 2.718);
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("ratio");
            assertThat(property.get()).isEqualTo(2.718);
        }

        @Test
        void defaultConstructor_beanIsNullAndNameIsEmpty() {
            var property = new SimpleDoubleProperty();
            assertThat(property.getBean()).isNull();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleDoubleProperty(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleDoubleProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleDoubleProperty();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set(99.9);
            assertThat(property.get()).isEqualTo(99.9);
        }

        @Test
        void settingSameValue_doesNotFireListener() {
            property.set(1.5);
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(1.5);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentValue_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(7.7);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentValue_firesChangeListenerWithOldAndNewValues() {
            property.set(1.0);
            var capturedOld = new AtomicReference<Number>();
            var capturedNew = new AtomicReference<Number>();
            property.addListener((ChangeListener<Number>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            property.set(2.0);

            assertThat(capturedOld.get().doubleValue()).isEqualTo(1.0);
            assertThat(capturedNew.get().doubleValue()).isEqualTo(2.0);
        }

        @Test
        void negativeValues_workCorrectly() {
            property.set(-0.001);
            assertThat(property.get()).isEqualTo(-0.001);
        }

        @Test
        void specialDoubleValues_workCorrectly() {
            property.set(Double.MAX_VALUE);
            assertThat(property.get()).isEqualTo(Double.MAX_VALUE);
        }
    }

    @Nested
    @DisplayName("Binding")
    class Binding {

        private SimpleDoubleProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleDoubleProperty();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleDoubleProperty(5.0);
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleDoubleProperty(42.5);
            property.bind(source);
            assertThat(property.get()).isEqualTo(42.5);
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleDoubleProperty(1.0);
            property.bind(source);
            source.set(2.0);
            assertThat(property.get()).isEqualTo(2.0);
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleDoubleProperty(5.0);
            property.bind(source);

            assertThatThrownBy(() -> property.set(99.0))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleDoubleProperty(77.7);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(77.7);
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleDoubleProperty(5.0);
            property.bind(source);
            property.unbind();

            property.set(100.0);
            assertThat(property.get()).isEqualTo(100.0);
        }

        @Test
        void bindToNull_throwsNullPointerException() {
            assertThatThrownBy(() -> property.bind(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void boundToNullNumberValue_getReturnsZero() {
            var source = new SimpleObjectProperty<Number>(null);
            property.bind(source);
            assertThat(property.get()).isEqualTo(0.0);
        }

        @Test
        void unbindFromNullNumberValue_capturesZero() {
            var source = new SimpleObjectProperty<Number>(null);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleDoubleProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleDoubleProperty();
        }

        @Test
        void multipleListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set(1.0);

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set(1.0);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleDoubleProperty(0.0);
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set(99.9);

            assertThat(fired.get()).isTrue();
        }
    }
}

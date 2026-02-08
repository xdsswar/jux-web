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

@DisplayName("SimpleIntegerProperty")
class SimpleIntegerPropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToZero() {
            var property = new SimpleIntegerProperty();
            assertThat(property.get()).isEqualTo(0);
        }

        @Test
        void initialValueConstructor_storesValue() {
            var property = new SimpleIntegerProperty(42);
            assertThat(property.get()).isEqualTo(42);
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleIntegerProperty(bean, "count");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("count");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleIntegerProperty(bean, "count", 99);
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("count");
            assertThat(property.get()).isEqualTo(99);
        }

        @Test
        void defaultConstructor_beanIsNullAndNameIsEmpty() {
            var property = new SimpleIntegerProperty();
            assertThat(property.getBean()).isNull();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleIntegerProperty(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleIntegerProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleIntegerProperty();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set(55);
            assertThat(property.get()).isEqualTo(55);
        }

        @Test
        void settingSameValue_doesNotFireListener() {
            property.set(10);
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(10);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentValue_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(7);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentValue_firesChangeListenerWithOldAndNewValues() {
            property.set(10);
            var capturedOld = new AtomicReference<Number>();
            var capturedNew = new AtomicReference<Number>();
            property.addListener((ChangeListener<Number>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            property.set(20);

            assertThat(capturedOld.get().intValue()).isEqualTo(10);
            assertThat(capturedNew.get().intValue()).isEqualTo(20);
        }

        @Test
        void negativeValues_workCorrectly() {
            property.set(-100);
            assertThat(property.get()).isEqualTo(-100);
        }
    }

    @Nested
    @DisplayName("Binding")
    class Binding {

        private SimpleIntegerProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleIntegerProperty();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleIntegerProperty(5);
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleIntegerProperty(42);
            property.bind(source);
            assertThat(property.get()).isEqualTo(42);
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleIntegerProperty(1);
            property.bind(source);
            source.set(2);
            assertThat(property.get()).isEqualTo(2);
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleIntegerProperty(5);
            property.bind(source);

            assertThatThrownBy(() -> property.set(99))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleIntegerProperty(77);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(77);
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleIntegerProperty(5);
            property.bind(source);
            property.unbind();

            property.set(100);
            assertThat(property.get()).isEqualTo(100);
        }

        @Test
        void bindToNull_throwsNullPointerException() {
            assertThatThrownBy(() -> property.bind(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void boundToNullNumberValue_getReturnsZero() {
            // When the observable holds null Number, get() should return 0
            var source = new SimpleObjectProperty<Number>(null);
            property.bind(source);
            assertThat(property.get()).isEqualTo(0);
        }

        @Test
        void unbindFromNullNumberValue_capturesZero() {
            var source = new SimpleObjectProperty<Number>(null);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleIntegerProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleIntegerProperty();
        }

        @Test
        void multipleListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set(1);

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set(1);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleIntegerProperty(0);
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set(99);

            assertThat(fired.get()).isTrue();
        }
    }
}

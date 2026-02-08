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

@DisplayName("SimpleLongProperty")
class SimpleLongPropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToZero() {
            var property = new SimpleLongProperty();
            assertThat(property.get()).isEqualTo(0L);
        }

        @Test
        void initialValueConstructor_storesValue() {
            var property = new SimpleLongProperty(123456789L);
            assertThat(property.get()).isEqualTo(123456789L);
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleLongProperty(bean, "timestamp");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("timestamp");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleLongProperty(bean, "timestamp", 999L);
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("timestamp");
            assertThat(property.get()).isEqualTo(999L);
        }

        @Test
        void defaultConstructor_beanIsNullAndNameIsEmpty() {
            var property = new SimpleLongProperty();
            assertThat(property.getBean()).isNull();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleLongProperty(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleLongProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleLongProperty();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set(Long.MAX_VALUE);
            assertThat(property.get()).isEqualTo(Long.MAX_VALUE);
        }

        @Test
        void settingSameValue_doesNotFireListener() {
            property.set(50L);
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(50L);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentValue_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(100L);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentValue_firesChangeListenerWithOldAndNewValues() {
            property.set(10L);
            var capturedOld = new AtomicReference<Number>();
            var capturedNew = new AtomicReference<Number>();
            property.addListener((ChangeListener<Number>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            property.set(20L);

            assertThat(capturedOld.get().longValue()).isEqualTo(10L);
            assertThat(capturedNew.get().longValue()).isEqualTo(20L);
        }

        @Test
        void negativeValues_workCorrectly() {
            property.set(-9999999L);
            assertThat(property.get()).isEqualTo(-9999999L);
        }
    }

    @Nested
    @DisplayName("Binding")
    class Binding {

        private SimpleLongProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleLongProperty();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleLongProperty(5L);
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleLongProperty(42L);
            property.bind(source);
            assertThat(property.get()).isEqualTo(42L);
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleLongProperty(1L);
            property.bind(source);
            source.set(2L);
            assertThat(property.get()).isEqualTo(2L);
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleLongProperty(5L);
            property.bind(source);

            assertThatThrownBy(() -> property.set(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleLongProperty(77L);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(77L);
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleLongProperty(5L);
            property.bind(source);
            property.unbind();

            property.set(100L);
            assertThat(property.get()).isEqualTo(100L);
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
            assertThat(property.get()).isEqualTo(0L);
        }

        @Test
        void unbindFromNullNumberValue_capturesZero() {
            var source = new SimpleObjectProperty<Number>(null);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo(0L);
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleLongProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleLongProperty();
        }

        @Test
        void multipleListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set(1L);

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set(1L);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleLongProperty(0L);
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set(99L);

            assertThat(fired.get()).isTrue();
        }
    }
}

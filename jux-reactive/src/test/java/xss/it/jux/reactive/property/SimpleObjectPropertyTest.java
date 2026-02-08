package xss.it.jux.reactive.property;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.value.ObservableValue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

@DisplayName("SimpleObjectProperty")
class SimpleObjectPropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToNull() {
            var property = new SimpleObjectProperty<String>();
            assertThat(property.get()).isNull();
        }

        @Test
        void initialValueConstructor_storesValue() {
            var property = new SimpleObjectProperty<>("hello");
            assertThat(property.get()).isEqualTo("hello");
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleObjectProperty<String>(bean, "myProp");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("myProp");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleObjectProperty<>(bean, "myProp", "initial");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("myProp");
            assertThat(property.get()).isEqualTo("initial");
        }

        @Test
        void defaultConstructor_beanIsNull() {
            var property = new SimpleObjectProperty<String>();
            assertThat(property.getBean()).isNull();
        }

        @Test
        void defaultConstructor_nameIsEmptyString() {
            var property = new SimpleObjectProperty<String>();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleObjectProperty<String>(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInFullConstructor_becomesEmptyString() {
            var property = new SimpleObjectProperty<>(new Object(), null, "val");
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleObjectProperty<String> property;

        @BeforeEach
        void setUp() {
            property = new SimpleObjectProperty<>();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set("test");
            assertThat(property.get()).isEqualTo("test");
        }

        @Test
        void settingSameReference_doesNotFireInvalidationListener() {
            var ref = new Object();
            var property = new SimpleObjectProperty<>(ref);
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(ref);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentReference_firesInvalidationListener() {
            property.set("old");
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set("new");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentReference_firesChangeListenerWithOldAndNewValues() {
            property.set("old");
            var capturedOld = new AtomicReference<String>();
            var capturedNew = new AtomicReference<String>();
            property.addListener((ChangeListener<String>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            property.set("new");

            assertThat(capturedOld.get()).isEqualTo("old");
            assertThat(capturedNew.get()).isEqualTo("new");
        }

        @Test
        void settingNullFromNonNull_firesListener() {
            property.set("something");
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(null);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingNonNullFromNull_firesListener() {
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set("value");

            assertThat(fired.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Binding")
    class Binding {

        private SimpleObjectProperty<String> property;

        @BeforeEach
        void setUp() {
            property = new SimpleObjectProperty<>();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleObjectProperty<>("source");
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleObjectProperty<>("source");
            property.bind(source);
            assertThat(property.get()).isEqualTo("source");
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleObjectProperty<>("initial");
            property.bind(source);
            source.set("updated");
            assertThat(property.get()).isEqualTo("updated");
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleObjectProperty<>("source");
            property.bind(source);

            assertThatThrownBy(() -> property.set("illegal"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleObjectProperty<>("captured");
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo("captured");
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleObjectProperty<>("src");
            property.bind(source);
            property.unbind();

            property.set("free");
            assertThat(property.get()).isEqualTo("free");
        }

        @Test
        void bindToNull_throwsNullPointerException() {
            assertThatThrownBy(() -> property.bind(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void unboundProperty_isBoundReturnsFalse() {
            assertThat(property.isBound()).isFalse();
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleObjectProperty<String> property;

        @BeforeEach
        void setUp() {
            property = new SimpleObjectProperty<>();
        }

        @Test
        void multipleInvalidationListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set("trigger");

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void multipleChangeListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((ChangeListener<String>) (obs, o, n) -> count.incrementAndGet());
            property.addListener((ChangeListener<String>) (obs, o, n) -> count.incrementAndGet());

            property.set("trigger");

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedInvalidationListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set("trigger");

            assertThat(fired.get()).isFalse();
        }

        @Test
        void removedChangeListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            ChangeListener<String> listener = (obs, o, n) -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set("trigger");

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleObjectProperty<>("a");
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set("b");

            assertThat(fired.get()).isTrue();
        }
    }
}

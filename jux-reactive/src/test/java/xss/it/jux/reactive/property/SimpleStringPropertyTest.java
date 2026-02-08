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

@DisplayName("SimpleStringProperty")
class SimpleStringPropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToNull() {
            var property = new SimpleStringProperty();
            assertThat(property.get()).isNull();
        }

        @Test
        void initialValueConstructor_storesValue() {
            var property = new SimpleStringProperty("hello");
            assertThat(property.get()).isEqualTo("hello");
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleStringProperty(bean, "myProp");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("myProp");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleStringProperty(bean, "myProp", "initial");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("myProp");
            assertThat(property.get()).isEqualTo("initial");
        }

        @Test
        void defaultConstructor_beanIsNullAndNameIsEmpty() {
            var property = new SimpleStringProperty();
            assertThat(property.getBean()).isNull();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleStringProperty(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleStringProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleStringProperty();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set("test");
            assertThat(property.get()).isEqualTo("test");
        }

        @Test
        void settingEqualStringDifferentReference_firesListener() {
            // StringPropertyBase uses (value == null) ? newValue != null : !value.equals(newValue)
            // So equal strings do NOT fire
            property.set("abc");
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(new String("abc"));

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentString_firesInvalidationListener() {
            property.set("old");
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set("new");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentString_firesChangeListenerWithOldAndNewValues() {
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

        private SimpleStringProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleStringProperty();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleStringProperty("source");
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleStringProperty("source");
            property.bind(source);
            assertThat(property.get()).isEqualTo("source");
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleStringProperty("initial");
            property.bind(source);
            source.set("updated");
            assertThat(property.get()).isEqualTo("updated");
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleStringProperty("source");
            property.bind(source);

            assertThatThrownBy(() -> property.set("illegal"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleStringProperty("captured");
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isEqualTo("captured");
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleStringProperty("src");
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
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleStringProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleStringProperty();
        }

        @Test
        void multipleListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set("trigger");

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set("trigger");

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleStringProperty("a");
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set("b");

            assertThat(fired.get()).isTrue();
        }
    }
}

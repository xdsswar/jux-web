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

@DisplayName("SimpleBooleanProperty")
class SimpleBooleanPropertyTest {

    @Nested
    @DisplayName("Constructors")
    class Constructors {

        @Test
        void defaultConstructor_initializesToFalse() {
            var property = new SimpleBooleanProperty();
            assertThat(property.get()).isFalse();
        }

        @Test
        void initialValueConstructor_storesTrue() {
            var property = new SimpleBooleanProperty(true);
            assertThat(property.get()).isTrue();
        }

        @Test
        void initialValueConstructor_storesFalse() {
            var property = new SimpleBooleanProperty(false);
            assertThat(property.get()).isFalse();
        }

        @Test
        void beanAndNameConstructor_storesBeanAndName() {
            var bean = new Object();
            var property = new SimpleBooleanProperty(bean, "flag");
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("flag");
        }

        @Test
        void beanNameAndInitialValueConstructor_storesAll() {
            var bean = new Object();
            var property = new SimpleBooleanProperty(bean, "flag", true);
            assertThat(property.getBean()).isSameAs(bean);
            assertThat(property.getName()).isEqualTo("flag");
            assertThat(property.get()).isTrue();
        }

        @Test
        void defaultConstructor_beanIsNullAndNameIsEmpty() {
            var property = new SimpleBooleanProperty();
            assertThat(property.getBean()).isNull();
            assertThat(property.getName()).isEqualTo("");
        }

        @Test
        void nullNameInConstructor_becomesEmptyString() {
            var property = new SimpleBooleanProperty(new Object(), null);
            assertThat(property.getName()).isEqualTo("");
        }
    }

    @Nested
    @DisplayName("Get and Set")
    class GetAndSet {

        private SimpleBooleanProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleBooleanProperty();
        }

        @Test
        void getAndSet_worksCorrectly() {
            property.set(true);
            assertThat(property.get()).isTrue();
        }

        @Test
        void settingSameValue_doesNotFireListener() {
            property.set(true);
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(true);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void settingDifferentValue_firesInvalidationListener() {
            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            property.set(true);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void settingDifferentValue_firesChangeListenerWithOldAndNewValues() {
            property.set(false);
            var capturedOld = new AtomicReference<Boolean>();
            var capturedNew = new AtomicReference<Boolean>();
            property.addListener((ChangeListener<Boolean>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            property.set(true);

            assertThat(capturedOld.get()).isFalse();
            assertThat(capturedNew.get()).isTrue();
        }
    }

    @Nested
    @DisplayName("Binding")
    class Binding {

        private SimpleBooleanProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleBooleanProperty();
        }

        @Test
        void bind_makesIsBoundTrue() {
            var source = new SimpleObjectProperty<>(Boolean.TRUE);
            property.bind(source);
            assertThat(property.isBound()).isTrue();
        }

        @Test
        void bind_getReturnsBoundValue() {
            var source = new SimpleObjectProperty<>(Boolean.TRUE);
            property.bind(source);
            assertThat(property.get()).isTrue();
        }

        @Test
        void bind_tracksBoundPropertyChanges() {
            var source = new SimpleObjectProperty<>(Boolean.FALSE);
            property.bind(source);
            source.set(Boolean.TRUE);
            assertThat(property.get()).isTrue();
        }

        @Test
        void setWhileBound_throwsRuntimeException() {
            var source = new SimpleObjectProperty<>(Boolean.TRUE);
            property.bind(source);

            assertThatThrownBy(() -> property.set(false))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("A bound value cannot be set");
        }

        @Test
        void unbind_capturesCurrentObservableValue() {
            var source = new SimpleObjectProperty<>(Boolean.TRUE);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isTrue();
            assertThat(property.isBound()).isFalse();
        }

        @Test
        void afterUnbind_setWorksAgain() {
            var source = new SimpleObjectProperty<>(Boolean.TRUE);
            property.bind(source);
            property.unbind();

            property.set(false);
            assertThat(property.get()).isFalse();
        }

        @Test
        void bindToNull_throwsNullPointerException() {
            assertThatThrownBy(() -> property.bind(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        void boundToNullBooleanValue_getReturnsFalse() {
            // When the observable holds null, Boolean.TRUE.equals(null) -> false
            var source = new SimpleObjectProperty<Boolean>(null);
            property.bind(source);
            assertThat(property.get()).isFalse();
        }

        @Test
        void unbindFromNullBooleanValue_capturesFalse() {
            var source = new SimpleObjectProperty<Boolean>(null);
            property.bind(source);
            property.unbind();

            assertThat(property.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleBooleanProperty property;

        @BeforeEach
        void setUp() {
            property = new SimpleBooleanProperty();
        }

        @Test
        void multipleListeners_allFire() {
            var count = new AtomicInteger(0);
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());
            property.addListener((InvalidationListener) obs -> count.incrementAndGet());

            property.set(true);

            assertThat(count.get()).isEqualTo(2);
        }

        @Test
        void removedListener_doesNotFire() {
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            property.addListener(listener);
            property.removeListener(listener);

            property.set(true);

            assertThat(fired.get()).isFalse();
        }

        @Test
        void boundPropertyChange_firesInvalidationListener() {
            var source = new SimpleObjectProperty<>(Boolean.FALSE);
            property.bind(source);

            var fired = new AtomicBoolean(false);
            property.addListener((InvalidationListener) obs -> fired.set(true));

            source.set(Boolean.TRUE);

            assertThat(fired.get()).isTrue();
        }
    }
}

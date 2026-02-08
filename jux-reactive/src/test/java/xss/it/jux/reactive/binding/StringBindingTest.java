package xss.it.jux.reactive.binding;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleStringProperty;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@DisplayName("StringBinding")
class StringBindingTest {

    @Nested
    @DisplayName("Lazy Evaluation and Caching")
    class LazyEvaluationAndCaching {

        private SimpleStringProperty dep;
        private AtomicInteger computeCount;
        private StringBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleStringProperty("hello");
            computeCount = new AtomicInteger(0);
            binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    computeCount.incrementAndGet();
                    return dep.get();
                }
            };
        }

        @Test
        void computeValueReturnsString_firstGetReturnsResult() {
            String result = binding.get();

            assertThat(result).isEqualTo("hello");
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        void subsequentGetReturnsCached_noRecomputation() {
            binding.get();
            int countAfterFirst = computeCount.get();

            String second = binding.get();

            assertThat(second).isEqualTo("hello");
            assertThat(computeCount.get()).isEqualTo(countAfterFirst);
        }

        @Test
        void getValueDelegatesToGet_sameResult() {
            assertThat(binding.getValue()).isEqualTo(binding.get());
        }

        @Test
        void invalidateCausesRecomputation_returnsUpdatedString() {
            binding.get();
            dep.set("world");

            String result = binding.get();

            assertThat(result).isEqualTo("world");
            assertThat(computeCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Validity State")
    class ValidityState {

        private SimpleStringProperty dep;
        private StringBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleStringProperty("text");
            binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };
        }

        @Test
        void isValidFalseInitially_startsInvalid() {
            assertThat(binding.isValid()).isFalse();
        }

        @Test
        void isValidTrueAfterGet_becomesValid() {
            binding.get();
            assertThat(binding.isValid()).isTrue();
        }

        @Test
        void isValidFalseAfterDependencyChange_becomesInvalidAgain() {
            binding.get();
            dep.set("changed");
            assertThat(binding.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dependency Binding and Unbinding")
    class DependencyBindingAndUnbinding {

        @Test
        void bindToDependency_changeInvalidatesAndUpdates() {
            var dep = new SimpleStringProperty("A");
            var binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get().toLowerCase();
                }
            };

            assertThat(binding.get()).isEqualTo("a");

            dep.set("HELLO");
            assertThat(binding.get()).isEqualTo("hello");
        }

        @Test
        void unbindStopsListening_changeDoesNotAffectBinding() {
            var dep = new SimpleStringProperty("original");
            var binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }

                void doUnbind() {
                    unbind(dep);
                }
            };

            binding.get();
            binding.doUnbind();
            dep.set("modified");

            assertThat(binding.isValid()).isTrue();
            assertThat(binding.get()).isEqualTo("original");
        }
    }

    @Nested
    @DisplayName("Null Value Handling")
    class NullValueHandling {

        @Test
        void computeValueReturningNull_getReturnsNull() {
            var dep = new SimpleStringProperty(null);
            var binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isNull();
        }

        @Test
        void transitionFromStringToNull_updatesCorrectly() {
            var dep = new SimpleStringProperty("present");
            var binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isEqualTo("present");

            dep.set(null);
            assertThat(binding.get()).isNull();
        }

        @Test
        void transitionFromNullToString_updatesCorrectly() {
            var dep = new SimpleStringProperty(null);
            var binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isNull();

            dep.set("now present");
            assertThat(binding.get()).isEqualTo("now present");
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleStringProperty dep;
        private StringBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleStringProperty("init");
            binding = new StringBinding() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };
        }

        @Test
        void invalidationListenerFires_onDependencyChange() {
            binding.get();
            var fired = new AtomicBoolean(false);
            binding.addListener((InvalidationListener) obs -> fired.set(true));

            dep.set("new");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void changeListenerFires_withOldAndNewValues() {
            binding.get();

            var capturedOld = new AtomicReference<String>();
            var capturedNew = new AtomicReference<String>();
            binding.addListener((ChangeListener<String>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            dep.set("updated");
            binding.get();

            assertThat(capturedOld.get()).isEqualTo("init");
            assertThat(capturedNew.get()).isEqualTo("updated");
        }

        @Test
        void removedListenersDoNotFire_afterRemoval() {
            binding.get();
            var invalidationFired = new AtomicBoolean(false);
            var changeFired = new AtomicBoolean(false);

            InvalidationListener il = obs -> invalidationFired.set(true);
            ChangeListener<String> cl = (obs, o, n) -> changeFired.set(true);

            binding.addListener(il);
            binding.addListener(cl);
            binding.removeListener(il);
            binding.removeListener(cl);

            dep.set("trigger");
            binding.get();

            assertThat(invalidationFired.get()).isFalse();
            assertThat(changeFired.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dispose and Defaults")
    class DisposeAndDefaults {

        @Test
        void disposeIsCallable_noException() {
            var binding = new StringBinding() {
                @Override
                protected String computeValue() {
                    return "test";
                }
            };

            assertThatCode(binding::dispose).doesNotThrowAnyException();
        }

        @Test
        void getDependenciesReturnsEmptyByDefault_emptyList() {
            var binding = new StringBinding() {
                @Override
                protected String computeValue() {
                    return "";
                }
            };

            assertThat(binding.getDependencies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("toString")
    class ToStringRepresentation {

        @Test
        void invalidBinding_containsInvalid() {
            var binding = new StringBinding() {
                @Override
                protected String computeValue() {
                    return "test";
                }
            };

            assertThat(binding.toString()).contains("invalid");
        }

        @Test
        void validBinding_containsValue() {
            var binding = new StringBinding() {
                @Override
                protected String computeValue() {
                    return "visible";
                }
            };
            binding.get();

            assertThat(binding.toString()).contains("visible");
        }
    }
}

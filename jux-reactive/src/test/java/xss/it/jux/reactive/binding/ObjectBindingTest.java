package xss.it.jux.reactive.binding;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleObjectProperty;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@DisplayName("ObjectBinding")
class ObjectBindingTest {

    @Nested
    @DisplayName("Lazy Evaluation and Caching")
    class LazyEvaluationAndCaching {

        private SimpleObjectProperty<String> dep;
        private AtomicInteger computeCount;
        private ObjectBinding<String> binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleObjectProperty<>("initial");
            computeCount = new AtomicInteger(0);
            binding = new ObjectBinding<>() {
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
        void computeValueCalledOnFirstGet_returnsComputedResult() {
            String result = binding.get();

            assertThat(result).isEqualTo("initial");
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        void subsequentGetReturnsCachedValue_computeValueNotCalledAgain() {
            binding.get();
            int countAfterFirst = computeCount.get();

            String second = binding.get();
            String third = binding.get();

            assertThat(second).isEqualTo("initial");
            assertThat(third).isEqualTo("initial");
            assertThat(computeCount.get()).isEqualTo(countAfterFirst);
        }

        @Test
        void getValueDelegatesToGet_returnsSameResult() {
            assertThat(binding.getValue()).isEqualTo(binding.get());
        }

        @Test
        void invalidateCausesRecomputationOnNextGet_returnsUpdatedValue() {
            binding.get();
            dep.set("updated");

            String result = binding.get();

            assertThat(result).isEqualTo("updated");
            assertThat(computeCount.get()).isEqualTo(2);
        }

        @Test
        void multipleInvalidationsWithoutGet_onlySingleRecomputation() {
            binding.get();
            dep.set("a");
            dep.set("b");
            dep.set("c");

            binding.get();

            // First get() + one recomputation after all invalidations
            assertThat(computeCount.get()).isEqualTo(2);
            assertThat(binding.get()).isEqualTo("c");
        }
    }

    @Nested
    @DisplayName("Validity State")
    class ValidityState {

        private SimpleObjectProperty<String> dep;
        private ObjectBinding<String> binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleObjectProperty<>("value");
            binding = new ObjectBinding<>() {
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
        void isValidFalseInitially_bindingStartsInvalid() {
            assertThat(binding.isValid()).isFalse();
        }

        @Test
        void isValidTrueAfterGet_bindingBecomesValid() {
            binding.get();
            assertThat(binding.isValid()).isTrue();
        }

        @Test
        void isValidFalseAfterInvalidate_bindingBecomesInvalidAgain() {
            binding.get();
            dep.set("changed");

            assertThat(binding.isValid()).isFalse();
        }

        @Test
        void manualInvalidateOnAlreadyInvalidBinding_noEffect() {
            // Binding starts invalid
            assertThat(binding.isValid()).isFalse();
            binding.invalidate();
            assertThat(binding.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dependency Binding and Unbinding")
    class DependencyBindingAndUnbinding {

        @Test
        void bindToDependency_dependencyChangeInvalidatesBinding() {
            var dep = new SimpleObjectProperty<>("start");
            var binding = new ObjectBinding<String>() {
                {
                    bind(dep);
                }

                @Override
                protected String computeValue() {
                    return dep.get();
                }
            };

            binding.get();
            assertThat(binding.isValid()).isTrue();

            dep.set("changed");
            assertThat(binding.isValid()).isFalse();
            assertThat(binding.get()).isEqualTo("changed");
        }

        @Test
        void unbindFromDependency_dependencyChangeNoLongerInvalidatesBinding() {
            var dep = new SimpleObjectProperty<>("start");
            var binding = new ObjectBinding<String>() {
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
            dep.set("changed");

            // Should still be valid since we unbound
            assertThat(binding.isValid()).isTrue();
            // The cached value should be "start" (the value before unbinding)
            assertThat(binding.get()).isEqualTo("start");
        }

        @Test
        void multipleDependencies_anyOneInvalidatingCausesRecomputation() {
            var dep1 = new SimpleObjectProperty<>("A");
            var dep2 = new SimpleObjectProperty<>("B");

            var binding = new ObjectBinding<String>() {
                {
                    bind(dep1, dep2);
                }

                @Override
                protected String computeValue() {
                    return dep1.get() + dep2.get();
                }
            };

            assertThat(binding.get()).isEqualTo("AB");

            dep1.set("X");
            assertThat(binding.isValid()).isFalse();
            assertThat(binding.get()).isEqualTo("XB");

            dep2.set("Y");
            assertThat(binding.isValid()).isFalse();
            assertThat(binding.get()).isEqualTo("XY");
        }
    }

    @Nested
    @DisplayName("Invalidation Listeners")
    class InvalidationListeners {

        private SimpleObjectProperty<String> dep;
        private ObjectBinding<String> binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleObjectProperty<>("val");
            binding = new ObjectBinding<>() {
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
        void invalidationListenerFiresOnInvalidate_listenerIsNotified() {
            binding.get(); // Make valid first
            var fired = new AtomicBoolean(false);
            binding.addListener((InvalidationListener) obs -> fired.set(true));

            dep.set("new");

            assertThat(fired.get()).isTrue();
        }

        @Test
        void removedInvalidationListenerDoesNotFire_listenerSilenced() {
            binding.get();
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            binding.addListener(listener);
            binding.removeListener(listener);

            dep.set("new");

            assertThat(fired.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Change Listeners")
    class ChangeListeners {

        private SimpleObjectProperty<String> dep;
        private ObjectBinding<String> binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleObjectProperty<>("initial");
            binding = new ObjectBinding<>() {
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
        void changeListenerFiresWhenValueChanges_oldAndNewValuesProvided() {
            binding.get(); // Make valid, cache "initial"

            var capturedOld = new AtomicReference<String>();
            var capturedNew = new AtomicReference<String>();
            binding.addListener((ChangeListener<String>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            dep.set("changed");
            // Force recomputation to trigger change listener
            binding.get();

            assertThat(capturedOld.get()).isEqualTo("initial");
            assertThat(capturedNew.get()).isEqualTo("changed");
        }

        @Test
        void removedChangeListenerDoesNotFire_listenerSilenced() {
            binding.get();
            var fired = new AtomicBoolean(false);
            ChangeListener<String> listener = (obs, o, n) -> fired.set(true);
            binding.addListener(listener);
            binding.removeListener(listener);

            dep.set("new");
            binding.get();

            assertThat(fired.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dispose and GetDependencies")
    class DisposeAndGetDependencies {

        @Test
        void disposeIsCallableByDefault_noException() {
            var binding = new ObjectBinding<String>() {
                @Override
                protected String computeValue() {
                    return "hello";
                }
            };

            assertThatCode(binding::dispose).doesNotThrowAnyException();
        }

        @Test
        void getDependenciesReturnsEmptyListByDefault_emptyCollection() {
            var binding = new ObjectBinding<String>() {
                @Override
                protected String computeValue() {
                    return "test";
                }
            };

            assertThat(binding.getDependencies()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Null Value Handling")
    class NullValueHandling {

        @Test
        void computeValueReturningNull_getReturnsNull() {
            var dep = new SimpleObjectProperty<String>(null);
            var binding = new ObjectBinding<String>() {
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
        void transitionFromNonNullToNull_invalidatesAndRecomputes() {
            var dep = new SimpleObjectProperty<>("present");
            var binding = new ObjectBinding<String>() {
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
    }

    @Nested
    @DisplayName("toString")
    class ToStringRepresentation {

        @Test
        void invalidBinding_showsInvalid() {
            var binding = new ObjectBinding<String>() {
                @Override
                protected String computeValue() {
                    return "test";
                }
            };

            assertThat(binding.toString()).contains("invalid");
        }

        @Test
        void validBinding_showsValue() {
            var binding = new ObjectBinding<String>() {
                @Override
                protected String computeValue() {
                    return "hello";
                }
            };
            binding.get();

            assertThat(binding.toString()).contains("hello");
        }
    }
}

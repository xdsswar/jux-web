package xss.it.jux.reactive.binding;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleBooleanProperty;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@DisplayName("BooleanBinding")
class BooleanBindingTest {

    @Nested
    @DisplayName("Lazy Evaluation and Caching")
    class LazyEvaluationAndCaching {

        private SimpleBooleanProperty dep;
        private AtomicInteger computeCount;
        private BooleanBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleBooleanProperty(true);
            computeCount = new AtomicInteger(0);
            binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    computeCount.incrementAndGet();
                    return dep.get();
                }
            };
        }

        @Test
        void computeValueReturnsBoolean_firstGetReturnsResult() {
            boolean result = binding.get();

            assertThat(result).isTrue();
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        void subsequentGetReturnsCached_noRecomputation() {
            binding.get();
            int countAfterFirst = computeCount.get();

            boolean second = binding.get();

            assertThat(second).isTrue();
            assertThat(computeCount.get()).isEqualTo(countAfterFirst);
        }

        @Test
        void getValueReturnsBoxedBoolean_delegatesToGet() {
            Boolean boxed = binding.getValue();
            assertThat(boxed).isEqualTo(binding.get());
        }

        @Test
        void invalidateCausesRecomputation_returnsUpdatedValue() {
            binding.get();
            dep.set(false);

            boolean result = binding.get();

            assertThat(result).isFalse();
            assertThat(computeCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Validity State")
    class ValidityState {

        private SimpleBooleanProperty dep;
        private BooleanBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleBooleanProperty(false);
            binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
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
            dep.set(true);
            assertThat(binding.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("True/False Transitions")
    class TrueFalseTransitions {

        @Test
        void transitionFromTrueToFalse_reflectsChange() {
            var dep = new SimpleBooleanProperty(true);
            var binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isTrue();

            dep.set(false);
            assertThat(binding.get()).isFalse();
        }

        @Test
        void transitionFromFalseToTrue_reflectsChange() {
            var dep = new SimpleBooleanProperty(false);
            var binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isFalse();

            dep.set(true);
            assertThat(binding.get()).isTrue();
        }

        @Test
        void multipleTrueFalseToggles_alwaysCorrect() {
            var dep = new SimpleBooleanProperty(true);
            var binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return dep.get();
                }
            };

            assertThat(binding.get()).isTrue();
            dep.set(false);
            assertThat(binding.get()).isFalse();
            dep.set(true);
            assertThat(binding.get()).isTrue();
            dep.set(false);
            assertThat(binding.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dependency Binding and Unbinding")
    class DependencyBindingAndUnbinding {

        @Test
        void bindToDependency_changeInvalidatesAndUpdates() {
            var dep = new SimpleBooleanProperty(false);
            var binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return !dep.get(); // NOT gate
                }
            };

            assertThat(binding.get()).isTrue(); // !false = true

            dep.set(true);
            assertThat(binding.get()).isFalse(); // !true = false
        }

        @Test
        void unbindStopsListening_changeDoesNotAffectBinding() {
            var dep = new SimpleBooleanProperty(true);
            var binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return dep.get();
                }

                void doUnbind() {
                    unbind(dep);
                }
            };

            binding.get();
            binding.doUnbind();
            dep.set(false);

            assertThat(binding.isValid()).isTrue();
            assertThat(binding.get()).isTrue(); // Still cached as true
        }

        @Test
        void multipleDependencies_eitherInvalidatesBinding() {
            var a = new SimpleBooleanProperty(true);
            var b = new SimpleBooleanProperty(true);

            var binding = new BooleanBinding() {
                {
                    bind(a, b);
                }

                @Override
                protected boolean computeValue() {
                    return a.get() && b.get();
                }
            };

            assertThat(binding.get()).isTrue();

            a.set(false);
            assertThat(binding.get()).isFalse();

            a.set(true);
            b.set(false);
            assertThat(binding.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleBooleanProperty dep;
        private BooleanBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleBooleanProperty(false);
            binding = new BooleanBinding() {
                {
                    bind(dep);
                }

                @Override
                protected boolean computeValue() {
                    return dep.get();
                }
            };
        }

        @Test
        void invalidationListenerFires_onDependencyChange() {
            binding.get();
            var fired = new AtomicBoolean(false);
            binding.addListener((InvalidationListener) obs -> fired.set(true));

            dep.set(true);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void changeListenerFires_withOldAndNewValues() {
            binding.get(); // false

            var capturedOld = new AtomicReference<Boolean>();
            var capturedNew = new AtomicReference<Boolean>();
            binding.addListener((ChangeListener<Boolean>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            dep.set(true);
            binding.get();

            assertThat(capturedOld.get()).isFalse();
            assertThat(capturedNew.get()).isTrue();
        }

        @Test
        void removedListenersDoNotFire_afterRemoval() {
            binding.get();
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            binding.addListener(listener);
            binding.removeListener(listener);

            dep.set(true);

            assertThat(fired.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dispose and Defaults")
    class DisposeAndDefaults {

        @Test
        void disposeIsCallable_noException() {
            var binding = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return false;
                }
            };

            assertThatCode(binding::dispose).doesNotThrowAnyException();
        }

        @Test
        void getDependenciesReturnsEmptyByDefault_emptyList() {
            var binding = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return true;
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
            var binding = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return true;
                }
            };

            assertThat(binding.toString()).contains("invalid");
        }

        @Test
        void validTrueBinding_containsTrue() {
            var binding = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return true;
                }
            };
            binding.get();

            assertThat(binding.toString()).contains("true");
        }

        @Test
        void validFalseBinding_containsFalse() {
            var binding = new BooleanBinding() {
                @Override
                protected boolean computeValue() {
                    return false;
                }
            };
            binding.get();

            assertThat(binding.toString()).contains("false");
        }
    }
}

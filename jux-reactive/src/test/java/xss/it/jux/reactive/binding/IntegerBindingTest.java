package xss.it.jux.reactive.binding;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.reactive.ChangeListener;
import xss.it.jux.reactive.InvalidationListener;
import xss.it.jux.reactive.Observable;
import xss.it.jux.reactive.property.SimpleIntegerProperty;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@DisplayName("IntegerBinding")
class IntegerBindingTest {

    @Nested
    @DisplayName("Lazy Evaluation and Caching")
    class LazyEvaluationAndCaching {

        private SimpleIntegerProperty dep;
        private AtomicInteger computeCount;
        private IntegerBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleIntegerProperty(10);
            computeCount = new AtomicInteger(0);
            binding = new IntegerBinding() {
                {
                    bind(dep);
                }

                @Override
                protected int computeValue() {
                    computeCount.incrementAndGet();
                    return dep.get();
                }
            };
        }

        @Test
        void computeValueReturnsInt_firstGetReturnsResult() {
            int result = binding.get();

            assertThat(result).isEqualTo(10);
            assertThat(computeCount.get()).isEqualTo(1);
        }

        @Test
        void subsequentGetReturnsCachedValue_noRecomputation() {
            binding.get();
            int countAfterFirst = computeCount.get();

            int second = binding.get();
            int third = binding.get();

            assertThat(second).isEqualTo(10);
            assertThat(third).isEqualTo(10);
            assertThat(computeCount.get()).isEqualTo(countAfterFirst);
        }

        @Test
        void invalidateCausesRecomputation_returnsUpdatedValue() {
            binding.get();
            dep.set(42);

            int result = binding.get();

            assertThat(result).isEqualTo(42);
            assertThat(computeCount.get()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("Validity State")
    class ValidityState {

        private SimpleIntegerProperty dep;
        private IntegerBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleIntegerProperty(5);
            binding = new IntegerBinding() {
                {
                    bind(dep);
                }

                @Override
                protected int computeValue() {
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
            dep.set(99);
            assertThat(binding.isValid()).isFalse();
        }
    }

    @Nested
    @DisplayName("Number Type Conversions")
    class NumberTypeConversions {

        private IntegerBinding binding;

        @BeforeEach
        void setUp() {
            binding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return 42;
                }
            };
        }

        @Test
        void intValueReturnsInt_exactValue() {
            assertThat(binding.intValue()).isEqualTo(42);
        }

        @Test
        void longValueReturnsLong_widenedValue() {
            assertThat(binding.longValue()).isEqualTo(42L);
        }

        @Test
        void doubleValueReturnsDouble_widenedValue() {
            assertThat(binding.doubleValue()).isEqualTo(42.0);
        }

        @Test
        void negativeIntConversions_preserveSign() {
            var negBinding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return -17;
                }
            };

            assertThat(negBinding.intValue()).isEqualTo(-17);
            assertThat(negBinding.longValue()).isEqualTo(-17L);
            assertThat(negBinding.doubleValue()).isEqualTo(-17.0);
        }
    }

    @Nested
    @DisplayName("Dependency Binding and Unbinding")
    class DependencyBindingAndUnbinding {

        @Test
        void bindToDependency_changeInvalidatesBinding() {
            var dep = new SimpleIntegerProperty(1);
            var binding = new IntegerBinding() {
                {
                    bind(dep);
                }

                @Override
                protected int computeValue() {
                    return dep.get() * 2;
                }
            };

            assertThat(binding.get()).isEqualTo(2);

            dep.set(5);
            assertThat(binding.isValid()).isFalse();
            assertThat(binding.get()).isEqualTo(10);
        }

        @Test
        void unbindStopsListening_changeDoesNotInvalidate() {
            var dep = new SimpleIntegerProperty(3);
            var binding = new IntegerBinding() {
                {
                    bind(dep);
                }

                @Override
                protected int computeValue() {
                    return dep.get();
                }

                void doUnbind() {
                    unbind(dep);
                }
            };

            binding.get();
            binding.doUnbind();
            dep.set(100);

            assertThat(binding.isValid()).isTrue();
            assertThat(binding.get()).isEqualTo(3);
        }

        @Test
        void multipleDependencies_eitherInvalidatesBinding() {
            var a = new SimpleIntegerProperty(10);
            var b = new SimpleIntegerProperty(20);

            var binding = new IntegerBinding() {
                {
                    bind(a, b);
                }

                @Override
                protected int computeValue() {
                    return a.get() + b.get();
                }
            };

            assertThat(binding.get()).isEqualTo(30);

            a.set(15);
            assertThat(binding.get()).isEqualTo(35);

            b.set(25);
            assertThat(binding.get()).isEqualTo(40);
        }
    }

    @Nested
    @DisplayName("Listeners")
    class Listeners {

        private SimpleIntegerProperty dep;
        private IntegerBinding binding;

        @BeforeEach
        void setUp() {
            dep = new SimpleIntegerProperty(0);
            binding = new IntegerBinding() {
                {
                    bind(dep);
                }

                @Override
                protected int computeValue() {
                    return dep.get();
                }
            };
        }

        @Test
        void invalidationListenerFires_onDependencyChange() {
            binding.get();
            var fired = new AtomicBoolean(false);
            binding.addListener((InvalidationListener) obs -> fired.set(true));

            dep.set(7);

            assertThat(fired.get()).isTrue();
        }

        @Test
        void changeListenerFires_withOldAndNewValues() {
            binding.get();

            var capturedOld = new AtomicReference<Number>();
            var capturedNew = new AtomicReference<Number>();
            binding.addListener((ChangeListener<Number>) (obs, oldVal, newVal) -> {
                capturedOld.set(oldVal);
                capturedNew.set(newVal);
            });

            dep.set(99);
            binding.get();

            assertThat(capturedOld.get().intValue()).isEqualTo(0);
            assertThat(capturedNew.get().intValue()).isEqualTo(99);
        }

        @Test
        void removedInvalidationListenerDoesNotFire_afterRemoval() {
            binding.get();
            var fired = new AtomicBoolean(false);
            InvalidationListener listener = obs -> fired.set(true);
            binding.addListener(listener);
            binding.removeListener(listener);

            dep.set(5);

            assertThat(fired.get()).isFalse();
        }
    }

    @Nested
    @DisplayName("Dispose and Defaults")
    class DisposeAndDefaults {

        @Test
        void disposeIsCallable_noException() {
            var binding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return 0;
                }
            };

            assertThatCode(binding::dispose).doesNotThrowAnyException();
        }

        @Test
        void getDependenciesReturnsEmptyByDefault_emptyList() {
            var binding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return 0;
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
            var binding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return 7;
                }
            };

            assertThat(binding.toString()).contains("invalid");
        }

        @Test
        void validBinding_containsValue() {
            var binding = new IntegerBinding() {
                @Override
                protected int computeValue() {
                    return 42;
                }
            };
            binding.get();

            assertThat(binding.toString()).contains("42");
        }
    }
}

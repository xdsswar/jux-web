package xss.it.jux.html.expression;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

/**
 * Tests for {@link I18nResolver}.
 *
 * <p>Covers simple key lookup, parameterized expressions, parameter
 * type coercion, resolveAll with mixed text, strict vs non-strict
 * mode, and null/empty edge cases.</p>
 */
@DisplayName("I18nResolver")
class I18nResolverTest {

    /**
     * A test message source that recognizes a handful of keys and
     * returns null for anything else (simulating missing translations).
     */
    private final I18nResolver.MessageSource testSource = (key, params) -> switch (key) {
        case "welcome" -> "Welcome!";
        case "greeting" -> "Hello, " + params[0];
        case "count" -> params[0] + " items";
        case "active" -> "Active: " + params[0];
        case "range" -> "From " + params[0] + " to " + params[1];
        case "multi" -> params[0] + "-" + params[1] + "-" + params[2];
        case "copyright" -> "Copyright " + params[0];
        default -> null;
    };

    // ── Simple key resolution ───────────────────────────────────

    @Nested
    @DisplayName("resolve() - simple keys")
    class SimpleKeyTests {

        private I18nResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new I18nResolver(testSource, false);
        }

        @Test
        @DisplayName("simple key resolves to translated message")
        void simpleKey_resolvesToTranslatedMessage() {
            assertThat(resolver.resolve("welcome")).isEqualTo("Welcome!");
        }

        @Test
        @DisplayName("key with surrounding whitespace is trimmed")
        void keyWithWhitespace_isTrimmed() {
            assertThat(resolver.resolve("  welcome  ")).isEqualTo("Welcome!");
        }

        @Test
        @DisplayName("null expression returns empty string")
        void nullExpression_returnsEmptyString() {
            assertThat(resolver.resolve(null)).isEmpty();
        }

        @Test
        @DisplayName("empty expression returns empty string")
        void emptyExpression_returnsEmptyString() {
            assertThat(resolver.resolve("")).isEmpty();
        }
    }

    // ── Parameterized key resolution ────────────────────────────

    @Nested
    @DisplayName("resolve() - keys with parameters")
    class ParameterizedKeyTests {

        private I18nResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new I18nResolver(testSource, false);
        }

        @Test
        @DisplayName("key with unquoted string parameter resolves correctly")
        void keyWithStringParam_resolvesCorrectly() {
            assertThat(resolver.resolve("greeting(World)")).isEqualTo("Hello, World");
        }

        @Test
        @DisplayName("key with integer parameter coerces to Integer")
        void keyWithIntegerParam_coercesToInteger() {
            String result = resolver.resolve("count(5)");
            assertThat(result).isEqualTo("5 items");
        }

        @Test
        @DisplayName("key with boolean parameter coerces to Boolean")
        void keyWithBooleanParam_coercesToBoolean() {
            String result = resolver.resolve("active(true)");
            assertThat(result).isEqualTo("Active: true");
        }

        @Test
        @DisplayName("single-quoted string parameter has quotes stripped")
        void singleQuotedParam_hasQuotesStripped() {
            assertThat(resolver.resolve("greeting('John')")).isEqualTo("Hello, John");
        }

        @Test
        @DisplayName("double-quoted string parameter has quotes stripped")
        void doubleQuotedParam_hasQuotesStripped() {
            assertThat(resolver.resolve("greeting(\"Jane\")")).isEqualTo("Hello, Jane");
        }

        @Test
        @DisplayName("multiple parameters are parsed and passed in order")
        void multipleParams_parsedAndPassedInOrder() {
            assertThat(resolver.resolve("range(1, 10)")).isEqualTo("From 1 to 10");
        }

        @Test
        @DisplayName("three mixed-type parameters are coerced correctly")
        void threeMixedTypeParams_coercedCorrectly() {
            assertThat(resolver.resolve("multi(hello, 42, true)"))
                    .isEqualTo("hello-42-true");
        }

        @Test
        @DisplayName("double value parameter is coerced to Double")
        void doubleParam_coercedToDouble() {
            /* 19.99 cannot be parsed as int, so it becomes Double */
            assertThat(resolver.resolve("copyright(19.99)")).isEqualTo("Copyright 19.99");
        }
    }

    // ── resolveAll ──────────────────────────────────────────────

    @Nested
    @DisplayName("resolveAll()")
    class ResolveAllTests {

        private I18nResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new I18nResolver(testSource, false);
        }

        @Test
        @DisplayName("replaces single #{...} expression in text")
        void singleExpression_replacedInText() {
            String result = resolver.resolveAll("Message: #{welcome}");
            assertThat(result).isEqualTo("Message: Welcome!");
        }

        @Test
        @DisplayName("replaces multiple #{...} expressions in same string")
        void multipleExpressions_allReplaced() {
            String result = resolver.resolveAll("#{welcome} #{greeting(World)}");
            assertThat(result).isEqualTo("Welcome! Hello, World");
        }

        @Test
        @DisplayName("text without expressions returned unchanged")
        void noExpressions_returnedUnchanged() {
            String input = "Just plain text, no expressions here.";
            assertThat(resolver.resolveAll(input)).isEqualTo(input);
        }

        @Test
        @DisplayName("null text returns null")
        void nullText_returnsNull() {
            assertThat(resolver.resolveAll(null)).isNull();
        }

        @Test
        @DisplayName("empty text returns empty string")
        void emptyText_returnsEmptyString() {
            assertThat(resolver.resolveAll("")).isEmpty();
        }

        @Test
        @DisplayName("mixed text and expressions preserves surrounding content")
        void mixedTextAndExpressions_preservesSurroundingContent() {
            String result = resolver.resolveAll("Start #{welcome} middle #{count(3)} end");
            assertThat(result).isEqualTo("Start Welcome! middle 3 items end");
        }

        @Test
        @DisplayName("unresolved key in non-strict mode kept as #{key}")
        void unresolvedKeyNonStrict_keptAsMarker() {
            String result = resolver.resolveAll("Value: #{unknown.key}");
            assertThat(result).isEqualTo("Value: #{unknown.key}");
        }
    }

    // ── Strict mode ─────────────────────────────────────────────

    @Nested
    @DisplayName("strict mode")
    class StrictModeTests {

        private I18nResolver strictResolver;

        @BeforeEach
        void setUp() {
            strictResolver = new I18nResolver(testSource, true);
        }

        @Test
        @DisplayName("unresolved key throws IllegalArgumentException")
        void unresolvedKey_throwsIllegalArgumentException() {
            assertThatThrownBy(() -> strictResolver.resolve("nonexistent"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unresolved i18n key: nonexistent");
        }

        @Test
        @DisplayName("resolved key works normally in strict mode")
        void resolvedKey_worksNormallyInStrictMode() {
            assertThat(strictResolver.resolve("welcome")).isEqualTo("Welcome!");
        }

        @Test
        @DisplayName("unresolved key in resolveAll throws in strict mode")
        void unresolvedKeyInResolveAll_throwsInStrictMode() {
            assertThatThrownBy(() -> strictResolver.resolveAll("#{missing.key}"))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Unresolved i18n key");
        }
    }

    // ── Non-strict mode ─────────────────────────────────────────

    @Nested
    @DisplayName("non-strict mode")
    class NonStrictModeTests {

        private I18nResolver resolver;

        @BeforeEach
        void setUp() {
            resolver = new I18nResolver(testSource, false);
        }

        @Test
        @DisplayName("unresolved key returns #{expression} marker")
        void unresolvedKey_returnsExpressionMarker() {
            assertThat(resolver.resolve("unknown.key")).isEqualTo("#{unknown.key}");
        }

        @Test
        @DisplayName("unresolved parameterized key returns #{expression} marker")
        void unresolvedParameterizedKey_returnsExpressionMarker() {
            assertThat(resolver.resolve("unknown(arg)")).isEqualTo("#{unknown(arg)}");
        }
    }

    // ── Constructor validation ───────────────────────────────────

    @Nested
    @DisplayName("constructor validation")
    class ConstructorTests {

        @Test
        @DisplayName("null messageSource throws NullPointerException")
        void nullMessageSource_throwsNullPointerException() {
            assertThatNullPointerException()
                    .isThrownBy(() -> new I18nResolver(null, false))
                    .withMessage("messageSource must not be null");
        }
    }

    // ── Parameter coercion edge cases ───────────────────────────

    @Nested
    @DisplayName("parameter coercion")
    class ParameterCoercionTests {

        private I18nResolver resolver;

        /**
         * A message source that echoes back parameter types for inspection.
         */
        private final I18nResolver.MessageSource echoSource = (key, params) -> {
            if (!"echo".equals(key)) return null;
            if (params.length == 0) return "no-params";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < params.length; i++) {
                if (i > 0) sb.append("|");
                sb.append(params[i].getClass().getSimpleName())
                  .append(":")
                  .append(params[i]);
            }
            return sb.toString();
        };

        @BeforeEach
        void setUp() {
            resolver = new I18nResolver(echoSource, false);
        }

        @Test
        @DisplayName("integer-like value becomes Integer")
        void integerValue_becomesInteger() {
            assertThat(resolver.resolve("echo(42)")).isEqualTo("Integer:42");
        }

        @Test
        @DisplayName("decimal value becomes Double")
        void decimalValue_becomesDouble() {
            assertThat(resolver.resolve("echo(3.14)")).isEqualTo("Double:3.14");
        }

        @Test
        @DisplayName("true becomes Boolean.TRUE")
        void trueValue_becomesBoolean() {
            assertThat(resolver.resolve("echo(true)")).isEqualTo("Boolean:true");
        }

        @Test
        @DisplayName("false becomes Boolean.FALSE")
        void falseValue_becomesBoolean() {
            assertThat(resolver.resolve("echo(false)")).isEqualTo("Boolean:false");
        }

        @Test
        @DisplayName("case-insensitive TRUE becomes Boolean")
        void uppercaseTrue_becomesBoolean() {
            assertThat(resolver.resolve("echo(TRUE)")).isEqualTo("Boolean:true");
        }

        @Test
        @DisplayName("unquoted non-numeric string stays as String")
        void unquotedString_staysAsString() {
            assertThat(resolver.resolve("echo(hello)")).isEqualTo("String:hello");
        }

        @Test
        @DisplayName("single-quoted value becomes String with quotes removed")
        void singleQuotedValue_becomesStringWithQuotesRemoved() {
            assertThat(resolver.resolve("echo('hello')")).isEqualTo("String:hello");
        }

        @Test
        @DisplayName("double-quoted value becomes String with quotes removed")
        void doubleQuotedValue_becomesStringWithQuotesRemoved() {
            assertThat(resolver.resolve("echo(\"world\")")).isEqualTo("String:world");
        }
    }
}

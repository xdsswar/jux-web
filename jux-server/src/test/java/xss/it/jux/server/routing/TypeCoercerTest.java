package xss.it.jux.server.routing;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link TypeCoercer} -- the utility that converts string values from
 * HTTP requests to strongly-typed Java objects for field injection.
 */
class TypeCoercerTest {

    // ══════════════════════════════════════════════════════════════════
    //  coerce() tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("coerce()")
    class CoerceTests {

        // ── String ──────────────────────────────────────────────────

        @Test
        @DisplayName("String to String returns same value")
        void stringToString() {
            Object result = TypeCoercer.coerce("hello", String.class);
            assertThat(result).isEqualTo("hello");
        }

        @Test
        @DisplayName("empty string to String returns empty string")
        void emptyStringToString() {
            Object result = TypeCoercer.coerce("", String.class);
            assertThat(result).isEqualTo("");
        }

        // ── int/Integer ─────────────────────────────────────────────

        @Test
        @DisplayName("String to int.class returns integer")
        void stringToIntPrimitive() {
            Object result = TypeCoercer.coerce("42", int.class);
            assertThat(result).isEqualTo(42);
        }

        @Test
        @DisplayName("String to Integer.class returns Integer")
        void stringToIntegerBoxed() {
            Object result = TypeCoercer.coerce("42", Integer.class);
            assertThat(result).isEqualTo(42);
        }

        // ── long/Long ───────────────────────────────────────────────

        @Test
        @DisplayName("String to long.class returns long")
        void stringToLongPrimitive() {
            Object result = TypeCoercer.coerce("42", long.class);
            assertThat(result).isEqualTo(42L);
        }

        @Test
        @DisplayName("String to Long.class returns Long")
        void stringToLongBoxed() {
            Object result = TypeCoercer.coerce("42", Long.class);
            assertThat(result).isEqualTo(42L);
        }

        // ── boolean/Boolean ─────────────────────────────────────────

        @Test
        @DisplayName("String 'true' to boolean.class returns true")
        void stringTrueToBooleanPrimitive() {
            Object result = TypeCoercer.coerce("true", boolean.class);
            assertThat(result).isEqualTo(true);
        }

        @Test
        @DisplayName("String 'false' to Boolean.class returns false")
        void stringFalseToBooleanBoxed() {
            Object result = TypeCoercer.coerce("false", Boolean.class);
            assertThat(result).isEqualTo(false);
        }

        // ── double/Double ───────────────────────────────────────────

        @Test
        @DisplayName("String to double.class returns double")
        void stringToDoublePrimitive() {
            Object result = TypeCoercer.coerce("3.14", double.class);
            assertThat(result).isEqualTo(3.14);
        }

        @Test
        @DisplayName("String to Double.class returns Double")
        void stringToDoubleBoxed() {
            Object result = TypeCoercer.coerce("3.14", Double.class);
            assertThat(result).isEqualTo(3.14);
        }

        // ── float/Float ─────────────────────────────────────────────

        @Test
        @DisplayName("String to float.class returns float")
        void stringToFloatPrimitive() {
            Object result = TypeCoercer.coerce("2.5", float.class);
            assertThat(result).isEqualTo(2.5f);
        }

        @Test
        @DisplayName("String to Float.class returns Float")
        void stringToFloatBoxed() {
            Object result = TypeCoercer.coerce("2.5", Float.class);
            assertThat(result).isEqualTo(2.5f);
        }

        // ── UUID ────────────────────────────────────────────────────

        @Test
        @DisplayName("valid UUID string coerces to UUID")
        void stringToUuid() {
            String uuidStr = "550e8400-e29b-41d4-a716-446655440000";
            Object result = TypeCoercer.coerce(uuidStr, UUID.class);
            assertThat(result).isEqualTo(UUID.fromString(uuidStr));
        }

        // ── LocalDate ───────────────────────────────────────────────

        @Test
        @DisplayName("ISO date string coerces to LocalDate")
        void stringToLocalDate() {
            Object result = TypeCoercer.coerce("2026-02-06", LocalDate.class);
            assertThat(result).isEqualTo(LocalDate.of(2026, 2, 6));
        }

        // ── Enum ────────────────────────────────────────────────────

        @Test
        @DisplayName("String to enum constant")
        void stringToEnum() {
            Object result = TypeCoercer.coerce("PENDING", TestStatus.class);
            assertThat(result).isEqualTo(TestStatus.PENDING);
        }

        // ── short/Short ─────────────────────────────────────────────

        @Test
        @DisplayName("String to short.class returns short")
        void stringToShortPrimitive() {
            Object result = TypeCoercer.coerce("100", short.class);
            assertThat(result).isEqualTo((short) 100);
        }

        // ── byte/Byte ───────────────────────────────────────────────

        @Test
        @DisplayName("String to byte.class returns byte")
        void stringToBytePrimitive() {
            Object result = TypeCoercer.coerce("7", byte.class);
            assertThat(result).isEqualTo((byte) 7);
        }

        // ── null handling ───────────────────────────────────────────

        @Test
        @DisplayName("null value returns null for any type")
        void nullReturnsNull() {
            assertThat(TypeCoercer.coerce(null, String.class)).isNull();
            assertThat(TypeCoercer.coerce(null, int.class)).isNull();
            assertThat(TypeCoercer.coerce(null, UUID.class)).isNull();
        }

        // ── Error cases ─────────────────────────────────────────────

        @Test
        @DisplayName("non-numeric string to int throws exception")
        void nonNumericStringToIntThrows() {
            assertThatThrownBy(() -> TypeCoercer.coerce("not-a-number", int.class))
                .isInstanceOf(NumberFormatException.class);
        }

        @Test
        @DisplayName("invalid UUID string throws exception")
        void invalidUuidStringThrows() {
            assertThatThrownBy(() -> TypeCoercer.coerce("not-a-uuid", UUID.class))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("invalid date string throws exception")
        void invalidDateStringThrows() {
            assertThatThrownBy(() -> TypeCoercer.coerce("not-a-date", LocalDate.class))
                .isInstanceOf(java.time.format.DateTimeParseException.class);
        }

        @Test
        @DisplayName("invalid enum constant throws exception")
        void invalidEnumConstantThrows() {
            assertThatThrownBy(() -> TypeCoercer.coerce("INVALID", TestStatus.class))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("unsupported type throws IllegalArgumentException")
        void unsupportedTypeThrows() {
            assertThatThrownBy(() -> TypeCoercer.coerce("test", java.net.URI.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Cannot coerce");
        }

        // ── Negative numbers ────────────────────────────────────────

        @Test
        @DisplayName("negative integer coerces correctly")
        void negativeIntegerCoerces() {
            Object result = TypeCoercer.coerce("-42", int.class);
            assertThat(result).isEqualTo(-42);
        }

        @Test
        @DisplayName("negative double coerces correctly")
        void negativeDoubleCoerces() {
            Object result = TypeCoercer.coerce("-3.14", double.class);
            assertThat(result).isEqualTo(-3.14);
        }

        // ── Large numbers ───────────────────────────────────────────

        @Test
        @DisplayName("large long coerces correctly")
        void largeLongCoerces() {
            Object result = TypeCoercer.coerce("9223372036854775807", long.class);
            assertThat(result).isEqualTo(Long.MAX_VALUE);
        }
    }

    // ══════════════════════════════════════════════════════════════════
    //  defaultValue() tests
    // ══════════════════════════════════════════════════════════════════

    @Nested
    @DisplayName("defaultValue()")
    class DefaultValueTests {

        @Test
        @DisplayName("int.class defaults to 0")
        void intDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(int.class)).isEqualTo(0);
        }

        @Test
        @DisplayName("long.class defaults to 0L")
        void longDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(long.class)).isEqualTo(0L);
        }

        @Test
        @DisplayName("double.class defaults to 0.0")
        void doubleDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(double.class)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("float.class defaults to 0.0f")
        void floatDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(float.class)).isEqualTo(0.0f);
        }

        @Test
        @DisplayName("boolean.class defaults to false")
        void booleanDefaultsToFalse() {
            assertThat(TypeCoercer.defaultValue(boolean.class)).isEqualTo(false);
        }

        @Test
        @DisplayName("short.class defaults to (short) 0")
        void shortDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(short.class)).isEqualTo((short) 0);
        }

        @Test
        @DisplayName("byte.class defaults to (byte) 0")
        void byteDefaultsToZero() {
            assertThat(TypeCoercer.defaultValue(byte.class)).isEqualTo((byte) 0);
        }

        @Test
        @DisplayName("char.class defaults to null char")
        void charDefaultsToNullChar() {
            assertThat(TypeCoercer.defaultValue(char.class)).isEqualTo('\0');
        }

        @Test
        @DisplayName("String.class defaults to null")
        void stringDefaultsToNull() {
            assertThat(TypeCoercer.defaultValue(String.class)).isNull();
        }

        @Test
        @DisplayName("UUID.class defaults to null")
        void uuidDefaultsToNull() {
            assertThat(TypeCoercer.defaultValue(UUID.class)).isNull();
        }

        @Test
        @DisplayName("Integer.class defaults to null (reference type)")
        void integerBoxedDefaultsToNull() {
            assertThat(TypeCoercer.defaultValue(Integer.class)).isNull();
        }
    }

    // ── Test Enum ───────────────────────────────────────────────────

    enum TestStatus {
        PENDING, ACTIVE, CLOSED
    }
}

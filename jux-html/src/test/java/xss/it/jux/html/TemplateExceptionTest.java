package xss.it.jux.html;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TemplateException}.
 *
 * <p>Validates message formatting, field accessors, cause chaining,
 * and edge-case handling for template path and line number values.</p>
 */
@DisplayName("TemplateException")
class TemplateExceptionTest {

    // ── Message Formatting ──────────────────────────────────────

    @Nested
    @DisplayName("getMessage()")
    class GetMessageTests {

        @Test
        @DisplayName("path and positive line number produce 'path:line - message'")
        void pathAndPositiveLineNumber_formatsAsPathColonLineMessage() {
            var ex = new TemplateException("Unexpected tag", "test.html", 42);

            assertThat(ex.getMessage()).isEqualTo("test.html:42 - Unexpected tag");
        }

        @Test
        @DisplayName("null path shows '<unknown>' in message")
        void nullPath_showsUnknownPlaceholder() {
            var ex = new TemplateException("Parse error", null, 10);

            assertThat(ex.getMessage()).isEqualTo("<unknown>:10 - Parse error");
        }

        @Test
        @DisplayName("negative line number omits the colon and line from message")
        void negativeLineNumber_omitsLineNumberFromMessage() {
            var ex = new TemplateException("Error occurred", "pages/home.html", -1);

            assertThat(ex.getMessage()).isEqualTo("pages/home.html - Error occurred");
        }

        @Test
        @DisplayName("null path and negative line number produce '<unknown> - message'")
        void nullPathAndNegativeLineNumber_showsUnknownWithoutLine() {
            var ex = new TemplateException("Something broke", null, -1);

            assertThat(ex.getMessage()).isEqualTo("<unknown> - Something broke");
        }

        @Test
        @DisplayName("line number zero is included in message")
        void zeroLineNumber_includedInMessage() {
            var ex = new TemplateException("Bad header", "layout.html", 0);

            assertThat(ex.getMessage()).isEqualTo("layout.html:0 - Bad header");
        }

        @Test
        @DisplayName("deeply nested path renders correctly")
        void deeplyNestedPath_rendersCorrectly() {
            var ex = new TemplateException("Missing closing tag",
                    "templates/pages/admin/dashboard.html", 256);

            assertThat(ex.getMessage())
                    .isEqualTo("templates/pages/admin/dashboard.html:256 - Missing closing tag");
        }
    }

    // ── Field Accessors ─────────────────────────────────────────

    @Nested
    @DisplayName("field accessors")
    class FieldAccessorTests {

        @Test
        @DisplayName("getTemplatePath returns the stored path")
        void getTemplatePath_returnsStoredPath() {
            var ex = new TemplateException("error", "pages/index.html", 1);

            assertThat(ex.getTemplatePath()).isEqualTo("pages/index.html");
        }

        @Test
        @DisplayName("getTemplatePath returns null when constructed with null path")
        void getTemplatePath_returnsNullWhenConstructedWithNull() {
            var ex = new TemplateException("error", null, 5);

            assertThat(ex.getTemplatePath()).isNull();
        }

        @Test
        @DisplayName("getLineNumber returns the stored line number")
        void getLineNumber_returnsStoredLineNumber() {
            var ex = new TemplateException("error", "x.html", 99);

            assertThat(ex.getLineNumber()).isEqualTo(99);
        }

        @Test
        @DisplayName("getLineNumber returns -1 when constructed with -1")
        void getLineNumber_returnsNegativeOneWhenUnknown() {
            var ex = new TemplateException("error", "x.html", -1);

            assertThat(ex.getLineNumber()).isEqualTo(-1);
        }
    }

    // ── Cause Chaining ──────────────────────────────────────────

    @Nested
    @DisplayName("cause chaining")
    class CauseTests {

        @Test
        @DisplayName("constructor with cause stores and exposes the cause")
        void constructorWithCause_storesAndExposesCause() {
            var root = new RuntimeException("root cause");
            var ex = new TemplateException("Wrapper", "t.html", 7, root);

            assertThat(ex.getCause()).isSameAs(root);
        }

        @Test
        @DisplayName("constructor without cause results in null getCause()")
        void constructorWithoutCause_hasNullCause() {
            var ex = new TemplateException("No cause", "t.html", 3);

            assertThat(ex.getCause()).isNull();
        }

        @Test
        @DisplayName("getMessage formats correctly even with a cause present")
        void messageFormatsCorrectly_whenCausePresent() {
            var root = new IllegalStateException("bad state");
            var ex = new TemplateException("Processing failed", "app.html", 15, root);

            assertThat(ex.getMessage()).isEqualTo("app.html:15 - Processing failed");
            assertThat(ex.getCause()).isSameAs(root);
        }
    }

    // ── Inheritance ─────────────────────────────────────────────

    @Nested
    @DisplayName("inheritance")
    class InheritanceTests {

        @Test
        @DisplayName("is a RuntimeException")
        void isRuntimeException() {
            var ex = new TemplateException("test", "a.html", 1);

            assertThat(ex).isInstanceOf(RuntimeException.class);
        }
    }
}

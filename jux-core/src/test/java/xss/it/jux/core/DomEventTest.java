package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link DomEvent} class.
 *
 * <p>Covers the full constructor, the {@code simple()} factory, all getters,
 * and the {@code preventDefault()} / {@code stopPropagation()} mechanism.</p>
 */
class DomEventTest {

    // ── Full Constructor ──────────────────────────────────────────────

    @Nested
    @DisplayName("Full constructor")
    class FullConstructor {

        @Test
        @DisplayName("stores all fields correctly")
        void allFieldsStored() {
            DomEvent event = new DomEvent(
                    "keydown", "input-1", "hello", "Enter",
                    true, true, false, true,
                    150.5, 200.75
            );

            assertThat(event.getType()).isEqualTo("keydown");
            assertThat(event.getTargetId()).isEqualTo("input-1");
            assertThat(event.getValue()).isEqualTo("hello");
            assertThat(event.getKey()).isEqualTo("Enter");
            assertThat(event.isShiftKey()).isTrue();
            assertThat(event.isCtrlKey()).isTrue();
            assertThat(event.isAltKey()).isFalse();
            assertThat(event.isMetaKey()).isTrue();
            assertThat(event.getClientX()).isEqualTo(150.5);
            assertThat(event.getClientY()).isEqualTo(200.75);
        }

        @Test
        @DisplayName("preventDefault and propagationStopped are false by default")
        void defaultFlags() {
            DomEvent event = new DomEvent("click", "", "", "",
                    false, false, false, false, 0, 0);
            assertThat(event.isDefaultPrevented()).isFalse();
            assertThat(event.isPropagationStopped()).isFalse();
        }
    }

    // ── simple() Factory ──────────────────────────────────────────────

    @Nested
    @DisplayName("simple() factory")
    class SimpleFactory {

        @Test
        @DisplayName("sets type and value, defaults everything else")
        void simpleDefaults() {
            DomEvent event = DomEvent.simple("click", "hello");

            assertThat(event.getType()).isEqualTo("click");
            assertThat(event.getTargetId()).isEmpty();
            assertThat(event.getValue()).isEqualTo("hello");
            assertThat(event.getKey()).isEmpty();
            assertThat(event.isShiftKey()).isFalse();
            assertThat(event.isCtrlKey()).isFalse();
            assertThat(event.isAltKey()).isFalse();
            assertThat(event.isMetaKey()).isFalse();
            assertThat(event.getClientX()).isEqualTo(0.0);
            assertThat(event.getClientY()).isEqualTo(0.0);
        }

        @Test
        @DisplayName("simple() event has no default prevention or propagation stop")
        void simpleFlags() {
            DomEvent event = DomEvent.simple("input", "text");
            assertThat(event.isDefaultPrevented()).isFalse();
            assertThat(event.isPropagationStopped()).isFalse();
        }
    }

    // ── preventDefault() ──────────────────────────────────────────────

    @Nested
    @DisplayName("preventDefault()")
    class PreventDefault {

        @Test
        @DisplayName("sets isDefaultPrevented() to true")
        void preventDefaultSetsFlag() {
            DomEvent event = DomEvent.simple("submit", "");
            event.preventDefault();
            assertThat(event.isDefaultPrevented()).isTrue();
        }

        @Test
        @DisplayName("multiple calls are idempotent")
        void preventDefaultIdempotent() {
            DomEvent event = DomEvent.simple("submit", "");
            event.preventDefault();
            event.preventDefault();
            assertThat(event.isDefaultPrevented()).isTrue();
        }
    }

    // ── stopPropagation() ─────────────────────────────────────────────

    @Nested
    @DisplayName("stopPropagation()")
    class StopPropagation {

        @Test
        @DisplayName("sets isPropagationStopped() to true")
        void stopPropagationSetsFlag() {
            DomEvent event = DomEvent.simple("click", "");
            event.stopPropagation();
            assertThat(event.isPropagationStopped()).isTrue();
        }

        @Test
        @DisplayName("multiple calls are idempotent")
        void stopPropagationIdempotent() {
            DomEvent event = DomEvent.simple("click", "");
            event.stopPropagation();
            event.stopPropagation();
            assertThat(event.isPropagationStopped()).isTrue();
        }
    }

    // ── preventDefault and stopPropagation are independent ────────────

    @Test
    @DisplayName("preventDefault() and stopPropagation() are independent")
    void independentFlags() {
        DomEvent event = DomEvent.simple("click", "");
        event.preventDefault();
        assertThat(event.isDefaultPrevented()).isTrue();
        assertThat(event.isPropagationStopped()).isFalse();

        event.stopPropagation();
        assertThat(event.isDefaultPrevented()).isTrue();
        assertThat(event.isPropagationStopped()).isTrue();
    }

    // ── All Getters ───────────────────────────────────────────────────

    @Test
    @DisplayName("all getters return the constructed values for a mouse event")
    void mouseEventGetters() {
        DomEvent event = new DomEvent(
                "click", "btn-submit", "", "",
                false, false, true, false,
                320.0, 480.0
        );

        assertThat(event.getType()).isEqualTo("click");
        assertThat(event.getTargetId()).isEqualTo("btn-submit");
        assertThat(event.getValue()).isEmpty();
        assertThat(event.getKey()).isEmpty();
        assertThat(event.isShiftKey()).isFalse();
        assertThat(event.isCtrlKey()).isFalse();
        assertThat(event.isAltKey()).isTrue();
        assertThat(event.isMetaKey()).isFalse();
        assertThat(event.getClientX()).isEqualTo(320.0);
        assertThat(event.getClientY()).isEqualTo(480.0);
    }
}

package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.JsPosition;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link JsResource} record.
 *
 * <p>Covers the canonical constructor, convenience constructors with
 * default values, and record equality semantics.</p>
 */
class JsResourceTest {

    // ── Canonical Constructor ─────────────────────────────────────────

    @Nested
    @DisplayName("Canonical constructor")
    class Canonical {

        @Test
        @DisplayName("creates resource with all fields set")
        void allFieldsSet() {
            JsResource res = new JsResource(
                    "app.js", JsPosition.HEAD, 50,
                    true, false, true, "sha384-abc", "#{env == 'prod'}"
            );
            assertThat(res.path()).isEqualTo("app.js");
            assertThat(res.position()).isEqualTo(JsPosition.HEAD);
            assertThat(res.order()).isEqualTo(50);
            assertThat(res.async()).isTrue();
            assertThat(res.defer()).isFalse();
            assertThat(res.module()).isTrue();
            assertThat(res.integrity()).isEqualTo("sha384-abc");
            assertThat(res.condition()).isEqualTo("#{env == 'prod'}");
        }
    }

    // ── Convenience: JsResource(path) ─────────────────────────────────

    @Nested
    @DisplayName("JsResource(path) convenience constructor")
    class PathOnly {

        @Test
        @DisplayName("sets BODY_END position, order 100, defer=true, and other defaults")
        void defaults() {
            JsResource res = new JsResource("app.js");
            assertThat(res.path()).isEqualTo("app.js");
            assertThat(res.position()).isEqualTo(JsPosition.BODY_END);
            assertThat(res.order()).isEqualTo(100);
            assertThat(res.async()).isFalse();
            assertThat(res.defer()).isTrue();
            assertThat(res.module()).isFalse();
            assertThat(res.integrity()).isEmpty();
            assertThat(res.condition()).isEmpty();
        }
    }

    // ── Convenience: JsResource(path, order) ──────────────────────────

    @Nested
    @DisplayName("JsResource(path, order) convenience constructor")
    class PathAndOrder {

        @Test
        @DisplayName("sets custom order with BODY_END position and other defaults")
        void customOrder() {
            JsResource res = new JsResource("app.js", 10);
            assertThat(res.path()).isEqualTo("app.js");
            assertThat(res.position()).isEqualTo(JsPosition.BODY_END);
            assertThat(res.order()).isEqualTo(10);
            assertThat(res.async()).isFalse();
            assertThat(res.defer()).isTrue();
            assertThat(res.module()).isFalse();
            assertThat(res.integrity()).isEmpty();
            assertThat(res.condition()).isEmpty();
        }
    }

    // ── Convenience: JsResource(path, order, position) ────────────────

    @Nested
    @DisplayName("JsResource(path, order, position) convenience constructor")
    class PathOrderPosition {

        @Test
        @DisplayName("sets custom order and position with other defaults")
        void customOrderAndPosition() {
            JsResource res = new JsResource("app.js", 5, JsPosition.HEAD);
            assertThat(res.path()).isEqualTo("app.js");
            assertThat(res.position()).isEqualTo(JsPosition.HEAD);
            assertThat(res.order()).isEqualTo(5);
            assertThat(res.async()).isFalse();
            assertThat(res.defer()).isTrue();
            assertThat(res.module()).isFalse();
            assertThat(res.integrity()).isEmpty();
            assertThat(res.condition()).isEmpty();
        }
    }

    // ── Record Equality ───────────────────────────────────────────────

    @Nested
    @DisplayName("Record equality and hashCode")
    class Equality {

        @Test
        @DisplayName("equal records have equal hashCodes")
        void equalRecordsEqualHashCodes() {
            JsResource a = new JsResource("app.js");
            JsResource b = new JsResource("app.js");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different records are not equal")
        void differentRecordsNotEqual() {
            JsResource a = new JsResource("a.js");
            JsResource b = new JsResource("b.js");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("same path but different order are not equal")
        void samePathDifferentOrder() {
            JsResource a = new JsResource("app.js", 1);
            JsResource b = new JsResource("app.js", 2);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("full canonical constructor records with identical fields are equal")
        void fullCanonicalEquality() {
            JsResource a = new JsResource("x.js", JsPosition.BODY_END, 100, false, true, false, "", "");
            JsResource b = new JsResource("x.js");
            assertThat(a).isEqualTo(b);
        }
    }
}

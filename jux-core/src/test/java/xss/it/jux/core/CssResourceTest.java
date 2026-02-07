package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.annotation.CssPosition;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link CssResource} record.
 *
 * <p>Covers the canonical constructor, convenience constructors with
 * default values, and record equality semantics.</p>
 */
class CssResourceTest {

    // ── Canonical Constructor ─────────────────────────────────────────

    @Nested
    @DisplayName("Canonical constructor")
    class Canonical {

        @Test
        @DisplayName("creates resource with all fields set")
        void allFieldsSet() {
            CssResource res = new CssResource(
                    "themes/default.css", CssPosition.BODY_END, 50,
                    "print", true, "sha384-abc", "#{profile == 'prod'}"
            );
            assertThat(res.path()).isEqualTo("themes/default.css");
            assertThat(res.position()).isEqualTo(CssPosition.BODY_END);
            assertThat(res.order()).isEqualTo(50);
            assertThat(res.media()).isEqualTo("print");
            assertThat(res.async()).isTrue();
            assertThat(res.integrity()).isEqualTo("sha384-abc");
            assertThat(res.condition()).isEqualTo("#{profile == 'prod'}");
        }
    }

    // ── Convenience: CssResource(path) ────────────────────────────────

    @Nested
    @DisplayName("CssResource(path) convenience constructor")
    class PathOnly {

        @Test
        @DisplayName("sets HEAD position and order 100 with empty defaults")
        void defaults() {
            CssResource res = new CssResource("main.css");
            assertThat(res.path()).isEqualTo("main.css");
            assertThat(res.position()).isEqualTo(CssPosition.HEAD);
            assertThat(res.order()).isEqualTo(100);
            assertThat(res.media()).isEmpty();
            assertThat(res.async()).isFalse();
            assertThat(res.integrity()).isEmpty();
            assertThat(res.condition()).isEmpty();
        }
    }

    // ── Convenience: CssResource(path, order) ─────────────────────────

    @Nested
    @DisplayName("CssResource(path, order) convenience constructor")
    class PathAndOrder {

        @Test
        @DisplayName("sets custom order with HEAD position and other defaults")
        void customOrder() {
            CssResource res = new CssResource("main.css", 10);
            assertThat(res.path()).isEqualTo("main.css");
            assertThat(res.position()).isEqualTo(CssPosition.HEAD);
            assertThat(res.order()).isEqualTo(10);
            assertThat(res.media()).isEmpty();
            assertThat(res.async()).isFalse();
            assertThat(res.integrity()).isEmpty();
            assertThat(res.condition()).isEmpty();
        }
    }

    // ── Convenience: CssResource(path, order, position) ───────────────

    @Nested
    @DisplayName("CssResource(path, order, position) convenience constructor")
    class PathOrderPosition {

        @Test
        @DisplayName("sets custom order and position with other defaults")
        void customOrderAndPosition() {
            CssResource res = new CssResource("main.css", 5, CssPosition.BODY_END);
            assertThat(res.path()).isEqualTo("main.css");
            assertThat(res.position()).isEqualTo(CssPosition.BODY_END);
            assertThat(res.order()).isEqualTo(5);
            assertThat(res.media()).isEmpty();
            assertThat(res.async()).isFalse();
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
            CssResource a = new CssResource("main.css");
            CssResource b = new CssResource("main.css");
            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("different records are not equal")
        void differentRecordsNotEqual() {
            CssResource a = new CssResource("a.css");
            CssResource b = new CssResource("b.css");
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("same path but different order are not equal")
        void samePathDifferentOrder() {
            CssResource a = new CssResource("main.css", 1);
            CssResource b = new CssResource("main.css", 2);
            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("full canonical constructor records with identical fields are equal")
        void fullCanonicalEquality() {
            CssResource a = new CssResource("x.css", CssPosition.HEAD, 100, "", false, "", "");
            CssResource b = new CssResource("x.css");
            assertThat(a).isEqualTo(b);
        }
    }
}

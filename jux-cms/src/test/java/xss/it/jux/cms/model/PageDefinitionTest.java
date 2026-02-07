package xss.it.jux.cms.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link PageDefinition} record -- the top-level model
 * for CMS-managed pages stored in the database.
 *
 * <p>Covers record construction, accessor methods, the locale-aware
 * content fallback method, and record equality semantics.</p>
 */
class PageDefinitionTest {

    /** Reusable locale constants for cleaner test code. */
    private static final Locale ES = Locale.of("es");
    private static final Locale DE = Locale.of("de");

    /** Reusable English content for tests. */
    private static final LocalizedContent EN_CONTENT = new LocalizedContent(
            "About Us",
            "Learn about our company",
            "https://example.com/og.jpg",
            "Company team photo",
            List.of(),
            Map.of("keywords", "company, about")
    );

    /** Reusable Spanish content for tests. */
    private static final LocalizedContent ES_CONTENT = new LocalizedContent(
            "Sobre Nosotros",
            "Conozca nuestra empresa",
            "https://example.com/og-es.jpg",
            "Foto del equipo",
            List.of(),
            Map.of("keywords", "empresa, sobre")
    );

    /** Reusable French content for tests. */
    private static final LocalizedContent FR_CONTENT = new LocalizedContent(
            "A Propos",
            "Decouvrez notre entreprise",
            null,
            null,
            List.of(),
            Map.of()
    );

    // ── Record construction and accessors ────────────────────────────

    @Nested
    @DisplayName("Record construction and accessors")
    class Construction {

        @Test
        @DisplayName("all accessors return the values passed to the constructor")
        void accessorsReturnConstructorValues() {
            var css = List.of(new ResourceRef("pages/about.css", 50, "HEAD"));
            var js = List.of(new ResourceRef("pages/about.js", 100, "BODY_END"));
            var now = Instant.now();
            var content = Map.of(Locale.ENGLISH, EN_CONTENT);

            var page = new PageDefinition(
                    "about", "DefaultLayout", content, css, js, true, now);

            assertThat(page.slug()).isEqualTo("about");
            assertThat(page.layout()).isEqualTo("DefaultLayout");
            assertThat(page.content()).isEqualTo(content);
            assertThat(page.css()).isEqualTo(css);
            assertThat(page.js()).isEqualTo(js);
            assertThat(page.published()).isTrue();
            assertThat(page.updatedAt()).isEqualTo(now);
        }

        @Test
        @DisplayName("published() returns false for draft pages")
        void publishedReturnsFalseForDraft() {
            var page = new PageDefinition(
                    "draft-page", "DefaultLayout", Map.of(Locale.ENGLISH, EN_CONTENT),
                    List.of(), List.of(), false, Instant.now());

            assertThat(page.published()).isFalse();
        }

        @Test
        @DisplayName("slug supports nested paths with slashes")
        void slugSupportsNestedPaths() {
            var page = new PageDefinition(
                    "services/web-design", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            assertThat(page.slug()).isEqualTo("services/web-design");
        }

        @Test
        @DisplayName("css and js lists can be empty")
        void emptyResourceLists() {
            var page = new PageDefinition(
                    "minimal", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            assertThat(page.css()).isEmpty();
            assertThat(page.js()).isEmpty();
        }
    }

    // ── content(Locale, Locale) fallback ─────────────────────────────

    @Nested
    @DisplayName("content(Locale, Locale) fallback resolution")
    class ContentFallback {

        @Test
        @DisplayName("returns exact locale content when present")
        void returnsExactLocaleContent() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT, Locale.FRENCH, FR_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            var result = page.content(Locale.FRENCH, Locale.ENGLISH);

            assertThat(result).isSameAs(FR_CONTENT);
            assertThat(result.title()).isEqualTo("A Propos");
        }

        @Test
        @DisplayName("falls back to default locale when requested locale is missing")
        void fallsBackToDefaultLocale() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            var result = page.content(Locale.FRENCH, Locale.ENGLISH);

            assertThat(result).isSameAs(EN_CONTENT);
            assertThat(result.title()).isEqualTo("About Us");
        }

        @Test
        @DisplayName("returns English content when English is both requested and fallback")
        void returnsSameLocaleWhenRequestedEqualsFallback() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT, ES, ES_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            var result = page.content(Locale.ENGLISH, Locale.ENGLISH);

            assertThat(result).isSameAs(EN_CONTENT);
        }

        @Test
        @DisplayName("returns Spanish content directly when present")
        void returnsSpanishContentWhenPresent() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(Locale.ENGLISH, EN_CONTENT, ES, ES_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            var result = page.content(ES, Locale.ENGLISH);

            assertThat(result).isSameAs(ES_CONTENT);
            assertThat(result.title()).isEqualTo("Sobre Nosotros");
        }

        @Test
        @DisplayName("returns null when neither requested nor fallback locale has content")
        void returnsNullWhenNoContentExists() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(DE, FR_CONTENT),
                    List.of(), List.of(), true, Instant.now());

            var result = page.content(Locale.FRENCH, Locale.ENGLISH);

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("multi-locale page with three languages resolves each correctly")
        void multiLocaleResolution() {
            var page = new PageDefinition(
                    "about", "DefaultLayout",
                    Map.of(
                            Locale.ENGLISH, EN_CONTENT,
                            ES, ES_CONTENT,
                            Locale.FRENCH, FR_CONTENT
                    ),
                    List.of(), List.of(), true, Instant.now());

            assertThat(page.content(Locale.ENGLISH, Locale.ENGLISH).title())
                    .isEqualTo("About Us");
            assertThat(page.content(ES, Locale.ENGLISH).title())
                    .isEqualTo("Sobre Nosotros");
            assertThat(page.content(Locale.FRENCH, Locale.ENGLISH).title())
                    .isEqualTo("A Propos");
        }
    }

    // ── Record equality ──────────────────────────────────────────────

    @Nested
    @DisplayName("Record equality")
    class Equality {

        @Test
        @DisplayName("two identical PageDefinitions are equal")
        void identicalRecordsAreEqual() {
            var now = Instant.parse("2026-02-06T12:00:00Z");
            var content = Map.of(Locale.ENGLISH, EN_CONTENT);
            var css = List.of(new ResourceRef("style.css", 50, "HEAD"));
            var js = List.<ResourceRef>of();

            var a = new PageDefinition("about", "DefaultLayout", content, css, js, true, now);
            var b = new PageDefinition("about", "DefaultLayout", content, css, js, true, now);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("PageDefinitions with different slugs are not equal")
        void differentSlugsNotEqual() {
            var now = Instant.now();
            var content = Map.of(Locale.ENGLISH, EN_CONTENT);

            var a = new PageDefinition("about", "DefaultLayout", content,
                    List.of(), List.of(), true, now);
            var b = new PageDefinition("contact", "DefaultLayout", content,
                    List.of(), List.of(), true, now);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("PageDefinitions with different published status are not equal")
        void differentPublishedNotEqual() {
            var now = Instant.now();
            var content = Map.of(Locale.ENGLISH, EN_CONTENT);

            var a = new PageDefinition("about", "DefaultLayout", content,
                    List.of(), List.of(), true, now);
            var b = new PageDefinition("about", "DefaultLayout", content,
                    List.of(), List.of(), false, now);

            assertThat(a).isNotEqualTo(b);
        }
    }
}

package xss.it.jux.cms.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link StyleConfig}, {@link BackgroundConfig},
 * {@link BackgroundType}, and {@link A11yConfig} records.
 *
 * <p>Covers record construction, accessor methods, enum values,
 * and record equality semantics for all style-related model types.</p>
 */
class StyleConfigTest {

    // ── StyleConfig ──────────────────────────────────────────────────

    @Nested
    @DisplayName("StyleConfig")
    class StyleConfigTests {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var bg = new BackgroundConfig(
                    BackgroundType.GRADIENT, null,
                    "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                    null, null, null, null, false, null);

            var style = new StyleConfig(
                    bg, 0.85, 8, "16px", "4rem 2rem", "2rem auto",
                    "100vh", "0 4px 24px rgba(0,0,0,0.12)", "slide-up",
                    "multiply", "1200px", "center");

            assertThat(style.background()).isSameAs(bg);
            assertThat(style.opacity()).isEqualTo(0.85);
            assertThat(style.blur()).isEqualTo(8);
            assertThat(style.borderRadius()).isEqualTo("16px");
            assertThat(style.padding()).isEqualTo("4rem 2rem");
            assertThat(style.margin()).isEqualTo("2rem auto");
            assertThat(style.minHeight()).isEqualTo("100vh");
            assertThat(style.shadow()).isEqualTo("0 4px 24px rgba(0,0,0,0.12)");
            assertThat(style.animation()).isEqualTo("slide-up");
            assertThat(style.blendMode()).isEqualTo("multiply");
            assertThat(style.maxWidth()).isEqualTo("1200px");
            assertThat(style.textAlign()).isEqualTo("center");
        }

        @Test
        @DisplayName("null fields are allowed for optional properties")
        void nullFieldsAllowed() {
            var style = new StyleConfig(
                    null, 1.0, 0, null, null, null,
                    null, null, null, null, null, null);

            assertThat(style.background()).isNull();
            assertThat(style.opacity()).isEqualTo(1.0);
            assertThat(style.blur()).isZero();
            assertThat(style.borderRadius()).isNull();
            assertThat(style.padding()).isNull();
            assertThat(style.margin()).isNull();
            assertThat(style.minHeight()).isNull();
            assertThat(style.shadow()).isNull();
            assertThat(style.animation()).isNull();
            assertThat(style.blendMode()).isNull();
            assertThat(style.maxWidth()).isNull();
            assertThat(style.textAlign()).isNull();
        }

        @Test
        @DisplayName("identical StyleConfigs are equal")
        void identicalStyleConfigsAreEqual() {
            var a = new StyleConfig(null, 1.0, 0, "8px", "2rem", null,
                    null, null, "fade-in", null, null, "center");
            var b = new StyleConfig(null, 1.0, 0, "8px", "2rem", null,
                    null, null, "fade-in", null, null, "center");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("StyleConfigs with different opacity are not equal")
        void differentOpacityNotEqual() {
            var a = new StyleConfig(null, 1.0, 0, null, null, null,
                    null, null, null, null, null, null);
            var b = new StyleConfig(null, 0.5, 0, null, null, null,
                    null, null, null, null, null, null);

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ── BackgroundType ───────────────────────────────────────────────

    @Nested
    @DisplayName("BackgroundType")
    class BackgroundTypeTests {

        @Test
        @DisplayName("has exactly 4 values: SOLID, GRADIENT, IMAGE, VIDEO")
        void hasFourValues() {
            assertThat(BackgroundType.values())
                    .containsExactly(
                            BackgroundType.SOLID,
                            BackgroundType.GRADIENT,
                            BackgroundType.IMAGE,
                            BackgroundType.VIDEO
                    );
        }

        @Test
        @DisplayName("valueOf resolves each type correctly")
        void valueOfResolvesCorrectly() {
            assertThat(BackgroundType.valueOf("SOLID")).isEqualTo(BackgroundType.SOLID);
            assertThat(BackgroundType.valueOf("GRADIENT")).isEqualTo(BackgroundType.GRADIENT);
            assertThat(BackgroundType.valueOf("IMAGE")).isEqualTo(BackgroundType.IMAGE);
            assertThat(BackgroundType.valueOf("VIDEO")).isEqualTo(BackgroundType.VIDEO);
        }

        @Test
        @DisplayName("valueOf throws for unknown name")
        void valueOfThrowsForUnknown() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> BackgroundType.valueOf("NONE"));
        }
    }

    // ── BackgroundConfig ─────────────────────────────────────────────

    @Nested
    @DisplayName("BackgroundConfig")
    class BackgroundConfigTests {

        @Test
        @DisplayName("solid background has correct accessors")
        void solidBackgroundAccessors() {
            var bg = new BackgroundConfig(
                    BackgroundType.SOLID, "#ffffff", null, null, null,
                    null, null, false, null);

            assertThat(bg.type()).isEqualTo(BackgroundType.SOLID);
            assertThat(bg.color()).isEqualTo("#ffffff");
            assertThat(bg.gradientCss()).isNull();
            assertThat(bg.imageUrl()).isNull();
            assertThat(bg.imageAlt()).isNull();
            assertThat(bg.imageSize()).isNull();
            assertThat(bg.imagePosition()).isNull();
            assertThat(bg.parallax()).isFalse();
            assertThat(bg.videoUrl()).isNull();
        }

        @Test
        @DisplayName("gradient background has correct accessors")
        void gradientBackgroundAccessors() {
            var gradient = "linear-gradient(135deg, #667eea 0%, #764ba2 100%)";
            var bg = new BackgroundConfig(
                    BackgroundType.GRADIENT, null, gradient, null, null,
                    null, null, false, null);

            assertThat(bg.type()).isEqualTo(BackgroundType.GRADIENT);
            assertThat(bg.gradientCss()).isEqualTo(gradient);
            assertThat(bg.color()).isNull();
        }

        @Test
        @DisplayName("image background with overlay and parallax has correct accessors")
        void imageBackgroundWithOverlayAndParallax() {
            var bg = new BackgroundConfig(
                    BackgroundType.IMAGE, "rgba(0,0,0,0.6)", null,
                    "/uploads/hero-bg.webp", "Office workspace",
                    "cover", "center top", true, null);

            assertThat(bg.type()).isEqualTo(BackgroundType.IMAGE);
            assertThat(bg.color()).isEqualTo("rgba(0,0,0,0.6)");
            assertThat(bg.imageUrl()).isEqualTo("/uploads/hero-bg.webp");
            assertThat(bg.imageAlt()).isEqualTo("Office workspace");
            assertThat(bg.imageSize()).isEqualTo("cover");
            assertThat(bg.imagePosition()).isEqualTo("center top");
            assertThat(bg.parallax()).isTrue();
            assertThat(bg.videoUrl()).isNull();
        }

        @Test
        @DisplayName("video background with poster has correct accessors")
        void videoBackgroundWithPoster() {
            var bg = new BackgroundConfig(
                    BackgroundType.VIDEO, "rgba(0,0,0,0.4)", null,
                    "/uploads/poster.jpg", "Video poster",
                    null, null, false, "/uploads/bg-video.mp4");

            assertThat(bg.type()).isEqualTo(BackgroundType.VIDEO);
            assertThat(bg.videoUrl()).isEqualTo("/uploads/bg-video.mp4");
            assertThat(bg.imageUrl()).isEqualTo("/uploads/poster.jpg");
            assertThat(bg.color()).isEqualTo("rgba(0,0,0,0.4)");
        }

        @Test
        @DisplayName("identical BackgroundConfigs are equal")
        void identicalBackgroundConfigsAreEqual() {
            var a = new BackgroundConfig(
                    BackgroundType.SOLID, "#000", null, null, null,
                    null, null, false, null);
            var b = new BackgroundConfig(
                    BackgroundType.SOLID, "#000", null, null, null,
                    null, null, false, null);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("BackgroundConfigs with different types are not equal")
        void differentTypesNotEqual() {
            var a = new BackgroundConfig(
                    BackgroundType.SOLID, "#000", null, null, null,
                    null, null, false, null);
            var b = new BackgroundConfig(
                    BackgroundType.GRADIENT, "#000", null, null, null,
                    null, null, false, null);

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ── A11yConfig ───────────────────────────────────────────────────

    @Nested
    @DisplayName("A11yConfig")
    class A11yConfigTests {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var a11y = new A11yConfig(
                    "Main hero banner", "hero-desc", "banner", "en");

            assertThat(a11y.ariaLabel()).isEqualTo("Main hero banner");
            assertThat(a11y.ariaDescribedBy()).isEqualTo("hero-desc");
            assertThat(a11y.role()).isEqualTo("banner");
            assertThat(a11y.lang()).isEqualTo("en");
        }

        @Test
        @DisplayName("all fields can be null")
        void allFieldsCanBeNull() {
            var a11y = new A11yConfig(null, null, null, null);

            assertThat(a11y.ariaLabel()).isNull();
            assertThat(a11y.ariaDescribedBy()).isNull();
            assertThat(a11y.role()).isNull();
            assertThat(a11y.lang()).isNull();
        }

        @Test
        @DisplayName("partial fields can be set")
        void partialFieldsSet() {
            var a11y = new A11yConfig("Spanish quote section", null, null, "es");

            assertThat(a11y.ariaLabel()).isEqualTo("Spanish quote section");
            assertThat(a11y.ariaDescribedBy()).isNull();
            assertThat(a11y.role()).isNull();
            assertThat(a11y.lang()).isEqualTo("es");
        }

        @Test
        @DisplayName("identical A11yConfigs are equal")
        void identicalA11yConfigsAreEqual() {
            var a = new A11yConfig("Label", "desc", "banner", "en");
            var b = new A11yConfig("Label", "desc", "banner", "en");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("A11yConfigs with different ariaLabel are not equal")
        void differentAriaLabelNotEqual() {
            var a = new A11yConfig("Label A", null, null, null);
            var b = new A11yConfig("Label B", null, null, null);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("A11yConfigs with different lang are not equal")
        void differentLangNotEqual() {
            var a = new A11yConfig(null, null, null, "en");
            var b = new A11yConfig(null, null, null, "es");

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ── ResourceRef ──────────────────────────────────────────────────

    @Nested
    @DisplayName("ResourceRef")
    class ResourceRefTests {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var ref = new ResourceRef("pages/about.css", 50, "HEAD");

            assertThat(ref.path()).isEqualTo("pages/about.css");
            assertThat(ref.order()).isEqualTo(50);
            assertThat(ref.position()).isEqualTo("HEAD");
        }

        @Test
        @DisplayName("supports CDN URL paths")
        void supportsCdnUrls() {
            var ref = new ResourceRef(
                    "https://cdn.example.com/lib.min.js", 200, "BODY_END");

            assertThat(ref.path()).isEqualTo("https://cdn.example.com/lib.min.js");
            assertThat(ref.position()).isEqualTo("BODY_END");
        }

        @Test
        @DisplayName("identical ResourceRefs are equal")
        void identicalResourceRefsAreEqual() {
            var a = new ResourceRef("style.css", 100, "HEAD");
            var b = new ResourceRef("style.css", 100, "HEAD");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("ResourceRefs with different order are not equal")
        void differentOrderNotEqual() {
            var a = new ResourceRef("style.css", 50, "HEAD");
            var b = new ResourceRef("style.css", 100, "HEAD");

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ── LocalizedContent ─────────────────────────────────────────────

    @Nested
    @DisplayName("LocalizedContent")
    class LocalizedContentTests {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var widgets = java.util.List.of(
                    new WidgetInstance("hero-1", "hero",
                            java.util.Map.of("heading", "Welcome"), null, null)
            );
            var meta = java.util.Map.of("keywords", "home, welcome");

            var content = new LocalizedContent(
                    "Welcome", "Welcome to our site",
                    "https://example.com/og.jpg", "Team photo",
                    widgets, meta);

            assertThat(content.title()).isEqualTo("Welcome");
            assertThat(content.description()).isEqualTo("Welcome to our site");
            assertThat(content.ogImage()).isEqualTo("https://example.com/og.jpg");
            assertThat(content.ogImageAlt()).isEqualTo("Team photo");
            assertThat(content.widgets()).hasSize(1);
            assertThat(content.widgets().getFirst().type()).isEqualTo("hero");
            assertThat(content.meta()).containsEntry("keywords", "home, welcome");
        }

        @Test
        @DisplayName("ogImage and ogImageAlt can be null")
        void nullOgFields() {
            var content = new LocalizedContent(
                    "Title", "Description", null, null,
                    java.util.List.of(), java.util.Map.of());

            assertThat(content.ogImage()).isNull();
            assertThat(content.ogImageAlt()).isNull();
        }

        @Test
        @DisplayName("empty widgets list produces empty page body")
        void emptyWidgetsList() {
            var content = new LocalizedContent(
                    "Empty Page", "No content",
                    null, null, java.util.List.of(), java.util.Map.of());

            assertThat(content.widgets()).isEmpty();
        }

        @Test
        @DisplayName("identical LocalizedContent records are equal")
        void identicalRecordsAreEqual() {
            var a = new LocalizedContent(
                    "Title", "Desc", null, null,
                    java.util.List.of(), java.util.Map.of());
            var b = new LocalizedContent(
                    "Title", "Desc", null, null,
                    java.util.List.of(), java.util.Map.of());

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}

package xss.it.jux.cms.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link WidgetInstance} record -- the serialized form
 * of a single widget placed on a CMS page.
 *
 * <p>Covers record construction, accessor methods, props map access,
 * nested record accessibility, and record equality semantics.</p>
 */
class WidgetInstanceTest {

    // ── Record construction and accessors ────────────────────────────

    @Nested
    @DisplayName("Record construction and accessors")
    class Construction {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var style = new StyleConfig(
                    null, 1.0, 0, null, "2rem", null,
                    "80vh", null, null, null, null, "center");
            var a11y = new A11yConfig("Hero banner", null, "banner", "en");
            var props = Map.<String, Object>of(
                    "heading", "Welcome",
                    "ctaText", "Get Started",
                    "ctaUrl", "/contact"
            );

            var instance = new WidgetInstance("hero-1", "hero", props, style, a11y);

            assertThat(instance.id()).isEqualTo("hero-1");
            assertThat(instance.type()).isEqualTo("hero");
            assertThat(instance.props()).isEqualTo(props);
            assertThat(instance.style()).isSameAs(style);
            assertThat(instance.a11y()).isSameAs(a11y);
        }

        @Test
        @DisplayName("props map values are accessible by key")
        void propsMapAccessByKey() {
            var props = Map.<String, Object>of(
                    "heading", "Welcome to Our Site",
                    "subheading", "We build amazing things",
                    "ctaText", "Get Started"
            );

            var instance = new WidgetInstance("hero-1", "hero", props, null, null);

            assertThat(instance.props().get("heading")).isEqualTo("Welcome to Our Site");
            assertThat(instance.props().get("subheading")).isEqualTo("We build amazing things");
            assertThat(instance.props().get("ctaText")).isEqualTo("Get Started");
        }

        @Test
        @DisplayName("props map returns null for missing keys")
        void propsMapMissingKey() {
            var props = Map.<String, Object>of("heading", "Title");

            var instance = new WidgetInstance("text-1", "text", props, null, null);

            assertThat(instance.props().get("nonexistent")).isNull();
        }

        @Test
        @DisplayName("style and a11y can be null")
        void nullStyleAndA11y() {
            var instance = new WidgetInstance(
                    "spacer-1", "spacer", Map.of("height", "4rem"), null, null);

            assertThat(instance.style()).isNull();
            assertThat(instance.a11y()).isNull();
        }
    }

    // ── Nested record access ─────────────────────────────────────────

    @Nested
    @DisplayName("Nested record access")
    class NestedRecords {

        @Test
        @DisplayName("StyleConfig is accessible through the instance")
        void styleConfigAccessible() {
            var bg = new BackgroundConfig(
                    BackgroundType.SOLID, "#ffffff", null, null, null,
                    null, null, false, null);
            var style = new StyleConfig(
                    bg, 0.9, 4, "8px", "2rem", "0", "auto",
                    "0 4px 24px rgba(0,0,0,0.12)", "fade-in", null, "1200px", "left");

            var instance = new WidgetInstance(
                    "card-1", "cards", Map.of(), style, null);

            assertThat(instance.style().background()).isSameAs(bg);
            assertThat(instance.style().background().type()).isEqualTo(BackgroundType.SOLID);
            assertThat(instance.style().background().color()).isEqualTo("#ffffff");
            assertThat(instance.style().opacity()).isEqualTo(0.9);
            assertThat(instance.style().blur()).isEqualTo(4);
            assertThat(instance.style().borderRadius()).isEqualTo("8px");
            assertThat(instance.style().padding()).isEqualTo("2rem");
            assertThat(instance.style().animation()).isEqualTo("fade-in");
            assertThat(instance.style().maxWidth()).isEqualTo("1200px");
        }

        @Test
        @DisplayName("A11yConfig is accessible through the instance")
        void a11yConfigAccessible() {
            var a11y = new A11yConfig(
                    "Main navigation", "nav-desc", "navigation", "en");

            var instance = new WidgetInstance(
                    "nav-1", "navigation", Map.of(), null, a11y);

            assertThat(instance.a11y().ariaLabel()).isEqualTo("Main navigation");
            assertThat(instance.a11y().ariaDescribedBy()).isEqualTo("nav-desc");
            assertThat(instance.a11y().role()).isEqualTo("navigation");
            assertThat(instance.a11y().lang()).isEqualTo("en");
        }
    }

    // ── Record equality ──────────────────────────────────────────────

    @Nested
    @DisplayName("Record equality")
    class Equality {

        @Test
        @DisplayName("identical instances are equal")
        void identicalInstancesAreEqual() {
            var props = Map.<String, Object>of("heading", "Hello");
            var style = new StyleConfig(
                    null, 1.0, 0, null, null, null, null,
                    null, null, null, null, null);
            var a11y = new A11yConfig("Label", null, null, null);

            var a = new WidgetInstance("hero-1", "hero", props, style, a11y);
            var b = new WidgetInstance("hero-1", "hero", props, style, a11y);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("instances with different IDs are not equal")
        void differentIdsNotEqual() {
            var props = Map.<String, Object>of("heading", "Hello");

            var a = new WidgetInstance("hero-1", "hero", props, null, null);
            var b = new WidgetInstance("hero-2", "hero", props, null, null);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("instances with different types are not equal")
        void differentTypesNotEqual() {
            var props = Map.<String, Object>of("heading", "Hello");

            var a = new WidgetInstance("widget-1", "hero", props, null, null);
            var b = new WidgetInstance("widget-1", "text", props, null, null);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("instances with different props are not equal")
        void differentPropsNotEqual() {
            var a = new WidgetInstance("hero-1", "hero",
                    Map.of("heading", "Hello"), null, null);
            var b = new WidgetInstance("hero-1", "hero",
                    Map.of("heading", "World"), null, null);

            assertThat(a).isNotEqualTo(b);
        }
    }
}

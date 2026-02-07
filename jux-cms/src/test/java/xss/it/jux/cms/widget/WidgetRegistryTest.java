package xss.it.jux.cms.widget;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link WidgetRegistry} -- the central registry of all
 * available widget types in the CMS.
 *
 * <p>Covers registration, lookup, listing, category grouping, duplicate
 * handling, and the returned {@link WidgetRegistry.WidgetRegistration}
 * marker record.</p>
 */
class WidgetRegistryTest {

    /**
     * Minimal widget component used exclusively for testing.
     * Renders a single {@code <div>} element with no content.
     */
    static class DummyWidget extends Component {
        @Override
        public Element render() {
            return Element.of("div");
        }
    }

    /**
     * Second dummy widget to distinguish registrations in multi-widget tests.
     */
    static class AnotherWidget extends Component {
        @Override
        public Element render() {
            return Element.of("span");
        }
    }

    private WidgetRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new WidgetRegistry();
    }

    // ── register() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("register()")
    class Register {

        @Test
        @DisplayName("returns WidgetRegistration with the registered type key")
        void returnsRegistrationWithType() {
            var schema = PropSchema.builder().build();

            var registration = registry.register(
                    "hero", DummyWidget.class, "Hero Banner", "image",
                    WidgetCategory.HERO, schema);

            assertThat(registration).isNotNull();
            assertThat(registration.type()).isEqualTo("hero");
        }

        @Test
        @DisplayName("makes widget retrievable via get()")
        void makesWidgetRetrievable() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Main heading", true)
                    .build();

            registry.register("hero", DummyWidget.class, "Hero Banner", "image",
                    WidgetCategory.HERO, schema);

            assertThat(registry.get("hero")).isPresent();
        }

        @Test
        @DisplayName("stores all definition fields correctly")
        void storesAllDefinitionFields() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Main heading", true)
                    .prop("ctaUrl", PropType.URL, "CTA link", false)
                    .build();

            registry.register("hero", DummyWidget.class, "Hero Banner", "image",
                    WidgetCategory.HERO, schema);

            var def = registry.get("hero").orElseThrow();
            assertThat(def.type()).isEqualTo("hero");
            assertThat(def.componentClass()).isEqualTo(DummyWidget.class);
            assertThat(def.label()).isEqualTo("Hero Banner");
            assertThat(def.icon()).isEqualTo("image");
            assertThat(def.category()).isEqualTo(WidgetCategory.HERO);
            assertThat(def.schema()).isEqualTo(schema);
            assertThat(def.schema().fields()).hasSize(2);
        }

        @Test
        @DisplayName("duplicate type key silently replaces the previous registration")
        void duplicateTypeReplacesExisting() {
            var schema1 = PropSchema.builder().build();
            var schema2 = PropSchema.builder()
                    .prop("title", PropType.STRING, "Title", true)
                    .build();

            registry.register("hero", DummyWidget.class, "Old Hero", "old-icon",
                    WidgetCategory.HERO, schema1);
            registry.register("hero", AnotherWidget.class, "New Hero", "new-icon",
                    WidgetCategory.HERO, schema2);

            var def = registry.get("hero").orElseThrow();
            assertThat(def.componentClass()).isEqualTo(AnotherWidget.class);
            assertThat(def.label()).isEqualTo("New Hero");
            assertThat(def.icon()).isEqualTo("new-icon");
            assertThat(def.schema().fields()).hasSize(1);
        }

        @Test
        @DisplayName("multiple widgets can be registered independently")
        void multipleWidgetsRegistered() {
            var schema = PropSchema.builder().build();

            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            registry.register("text", AnotherWidget.class, "Text Block", "text",
                    WidgetCategory.CONTENT, schema);
            registry.register("form", DummyWidget.class, "Contact Form", "form",
                    WidgetCategory.FORMS, schema);

            assertThat(registry.get("hero")).isPresent();
            assertThat(registry.get("text")).isPresent();
            assertThat(registry.get("form")).isPresent();
            assertThat(registry.allTypes()).hasSize(3);
        }
    }

    // ── get() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("get()")
    class Get {

        @Test
        @DisplayName("returns present Optional for registered type")
        void returnsPresentForRegisteredType() {
            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, PropSchema.builder().build());

            assertThat(registry.get("hero")).isPresent();
        }

        @Test
        @DisplayName("returns empty Optional for unregistered type")
        void returnsEmptyForUnregisteredType() {
            assertThat(registry.get("nonexistent")).isEmpty();
        }

        @Test
        @DisplayName("returns empty Optional for null key")
        void returnsEmptyForNullKey() {
            assertThat(registry.get(null)).isEmpty();
        }

        @Test
        @DisplayName("returns correct definition for the requested type")
        void returnsCorrectDefinition() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();

            registry.register("hero", DummyWidget.class, "Hero Banner", "image",
                    WidgetCategory.HERO, schema);
            registry.register("text", AnotherWidget.class, "Text", "text",
                    WidgetCategory.CONTENT, PropSchema.builder().build());

            var def = registry.get("hero").orElseThrow();
            assertThat(def.type()).isEqualTo("hero");
            assertThat(def.componentClass()).isEqualTo(DummyWidget.class);
        }
    }

    // ── allTypes() ──────────────────────────────────────────────────

    @Nested
    @DisplayName("allTypes()")
    class AllTypes {

        @Test
        @DisplayName("returns empty list when no widgets registered")
        void emptyWhenNoRegistrations() {
            assertThat(registry.allTypes()).isEmpty();
        }

        @Test
        @DisplayName("returns type keys in registration order")
        void returnsKeysInRegistrationOrder() {
            var schema = PropSchema.builder().build();

            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            registry.register("text", DummyWidget.class, "Text", "text",
                    WidgetCategory.CONTENT, schema);
            registry.register("form", DummyWidget.class, "Form", "form",
                    WidgetCategory.FORMS, schema);

            assertThat(registry.allTypes())
                    .containsExactly("hero", "text", "form");
        }

        @Test
        @DisplayName("returned list is unmodifiable")
        void returnedListIsUnmodifiable() {
            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, PropSchema.builder().build());

            List<String> types = registry.allTypes();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> types.add("injected"));
        }
    }

    // ── allByCategory() ─────────────────────────────────────────────

    @Nested
    @DisplayName("allByCategory()")
    class AllByCategory {

        @Test
        @DisplayName("returns empty map when no widgets registered")
        void emptyWhenNoRegistrations() {
            assertThat(registry.allByCategory()).isEmpty();
        }

        @Test
        @DisplayName("groups widgets by their category")
        void groupsByCategory() {
            var schema = PropSchema.builder().build();

            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            registry.register("page-header", AnotherWidget.class, "Page Header", "header",
                    WidgetCategory.HERO, schema);
            registry.register("text", DummyWidget.class, "Text", "text",
                    WidgetCategory.CONTENT, schema);
            registry.register("form", DummyWidget.class, "Form", "form",
                    WidgetCategory.FORMS, schema);

            Map<WidgetCategory, List<WidgetDefinition>> grouped = registry.allByCategory();

            assertThat(grouped).hasSize(3);
            assertThat(grouped).containsKeys(
                    WidgetCategory.HERO, WidgetCategory.CONTENT, WidgetCategory.FORMS);
        }

        @Test
        @DisplayName("each category contains correct widgets in registration order")
        void widgetsInCorrectCategoryAndOrder() {
            var schema = PropSchema.builder().build();

            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            registry.register("page-header", AnotherWidget.class, "Page Header", "header",
                    WidgetCategory.HERO, schema);
            registry.register("text", DummyWidget.class, "Text", "text",
                    WidgetCategory.CONTENT, schema);

            var heroWidgets = registry.allByCategory().get(WidgetCategory.HERO);

            assertThat(heroWidgets).hasSize(2);
            assertThat(heroWidgets.get(0).type()).isEqualTo("hero");
            assertThat(heroWidgets.get(1).type()).isEqualTo("page-header");
        }

        @Test
        @DisplayName("omits categories with no registered widgets")
        void omitsEmptyCategories() {
            var schema = PropSchema.builder().build();

            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);

            assertThat(registry.allByCategory()).doesNotContainKeys(
                    WidgetCategory.CONTENT, WidgetCategory.FORMS, WidgetCategory.MEDIA,
                    WidgetCategory.CONVERSION, WidgetCategory.SOCIAL_PROOF,
                    WidgetCategory.INTERACTIVE, WidgetCategory.UTILITY,
                    WidgetCategory.NAVIGATION);
        }

        @Test
        @DisplayName("returned map is unmodifiable")
        void returnedMapIsUnmodifiable() {
            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, PropSchema.builder().build());

            Map<WidgetCategory, List<WidgetDefinition>> grouped = registry.allByCategory();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> grouped.put(WidgetCategory.CONTENT, List.of()));
        }

        @Test
        @DisplayName("widget lists within categories are unmodifiable")
        void widgetListsAreUnmodifiable() {
            registry.register("hero", DummyWidget.class, "Hero", "image",
                    WidgetCategory.HERO, PropSchema.builder().build());

            var heroWidgets = registry.allByCategory().get(WidgetCategory.HERO);

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> heroWidgets.add(
                            new WidgetDefinition("fake", DummyWidget.class, "Fake", "fake",
                                    WidgetCategory.HERO, PropSchema.builder().build())));
        }
    }

    // ── WidgetRegistration record ───────────────────────────────────

    @Nested
    @DisplayName("WidgetRegistration")
    class WidgetRegistrationTests {

        @Test
        @DisplayName("type() returns the registered type key")
        void typeReturnsKey() {
            var registration = new WidgetRegistry.WidgetRegistration("hero");

            assertThat(registration.type()).isEqualTo("hero");
        }

        @Test
        @DisplayName("records with same type are equal")
        void equalityBySameType() {
            var a = new WidgetRegistry.WidgetRegistration("hero");
            var b = new WidgetRegistry.WidgetRegistration("hero");

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("records with different types are not equal")
        void inequalityByDifferentType() {
            var a = new WidgetRegistry.WidgetRegistration("hero");
            var b = new WidgetRegistry.WidgetRegistration("text");

            assertThat(a).isNotEqualTo(b);
        }
    }
}

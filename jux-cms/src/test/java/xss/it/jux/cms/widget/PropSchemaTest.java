package xss.it.jux.cms.widget;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests for the {@link PropSchema}, {@link PropField}, {@link PropType},
 * and {@link WidgetCategory} types.
 *
 * <p>Covers the builder API, field construction, enum completeness,
 * and record equality semantics.</p>
 */
class PropSchemaTest {

    // ── PropSchema.builder() ─────────────────────────────────────────

    @Nested
    @DisplayName("PropSchema.builder()")
    class Builder {

        @Test
        @DisplayName("build() with no props returns empty fields list")
        void emptyBuilderProducesEmptyFields() {
            var schema = PropSchema.builder().build();

            assertThat(schema.fields()).isEmpty();
        }

        @Test
        @DisplayName("single prop is present in fields()")
        void singlePropPresent() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Main heading", true)
                    .build();

            assertThat(schema.fields()).hasSize(1);

            var field = schema.fields().getFirst();
            assertThat(field.key()).isEqualTo("heading");
            assertThat(field.type()).isEqualTo(PropType.STRING);
            assertThat(field.description()).isEqualTo("Main heading");
            assertThat(field.required()).isTrue();
            assertThat(field.defaultValue()).isNull();
            assertThat(field.enumValues()).isEmpty();
            assertThat(field.placeholder()).isNull();
        }

        @Test
        @DisplayName("multiple props are all present in declaration order")
        void multiplePropsInOrder() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Main heading text", true)
                    .prop("subheading", PropType.STRING, "Supporting text", false)
                    .prop("ctaText", PropType.STRING, "CTA button label", false)
                    .prop("ctaUrl", PropType.URL, "CTA destination", false)
                    .prop("backgroundImage", PropType.IMAGE, "Background image", false)
                    .build();

            assertThat(schema.fields()).hasSize(5);
            assertThat(schema.fields().get(0).key()).isEqualTo("heading");
            assertThat(schema.fields().get(1).key()).isEqualTo("subheading");
            assertThat(schema.fields().get(2).key()).isEqualTo("ctaText");
            assertThat(schema.fields().get(3).key()).isEqualTo("ctaUrl");
            assertThat(schema.fields().get(4).key()).isEqualTo("backgroundImage");
        }

        @Test
        @DisplayName("prop with enum values stores the allowed values")
        void propWithEnumValues() {
            var schema = PropSchema.builder()
                    .prop("alignment", PropType.ENUM, "Text alignment", false,
                            List.of("left", "center", "right"))
                    .build();

            var field = schema.fields().getFirst();
            assertThat(field.key()).isEqualTo("alignment");
            assertThat(field.type()).isEqualTo(PropType.ENUM);
            assertThat(field.required()).isFalse();
            assertThat(field.enumValues()).containsExactly("left", "center", "right");
        }

        @Test
        @DisplayName("fields list is unmodifiable after build")
        void fieldsListIsUnmodifiable() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();

            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> schema.fields().add(
                            new PropField("injected", PropType.STRING, "Injected",
                                    false, null, List.of(), null)));
        }

        @Test
        @DisplayName("builder can mix simple and enum props")
        void mixSimpleAndEnumProps() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Main heading", true)
                    .prop("alignment", PropType.ENUM, "Alignment", false,
                            List.of("left", "center", "right"))
                    .prop("autoplay", PropType.BOOLEAN, "Auto-rotate", false)
                    .prop("layout", PropType.ENUM, "Layout mode", true,
                            List.of("grid", "carousel"))
                    .build();

            assertThat(schema.fields()).hasSize(4);
            assertThat(schema.fields().get(0).enumValues()).isEmpty();
            assertThat(schema.fields().get(1).enumValues()).containsExactly("left", "center", "right");
            assertThat(schema.fields().get(2).enumValues()).isEmpty();
            assertThat(schema.fields().get(3).enumValues()).containsExactly("grid", "carousel");
        }

        @Test
        @DisplayName("building does not affect subsequent builder additions")
        void buildDoesNotAffectSubsequentAdditions() {
            var builder = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true);

            var schema1 = builder.build();

            builder.prop("subheading", PropType.STRING, "Subheading", false);
            var schema2 = builder.build();

            assertThat(schema1.fields()).hasSize(1);
            assertThat(schema2.fields()).hasSize(2);
        }
    }

    // ── PropSchema record equality ───────────────────────────────────

    @Nested
    @DisplayName("PropSchema equality")
    class SchemaEquality {

        @Test
        @DisplayName("schemas with same fields are equal")
        void sameSchemasAreEqual() {
            var a = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();
            var b = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("schemas with different fields are not equal")
        void differentSchemasNotEqual() {
            var a = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();
            var b = PropSchema.builder()
                    .prop("title", PropType.STRING, "Title", true)
                    .build();

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("empty schemas are equal")
        void emptySchemasAreEqual() {
            var a = PropSchema.builder().build();
            var b = PropSchema.builder().build();

            assertThat(a).isEqualTo(b);
        }
    }

    // ── PropField ────────────────────────────────────────────────────

    @Nested
    @DisplayName("PropField")
    class PropFieldTests {

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var field = new PropField(
                    "heading", PropType.STRING, "Main heading text",
                    true, "Default Title", List.of(), "Enter heading");

            assertThat(field.key()).isEqualTo("heading");
            assertThat(field.type()).isEqualTo(PropType.STRING);
            assertThat(field.description()).isEqualTo("Main heading text");
            assertThat(field.required()).isTrue();
            assertThat(field.defaultValue()).isEqualTo("Default Title");
            assertThat(field.enumValues()).isEmpty();
            assertThat(field.placeholder()).isEqualTo("Enter heading");
        }

        @Test
        @DisplayName("enum field has enum values accessible")
        void enumFieldValues() {
            var field = new PropField(
                    "alignment", PropType.ENUM, "Text alignment",
                    false, "center", List.of("left", "center", "right"), null);

            assertThat(field.enumValues()).containsExactly("left", "center", "right");
            assertThat(field.defaultValue()).isEqualTo("center");
        }

        @Test
        @DisplayName("optional field with no default")
        void optionalFieldNoDefault() {
            var field = new PropField(
                    "subheading", PropType.TEXT, "Supporting text",
                    false, null, List.of(), null);

            assertThat(field.required()).isFalse();
            assertThat(field.defaultValue()).isNull();
        }

        @Test
        @DisplayName("identical PropFields are equal")
        void identicalFieldsAreEqual() {
            var a = new PropField("key", PropType.STRING, "desc", true, null, List.of(), null);
            var b = new PropField("key", PropType.STRING, "desc", true, null, List.of(), null);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("PropFields with different keys are not equal")
        void differentKeysNotEqual() {
            var a = new PropField("key1", PropType.STRING, "desc", true, null, List.of(), null);
            var b = new PropField("key2", PropType.STRING, "desc", true, null, List.of(), null);

            assertThat(a).isNotEqualTo(b);
        }

        @Test
        @DisplayName("PropFields with different types are not equal")
        void differentTypesNotEqual() {
            var a = new PropField("key", PropType.STRING, "desc", true, null, List.of(), null);
            var b = new PropField("key", PropType.URL, "desc", true, null, List.of(), null);

            assertThat(a).isNotEqualTo(b);
        }
    }

    // ── PropType enum ────────────────────────────────────────────────

    @Nested
    @DisplayName("PropType")
    class PropTypeTests {

        @Test
        @DisplayName("has exactly 16 values")
        void hasExactly16Values() {
            assertThat(PropType.values()).hasSize(16);
        }

        @Test
        @DisplayName("contains all expected types in declaration order")
        void containsAllExpectedTypes() {
            assertThat(PropType.values())
                    .containsExactly(
                            PropType.STRING,
                            PropType.TEXT,
                            PropType.RICH_TEXT,
                            PropType.INT,
                            PropType.DOUBLE,
                            PropType.BOOLEAN,
                            PropType.URL,
                            PropType.EMAIL,
                            PropType.IMAGE,
                            PropType.VIDEO,
                            PropType.COLOR,
                            PropType.ENUM,
                            PropType.DATE,
                            PropType.LIST,
                            PropType.OBJECT,
                            PropType.ICON
                    );
        }

        @Test
        @DisplayName("valueOf resolves each type correctly")
        void valueOfResolvesCorrectly() {
            assertThat(PropType.valueOf("STRING")).isEqualTo(PropType.STRING);
            assertThat(PropType.valueOf("TEXT")).isEqualTo(PropType.TEXT);
            assertThat(PropType.valueOf("RICH_TEXT")).isEqualTo(PropType.RICH_TEXT);
            assertThat(PropType.valueOf("INT")).isEqualTo(PropType.INT);
            assertThat(PropType.valueOf("DOUBLE")).isEqualTo(PropType.DOUBLE);
            assertThat(PropType.valueOf("BOOLEAN")).isEqualTo(PropType.BOOLEAN);
            assertThat(PropType.valueOf("URL")).isEqualTo(PropType.URL);
            assertThat(PropType.valueOf("EMAIL")).isEqualTo(PropType.EMAIL);
            assertThat(PropType.valueOf("IMAGE")).isEqualTo(PropType.IMAGE);
            assertThat(PropType.valueOf("VIDEO")).isEqualTo(PropType.VIDEO);
            assertThat(PropType.valueOf("COLOR")).isEqualTo(PropType.COLOR);
            assertThat(PropType.valueOf("ENUM")).isEqualTo(PropType.ENUM);
            assertThat(PropType.valueOf("DATE")).isEqualTo(PropType.DATE);
            assertThat(PropType.valueOf("LIST")).isEqualTo(PropType.LIST);
            assertThat(PropType.valueOf("OBJECT")).isEqualTo(PropType.OBJECT);
            assertThat(PropType.valueOf("ICON")).isEqualTo(PropType.ICON);
        }

        @Test
        @DisplayName("valueOf throws for unknown name")
        void valueOfThrowsForUnknown() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> PropType.valueOf("UNKNOWN"));
        }
    }

    // ── WidgetCategory enum ──────────────────────────────────────────

    @Nested
    @DisplayName("WidgetCategory")
    class WidgetCategoryTests {

        @Test
        @DisplayName("has exactly 9 values")
        void hasExactly9Values() {
            assertThat(WidgetCategory.values()).hasSize(9);
        }

        @Test
        @DisplayName("contains all expected categories in declaration order")
        void containsAllExpectedCategories() {
            assertThat(WidgetCategory.values())
                    .containsExactly(
                            WidgetCategory.HERO,
                            WidgetCategory.CONTENT,
                            WidgetCategory.MEDIA,
                            WidgetCategory.FORMS,
                            WidgetCategory.CONVERSION,
                            WidgetCategory.SOCIAL_PROOF,
                            WidgetCategory.INTERACTIVE,
                            WidgetCategory.UTILITY,
                            WidgetCategory.NAVIGATION
                    );
        }

        @Test
        @DisplayName("each category has the correct human-readable label")
        void categoriesHaveCorrectLabels() {
            assertThat(WidgetCategory.HERO.label()).isEqualTo("Hero & Headers");
            assertThat(WidgetCategory.CONTENT.label()).isEqualTo("Content");
            assertThat(WidgetCategory.MEDIA.label()).isEqualTo("Media");
            assertThat(WidgetCategory.FORMS.label()).isEqualTo("Forms");
            assertThat(WidgetCategory.CONVERSION.label()).isEqualTo("Conversion");
            assertThat(WidgetCategory.SOCIAL_PROOF.label()).isEqualTo("Social Proof");
            assertThat(WidgetCategory.INTERACTIVE.label()).isEqualTo("Interactive");
            assertThat(WidgetCategory.UTILITY.label()).isEqualTo("Utility");
            assertThat(WidgetCategory.NAVIGATION.label()).isEqualTo("Navigation");
        }

        @Test
        @DisplayName("valueOf resolves each category correctly")
        void valueOfResolvesCorrectly() {
            assertThat(WidgetCategory.valueOf("HERO")).isEqualTo(WidgetCategory.HERO);
            assertThat(WidgetCategory.valueOf("CONTENT")).isEqualTo(WidgetCategory.CONTENT);
            assertThat(WidgetCategory.valueOf("MEDIA")).isEqualTo(WidgetCategory.MEDIA);
            assertThat(WidgetCategory.valueOf("FORMS")).isEqualTo(WidgetCategory.FORMS);
            assertThat(WidgetCategory.valueOf("CONVERSION")).isEqualTo(WidgetCategory.CONVERSION);
            assertThat(WidgetCategory.valueOf("SOCIAL_PROOF")).isEqualTo(WidgetCategory.SOCIAL_PROOF);
            assertThat(WidgetCategory.valueOf("INTERACTIVE")).isEqualTo(WidgetCategory.INTERACTIVE);
            assertThat(WidgetCategory.valueOf("UTILITY")).isEqualTo(WidgetCategory.UTILITY);
            assertThat(WidgetCategory.valueOf("NAVIGATION")).isEqualTo(WidgetCategory.NAVIGATION);
        }

        @Test
        @DisplayName("valueOf throws for unknown name")
        void valueOfThrowsForUnknown() {
            assertThatIllegalArgumentException()
                    .isThrownBy(() -> WidgetCategory.valueOf("UNKNOWN_CATEGORY"));
        }

        @Test
        @DisplayName("label() never returns null")
        void labelNeverNull() {
            for (WidgetCategory category : WidgetCategory.values()) {
                assertThat(category.label()).isNotNull().isNotEmpty();
            }
        }
    }

    // ── WidgetDefinition ─────────────────────────────────────────────

    @Nested
    @DisplayName("WidgetDefinition")
    class WidgetDefinitionTests {

        /** Minimal dummy component for WidgetDefinition construction. */
        static class TestWidget extends xss.it.jux.core.Component {
            @Override
            public xss.it.jux.core.Element render() {
                return xss.it.jux.core.Element.of("div");
            }
        }

        @Test
        @DisplayName("all accessors return constructor values")
        void accessorsReturnConstructorValues() {
            var schema = PropSchema.builder()
                    .prop("heading", PropType.STRING, "Heading", true)
                    .build();

            var def = new WidgetDefinition(
                    "hero", TestWidget.class, "Hero Banner", "image",
                    WidgetCategory.HERO, schema);

            assertThat(def.type()).isEqualTo("hero");
            assertThat(def.componentClass()).isEqualTo(TestWidget.class);
            assertThat(def.label()).isEqualTo("Hero Banner");
            assertThat(def.icon()).isEqualTo("image");
            assertThat(def.category()).isEqualTo(WidgetCategory.HERO);
            assertThat(def.schema()).isEqualTo(schema);
        }

        @Test
        @DisplayName("identical definitions are equal")
        void identicalDefinitionsAreEqual() {
            var schema = PropSchema.builder().build();

            var a = new WidgetDefinition(
                    "hero", TestWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            var b = new WidgetDefinition(
                    "hero", TestWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }

        @Test
        @DisplayName("definitions with different types are not equal")
        void differentTypesNotEqual() {
            var schema = PropSchema.builder().build();

            var a = new WidgetDefinition(
                    "hero", TestWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);
            var b = new WidgetDefinition(
                    "text", TestWidget.class, "Hero", "image",
                    WidgetCategory.HERO, schema);

            assertThat(a).isNotEqualTo(b);
        }
    }
}

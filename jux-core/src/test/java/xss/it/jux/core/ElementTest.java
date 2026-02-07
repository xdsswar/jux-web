package xss.it.jux.core;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for the {@link Element} virtual DOM node class.
 *
 * <p>Covers the fluent builder API, attribute management, CSS classes,
 * inline styles, children, event handlers, ARIA accessibility attributes,
 * language, and unmodifiable getter contracts.</p>
 */
class ElementTest {

    // ── Factory ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("Element.of()")
    class Factory {

        @Test
        @DisplayName("creates element with the given tag")
        void ofCreatesElementWithTag() {
            Element el = Element.of("div");
            assertThat(el.getTag()).isEqualTo("div");
        }

        @Test
        @DisplayName("throws NullPointerException when tag is null")
        void ofThrowsOnNullTag() {
            assertThatNullPointerException().isThrownBy(() -> Element.of(null));
        }
    }

    // ── attr() ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("attr()")
    class Attr {

        @Test
        @DisplayName("stores attribute in getAttributes()")
        void attrSetsAttribute() {
            Element el = Element.of("a").attr("href", "/about");
            assertThat(el.getAttributes().get("href")).isEqualTo("/about");
        }

        @Test
        @DisplayName("null value is a no-op")
        void attrNullValueIsNoOp() {
            Element el = Element.of("div").attr("key", null);
            assertThat(el.getAttributes()).doesNotContainKey("key");
        }

        @Test
        @DisplayName("throws NullPointerException when key is null")
        void attrThrowsOnNullKey() {
            assertThatNullPointerException()
                    .isThrownBy(() -> Element.of("div").attr(null, "value"));
        }
    }

    // ── cls() ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cls()")
    class Cls {

        @Test
        @DisplayName("adds CSS classes from varargs")
        void clsAddsClasses() {
            Element el = div().cls("a", "b");
            assertThat(el.getCssClasses()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("multiple cls() calls are additive")
        void clsIsAdditive() {
            Element el = div().cls("a").cls("b");
            assertThat(el.getCssClasses()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("skips null and empty strings")
        void clsSkipsNullAndEmpty() {
            Element el = div().cls("a", null, "", "b");
            assertThat(el.getCssClasses()).containsExactly("a", "b");
        }

        @Test
        @DisplayName("class attribute appears in getAttributes()")
        void clsAppearsInGetAttributes() {
            Element el = div().cls("card", "shadow");
            assertThat(el.getAttributes().get("class")).isEqualTo("card shadow");
        }
    }

    // ── id() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("id() sets id attribute")
    void idSetsAttribute() {
        Element el = div().id("x");
        assertThat(el.getAttributes().get("id")).isEqualTo("x");
    }

    // ── style() ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("style()")
    class Style {

        @Test
        @DisplayName("stores inline style property")
        void styleSetsProperty() {
            Element el = div().style("color", "red");
            assertThat(el.getStyles().get("color")).isEqualTo("red");
        }

        @Test
        @DisplayName("null value is a no-op")
        void styleNullValueIsNoOp() {
            Element el = div().style("key", null);
            assertThat(el.getStyles()).doesNotContainKey("key");
        }

        @Test
        @DisplayName("style attribute appears in getAttributes()")
        void styleAppearsInGetAttributes() {
            Element el = div().style("color", "red").style("padding", "1rem");
            assertThat(el.getAttributes().get("style")).isEqualTo("color: red; padding: 1rem");
        }
    }

    // ── text() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("text() sets textContent")
    void textSetsContent() {
        Element el = p().text("hi");
        assertThat(el.getTextContent()).isEqualTo("hi");
    }

    // ── children() ────────────────────────────────────────────────────

    @Nested
    @DisplayName("children()")
    class Children {

        @Test
        @DisplayName("varargs children in order, null children skipped")
        void childrenVarargs() {
            Element a = span().text("a");
            Element b = span().text("b");
            Element parent = div().children(a, null, b);
            assertThat(parent.getChildren()).containsExactly(a, b);
        }

        @Test
        @DisplayName("list children in order")
        void childrenList() {
            Element a = span().text("a");
            Element b = span().text("b");
            Element parent = div().children(List.of(a, b));
            assertThat(parent.getChildren()).containsExactly(a, b);
        }
    }

    // ── child(Component) ──────────────────────────────────────────────

    @Test
    @DisplayName("child(Component) renders the component inline")
    void childComponent() {
        Component comp = new Component() {
            @Override
            public Element render() {
                return span().text("rendered");
            }
        };
        Element parent = div().child(comp);
        assertThat(parent.getChildren()).hasSize(1);
        assertThat(parent.getChildren().getFirst().getTag()).isEqualTo("span");
        assertThat(parent.getChildren().getFirst().getTextContent()).isEqualTo("rendered");
    }

    // ── on() ──────────────────────────────────────────────────────────

    @Test
    @DisplayName("on() registers an event handler")
    void onRegistersHandler() {
        EventHandler handler = e -> {};
        Element el = button().on("click", handler);
        assertThat(el.getEventHandlers().get("click")).isSameAs(handler);
    }

    // ── ARIA attributes ───────────────────────────────────────────────

    @Nested
    @DisplayName("ARIA methods")
    class Aria {

        @Test
        @DisplayName("aria(property, value) sets aria-prefixed attribute")
        void ariaGeneric() {
            Element el = div().aria("label", "x");
            assertThat(el.getAttributes().get("aria-label")).isEqualTo("x");
        }

        @Test
        @DisplayName("role() sets role attribute")
        void role() {
            Element el = div().role("button");
            assertThat(el.getAttributes().get("role")).isEqualTo("button");
        }

        @Test
        @DisplayName("tabIndex() sets tabindex attribute as string")
        void tabIndex() {
            Element el = div().tabIndex(0);
            assertThat(el.getAttributes().get("tabindex")).isEqualTo("0");
        }

        @Test
        @DisplayName("ariaLive() sets aria-live attribute")
        void ariaLive() {
            Element el = div().ariaLive("polite");
            assertThat(el.getAttributes().get("aria-live")).isEqualTo("polite");
        }

        @Test
        @DisplayName("ariaAtomic(true) sets aria-atomic to 'true'")
        void ariaAtomic() {
            Element el = div().ariaAtomic(true);
            assertThat(el.getAttributes().get("aria-atomic")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaHidden(true) sets aria-hidden to 'true'")
        void ariaHidden() {
            Element el = div().ariaHidden(true);
            assertThat(el.getAttributes().get("aria-hidden")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaExpanded(false) sets aria-expanded to 'false'")
        void ariaExpanded() {
            Element el = div().ariaExpanded(false);
            assertThat(el.getAttributes().get("aria-expanded")).isEqualTo("false");
        }

        @Test
        @DisplayName("ariaControls() sets aria-controls attribute")
        void ariaControls() {
            Element el = div().ariaControls("panel1");
            assertThat(el.getAttributes().get("aria-controls")).isEqualTo("panel1");
        }

        @Test
        @DisplayName("ariaLabelledBy() sets aria-labelledby attribute")
        void ariaLabelledBy() {
            Element el = div().ariaLabelledBy("lbl");
            assertThat(el.getAttributes().get("aria-labelledby")).isEqualTo("lbl");
        }

        @Test
        @DisplayName("ariaDescribedBy() sets aria-describedby attribute")
        void ariaDescribedBy() {
            Element el = div().ariaDescribedBy("desc");
            assertThat(el.getAttributes().get("aria-describedby")).isEqualTo("desc");
        }

        @Test
        @DisplayName("ariaRequired(true) sets aria-required to 'true'")
        void ariaRequired() {
            Element el = input().ariaRequired(true);
            assertThat(el.getAttributes().get("aria-required")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaInvalid(true) sets aria-invalid to 'true'")
        void ariaInvalid() {
            Element el = input().ariaInvalid(true);
            assertThat(el.getAttributes().get("aria-invalid")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaCurrent('page') sets aria-current to 'page'")
        void ariaCurrent() {
            Element el = a().ariaCurrent("page");
            assertThat(el.getAttributes().get("aria-current")).isEqualTo("page");
        }

        @Test
        @DisplayName("ariaDisabled(true) sets aria-disabled to 'true'")
        void ariaDisabled() {
            Element el = button().ariaDisabled(true);
            assertThat(el.getAttributes().get("aria-disabled")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaSelected(true) sets aria-selected to 'true'")
        void ariaSelected() {
            Element el = li().ariaSelected(true);
            assertThat(el.getAttributes().get("aria-selected")).isEqualTo("true");
        }

        @Test
        @DisplayName("ariaChecked('mixed') sets aria-checked to 'mixed'")
        void ariaChecked() {
            Element el = div().ariaChecked("mixed");
            assertThat(el.getAttributes().get("aria-checked")).isEqualTo("mixed");
        }

        @Test
        @DisplayName("ariaHasPopup('menu') sets aria-haspopup to 'menu'")
        void ariaHasPopup() {
            Element el = button().ariaHasPopup("menu");
            assertThat(el.getAttributes().get("aria-haspopup")).isEqualTo("menu");
        }
    }

    // ── lang() ────────────────────────────────────────────────────────

    @Test
    @DisplayName("lang() sets lang attribute")
    void langSetsAttribute() {
        Element el = div().lang("es");
        assertThat(el.getAttributes().get("lang")).isEqualTo("es");
    }

    // ── Chaining ──────────────────────────────────────────────────────

    @Test
    @DisplayName("builder methods return the same instance for chaining")
    void chainingReturnsSameInstance() {
        Element el = div();
        Element result = el.id("x").cls("a").attr("data-v", "1");
        assertThat(result).isSameAs(el);
    }

    // ── getAttributes() merges class and style ────────────────────────

    @Test
    @DisplayName("getAttributes() merges class and style into the map")
    void getAttributesMergesClassAndStyle() {
        Element el = div().id("x").cls("card").style("color", "red");
        Map<String, String> attrs = el.getAttributes();
        assertThat(attrs.get("id")).isEqualTo("x");
        assertThat(attrs.get("class")).isEqualTo("card");
        assertThat(attrs.get("style")).isEqualTo("color: red");
    }

    // ── Unmodifiable collections ──────────────────────────────────────

    @Nested
    @DisplayName("Unmodifiable getters")
    class UnmodifiableGetters {

        @Test
        @DisplayName("getChildren() returns unmodifiable list")
        void childrenUnmodifiable() {
            Element el = div().children(span());
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> el.getChildren().add(span()));
        }

        @Test
        @DisplayName("getEventHandlers() returns unmodifiable map")
        void eventHandlersUnmodifiable() {
            Element el = div().on("click", e -> {});
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> el.getEventHandlers().put("input", e -> {}));
        }

        @Test
        @DisplayName("getCssClasses() returns unmodifiable list")
        void cssClassesUnmodifiable() {
            Element el = div().cls("a");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> el.getCssClasses().add("b"));
        }

        @Test
        @DisplayName("getStyles() returns unmodifiable map")
        void stylesUnmodifiable() {
            Element el = div().style("color", "red");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> el.getStyles().put("padding", "0"));
        }

        @Test
        @DisplayName("getAttributes() returns unmodifiable map")
        void attributesUnmodifiable() {
            Element el = div().attr("data-x", "1");
            assertThatExceptionOfType(UnsupportedOperationException.class)
                    .isThrownBy(() -> el.getAttributes().put("data-y", "2"));
        }
    }
}

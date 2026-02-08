package xss.it.jux.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link TemplateCache} covering storage, retrieval, deep cloning,
 * enable/disable behavior, clear, and size tracking.
 */
@DisplayName("TemplateCache")
class TemplateCacheTest {

    private TemplateCache cache;

    @BeforeEach
    void setUp() {
        cache = new TemplateCache();
    }

    // ── Basic storage and retrieval ──────────────────────────────

    @Nested
    @DisplayName("Basic storage and retrieval")
    class BasicStorageAndRetrieval {

        @Test
        @DisplayName("New cache is empty with size zero")
        void newCache_hasSizeZero() {
            assertThat(cache.size()).isZero();
        }

        @Test
        @DisplayName("put then get returns element with same tag")
        void putThenGet_returnsElementWithSameTag() {
            Element original = Element.of("div").id("root");
            cache.put("test.html", original);

            Element retrieved = cache.get("test.html");

            assertThat(retrieved).isNotNull();
            assertThat(retrieved.getTag()).isEqualTo("div");
            assertThat(retrieved.getAttributes()).containsEntry("id", "root");
        }

        @Test
        @DisplayName("get returns null for missing key")
        void getMissingKey_returnsNull() {
            Element result = cache.get("nonexistent.html");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("size tracks number of entries")
        void size_tracksNumberOfEntries() {
            cache.put("a.html", Element.of("div"));
            cache.put("b.html", Element.of("span"));
            cache.put("c.html", Element.of("p"));

            assertThat(cache.size()).isEqualTo(3);
        }

        @Test
        @DisplayName("put with null path is a no-op")
        void putNullPath_isNoOp() {
            cache.put(null, Element.of("div"));

            assertThat(cache.size()).isZero();
        }

        @Test
        @DisplayName("put with null template is a no-op")
        void putNullTemplate_isNoOp() {
            cache.put("test.html", null);

            assertThat(cache.size()).isZero();
        }
    }

    // ── Deep cloning ─────────────────────────────────────────────

    @Nested
    @DisplayName("Deep cloning behavior")
    class DeepCloningBehavior {

        @Test
        @DisplayName("get returns a different object instance than the cached prototype")
        void getReturnsDifferentInstance_thanCachedPrototype() {
            Element original = Element.of("div").id("root");
            cache.put("test.html", original);

            Element clone1 = cache.get("test.html");
            Element clone2 = cache.get("test.html");

            assertThat(clone1).isNotSameAs(clone2);
            assertThat(clone1).isNotSameAs(original);
        }

        @Test
        @DisplayName("Modifying cloned element text does not affect cached prototype")
        void modifyingCloneText_doesNotAffectPrototype() {
            Element original = Element.of("p").text("Original text");
            cache.put("test.html", original);

            Element clone = cache.get("test.html");
            clone.text("Modified text");

            Element freshClone = cache.get("test.html");
            assertThat(freshClone.getTextContent()).isEqualTo("Original text");
        }

        @Test
        @DisplayName("Modifying child of clone does not affect cached prototype")
        void modifyingChildOfClone_doesNotAffectPrototype() {
            Element child = Element.of("span").text("Child text");
            Element original = Element.of("div").children(child);
            cache.put("test.html", original);

            Element clone = cache.get("test.html");
            clone.getChildren().getFirst().text("Changed");

            Element freshClone = cache.get("test.html");
            assertThat(freshClone.getChildren().getFirst().getTextContent())
                    .isEqualTo("Child text");
        }

        @Test
        @DisplayName("Deep clone preserves tag, attributes, classes, styles, text, and children")
        void deepClone_preservesAllElementProperties() {
            Element original = Element.of("section")
                    .id("hero")
                    .cls("primary", "wide")
                    .style("color", "red")
                    .style("padding", "2rem")
                    .attr("data-custom", "value")
                    .children(
                            Element.of("h1").text("Title"),
                            Element.of("p").text("Body")
                    );
            cache.put("test.html", original);

            Element clone = cache.get("test.html");

            assertThat(clone.getTag()).isEqualTo("section");
            assertThat(clone.getAttributes()).containsEntry("id", "hero");
            assertThat(clone.getAttributes()).containsEntry("data-custom", "value");
            assertThat(clone.getCssClasses()).containsExactly("primary", "wide");
            assertThat(clone.getStyles())
                    .containsEntry("color", "red")
                    .containsEntry("padding", "2rem");
            assertThat(clone.getChildren()).hasSize(2);
            assertThat(clone.getChildren().get(0).getTag()).isEqualTo("h1");
            assertThat(clone.getChildren().get(0).getTextContent()).isEqualTo("Title");
            assertThat(clone.getChildren().get(1).getTag()).isEqualTo("p");
            assertThat(clone.getChildren().get(1).getTextContent()).isEqualTo("Body");
        }

        @Test
        @DisplayName("Adding children to clone does not affect prototype")
        void addingChildrenToClone_doesNotAffectPrototype() {
            Element original = Element.of("div");
            cache.put("test.html", original);

            Element clone = cache.get("test.html");
            clone.children(Element.of("span").text("New child"));

            Element freshClone = cache.get("test.html");
            assertThat(freshClone.getChildren()).isEmpty();
        }
    }

    // ── Enable/disable ───────────────────────────────────────────

    @Nested
    @DisplayName("Enable and disable behavior")
    class EnableDisableBehavior {

        @Test
        @DisplayName("Cache is enabled by default")
        void cacheEnabledByDefault() {
            assertThat(cache.isEnabled()).isTrue();
        }

        @Test
        @DisplayName("Disabled cache: put is a no-op")
        void disabledCache_putIsNoOp() {
            cache.setEnabled(false);
            cache.put("test.html", Element.of("div"));

            assertThat(cache.size()).isZero();
        }

        @Test
        @DisplayName("Disabled cache: get returns null")
        void disabledCache_getReturnsNull() {
            cache.put("test.html", Element.of("div"));
            cache.setEnabled(false);

            Element result = cache.get("test.html");

            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Re-enable after disable: previously cached items accessible again")
        void reEnableAfterDisable_previousItemsAccessible() {
            cache.put("test.html", Element.of("div").id("cached"));
            cache.setEnabled(false);

            assertThat(cache.get("test.html")).isNull();

            cache.setEnabled(true);
            Element result = cache.get("test.html");

            assertThat(result).isNotNull();
            assertThat(result.getAttributes()).containsEntry("id", "cached");
        }
    }

    // ── Clear ────────────────────────────────────────────────────

    @Nested
    @DisplayName("Clear behavior")
    class ClearBehavior {

        @Test
        @DisplayName("clear empties the cache")
        void clear_emptiesCache() {
            cache.put("a.html", Element.of("div"));
            cache.put("b.html", Element.of("span"));
            assertThat(cache.size()).isEqualTo(2);

            cache.clear();

            assertThat(cache.size()).isZero();
            assertThat(cache.get("a.html")).isNull();
            assertThat(cache.get("b.html")).isNull();
        }

        @Test
        @DisplayName("clear on empty cache is safe")
        void clearOnEmptyCache_isSafe() {
            cache.clear();

            assertThat(cache.size()).isZero();
        }
    }
}

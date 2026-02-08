package xss.it.jux.html;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;
import xss.it.jux.html.annotation.Html;
import xss.it.jux.html.annotation.HtmlId;
import xss.it.jux.html.annotation.Slot;
import xss.it.jux.reactive.Initializable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link HtmlLoader} covering template loading from classpath,
 * {@code @HtmlId} and {@code @Slot} field injection, {@link Initializable}
 * callback, caching, and error handling.
 */
@DisplayName("HtmlLoader")
class HtmlLoaderTest {

    @BeforeEach
    void setUp() {
        /* Clear the shared cache before each test to ensure isolation. */
        HtmlLoader.getCache().clear();
        HtmlLoader.getCache().setEnabled(true);
    }

    // ── Test component inner classes ─────────────────────────────

    @Html("simple.html")
    static class SimpleComponent {
    }

    @Html("with-ids.html")
    static class ComponentWithIds {
        @HtmlId Element title;
        @HtmlId Element content;
    }

    @Html("with-ids.html")
    static class ComponentWithExplicitId {
        @HtmlId("title") Element myTitleField;
    }

    @Html("with-ids.html")
    static class ComponentWithFieldNameAsId {
        @HtmlId Element container;
    }

    @Html("with-slots.html")
    static class ComponentWithSlots {
        @Slot("sidebar") Element sidebarSlot;
    }

    @Html("with-slots.html")
    static class ComponentWithDefaultSlot {
        @Slot Element defaultSlot;
    }

    @Html("nonexistent.html")
    static class ComponentWithMissingTemplate {
    }

    @Html("with-ids.html")
    static class ComponentWithMissingId {
        @HtmlId Element nonexistent;
    }

    static class ComponentWithoutAnnotation {
    }

    @Html("simple.html")
    static class InitializableComponent implements Initializable {
        boolean initialized = false;

        @Override
        public void initialize() {
            initialized = true;
        }
    }

    @Html("nested.html")
    static class ComponentWithDeepId {
        @HtmlId Element deep;
    }

    @Html("with-expressions.html")
    static class ComponentWithExpressions {
    }

    @Html("with-ids.html")
    static class ComponentWithMultipleIds {
        @HtmlId Element container;
        @HtmlId Element title;
        @HtmlId Element content;
    }

    @Html("with-slots.html")
    static class ComponentWithSlotAndId {
        @Slot("sidebar") Element sidebar;
        @Slot("default") Element defaultContent;
    }

    // ── Basic template loading ───────────────────────────────────

    @Nested
    @DisplayName("Basic template loading")
    class BasicTemplateLoading {

        @Test
        @DisplayName("Load component with @Html annotation returns element tree")
        void loadComponentWithAnnotation_returnsElementTree() {
            SimpleComponent component = new SimpleComponent();

            Element root = HtmlLoader.load(component);

            assertThat(root).isNotNull();
            assertThat(root.getTag()).isEqualTo("div");
            assertThat(root.getAttributes()).containsEntry("id", "root");
            assertThat(root.getChildren()).hasSize(1);
            assertThat(root.getChildren().getFirst().getTag()).isEqualTo("h1");
            assertThat(root.getChildren().getFirst().getTextContent()).isEqualTo("Hello");
        }

        @Test
        @DisplayName("Load component with expressions template parses correctly")
        void loadComponentWithExpressions_parsesCorrectly() {
            ComponentWithExpressions component = new ComponentWithExpressions();

            Element root = HtmlLoader.load(component);

            assertThat(root).isNotNull();
            assertThat(root.getTag()).isEqualTo("div");
            assertThat(root.getChildren()).hasSize(2);
        }
    }

    // ── @HtmlId injection ────────────────────────────────────────

    @Nested
    @DisplayName("@HtmlId field injection")
    class HtmlIdInjection {

        @Test
        @DisplayName("@HtmlId fields injected with matching elements by field name")
        void htmlIdByFieldName_injectsCorrectElement() {
            ComponentWithIds component = new ComponentWithIds();

            HtmlLoader.load(component);

            assertThat(component.title).isNotNull();
            assertThat(component.title.getTag()).isEqualTo("span");
            assertThat(component.title.getTextContent()).isEqualTo("Title");

            assertThat(component.content).isNotNull();
            assertThat(component.content.getTag()).isEqualTo("p");
            assertThat(component.content.getTextContent()).isEqualTo("Content");
        }

        @Test
        @DisplayName("@HtmlId with explicit id value injects correct element")
        void htmlIdWithExplicitValue_injectsCorrectElement() {
            ComponentWithExplicitId component = new ComponentWithExplicitId();

            HtmlLoader.load(component);

            assertThat(component.myTitleField).isNotNull();
            assertThat(component.myTitleField.getTag()).isEqualTo("span");
            assertThat(component.myTitleField.getTextContent()).isEqualTo("Title");
        }

        @Test
        @DisplayName("@HtmlId with field name as default id resolves container")
        void htmlIdWithFieldNameDefault_resolvesContainer() {
            ComponentWithFieldNameAsId component = new ComponentWithFieldNameAsId();

            HtmlLoader.load(component);

            assertThat(component.container).isNotNull();
            assertThat(component.container.getTag()).isEqualTo("div");
            assertThat(component.container.getAttributes()).containsEntry("id", "container");
        }

        @Test
        @DisplayName("Multiple @HtmlId fields all injected correctly")
        void multipleHtmlIdFields_allInjectedCorrectly() {
            ComponentWithMultipleIds component = new ComponentWithMultipleIds();

            HtmlLoader.load(component);

            assertThat(component.container).isNotNull();
            assertThat(component.container.getTag()).isEqualTo("div");
            assertThat(component.title).isNotNull();
            assertThat(component.title.getTag()).isEqualTo("span");
            assertThat(component.content).isNotNull();
            assertThat(component.content.getTag()).isEqualTo("p");
        }

        @Test
        @DisplayName("@HtmlId finds deeply nested element by id")
        void htmlIdWithDeepNesting_findsNestedElement() {
            ComponentWithDeepId component = new ComponentWithDeepId();

            HtmlLoader.load(component);

            assertThat(component.deep).isNotNull();
            assertThat(component.deep.getTag()).isEqualTo("p");
            assertThat(component.deep.getTextContent()).isEqualTo("Deep");
        }

        @Test
        @DisplayName("Injected @HtmlId element is a live reference into the tree")
        void injectedElement_isLiveReferenceIntoTree() {
            ComponentWithIds component = new ComponentWithIds();

            Element root = HtmlLoader.load(component);

            /* Modify via injected reference. */
            component.title.text("Modified Title");

            /*
             * Walk the returned tree to find the span with id="title".
             * It should reflect the modification because it is the same object.
             */
            Element titleInTree = findById(root, "title");
            assertThat(titleInTree).isNotNull();
            assertThat(titleInTree.getTextContent()).isEqualTo("Modified Title");
        }
    }

    // ── @Slot injection ──────────────────────────────────────────

    @Nested
    @DisplayName("@Slot field injection")
    class SlotInjection {

        @Test
        @DisplayName("@Slot with explicit name injects correct element")
        void slotWithExplicitName_injectsCorrectElement() {
            ComponentWithSlots component = new ComponentWithSlots();

            HtmlLoader.load(component);

            assertThat(component.sidebarSlot).isNotNull();
            assertThat(component.sidebarSlot.getTag()).isEqualTo("aside");
            assertThat(component.sidebarSlot.getTextContent()).isEqualTo("Sidebar slot");
        }

        @Test
        @DisplayName("@Slot with default value maps to id='default'")
        void slotWithDefaultValue_mapsToDefaultId() {
            ComponentWithDefaultSlot component = new ComponentWithDefaultSlot();

            HtmlLoader.load(component);

            assertThat(component.defaultSlot).isNotNull();
            assertThat(component.defaultSlot.getTag()).isEqualTo("div");
            assertThat(component.defaultSlot.getTextContent()).isEqualTo("Default slot");
        }

        @Test
        @DisplayName("Multiple @Slot fields all injected correctly")
        void multipleSlotFields_allInjectedCorrectly() {
            ComponentWithSlotAndId component = new ComponentWithSlotAndId();

            HtmlLoader.load(component);

            assertThat(component.sidebar).isNotNull();
            assertThat(component.sidebar.getTag()).isEqualTo("aside");
            assertThat(component.defaultContent).isNotNull();
            assertThat(component.defaultContent.getTag()).isEqualTo("div");
        }
    }

    // ── Initializable callback ───────────────────────────────────

    @Nested
    @DisplayName("Initializable lifecycle callback")
    class InitializableCallback {

        @Test
        @DisplayName("initialize() called after injection for Initializable component")
        void initializableComponent_initializeCalledAfterInjection() {
            InitializableComponent component = new InitializableComponent();

            HtmlLoader.load(component);

            assertThat(component.initialized).isTrue();
        }

        @Test
        @DisplayName("Non-Initializable component does not fail")
        void nonInitializableComponent_doesNotFail() {
            SimpleComponent component = new SimpleComponent();

            Element result = HtmlLoader.load(component);

            assertThat(result).isNotNull();
        }
    }

    // ── Caching behavior ─────────────────────────────────────────

    @Nested
    @DisplayName("Caching behavior")
    class CachingBehavior {

        @Test
        @DisplayName("Cache populated after first load")
        void cachePopulatedAfterFirstLoad() {
            assertThat(HtmlLoader.getCache().size()).isZero();

            HtmlLoader.load(new SimpleComponent());

            assertThat(HtmlLoader.getCache().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Cache hit on second load for same template")
        void cacheHitOnSecondLoad_sameCacheSize() {
            HtmlLoader.load(new SimpleComponent());
            assertThat(HtmlLoader.getCache().size()).isEqualTo(1);

            HtmlLoader.load(new SimpleComponent());
            assertThat(HtmlLoader.getCache().size()).isEqualTo(1);
        }

        @Test
        @DisplayName("Each load returns independent element tree")
        void eachLoad_returnsIndependentTree() {
            SimpleComponent first = new SimpleComponent();
            SimpleComponent second = new SimpleComponent();

            Element tree1 = HtmlLoader.load(first);
            Element tree2 = HtmlLoader.load(second);

            assertThat(tree1).isNotSameAs(tree2);
        }
    }

    // ── Error handling ───────────────────────────────────────────

    @Nested
    @DisplayName("Error handling")
    class ErrorHandling {

        @Test
        @DisplayName("Null component throws NullPointerException")
        void nullComponent_throwsNullPointerException() {
            assertThatThrownBy(() -> HtmlLoader.load(null))
                    .isInstanceOf(NullPointerException.class);
        }

        @Test
        @DisplayName("Component without @Html throws TemplateException")
        void componentWithoutAnnotation_throwsTemplateException() {
            assertThatThrownBy(() -> HtmlLoader.load(new ComponentWithoutAnnotation()))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("@Html");
        }

        @Test
        @DisplayName("Missing template file throws TemplateException")
        void missingTemplateFile_throwsTemplateException() {
            assertThatThrownBy(() -> HtmlLoader.load(new ComponentWithMissingTemplate()))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("not found");
        }

        @Test
        @DisplayName("Missing id for @HtmlId throws TemplateException")
        void missingIdForHtmlId_throwsTemplateException() {
            assertThatThrownBy(() -> HtmlLoader.load(new ComponentWithMissingId()))
                    .isInstanceOf(TemplateException.class)
                    .hasMessageContaining("nonexistent")
                    .hasMessageContaining("@HtmlId");
        }
    }

    // ── getCache() accessor ──────────────────────────────────────

    @Nested
    @DisplayName("getCache() accessor")
    class GetCacheAccessor {

        @Test
        @DisplayName("getCache returns the shared TemplateCache instance")
        void getCache_returnsSharedInstance() {
            TemplateCache cache1 = HtmlLoader.getCache();
            TemplateCache cache2 = HtmlLoader.getCache();

            assertThat(cache1).isSameAs(cache2);
        }
    }

    // ── Helper methods ───────────────────────────────────────────

    /**
     * Recursively search an Element tree for an element with the given id attribute.
     */
    private static Element findById(Element root, String id) {
        if (root == null) {
            return null;
        }
        String rootId = root.getAttributes().get("id");
        if (id.equals(rootId)) {
            return root;
        }
        for (Element child : root.getChildren()) {
            Element found = findById(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }
}

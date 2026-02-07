package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link JuxAccordion} -- accessible accordion component.
 *
 * <p>Validates the rendered ARIA structure, trigger/panel cross-references,
 * default collapsed state, and constructor validation.</p>
 */
class JuxAccordionTest {

    // ── Helper methods ───────────────────────────────────────────

    private Element findByAttribute(Element root, String attr, String value) {
        if (value.equals(root.getAttributes().get(attr))) return root;
        for (Element child : root.getChildren()) {
            Element found = findByAttribute(child, attr, value);
            if (found != null) return found;
        }
        return null;
    }

    private List<Element> findAllByTag(Element root, String tag) {
        List<Element> results = new ArrayList<>();
        collectByTag(root, tag, results);
        return results;
    }

    private void collectByTag(Element el, String tag, List<Element> results) {
        if (tag.equals(el.getTag())) results.add(el);
        for (Element child : el.getChildren()) collectByTag(child, tag, results);
    }

    private List<Element> findAllByAttribute(Element root, String attr, String value) {
        List<Element> results = new ArrayList<>();
        collectByAttribute(root, attr, value, results);
        return results;
    }

    private void collectByAttribute(Element el, String attr, String value, List<Element> results) {
        if (value.equals(el.getAttributes().get(attr))) results.add(el);
        for (Element child : el.getChildren()) collectByAttribute(child, attr, value, results);
    }

    // ── Tests ────────────────────────────────────────────────────

    @Test
    void render_producesAccordionStructure() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        assertThat(rendered.getTag()).isEqualTo("div");
        assertThat(rendered.getCssClasses()).contains("accordion");
    }

    @Test
    void render_eachSectionTriggerHasAriaExpandedAttribute() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        // Triggers are buttons inside h3 elements
        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons).hasSize(2);

        for (Element button : buttons) {
            assertThat(button.getAttributes()).containsKey("aria-expanded");
        }
    }

    @Test
    void render_allSectionsCollapsedByDefault() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2")),
                new JuxAccordion.Section("Q3", p().text("A3"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons).allSatisfy(button ->
                assertThat(button.getAttributes().get("aria-expanded")).isEqualTo("false")
        );
    }

    @Test
    void render_eachTriggerHasAriaControlsPointingToPanelId() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        for (Element button : buttons) {
            String controlsId = button.getAttributes().get("aria-controls");
            assertThat(controlsId).isNotNull();
            assertThat(controlsId).startsWith("jux-accordion-");
            assertThat(controlsId).contains("-panel-");
        }
    }

    @Test
    void render_panelIdsMatchTriggerAriaControls() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        for (Element button : buttons) {
            String controlsId = button.getAttributes().get("aria-controls");
            // The panel with this ID should exist in the tree
            Element panel = findByAttribute(rendered, "id", controlsId);
            assertThat(panel)
                    .as("Panel with id '%s' should exist", controlsId)
                    .isNotNull();
        }
    }

    @Test
    void render_panelContentWrappedInAccordionBody() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        // Find the collapse div (panel), check it has an accordion-body child
        List<Element> buttons = findAllByTag(rendered, "button");
        String panelId = buttons.get(0).getAttributes().get("aria-controls");
        Element panel = findByAttribute(rendered, "id", panelId);
        assertThat(panel).isNotNull();

        // The panel should contain a div.accordion-body
        assertThat(panel.getChildren()).isNotEmpty();
        Element bodyDiv = panel.getChildren().get(0);
        assertThat(bodyDiv.getCssClasses()).contains("accordion-body");
    }

    @Test
    void render_triggersAreWrappedInH3Elements() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> headings = findAllByTag(rendered, "h3");
        assertThat(headings).hasSize(2);

        for (Element heading : headings) {
            assertThat(heading.getCssClasses()).contains("accordion-header");
            // Each h3 should contain a button child
            assertThat(heading.getChildren()).isNotEmpty();
            assertThat(heading.getChildren().get(0).getTag()).isEqualTo("button");
        }
    }

    @Test
    void render_triggerButtonsHaveCorrectText() {
        var sections = List.of(
                new JuxAccordion.Section("What is JUX?", p().text("A framework")),
                new JuxAccordion.Section("How to install?", p().text("Add dependency"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons.get(0).getTextContent()).isEqualTo("What is JUX?");
        assertThat(buttons.get(1).getTextContent()).isEqualTo("How to install?");
    }

    @Test
    void render_accordionItemsHaveCorrectStructure() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        // Root div has accordion-item children
        assertThat(rendered.getChildren()).isNotEmpty();
        Element firstItem = rendered.getChildren().get(0);
        assertThat(firstItem.getCssClasses()).contains("accordion-item");

        // Each item should have 2 children: h3 (header) and div (collapse)
        assertThat(firstItem.getChildren()).hasSize(2);
        assertThat(firstItem.getChildren().get(0).getTag()).isEqualTo("h3");
        assertThat(firstItem.getChildren().get(1).getTag()).isEqualTo("div");
    }

    @Test
    void render_panelsHaveCollapseClasses() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        String panelId = buttons.get(0).getAttributes().get("aria-controls");
        Element panel = findByAttribute(rendered, "id", panelId);

        assertThat(panel).isNotNull();
        assertThat(panel.getCssClasses()).contains("accordion-collapse", "collapse");
    }

    @Test
    void render_triggerButtonsHaveCollapsedClass() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons.get(0).getCssClasses()).contains("collapsed");
    }

    @Test
    void render_rootHasUniqueId() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        String rootId = rendered.getAttributes().get("id");
        assertThat(rootId).isNotNull();
        assertThat(rootId).startsWith("jux-accordion-");
    }

    @Test
    void render_panelsHaveDataBsParentReferringToRoot() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1"))
        );
        Element rendered = new JuxAccordion(sections).render();

        String rootId = rendered.getAttributes().get("id");
        List<Element> buttons = findAllByTag(rendered, "button");
        String panelId = buttons.get(0).getAttributes().get("aria-controls");
        Element panel = findByAttribute(rendered, "id", panelId);

        assertThat(panel).isNotNull();
        assertThat(panel.getAttributes().get("data-bs-parent")).isEqualTo("#" + rootId);
    }

    @Test
    void constructor_emptyListThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxAccordion(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_nullListThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxAccordion(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_multipleSectionsProduceCorrectNumberOfItems() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2")),
                new JuxAccordion.Section("Q3", p().text("A3")),
                new JuxAccordion.Section("Q4", p().text("A4"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons).hasSize(4);
    }

    @Test
    void render_headerIdsAndPanelIdsAreUnique() {
        var sections = List.of(
                new JuxAccordion.Section("Q1", p().text("A1")),
                new JuxAccordion.Section("Q2", p().text("A2"))
        );
        Element rendered = new JuxAccordion(sections).render();

        List<Element> headings = findAllByTag(rendered, "h3");
        List<String> headerIds = headings.stream()
                .map(h -> h.getAttributes().get("id"))
                .toList();
        assertThat(headerIds).doesNotHaveDuplicates();

        List<Element> buttons = findAllByTag(rendered, "button");
        List<String> panelIds = buttons.stream()
                .map(b -> b.getAttributes().get("aria-controls"))
                .toList();
        assertThat(panelIds).doesNotHaveDuplicates();
    }
}

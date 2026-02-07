package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link JuxTabs} -- accessible tabbed content component.
 *
 * <p>Validates the rendered ARIA structure, tab/panel cross-references,
 * selection state, and constructor validation.</p>
 */
class JuxTabsTest {

    // ── Helper methods ───────────────────────────────────────────

    private Element findByAttribute(Element root, String attr, String value) {
        if (value.equals(root.getAttributes().get(attr))) return root;
        for (Element child : root.getChildren()) {
            Element found = findByAttribute(child, attr, value);
            if (found != null) return found;
        }
        return null;
    }

    private Element findByTag(Element root, String tag) {
        if (tag.equals(root.getTag())) return root;
        for (Element child : root.getChildren()) {
            Element found = findByTag(child, tag);
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
    void render_containsTablistRole() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        Element tablist = findByAttribute(rendered, "role", "tablist");
        assertThat(tablist).isNotNull();
    }

    @Test
    void render_eachTabButtonHasRoleTab() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B")),
                new JuxTabs.Tab("Tab C", p().text("Content C"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        assertThat(tabButtons).hasSize(3);
    }

    @Test
    void render_eachPanelHasRoleTabpanel() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");
        assertThat(panels).hasSize(2);
    }

    @Test
    void render_firstTabIsSelected() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        assertThat(tabButtons.get(0).getAttributes().get("aria-selected")).isEqualTo("true");
    }

    @Test
    void render_otherTabsAreNotSelected() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B")),
                new JuxTabs.Tab("Tab C", p().text("Content C"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        assertThat(tabButtons.get(1).getAttributes().get("aria-selected")).isEqualTo("false");
        assertThat(tabButtons.get(2).getAttributes().get("aria-selected")).isEqualTo("false");
    }

    @Test
    void render_panelsHaveAriaLabelledbyPointingToTabId() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");

        for (int i = 0; i < tabs.size(); i++) {
            String tabId = tabButtons.get(i).getAttributes().get("id");
            String labelledBy = panels.get(i).getAttributes().get("aria-labelledby");
            assertThat(labelledBy).isEqualTo(tabId);
        }
    }

    @Test
    void render_tabsHaveAriaControlsPointingToPanelId() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");

        for (int i = 0; i < tabs.size(); i++) {
            String panelId = panels.get(i).getAttributes().get("id");
            String controls = tabButtons.get(i).getAttributes().get("aria-controls");
            assertThat(controls).isEqualTo(panelId);
        }
    }

    @Test
    void render_tabAndPanelIdsAreConsistentlyGenerated() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");

        String tabId = tabButtons.get(0).getAttributes().get("id");
        String panelId = panels.get(0).getAttributes().get("id");

        // Both IDs share the same prefix pattern
        assertThat(tabId).startsWith("jux-tabs-");
        assertThat(panelId).startsWith("jux-tabs-");
        assertThat(tabId).contains("-tab-0");
        assertThat(panelId).contains("-panel-0");
    }

    @Test
    void render_tabButtonsHaveCorrectText() {
        var tabs = List.of(
                new JuxTabs.Tab("Overview", p().text("Content A")),
                new JuxTabs.Tab("Specs", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        assertThat(tabButtons.get(0).getTextContent()).isEqualTo("Overview");
        assertThat(tabButtons.get(1).getTextContent()).isEqualTo("Specs");
    }

    @Test
    void render_tabButtonsAreButtonElements() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        assertThat(tabButtons).allSatisfy(btn ->
                assertThat(btn.getTag()).isEqualTo("button")
        );
    }

    @Test
    void render_panelsHaveTabindex0ForFocusability() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");
        assertThat(panels).allSatisfy(panel ->
                assertThat(panel.getAttributes().get("tabindex")).isEqualTo("0")
        );
    }

    @Test
    void render_firstTabPanelHasShowActiveClasses() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");
        assertThat(panels.get(0).getCssClasses()).contains("show", "active");
    }

    @Test
    void render_nonFirstPanelsDoNotHaveShowActiveClasses() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A")),
                new JuxTabs.Tab("Tab B", p().text("Content B"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");
        assertThat(panels.get(1).getCssClasses()).doesNotContain("show", "active");
    }

    @Test
    void constructor_emptyListThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxTabs(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_nullListThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxTabs(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_singleTabWorks() {
        var tabs = List.of(
                new JuxTabs.Tab("Only Tab", p().text("Only Content"))
        );
        Element rendered = new JuxTabs(tabs).render();

        List<Element> tabButtons = findAllByAttribute(rendered, "role", "tab");
        List<Element> panels = findAllByAttribute(rendered, "role", "tabpanel");

        assertThat(tabButtons).hasSize(1);
        assertThat(panels).hasSize(1);
        assertThat(tabButtons.get(0).getAttributes().get("aria-selected")).isEqualTo("true");
    }

    @Test
    void render_tabListItemsHaveRolePresentation() {
        var tabs = List.of(
                new JuxTabs.Tab("Tab A", p().text("Content A"))
        );
        Element rendered = new JuxTabs(tabs).render();

        Element tablist = findByAttribute(rendered, "role", "tablist");
        assertThat(tablist).isNotNull();

        // The tablist is a <ul>; its direct children are <li> with role="presentation"
        List<Element> liItems = tablist.getChildren();
        assertThat(liItems).allSatisfy(li ->
                assertThat(li.getAttributes().get("role")).isEqualTo("presentation")
        );
    }
}

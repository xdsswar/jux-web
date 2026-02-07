package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxDropdown} -- accessible dropdown menu component.
 *
 * <p>Validates the rendered ARIA menu button structure, trigger attributes,
 * menu items, and constructor validation.</p>
 */
class JuxDropdownTest {

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

    // ── Tests ────────────────────────────────────────────────────

    @Test
    void render_containsTriggerButton() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile"),
                new JuxDropdown.DropdownItem("Settings", "/settings")
        );
        Element rendered = new JuxDropdown("Account", items).render();

        Element button = findByTag(rendered, "button");
        assertThat(button).isNotNull();
        assertThat(button.getTextContent()).isEqualTo("Account");
    }

    @Test
    void render_triggerButtonHasAriaExpandedFalse() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        Element button = findByTag(rendered, "button");
        assertThat(button).isNotNull();
        assertThat(button.getAttributes().get("aria-expanded")).isEqualTo("false");
    }

    @Test
    void render_triggerButtonHasDataBsToggleDropdown() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        Element button = findByTag(rendered, "button");
        assertThat(button).isNotNull();
        assertThat(button.getAttributes().get("data-bs-toggle")).isEqualTo("dropdown");
    }

    @Test
    void render_menuContainerIsUnorderedList() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        Element menu = findByTag(rendered, "ul");
        assertThat(menu).isNotNull();
        assertThat(menu.getCssClasses()).contains("dropdown-menu");
    }

    @Test
    void render_menuItemsAreListItemsWithLinks() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile"),
                new JuxDropdown.DropdownItem("Settings", "/settings"),
                new JuxDropdown.DropdownItem("Sign Out", "/logout")
        );
        Element rendered = new JuxDropdown("Account", items).render();

        Element menu = findByTag(rendered, "ul");
        assertThat(menu).isNotNull();

        List<Element> listItems = menu.getChildren();
        assertThat(listItems).hasSize(3);

        for (Element li : listItems) {
            assertThat(li.getTag()).isEqualTo("li");
            assertThat(li.getChildren()).isNotEmpty();
            Element link = li.getChildren().get(0);
            assertThat(link.getTag()).isEqualTo("a");
            assertThat(link.getCssClasses()).contains("dropdown-item");
        }
    }

    @Test
    void render_menuItemLinksHaveCorrectHrefsAndLabels() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile"),
                new JuxDropdown.DropdownItem("Settings", "/settings")
        );
        Element rendered = new JuxDropdown("Account", items).render();

        List<Element> links = findAllByTag(rendered, "a");
        assertThat(links).hasSize(2);

        assertThat(links.get(0).getAttributes().get("href")).isEqualTo("/profile");
        assertThat(links.get(0).getTextContent()).isEqualTo("Profile");

        assertThat(links.get(1).getAttributes().get("href")).isEqualTo("/settings");
        assertThat(links.get(1).getTextContent()).isEqualTo("Settings");
    }

    @Test
    void render_rootContainerHasDropdownClass() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        assertThat(rendered.getTag()).isEqualTo("div");
        assertThat(rendered.getCssClasses()).contains("dropdown");
    }

    @Test
    void render_triggerButtonHasDropdownToggleClass() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        Element button = findByTag(rendered, "button");
        assertThat(button).isNotNull();
        assertThat(button.getCssClasses()).contains("dropdown-toggle");
    }

    @Test
    void render_triggerButtonHasTypeButton() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Profile", "/profile")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        Element button = findByTag(rendered, "button");
        assertThat(button).isNotNull();
        assertThat(button.getAttributes().get("type")).isEqualTo("button");
    }

    @Test
    void constructor_emptyItemsThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxDropdown("Menu", List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_nullItemsThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxDropdown("Menu", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullLabelThrowsNullPointerException() {
        var items = List.of(new JuxDropdown.DropdownItem("Profile", "/profile"));
        assertThatThrownBy(() -> new JuxDropdown(null, items))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void dropdownItem_nullLabelThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxDropdown.DropdownItem(null, "/profile"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void dropdownItem_nullHrefThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxDropdown.DropdownItem("Profile", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_singleItemWorks() {
        var items = List.of(
                new JuxDropdown.DropdownItem("Only Item", "/only")
        );
        Element rendered = new JuxDropdown("Menu", items).render();

        List<Element> links = findAllByTag(rendered, "a");
        assertThat(links).hasSize(1);
        assertThat(links.get(0).getTextContent()).isEqualTo("Only Item");
    }
}

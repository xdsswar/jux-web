package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxBreadcrumb} -- accessible breadcrumb navigation component.
 *
 * <p>Validates the rendered ARIA landmark, ordered list structure,
 * current-page indication, and constructor validation.</p>
 */
class JuxBreadcrumbTest {

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
    void render_returnsNavElement() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("About", "/about")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        assertThat(rendered.getTag()).isEqualTo("nav");
    }

    @Test
    void render_navHasAriaLabelBreadcrumb() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("About", "/about")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        assertThat(rendered.getAttributes().get("aria-label")).isEqualTo("Breadcrumb");
    }

    @Test
    void render_containsOrderedList() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("About", "/about")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        Element ol = findByTag(rendered, "ol");
        assertThat(ol).isNotNull();
        assertThat(ol.getCssClasses()).contains("jux-breadcrumb-list");
    }

    @Test
    void render_lastCrumbIsSpanWithAriaCurrentPage() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("Products", "/products"),
                new JuxBreadcrumb.Crumb("Widget", "/products/widget")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        Element currentPage = findByAttribute(rendered, "aria-current", "page");
        assertThat(currentPage).isNotNull();
        assertThat(currentPage.getTag()).isEqualTo("span");
        assertThat(currentPage.getTextContent()).isEqualTo("Widget");
    }

    @Test
    void render_intermediateCrumbsAreLinks() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("Products", "/products"),
                new JuxBreadcrumb.Crumb("Widget", "/products/widget")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        List<Element> links = findAllByTag(rendered, "a");
        assertThat(links).hasSize(2);

        assertThat(links.get(0).getAttributes().get("href")).isEqualTo("/");
        assertThat(links.get(0).getTextContent()).isEqualTo("Home");

        assertThat(links.get(1).getAttributes().get("href")).isEqualTo("/products");
        assertThat(links.get(1).getTextContent()).isEqualTo("Products");
    }

    @Test
    void render_singleCrumbWorksAsCurrentPage() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        Element currentPage = findByAttribute(rendered, "aria-current", "page");
        assertThat(currentPage).isNotNull();
        assertThat(currentPage.getTag()).isEqualTo("span");
        assertThat(currentPage.getTextContent()).isEqualTo("Home");

        // No links should exist since the only crumb is the current page
        List<Element> links = findAllByTag(rendered, "a");
        assertThat(links).isEmpty();
    }

    @Test
    void render_listItemsHaveCssClass() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("About", "/about")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        List<Element> listItems = findAllByTag(rendered, "li");
        assertThat(listItems).hasSize(2);
        assertThat(listItems).allSatisfy(li ->
                assertThat(li.getCssClasses()).contains("jux-breadcrumb-item")
        );
    }

    @Test
    void render_navHasBreadcrumbCssClass() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        assertThat(rendered.getCssClasses()).contains("jux-breadcrumb");
    }

    @Test
    void render_correctNumberOfListItems() {
        var crumbs = List.of(
                new JuxBreadcrumb.Crumb("Home", "/"),
                new JuxBreadcrumb.Crumb("Blog", "/blog"),
                new JuxBreadcrumb.Crumb("Post", "/blog/post"),
                new JuxBreadcrumb.Crumb("Comment", "/blog/post/comment")
        );
        Element rendered = new JuxBreadcrumb(crumbs).render();

        List<Element> listItems = findAllByTag(rendered, "li");
        assertThat(listItems).hasSize(4);
    }

    @Test
    void constructor_emptyListThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxBreadcrumb(List.of()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_nullListThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxBreadcrumb(null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void crumb_nullLabelThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxBreadcrumb.Crumb(null, "/"))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void crumb_nullHrefIsAllowed() {
        // Null href is allowed for the current page crumb
        var crumb = new JuxBreadcrumb.Crumb("Current Page", null);
        assertThat(crumb.label()).isEqualTo("Current Page");
        assertThat(crumb.href()).isNull();
    }
}

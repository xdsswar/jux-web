package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxPagination} -- accessible pagination navigation component.
 *
 * <p>Validates the rendered ARIA landmark, current page indication,
 * previous/next button states, ellipsis rendering, and constructor validation.</p>
 */
class JuxPaginationTest {

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
    void render_returnsNavElement() {
        Element rendered = new JuxPagination(1, 5, "/page=").render();

        assertThat(rendered.getTag()).isEqualTo("nav");
    }

    @Test
    void render_navHasAriaLabelPagination() {
        Element rendered = new JuxPagination(1, 5, "/page=").render();

        assertThat(rendered.getAttributes().get("aria-label")).isEqualTo("Pagination");
    }

    @Test
    void render_currentPageLinkHasAriaCurrentPage() {
        Element rendered = new JuxPagination(3, 5, "/page=").render();

        Element currentLink = findByAttribute(rendered, "aria-current", "page");
        assertThat(currentLink).isNotNull();
        assertThat(currentLink.getTextContent()).isEqualTo("3");
    }

    @Test
    void render_currentPageLinkHasCorrectAriaLabel() {
        Element rendered = new JuxPagination(3, 5, "/page=").render();

        Element currentLink = findByAttribute(rendered, "aria-current", "page");
        assertThat(currentLink).isNotNull();
        assertThat(currentLink.getAttributes().get("aria-label")).isEqualTo("Page 3, current page");
    }

    @Test
    void render_previousButtonDisabledOnFirstPage() {
        Element rendered = new JuxPagination(1, 5, "/page=").render();

        // The first li in the pagination list is the previous button
        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        Element firstLi = ul.getChildren().get(0);
        assertThat(firstLi.getCssClasses()).contains("disabled");

        // The disabled previous uses a <span> instead of <a>
        Element prevChild = firstLi.getChildren().get(0);
        assertThat(prevChild.getTag()).isEqualTo("span");
        assertThat(prevChild.getTextContent()).isEqualTo("Previous");
    }

    @Test
    void render_previousButtonEnabledOnNonFirstPage() {
        Element rendered = new JuxPagination(2, 5, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        Element firstLi = ul.getChildren().get(0);
        assertThat(firstLi.getCssClasses()).doesNotContain("disabled");

        // Active previous uses an <a> element
        Element prevLink = firstLi.getChildren().get(0);
        assertThat(prevLink.getTag()).isEqualTo("a");
        assertThat(prevLink.getAttributes().get("href")).isEqualTo("/page=1");
    }

    @Test
    void render_nextButtonDisabledOnLastPage() {
        Element rendered = new JuxPagination(5, 5, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        // Last li is the next button
        List<Element> liItems = ul.getChildren();
        Element lastLi = liItems.get(liItems.size() - 1);
        assertThat(lastLi.getCssClasses()).contains("disabled");

        Element nextChild = lastLi.getChildren().get(0);
        assertThat(nextChild.getTag()).isEqualTo("span");
        assertThat(nextChild.getTextContent()).isEqualTo("Next");
    }

    @Test
    void render_nextButtonEnabledOnNonLastPage() {
        Element rendered = new JuxPagination(3, 5, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        List<Element> liItems = ul.getChildren();
        Element lastLi = liItems.get(liItems.size() - 1);
        assertThat(lastLi.getCssClasses()).doesNotContain("disabled");

        Element nextLink = lastLi.getChildren().get(0);
        assertThat(nextLink.getTag()).isEqualTo("a");
        assertThat(nextLink.getAttributes().get("href")).isEqualTo("/page=4");
    }

    @Test
    void render_singlePageHasBothPrevAndNextDisabled() {
        Element rendered = new JuxPagination(1, 1, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        List<Element> liItems = ul.getChildren();

        // First li = previous, should be disabled
        Element prevLi = liItems.get(0);
        assertThat(prevLi.getCssClasses()).contains("disabled");

        // Last li = next, should be disabled
        Element nextLi = liItems.get(liItems.size() - 1);
        assertThat(nextLi.getCssClasses()).contains("disabled");
    }

    @Test
    void render_smallPageCount_allPagesShown() {
        // For 7 or fewer pages, all page numbers should be visible
        Element rendered = new JuxPagination(3, 5, "/page=").render();

        // Count page number links (excluding prev/next)
        List<Element> links = findAllByTag(rendered, "a");
        // Should have links for: prev(enabled), 1, 2, 3(current), 4, 5, next(enabled)
        // But current page is also an <a> with aria-current
        long pageLinks = links.stream()
                .filter(a -> a.getAttributes().get("aria-label") != null
                        && a.getAttributes().get("aria-label").startsWith("Page "))
                .count();
        assertThat(pageLinks).isEqualTo(5);
    }

    @Test
    void render_largePageCount_ellipsisAppears() {
        // For more than 7 pages, ellipsis should appear
        Element rendered = new JuxPagination(5, 20, "/page=").render();

        // Find spans with ellipsis character
        List<Element> spans = findAllByTag(rendered, "span");
        boolean hasEllipsis = spans.stream()
                .anyMatch(s -> "\u2026".equals(s.getTextContent()));
        assertThat(hasEllipsis).isTrue();
    }

    @Test
    void render_largePageCount_firstAndLastAlwaysShown() {
        Element rendered = new JuxPagination(10, 20, "/page=").render();

        // Page 1 link should exist
        List<Element> allLinks = findAllByTag(rendered, "a");
        boolean hasPage1 = allLinks.stream()
                .anyMatch(a -> "Page 1".equals(a.getAttributes().get("aria-label")));
        assertThat(hasPage1).isTrue();

        // Page 20 link should exist
        boolean hasPage20 = allLinks.stream()
                .anyMatch(a -> "Page 20".equals(a.getAttributes().get("aria-label")));
        assertThat(hasPage20).isTrue();
    }

    @Test
    void render_largePageCount_currentAndAdjacentPagesShown() {
        Element rendered = new JuxPagination(10, 20, "/page=").render();

        List<Element> allLinks = findAllByTag(rendered, "a");

        // Page 9 (current-1) should exist
        boolean hasPage9 = allLinks.stream()
                .anyMatch(a -> "Page 9".equals(a.getAttributes().get("aria-label")));
        assertThat(hasPage9).isTrue();

        // Page 10 (current) should exist
        boolean hasPage10 = allLinks.stream()
                .anyMatch(a -> {
                    String label = a.getAttributes().get("aria-label");
                    return label != null && label.startsWith("Page 10");
                });
        assertThat(hasPage10).isTrue();

        // Page 11 (current+1) should exist
        boolean hasPage11 = allLinks.stream()
                .anyMatch(a -> "Page 11".equals(a.getAttributes().get("aria-label")));
        assertThat(hasPage11).isTrue();
    }

    @Test
    void render_ellipsisIsAriaHidden() {
        Element rendered = new JuxPagination(5, 20, "/page=").render();

        List<Element> spans = findAllByTag(rendered, "span");
        List<Element> ellipsisSpans = spans.stream()
                .filter(s -> "\u2026".equals(s.getTextContent()))
                .toList();

        assertThat(ellipsisSpans).isNotEmpty();
        for (Element ellipsis : ellipsisSpans) {
            assertThat(ellipsis.getAttributes().get("aria-hidden")).isEqualTo("true");
        }
    }

    @Test
    void render_currentPageLiHasActiveClass() {
        Element rendered = new JuxPagination(3, 5, "/page=").render();

        Element currentLink = findByAttribute(rendered, "aria-current", "page");
        assertThat(currentLink).isNotNull();

        // The parent <li> of the current page link should have "active" class
        // We check by finding li elements with "active" class
        Element ul = findByTag(rendered, "ul");
        assertThat(ul).isNotNull();

        boolean hasActiveLi = ul.getChildren().stream()
                .anyMatch(li -> li.getCssClasses().contains("active"));
        assertThat(hasActiveLi).isTrue();
    }

    @Test
    void render_previousLinkHasAriaLabel() {
        Element rendered = new JuxPagination(2, 5, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        Element prevLi = ul.getChildren().get(0);
        Element prevChild = prevLi.getChildren().get(0);

        assertThat(prevChild.getAttributes().get("aria-label")).isEqualTo("Previous page");
    }

    @Test
    void render_nextLinkHasAriaLabel() {
        Element rendered = new JuxPagination(2, 5, "/page=").render();

        Element ul = findByTag(rendered, "ul");
        List<Element> liItems = ul.getChildren();
        Element nextLi = liItems.get(liItems.size() - 1);
        Element nextChild = nextLi.getChildren().get(0);

        assertThat(nextChild.getAttributes().get("aria-label")).isEqualTo("Next page");
    }

    @Test
    void constructor_nullBaseUrlThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxPagination(1, 5, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_totalPagesLessThan1ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxPagination(1, 0, "/page="))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_currentPageLessThan1ThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxPagination(0, 5, "/page="))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void constructor_currentPageGreaterThanTotalThrowsIllegalArgumentException() {
        assertThatThrownBy(() -> new JuxPagination(6, 5, "/page="))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void render_sevenPagesShowsAllWithoutEllipsis() {
        Element rendered = new JuxPagination(4, 7, "/page=").render();

        List<Element> spans = findAllByTag(rendered, "span");
        boolean hasEllipsis = spans.stream()
                .anyMatch(s -> "\u2026".equals(s.getTextContent()));
        assertThat(hasEllipsis).isFalse();

        // All 7 page links should be present
        List<Element> links = findAllByTag(rendered, "a");
        long pageLinks = links.stream()
                .filter(a -> {
                    String label = a.getAttributes().get("aria-label");
                    return label != null && label.startsWith("Page ");
                })
                .count();
        assertThat(pageLinks).isEqualTo(7);
    }

    @Test
    void render_eightPagesShowsEllipsis() {
        Element rendered = new JuxPagination(4, 8, "/page=").render();

        List<Element> spans = findAllByTag(rendered, "span");
        boolean hasEllipsis = spans.stream()
                .anyMatch(s -> "\u2026".equals(s.getTextContent()));
        assertThat(hasEllipsis).isTrue();
    }
}

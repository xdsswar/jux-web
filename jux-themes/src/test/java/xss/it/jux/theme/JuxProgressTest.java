package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxProgress} -- accessible progress bar component.
 *
 * <p>Validates the rendered ARIA progressbar role, value attributes,
 * label, value clamping, and constructor validation.</p>
 */
class JuxProgressTest {

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

    // ── Tests ────────────────────────────────────────────────────

    @Test
    void render_hasRoleProgressbar() {
        Element rendered = new JuxProgress(75, "Upload progress").render();

        assertThat(rendered.getAttributes().get("role")).isEqualTo("progressbar");
    }

    @Test
    void render_hasAriaValuenowSetToValue() {
        Element rendered = new JuxProgress(75, "Upload progress").render();

        assertThat(rendered.getAttributes().get("aria-valuenow")).isEqualTo("75");
    }

    @Test
    void render_hasAriaValueminZero() {
        Element rendered = new JuxProgress(50, "Loading").render();

        assertThat(rendered.getAttributes().get("aria-valuemin")).isEqualTo("0");
    }

    @Test
    void render_hasAriaValuemax100() {
        Element rendered = new JuxProgress(50, "Loading").render();

        assertThat(rendered.getAttributes().get("aria-valuemax")).isEqualTo("100");
    }

    @Test
    void render_hasAriaLabelSetToLabel() {
        Element rendered = new JuxProgress(50, "Upload progress").render();

        assertThat(rendered.getAttributes().get("aria-label")).isEqualTo("Upload progress");
    }

    @Test
    void render_valueClamped_negativeBecomes0() {
        Element rendered = new JuxProgress(-10, "Progress").render();

        assertThat(rendered.getAttributes().get("aria-valuenow")).isEqualTo("0");
    }

    @Test
    void render_valueClamped_over100Becomes100() {
        Element rendered = new JuxProgress(150, "Progress").render();

        assertThat(rendered.getAttributes().get("aria-valuenow")).isEqualTo("100");
    }

    @Test
    void render_value0Works() {
        Element rendered = new JuxProgress(0, "Progress").render();

        assertThat(rendered.getAttributes().get("aria-valuenow")).isEqualTo("0");
    }

    @Test
    void render_value100Works() {
        Element rendered = new JuxProgress(100, "Complete").render();

        assertThat(rendered.getAttributes().get("aria-valuenow")).isEqualTo("100");
    }

    @Test
    void render_containsFillElement() {
        Element rendered = new JuxProgress(60, "Loading").render();

        assertThat(rendered.getChildren()).isNotEmpty();

        Element fill = rendered.getChildren().get(0);
        assertThat(fill.getCssClasses()).contains("jux-progress-fill");
    }

    @Test
    void render_fillElementHasCorrectWidthStyle() {
        Element rendered = new JuxProgress(60, "Loading").render();

        Element fill = rendered.getChildren().get(0);
        assertThat(fill.getStyles().get("width")).isEqualTo("60%");
    }

    @Test
    void render_fillElementWidthClampedForNegativeValue() {
        Element rendered = new JuxProgress(-20, "Loading").render();

        Element fill = rendered.getChildren().get(0);
        assertThat(fill.getStyles().get("width")).isEqualTo("0%");
    }

    @Test
    void render_fillElementWidthClampedForOverValue() {
        Element rendered = new JuxProgress(200, "Loading").render();

        Element fill = rendered.getChildren().get(0);
        assertThat(fill.getStyles().get("width")).isEqualTo("100%");
    }

    @Test
    void render_fillElementIsAriaHidden() {
        Element rendered = new JuxProgress(50, "Loading").render();

        Element fill = rendered.getChildren().get(0);
        assertThat(fill.getAttributes().get("aria-hidden")).isEqualTo("true");
    }

    @Test
    void render_rootElementHasProgressCssClass() {
        Element rendered = new JuxProgress(50, "Loading").render();

        assertThat(rendered.getCssClasses()).contains("jux-progress");
    }

    @Test
    void render_rootElementIsDiv() {
        Element rendered = new JuxProgress(50, "Loading").render();

        assertThat(rendered.getTag()).isEqualTo("div");
    }

    @Test
    void constructor_nullLabelThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxProgress(50, null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_differentValuesProduceDifferentOutput() {
        Element rendered25 = new JuxProgress(25, "Progress").render();
        Element rendered75 = new JuxProgress(75, "Progress").render();

        assertThat(rendered25.getAttributes().get("aria-valuenow")).isEqualTo("25");
        assertThat(rendered75.getAttributes().get("aria-valuenow")).isEqualTo("75");

        Element fill25 = rendered25.getChildren().get(0);
        Element fill75 = rendered75.getChildren().get(0);
        assertThat(fill25.getStyles().get("width")).isEqualTo("25%");
        assertThat(fill75.getStyles().get("width")).isEqualTo("75%");
    }
}

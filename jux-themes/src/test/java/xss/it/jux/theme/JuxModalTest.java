package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link JuxModal} -- accessible modal dialog component.
 *
 * <p>Validates the rendered ARIA dialog structure, labelledby reference,
 * hidden state, close button, and constructor validation.</p>
 */
class JuxModalTest {

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
    void render_rootHasAriaHiddenTrue() {
        Element rendered = new JuxModal("Test Title", p().text("Test content")).render();

        // The root div is the modal container with aria-hidden="true"
        assertThat(rendered.getAttributes().get("aria-hidden")).isEqualTo("true");
    }

    @Test
    void render_rootHasAriaLabelledbyPointingToTitleId() {
        JuxModal modal = new JuxModal("Test Title", p().text("Test content"));
        Element rendered = modal.render();

        String labelledBy = rendered.getAttributes().get("aria-labelledby");
        assertThat(labelledBy).isNotNull();
        assertThat(labelledBy).startsWith("jux-modal-title-");

        // The title element with matching id should exist
        Element titleElement = findByAttribute(rendered, "id", labelledBy);
        assertThat(titleElement).isNotNull();
        assertThat(titleElement.getTag()).isEqualTo("h2");
        assertThat(titleElement.getTextContent()).isEqualTo("Test Title");
    }

    @Test
    void render_containsCloseButton() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        List<Element> buttons = findAllByTag(rendered, "button");
        assertThat(buttons).isNotEmpty();

        // Find the close button -- it has aria-label="Close" or data-bs-dismiss="modal"
        Element closeButton = null;
        for (Element button : buttons) {
            if ("modal".equals(button.getAttributes().get("data-bs-dismiss"))) {
                closeButton = button;
                break;
            }
        }
        assertThat(closeButton).isNotNull();
        assertThat(closeButton.getAttributes().get("aria-label")).isEqualTo("Close");
    }

    @Test
    void render_dialogIdMatchesGetDialogId() {
        JuxModal modal = new JuxModal("Title", p().text("Content"));
        Element rendered = modal.render();

        assertThat(rendered.getAttributes().get("id")).isEqualTo(modal.getDialogId());
    }

    @Test
    void getDialogId_returnsNonNullString() {
        JuxModal modal = new JuxModal("Title", p().text("Content"));

        assertThat(modal.getDialogId()).isNotNull();
        assertThat(modal.getDialogId()).startsWith("jux-modal-");
    }

    @Test
    void render_hasModalCssClass() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        assertThat(rendered.getCssClasses()).contains("modal", "fade");
    }

    @Test
    void render_hasTabindexNegativeOne() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        assertThat(rendered.getAttributes().get("tabindex")).isEqualTo("-1");
    }

    @Test
    void render_titleHasModalTitleClass() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        Element h2 = findByTag(rendered, "h2");
        assertThat(h2).isNotNull();
        assertThat(h2.getCssClasses()).contains("modal-title");
    }

    @Test
    void render_containsModalHeaderSection() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        // Look for a div with modal-header class
        List<Element> divs = findAllByTag(rendered, "div");
        Element headerDiv = divs.stream()
                .filter(d -> d.getCssClasses().contains("modal-header"))
                .findFirst()
                .orElse(null);
        assertThat(headerDiv).isNotNull();
    }

    @Test
    void render_containsModalBodySection() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        List<Element> divs = findAllByTag(rendered, "div");
        Element bodyDiv = divs.stream()
                .filter(d -> d.getCssClasses().contains("modal-body"))
                .findFirst()
                .orElse(null);
        assertThat(bodyDiv).isNotNull();
    }

    @Test
    void render_bodyContainsProvidedContent() {
        Element content = p().text("This is the modal body content.");
        Element rendered = new JuxModal("Title", content).render();

        List<Element> divs = findAllByTag(rendered, "div");
        Element bodyDiv = divs.stream()
                .filter(d -> d.getCssClasses().contains("modal-body"))
                .findFirst()
                .orElse(null);
        assertThat(bodyDiv).isNotNull();
        assertThat(bodyDiv.getChildren()).isNotEmpty();

        Element bodyChild = bodyDiv.getChildren().get(0);
        assertThat(bodyChild.getTag()).isEqualTo("p");
        assertThat(bodyChild.getTextContent()).isEqualTo("This is the modal body content.");
    }

    @Test
    void render_containsModalDialogAndModalContentWrappers() {
        Element rendered = new JuxModal("Title", p().text("Content")).render();

        List<Element> divs = findAllByTag(rendered, "div");
        assertThat(divs.stream().anyMatch(d -> d.getCssClasses().contains("modal-dialog"))).isTrue();
        assertThat(divs.stream().anyMatch(d -> d.getCssClasses().contains("modal-content"))).isTrue();
    }

    @Test
    void constructor_nullTitleThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxModal(null, p().text("Content")))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullContentThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxModal("Title", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_twoModalsHaveDifferentIds() {
        JuxModal modal1 = new JuxModal("First", p().text("Content 1"));
        JuxModal modal2 = new JuxModal("Second", p().text("Content 2"));

        assertThat(modal1.getDialogId()).isNotEqualTo(modal2.getDialogId());
    }
}

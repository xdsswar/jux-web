package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for {@link JuxToast} -- accessible toast notification component.
 *
 * <p>Validates the rendered ARIA alert semantics, live region attributes,
 * message content, toast type CSS classes, and constructor validation.</p>
 */
class JuxToastTest {

    // ── Helper methods ───────────────────────────────────────────

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
    void render_hasRoleAlert() {
        Element rendered = new JuxToast("Saved!", JuxToast.ToastType.SUCCESS).render();

        assertThat(rendered.getAttributes().get("role")).isEqualTo("alert");
    }

    @Test
    void render_hasAriaLiveAssertive() {
        Element rendered = new JuxToast("Saved!", JuxToast.ToastType.SUCCESS).render();

        assertThat(rendered.getAttributes().get("aria-live")).isEqualTo("assertive");
    }

    @Test
    void render_hasAriaAtomicTrue() {
        Element rendered = new JuxToast("Saved!", JuxToast.ToastType.SUCCESS).render();

        assertThat(rendered.getAttributes().get("aria-atomic")).isEqualTo("true");
    }

    @Test
    void render_containsMessageText() {
        Element rendered = new JuxToast("Settings saved successfully.", JuxToast.ToastType.SUCCESS).render();

        Element message = findByTag(rendered, "p");
        assertThat(message).isNotNull();
        assertThat(message.getTextContent()).isEqualTo("Settings saved successfully.");
    }

    @Test
    void render_messageHasCorrectCssClass() {
        Element rendered = new JuxToast("Error!", JuxToast.ToastType.ERROR).render();

        Element message = findByTag(rendered, "p");
        assertThat(message).isNotNull();
        assertThat(message.getCssClasses()).contains("jux-toast-message");
    }

    @Test
    void render_successToastHasCorrectCssClass() {
        Element rendered = new JuxToast("Done!", JuxToast.ToastType.SUCCESS).render();

        assertThat(rendered.getCssClasses()).contains("jux-toast", "jux-toast-success");
    }

    @Test
    void render_errorToastHasCorrectCssClass() {
        Element rendered = new JuxToast("Failed!", JuxToast.ToastType.ERROR).render();

        assertThat(rendered.getCssClasses()).contains("jux-toast", "jux-toast-error");
    }

    @Test
    void render_warningToastHasCorrectCssClass() {
        Element rendered = new JuxToast("Warning!", JuxToast.ToastType.WARNING).render();

        assertThat(rendered.getCssClasses()).contains("jux-toast", "jux-toast-warning");
    }

    @Test
    void render_infoToastHasCorrectCssClass() {
        Element rendered = new JuxToast("FYI.", JuxToast.ToastType.INFO).render();

        assertThat(rendered.getCssClasses()).contains("jux-toast", "jux-toast-info");
    }

    @Test
    void toastType_successCssClass() {
        assertThat(JuxToast.ToastType.SUCCESS.cssClass()).isEqualTo("success");
    }

    @Test
    void toastType_errorCssClass() {
        assertThat(JuxToast.ToastType.ERROR.cssClass()).isEqualTo("error");
    }

    @Test
    void toastType_warningCssClass() {
        assertThat(JuxToast.ToastType.WARNING.cssClass()).isEqualTo("warning");
    }

    @Test
    void toastType_infoCssClass() {
        assertThat(JuxToast.ToastType.INFO.cssClass()).isEqualTo("info");
    }

    @Test
    void render_rootElementIsDiv() {
        Element rendered = new JuxToast("Test", JuxToast.ToastType.INFO).render();

        assertThat(rendered.getTag()).isEqualTo("div");
    }

    @Test
    void constructor_nullMessageThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxToast(null, JuxToast.ToastType.SUCCESS))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void constructor_nullTypeThrowsNullPointerException() {
        assertThatThrownBy(() -> new JuxToast("Message", null))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void render_differentMessagesProduceDifferentContent() {
        Element rendered1 = new JuxToast("First message", JuxToast.ToastType.SUCCESS).render();
        Element rendered2 = new JuxToast("Second message", JuxToast.ToastType.ERROR).render();

        Element msg1 = findByTag(rendered1, "p");
        Element msg2 = findByTag(rendered2, "p");

        assertThat(msg1.getTextContent()).isEqualTo("First message");
        assertThat(msg2.getTextContent()).isEqualTo("Second message");
    }
}

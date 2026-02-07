package xss.it.jux.theme;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link JuxSkipLinks} -- skip navigation link component.
 *
 * <p>Validates the rendered anchor element, href target, CSS class,
 * and link text per WCAG 2.4.1 Bypass Blocks requirements.</p>
 */
class JuxSkipLinksTest {

    @Test
    void render_returnsAnchorElement() {
        Element rendered = new JuxSkipLinks().render();

        assertThat(rendered.getTag()).isEqualTo("a");
    }

    @Test
    void render_hrefPointsToMainContent() {
        Element rendered = new JuxSkipLinks().render();

        assertThat(rendered.getAttributes().get("href")).isEqualTo("#main-content");
    }

    @Test
    void render_hasSkipNavCssClass() {
        Element rendered = new JuxSkipLinks().render();

        assertThat(rendered.getCssClasses()).contains("jux-skip-nav");
    }

    @Test
    void render_hasCorrectLinkText() {
        Element rendered = new JuxSkipLinks().render();

        assertThat(rendered.getTextContent()).isEqualTo("Skip to main content");
    }

    @Test
    void render_isConsistentAcrossMultipleInvocations() {
        JuxSkipLinks skipLinks = new JuxSkipLinks();

        Element first = skipLinks.render();
        Element second = skipLinks.render();

        assertThat(first.getTag()).isEqualTo(second.getTag());
        assertThat(first.getAttributes().get("href")).isEqualTo(second.getAttributes().get("href"));
        assertThat(first.getTextContent()).isEqualTo(second.getTextContent());
    }

    @Test
    void render_noChildElements() {
        Element rendered = new JuxSkipLinks().render();

        // skipNav produces an <a> with text content, not children
        assertThat(rendered.getChildren()).isEmpty();
    }
}

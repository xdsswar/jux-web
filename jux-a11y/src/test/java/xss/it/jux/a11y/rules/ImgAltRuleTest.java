package xss.it.jux.a11y.rules;

import org.junit.jupiter.api.Test;
import xss.it.jux.a11y.A11yRules;
import xss.it.jux.a11y.A11ySeverity;
import xss.it.jux.a11y.A11yViolation;
import xss.it.jux.core.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link A11yRules.ImgAltRule} -- WCAG 1.1.1 (Non-text Content).
 *
 * <p>Verifies that images without an {@code alt} attribute are flagged as
 * ERROR violations, while images with alt text (including empty alt for
 * decorative images) pass the rule.</p>
 */
class ImgAltRuleTest {

    private final A11yRules.ImgAltRule rule = new A11yRules.ImgAltRule();

    @Test
    void imgWithAltText_noViolations() {
        Element element = img("/photo.jpg", "A scenic mountain view");

        List<A11yViolation> violations = rule.check(element, "img");

        assertThat(violations).isEmpty();
    }

    @Test
    void imgWithoutAlt_errorViolationWithCriterion1_1_1() {
        // Create img without alt using Element.of directly
        Element element = Element.of("img").attr("src", "/photo.jpg");

        List<A11yViolation> violations = rule.check(element, "img");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.ERROR);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("1.1.1");
        assertThat(violations.get(0).rule()).isEqualTo("img-alt");
    }

    @Test
    void imgWithEmptyAlt_decorative_noViolations() {
        // Decorative image with alt="" is valid
        Element element = Element.of("img").attr("src", "/decorative.png").attr("alt", "");

        List<A11yViolation> violations = rule.check(element, "img");

        assertThat(violations).isEmpty();
    }

    @Test
    void nonImgElement_noViolations() {
        Element element = div().text("Not an image");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}

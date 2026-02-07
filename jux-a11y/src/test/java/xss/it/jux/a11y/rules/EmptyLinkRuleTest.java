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
 * Tests for {@link A11yRules.EmptyLinkRule} -- WCAG 2.4.4 (Link Purpose).
 *
 * <p>Verifies that anchor elements with no discernible text (no text content,
 * no children, no aria-label) are flagged as WARNING violations.</p>
 */
class EmptyLinkRuleTest {

    private final A11yRules.EmptyLinkRule rule = new A11yRules.EmptyLinkRule();

    @Test
    void anchorWithText_noViolations() {
        Element element = a().attr("href", "/about").text("About Us");

        List<A11yViolation> violations = rule.check(element, "a");

        assertThat(violations).isEmpty();
    }

    @Test
    void anchorWithNoTextAndNoAriaLabel_warningWithCriterion2_4_4() {
        Element element = a().attr("href", "/empty");

        List<A11yViolation> violations = rule.check(element, "a");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("2.4.4");
        assertThat(violations.get(0).rule()).isEqualTo("empty-link");
    }

    @Test
    void anchorWithChildSpanContainingText_noViolations() {
        Element element = a().attr("href", "/about").children(
            span().text("About")
        );

        List<A11yViolation> violations = rule.check(element, "a");

        assertThat(violations).isEmpty();
    }

    @Test
    void anchorWithAriaLabel_noViolations() {
        Element element = a().attr("href", "/about").aria("label", "About Us");

        List<A11yViolation> violations = rule.check(element, "a");

        assertThat(violations).isEmpty();
    }

    @Test
    void nonAnchorElement_noViolations() {
        Element element = div().text("Not a link");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}

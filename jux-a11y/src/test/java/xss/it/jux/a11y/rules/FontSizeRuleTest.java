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
 * Tests for {@link A11yRules.FontSizeRule} -- WCAG 1.4.4 (Resize Text).
 *
 * <p>Verifies that inline font-size styles using px units are flagged as
 * WARNING violations, while rem and em units pass the rule.</p>
 */
class FontSizeRuleTest {

    private final A11yRules.FontSizeRule rule = new A11yRules.FontSizeRule();

    @Test
    void fontSizeUsingRem_noViolations() {
        Element element = p().style("font-size", "1rem");

        List<A11yViolation> violations = rule.check(element, "p");

        assertThat(violations).isEmpty();
    }

    @Test
    void fontSizeUsingPx_warningWithCriterion1_4_4() {
        Element element = p().style("font-size", "16px");

        List<A11yViolation> violations = rule.check(element, "p");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("1.4.4");
        assertThat(violations.get(0).rule()).isEqualTo("font-size-px");
    }

    @Test
    void fontSizeUsingEm_noViolations() {
        Element element = p().style("font-size", "1em");

        List<A11yViolation> violations = rule.check(element, "p");

        assertThat(violations).isEmpty();
    }

    @Test
    void elementWithoutFontSizeStyle_noViolations() {
        Element element = p().style("color", "red");

        List<A11yViolation> violations = rule.check(element, "p");

        assertThat(violations).isEmpty();
    }
}

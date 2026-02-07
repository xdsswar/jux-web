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
 * Tests for {@link A11yRules.TabIndexRule} -- WCAG 2.4.3 (Focus Order).
 *
 * <p>Verifies that elements with positive tabindex values are flagged as
 * WARNING violations, while tabindex="0" and tabindex="-1" pass.</p>
 */
class TabIndexRuleTest {

    private final A11yRules.TabIndexRule rule = new A11yRules.TabIndexRule();

    @Test
    void tabindexZero_noViolations() {
        Element element = div().tabIndex(0);

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void tabindexNegativeOne_noViolations() {
        Element element = div().tabIndex(-1);

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void tabindexPositive_warningWithCriterion2_4_3() {
        Element element = div().tabIndex(5);

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("2.4.3");
        assertThat(violations.get(0).rule()).isEqualTo("positive-tabindex");
    }

    @Test
    void elementWithoutTabindex_noViolations() {
        Element element = div().text("No tabindex");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}

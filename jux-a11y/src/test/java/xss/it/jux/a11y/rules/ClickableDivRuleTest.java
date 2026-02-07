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
 * Tests for {@link A11yRules.ClickableDivRule} -- WCAG 2.1.1 (Keyboard).
 *
 * <p>Verifies that non-interactive elements (div, span) with a click handler
 * but no keyboard support, tabindex, or role are flagged as WARNING violations.</p>
 */
class ClickableDivRuleTest {

    private final A11yRules.ClickableDivRule rule = new A11yRules.ClickableDivRule();

    @Test
    void divWithClickButNoKeyboardHandlerNoRoleNoTabindex_warningWithCriterion2_1_1() {
        Element element = div().on("click", e -> {});

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("2.1.1");
        assertThat(violations.get(0).rule()).isEqualTo("clickable-div");
    }

    @Test
    void divWithClickAndKeydownHandler_noViolations() {
        Element element = div()
            .on("click", e -> {})
            .on("keydown", e -> {});

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void divWithClickAndTabindex_noViolations() {
        Element element = div()
            .on("click", e -> {})
            .tabIndex(0);

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void divWithClickAndRole_noViolations() {
        Element element = div()
            .on("click", e -> {})
            .role("button");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void divWithoutClickHandler_noViolations() {
        Element element = div().text("Just a div");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void buttonWithClickHandler_noViolations() {
        // button is a natively interactive element -- rule only checks div/span
        Element element = button().on("click", e -> {}).text("Click me");

        List<A11yViolation> violations = rule.check(element, "button");

        assertThat(violations).isEmpty();
    }
}

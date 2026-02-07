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
 * Tests for {@link A11yRules.InputLabelRule} -- WCAG 3.3.2 (Labels or Instructions).
 *
 * <p>This is a tree-walking rule. It only runs at the root (path without " > ").
 * It collects all {@code <label for="...">} associations from the full tree,
 * then checks each form input for a valid label.</p>
 *
 * <p>All tests pass the root element with a path that does NOT contain " > "
 * to trigger the full tree scan.</p>
 */
class InputLabelRuleTest {

    private final A11yRules.InputLabelRule rule = new A11yRules.InputLabelRule();

    @Test
    void formWithLabelForMatchingInput_noViolations() {
        Element tree = form().children(
            label().attr("for", "email").text("Email"),
            input().id("email").attr("type", "email")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void formWithInputWithoutMatchingLabel_warningWithCriterion3_3_2() {
        Element tree = form().children(
            input().id("email").attr("type", "email")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("3.3.2");
        assertThat(violations.get(0).rule()).isEqualTo("input-label");
    }

    @Test
    void inputWithAriaLabel_noViolations() {
        Element tree = form().children(
            input().id("search").attr("type", "text").aria("label", "Search")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void inputWithAriaLabelledBy_noViolations() {
        Element tree = form().children(
            span().id("lbl").text("Username"),
            input().id("user").attr("type", "text").ariaLabelledBy("lbl")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void inputWithTitle_noViolations() {
        Element tree = form().children(
            input().id("phone").attr("type", "tel").attr("title", "Phone number")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void inputWrappedInsideLabel_noViolations() {
        Element tree = form().children(
            label().text("Username").children(
                input().attr("name", "username").attr("type", "text")
            )
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void inputTypeHidden_exempt_noViolations() {
        Element tree = form().children(
            input().attr("type", "hidden").attr("name", "csrf")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }

    @Test
    void inputTypeSubmit_exempt_noViolations() {
        Element tree = form().children(
            input().attr("type", "submit").attr("value", "Send")
        );

        List<A11yViolation> violations = rule.check(tree, "form");

        assertThat(violations).isEmpty();
    }
}

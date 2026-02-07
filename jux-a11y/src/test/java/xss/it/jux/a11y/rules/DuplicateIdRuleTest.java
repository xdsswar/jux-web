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
 * Tests for {@link A11yRules.DuplicateIdRule} -- WCAG 4.1.1 (Parsing / Unique IDs).
 *
 * <p>This is a tree-walking rule that only runs at root (path without " > ").
 * It collects all IDs from the subtree and flags duplicates as ERROR violations.</p>
 */
class DuplicateIdRuleTest {

    private final A11yRules.DuplicateIdRule rule = new A11yRules.DuplicateIdRule();

    @Test
    void allUniqueIds_noViolations() {
        Element tree = div().children(
            div().id("first"),
            div().id("second"),
            div().id("third")
        );

        List<A11yViolation> violations = rule.check(tree, "div");

        assertThat(violations).isEmpty();
    }

    @Test
    void twoElementsWithSameId_errorWithCriterion4_1_1() {
        Element tree = div().children(
            div().id("duplicate"),
            div().id("duplicate")
        );

        List<A11yViolation> violations = rule.check(tree, "div");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.ERROR);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("4.1.1");
        assertThat(violations.get(0).rule()).isEqualTo("duplicate-id");
    }

    @Test
    void elementsWithoutIds_noViolations() {
        Element tree = div().children(
            div().text("No id"),
            span().text("Also no id"),
            p().text("Same")
        );

        List<A11yViolation> violations = rule.check(tree, "div");

        assertThat(violations).isEmpty();
    }
}

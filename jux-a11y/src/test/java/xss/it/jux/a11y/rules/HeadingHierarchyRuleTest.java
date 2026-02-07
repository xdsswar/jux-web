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
 * Tests for {@link A11yRules.HeadingHierarchyRule} -- heading level skip detection.
 *
 * <p>This is a tree-walking rule that only runs at root (path without " > ").
 * It collects all headings in document order, then checks for level skips
 * (e.g. h1 followed by h3 without intervening h2).</p>
 */
class HeadingHierarchyRuleTest {

    private final A11yRules.HeadingHierarchyRule rule = new A11yRules.HeadingHierarchyRule();

    @Test
    void h1_h2_h3_properHierarchy_noViolations() {
        Element tree = main_().children(
            h1().text("Title"),
            section().children(
                h2().text("Section"),
                h3().text("Subsection")
            )
        );

        List<A11yViolation> violations = rule.check(tree, "main");

        assertThat(violations).isEmpty();
    }

    @Test
    void h1_h3_skipsH2_warningWithCriterion1_3_1() {
        Element tree = main_().children(
            h1().text("Title"),
            h3().text("Jumped to h3")
        );

        List<A11yViolation> violations = rule.check(tree, "main");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("1.3.1");
        assertThat(violations.get(0).rule()).isEqualTo("heading-hierarchy");
    }

    @Test
    void h2_h1_goingShallower_noViolations() {
        // Going to a shallower or equal level is always valid
        Element tree = main_().children(
            h2().text("Section"),
            h1().text("New Top Level")
        );

        List<A11yViolation> violations = rule.check(tree, "main");

        assertThat(violations).isEmpty();
    }

    @Test
    void singleH1_noViolations() {
        Element tree = main_().children(
            h1().text("Only Heading")
        );

        List<A11yViolation> violations = rule.check(tree, "main");

        assertThat(violations).isEmpty();
    }
}

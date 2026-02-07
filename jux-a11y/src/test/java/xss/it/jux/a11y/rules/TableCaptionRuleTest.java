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
 * Tests for {@link A11yRules.TableCaptionRule} -- WCAG 1.3.1 (Info and Relationships).
 *
 * <p>Verifies that tables without a {@code <caption>} child are flagged as
 * WARNING violations, while tables with a caption pass the rule.</p>
 */
class TableCaptionRuleTest {

    private final A11yRules.TableCaptionRule rule = new A11yRules.TableCaptionRule();

    @Test
    void tableWithCaption_noViolations() {
        Element element = table().children(
            caption().text("User Data"),
            thead(),
            tbody()
        );

        List<A11yViolation> violations = rule.check(element, "table");

        assertThat(violations).isEmpty();
    }

    @Test
    void tableWithoutCaption_warningWithCriterion1_3_1() {
        Element element = table().children(
            thead(),
            tbody()
        );

        List<A11yViolation> violations = rule.check(element, "table");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.WARNING);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("1.3.1");
        assertThat(violations.get(0).rule()).isEqualTo("table-caption");
    }

    @Test
    void nonTableElement_noViolations() {
        Element element = div().text("Not a table");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}

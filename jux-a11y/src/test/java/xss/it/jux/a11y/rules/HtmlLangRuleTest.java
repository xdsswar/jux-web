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
 * Tests for {@link A11yRules.HtmlLangRule} -- WCAG 3.1.1 (Language of Page).
 *
 * <p>Verifies that the {@code <html>} element without a {@code lang} attribute
 * is flagged as an ERROR violation. Only elements with the "html" tag are checked.</p>
 */
class HtmlLangRuleTest {

    private final A11yRules.HtmlLangRule rule = new A11yRules.HtmlLangRule();

    @Test
    void htmlWithLang_noViolations() {
        Element element = Element.of("html").lang("en");

        List<A11yViolation> violations = rule.check(element, "html");

        assertThat(violations).isEmpty();
    }

    @Test
    void htmlWithoutLang_errorWithCriterion3_1_1() {
        Element element = Element.of("html");

        List<A11yViolation> violations = rule.check(element, "html");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.ERROR);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("3.1.1");
        assertThat(violations.get(0).rule()).isEqualTo("html-lang");
    }

    @Test
    void htmlWithBlankLang_errorWithCriterion3_1_1() {
        Element element = Element.of("html").lang("   ");

        List<A11yViolation> violations = rule.check(element, "html");

        assertThat(violations).hasSize(1);
        assertThat(violations.get(0).severity()).isEqualTo(A11ySeverity.ERROR);
        assertThat(violations.get(0).wcagCriterion()).isEqualTo("3.1.1");
    }

    @Test
    void nonHtmlElement_noViolations() {
        // The rule only fires on elements with the "html" tag
        Element element = div().text("Not html tag");

        List<A11yViolation> violations = rule.check(element, "div");

        assertThat(violations).isEmpty();
    }
}

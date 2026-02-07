package xss.it.jux.a11y;

import org.junit.jupiter.api.Test;
import xss.it.jux.core.Element;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static xss.it.jux.core.Elements.*;

/**
 * Tests for {@link JuxAccessibilityEngine} -- the main WCAG 2.2 AA audit engine.
 *
 * <p>Verifies that the engine correctly delegates to its rules, aggregates
 * violations, sorts them by severity, and supports custom rule lists and
 * auto-fixing.</p>
 */
class JuxAccessibilityEngineTest {

    // ── audit() on a compliant tree ──────────────────────────────

    @Test
    void audit_compliantTree_returnsNoViolations() {
        var engine = new JuxAccessibilityEngine();

        // A minimal compliant tree: html[lang] > main > h1
        Element tree = Element.of("html").lang("en").children(
            main_().children(
                h1().text("Welcome")
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        assertThat(violations).isEmpty();
    }

    // ── audit() detects img missing alt ──────────────────────────

    @Test
    void audit_imgMissingAlt_returnsViolationWithCriterion1_1_1() {
        var engine = new JuxAccessibilityEngine();

        // img without alt attribute (using Element.of directly to bypass Elements.img which requires alt)
        Element tree = Element.of("html").lang("en").children(
            main_().children(
                h1().text("Page"),
                Element.of("img").attr("src", "/photo.jpg")
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        assertThat(violations)
            .anyMatch(v -> v.severity() == A11ySeverity.ERROR
                && "1.1.1".equals(v.wcagCriterion()));
    }

    // ── audit() returns multiple violations ──────────────────────

    @Test
    void audit_multipleIssues_returnsAllViolations() {
        var engine = new JuxAccessibilityEngine();

        // img without alt + two elements with duplicate IDs
        Element tree = Element.of("html").lang("en").children(
            main_().children(
                h1().text("Page"),
                Element.of("img").attr("src", "/photo.jpg"),
                div().id("dup"),
                div().id("dup")
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        // Should contain at least the img-alt violation and the duplicate-id violation
        assertThat(violations)
            .anyMatch(v -> "1.1.1".equals(v.wcagCriterion()))
            .anyMatch(v -> "4.1.1".equals(v.wcagCriterion()));
        assertThat(violations.size()).isGreaterThanOrEqualTo(2);
    }

    // ── audit() sorts violations by severity ─────────────────────

    @Test
    void audit_violationsSortedBySeverity_errorsBeforeWarnings() {
        var engine = new JuxAccessibilityEngine();

        // html without lang (ERROR) + table without caption (WARNING)
        Element tree = Element.of("html").children(
            main_().children(
                h1().text("Page"),
                table().children(
                    Element.of("thead"),
                    Element.of("tbody")
                )
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        // Should contain both ERROR and WARNING
        assertThat(violations).isNotEmpty();

        // Verify sorting: all ERRORs come before all WARNINGs
        boolean seenWarning = false;
        for (A11yViolation v : violations) {
            if (v.severity() == A11ySeverity.WARNING) {
                seenWarning = true;
            }
            if (seenWarning && v.severity() == A11ySeverity.ERROR) {
                org.junit.jupiter.api.Assertions.fail(
                    "ERROR violation found after WARNING -- violations should be sorted by severity");
            }
        }
    }

    // ── Engine with custom rules list ────────────────────────────

    @Test
    void audit_customRulesList_onlyRunsThoseRules() {
        // Only use ImgAltRule -- so a table without caption should NOT be flagged
        List<A11yRule> customRules = List.of(new A11yRules.ImgAltRule());
        var engine = new JuxAccessibilityEngine(customRules);

        Element tree = Element.of("html").children(
            main_().children(
                table().children(
                    Element.of("thead"),
                    Element.of("tbody")
                )
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        // No img-alt violations (no imgs) and no table-caption violations (rule not included)
        assertThat(violations).noneMatch(v -> "1.3.1".equals(v.wcagCriterion())
            && "table-caption".equals(v.rule()));
    }

    // ── Engine with empty rules list ─────────────────────────────

    @Test
    void audit_emptyRulesList_returnsNoViolationsOnAnyTree() {
        var engine = new JuxAccessibilityEngine(List.of());

        // A tree with many issues -- but engine has no rules
        Element tree = Element.of("html").children(
            main_().children(
                Element.of("img").attr("src", "/x.jpg"),
                table()
            )
        );

        List<A11yViolation> violations = engine.audit(tree);

        assertThat(violations).isEmpty();
    }

    // ── autoFix() on decorative image ────────────────────────────

    @Test
    void autoFix_decorativeImgWithEmptyAlt_addsRolePresentation() {
        var engine = new JuxAccessibilityEngine();

        // img with alt="" but no role -- autoFix should add role="presentation"
        Element tree = div().children(
            Element.of("img").attr("src", "/bg.jpg").attr("alt", "")
        );

        Element fixed = engine.autoFix(tree);

        // The img is the first child
        Element img = fixed.getChildren().get(0);
        assertThat(img.getAttributes().get("role")).isEqualTo("presentation");
    }
}

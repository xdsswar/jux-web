/*
 * Copyright (c) 2026 Xtreme Software Solutions (XDSSWAR). All rights reserved.
 *
 * Licensed under the Xtreme Software Solutions Source License v1.0 (the "License").
 * You may not use this file except in compliance with the License.
 *
 * - Open-source use: Free (see License for conditions)
 * - Commercial use: Requires explicit written permission from Xtreme Software Solutions
 *
 * This software is provided "AS IS", without warranty of any kind.
 * See the LICENSE file in the project root for full terms.
 */

package xss.it.jux.a11y;

import xss.it.jux.core.Element;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * WCAG 2.2 Level AA compliance audit engine.
 *
 * <p>Scans an {@link Element} tree for accessibility violations and
 * optionally auto-fixes safe issues. This engine runs automatically
 * during SSR in development mode ({@code jux.a11y.audit-on-render = true}).</p>
 *
 * <p><b>In development:</b> violations are logged as warnings/errors.
 * If {@code jux.a11y.fail-on-error = true}, ERROR-level violations
 * cause the page to fail rendering with a 500 status and a detailed
 * violation report.</p>
 *
 * <p><b>In production:</b> the audit is disabled by default for
 * zero overhead. Enable selectively via config or the report endpoint.</p>
 *
 * <p><b>Usage:</b></p>
 * <pre>{@code
 * var engine = new JuxAccessibilityEngine();
 * List<A11yViolation> violations = engine.audit(rootElement);
 *
 * // Or auto-fix safe issues first:
 * Element fixed = engine.autoFix(rootElement);
 * violations = engine.audit(fixed);
 * }</pre>
 *
 * <p><b>Custom rules:</b> Pass a custom rule list to the constructor
 * to add project-specific checks or override built-in rules:</p>
 * <pre>{@code
 * List<A11yRule> rules = A11yRules.allRules();
 * rules.add(new MyCustomRule());
 * var engine = new JuxAccessibilityEngine(rules);
 * }</pre>
 *
 * @see A11yRule
 * @see A11yRules
 * @see A11yViolation
 * @see A11yAutoFixer
 */
public class JuxAccessibilityEngine {

    /**
     * The list of WCAG audit rules that this engine checks during {@link #audit(Element)}.
     * Each rule implements {@link A11yRule} and targets a specific WCAG 2.2 success criterion.
     * Initialized either from the built-in rule set ({@link A11yRules#allRules()}) or from
     * a custom list provided to the constructor.
     */
    private final List<A11yRule> rules;

    /**
     * The auto-fixer instance used by {@link #autoFix(Element)} to apply safe,
     * unambiguous accessibility corrections to an element tree. Created once at
     * construction time and reused across all auto-fix invocations.
     */
    private final A11yAutoFixer autoFixer;

    /**
     * Creates an engine with all built-in WCAG 2.2 AA rules.
     */
    public JuxAccessibilityEngine() {
        this.rules = A11yRules.allRules();
        this.autoFixer = new A11yAutoFixer();
    }

    /**
     * Creates an engine with a custom set of rules.
     *
     * <p>Use this to add project-specific rules or to run a subset
     * of the built-in rules.</p>
     *
     * @param rules the list of rules to run during audit
     */
    public JuxAccessibilityEngine(List<A11yRule> rules) {
        this.rules = rules;
        this.autoFixer = new A11yAutoFixer();
    }

    /**
     * Audit an element tree for WCAG 2.2 AA violations.
     *
     * <p>Walks the entire tree recursively, checking each element against
     * all configured WCAG rules. Returns a list of violations sorted by
     * severity (ERROR first, then WARNING, then INFO).</p>
     *
     * @param root the root element of the page to audit
     * @return list of violations found, empty if fully compliant
     */
    public List<A11yViolation> audit(Element root) {
        List<A11yViolation> violations = new ArrayList<>();
        auditElement(root, root.getTag(), violations);
        violations.sort(Comparator.comparingInt(v -> v.severity().ordinal()));
        return violations;
    }

    /**
     * Recursively audit a single element and all its children.
     *
     * <p>Each element is checked against every rule. The path string
     * accumulates the tree location for diagnostic reporting
     * (e.g. "main > section:0 > img:1").</p>
     *
     * @param element    the element to check
     * @param path       the current path in the tree
     * @param violations the accumulator for found violations
     */
    private void auditElement(Element element, String path, List<A11yViolation> violations) {
        for (A11yRule rule : rules) {
            violations.addAll(rule.check(element, path));
        }

        List<Element> children = element.getChildren();
        for (int i = 0; i < children.size(); i++) {
            Element child = children.get(i);
            String childPath = path + " > " + child.getTag() + ":" + i;
            auditElement(child, childPath, violations);
        }
    }

    /**
     * Auto-fix safe accessibility issues and return the corrected tree.
     *
     * <p>Only fixes issues that have a single unambiguous solution:</p>
     * <ul>
     *   <li>Add {@code role="presentation"} to decorative images</li>
     *   <li>Add {@code aria-hidden="true"} to icon-only elements</li>
     * </ul>
     *
     * <p>Does NOT auto-fix issues that require human judgment
     * (alt text content, label text, heading hierarchy).</p>
     *
     * <p><b>Note:</b> The current implementation mutates the tree in place
     * (Element uses a mutable builder pattern). The returned element is
     * the same object as the input with modifications applied.</p>
     *
     * @param root the original element tree
     * @return the tree with safe fixes applied
     */
    public Element autoFix(Element root) {
        return autoFixer.autoFix(root);
    }
}

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

/**
 * A single WCAG accessibility violation found during audit.
 *
 * <p>Contains enough information for the developer to locate and fix the issue:
 * the WCAG criterion violated, the specific rule, a human-readable message,
 * the path to the offending element in the tree, and a suggested fix.</p>
 *
 * @param severity      ERROR = must fix (page may be legally non-compliant),
 *                      WARNING = should fix (best practice),
 *                      INFO = suggestion for improvement
 * @param wcagCriterion the WCAG 2.2 success criterion (e.g. "1.1.1", "2.4.2", "3.1.1")
 * @param rule          machine-readable rule ID (e.g. "img-alt", "page-title", "html-lang")
 * @param message       human-readable description of the violation
 * @param elementPath   path to the offending element (e.g. "main > section:0 > img:1")
 * @param suggestion    recommended fix (e.g. "Add alt attribute with descriptive text")
 *
 * @see A11ySeverity
 * @see JuxAccessibilityEngine
 */
public record A11yViolation(
    A11ySeverity severity,
    String wcagCriterion,
    String rule,
    String message,
    String elementPath,
    String suggestion
) {}

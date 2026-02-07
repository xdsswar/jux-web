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
 * Severity levels for accessibility audit violations.
 *
 * <p>Used by {@link A11yViolation} to indicate how critical a finding is.
 * The audit engine sorts violations by severity (ERROR first) so that
 * the most critical issues are addressed first.</p>
 *
 * @see A11yViolation
 * @see JuxAccessibilityEngine
 */
public enum A11ySeverity {

    /** Must fix -- the page is non-compliant with WCAG 2.2 AA. Legal risk. */
    ERROR,

    /** Should fix -- best practice, improves user experience for assistive tech users. */
    WARNING,

    /** Nice to have -- minor improvement suggestion. */
    INFO
}

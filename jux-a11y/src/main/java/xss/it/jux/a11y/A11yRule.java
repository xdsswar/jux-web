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

import java.util.List;

/**
 * Interface for individual WCAG 2.2 AA audit rules.
 *
 * <p>Each implementation checks a specific WCAG success criterion against
 * a single {@link Element} node (and optionally its subtree). Rules are
 * collected by {@link A11yRules#allRules()} and executed by
 * {@link JuxAccessibilityEngine#audit(Element)}.</p>
 *
 * <p>Implementations should be stateless -- the engine may reuse rule
 * instances across multiple audits. Any tree-walking state (e.g. collecting
 * all IDs for duplicate detection) should be local to the
 * {@link #check(Element, String)} call.</p>
 *
 * @see A11yRules
 * @see A11yViolation
 * @see JuxAccessibilityEngine
 */
public interface A11yRule {

    /**
     * Check the given element for accessibility violations.
     *
     * <p>The element is a single node in the virtual DOM tree. The path
     * string describes the location of this element in the tree for
     * diagnostic purposes (e.g. "main > section:0 > img:1").</p>
     *
     * <p>Most rules check only the current element. Tree-walking rules
     * (e.g. duplicate ID detection, heading hierarchy) should recursively
     * walk children from within this method when invoked on the root.</p>
     *
     * @param element the element to check, never null
     * @param path    the element's path in the tree for violation reporting
     * @return a list of violations found, empty if the element passes this rule
     */
    List<A11yViolation> check(Element element, String path);
}

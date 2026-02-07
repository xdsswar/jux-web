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

import java.util.Map;

/**
 * Auto-fixes safe accessibility issues in an Element tree.
 *
 * <p>Only fixes issues that have a single, unambiguous solution.
 * Does NOT auto-fix issues requiring human judgment (alt text content,
 * label text, heading hierarchy).</p>
 *
 * <p><b>Current safe fixes:</b></p>
 * <ul>
 *   <li>Add {@code role="presentation"} to {@code <img>} elements with empty
 *       {@code alt=""} that are missing the role attribute (decorative images)</li>
 *   <li>Add {@code aria-hidden="true"} to elements with a CSS class containing
 *       "icon" that have no text content and no children (icon-only elements)</li>
 * </ul>
 *
 * <p><b>Note:</b> Since {@link Element} uses a mutable builder pattern,
 * this fixer mutates the tree in place. The original tree is modified.
 * If immutability is needed, clone the tree before calling {@link #autoFix(Element)}.</p>
 *
 * @see JuxAccessibilityEngine#autoFix(Element)
 */
public class A11yAutoFixer {

    /**
     * Apply safe auto-fixes to the element tree.
     *
     * <p>Walks the tree recursively and applies fixes where safe.
     * Returns the same root element (mutated in place).</p>
     *
     * @param root the root element of the tree to fix
     * @return the root element with safe fixes applied
     */
    public Element autoFix(Element root) {
        return fixElement(root);
    }

    /**
     * Applies safe accessibility fixes to a single element and recurses into its children.
     *
     * <p>Currently applies two fixes:</p>
     * <ol>
     *   <li><b>Decorative image role:</b> If the element is an {@code <img>} with an
     *       empty {@code alt=""} attribute but no {@code role} attribute, adds
     *       {@code role="presentation"} to properly hide it from the accessibility
     *       tree. This ensures decorative images are not announced by screen readers.</li>
     *   <li><b>Icon-only aria-hidden:</b> If the element has no text content and no
     *       children, and its CSS class attribute contains "icon", and it has neither
     *       {@code aria-hidden} nor {@code aria-label} already set, adds
     *       {@code aria-hidden="true"} to hide the purely visual icon from screen
     *       readers.</li>
     * </ol>
     *
     * <p>After applying fixes to the current element, recurses into all child
     * elements to apply the same fixes throughout the tree.</p>
     *
     * @param el the element to fix (mutated in place)
     * @return the same element reference, with any applicable fixes applied
     */
    private Element fixElement(Element el) {
        Map<String, String> attrs = el.getAttributes();

        // Fix: decorative images (alt="") should have role="presentation"
        // to be properly hidden from the accessibility tree.
        if ("img".equals(el.getTag())) {
            String alt = attrs.get("alt");
            if (alt != null && alt.isEmpty() && !attrs.containsKey("role")) {
                el.role("presentation");
            }
        }

        // Fix: icon-only elements (class contains "icon", no text, no children)
        // should be hidden from screen readers with aria-hidden="true".
        if (el.getTextContent() == null && el.getChildren().isEmpty()) {
            String classAttr = attrs.get("class");
            if (classAttr != null && classAttr.contains("icon")
                    && !attrs.containsKey("aria-hidden")
                    && !attrs.containsKey("aria-label")) {
                el.ariaHidden(true);
            }
        }

        // Recurse into children.
        for (Element child : el.getChildren()) {
            fixElement(child);
        }

        return el;
    }
}

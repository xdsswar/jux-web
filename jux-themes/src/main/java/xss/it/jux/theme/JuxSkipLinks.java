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

package xss.it.jux.theme;

import xss.it.jux.annotation.JuxComponent;
import xss.it.jux.core.Component;
import xss.it.jux.core.Element;

import static xss.it.jux.core.Elements.*;

/**
 * Skip navigation link component (WCAG 2.2 AA -- Success Criterion 2.4.1).
 *
 * <p>Renders an anchor link that allows keyboard users to bypass repeated
 * navigation blocks (headers, nav bars, sidebars) and jump directly to the
 * main content area. This is a mandatory WCAG 2.4.1 requirement for pages
 * with repeated navigation.</p>
 *
 * <p><b>How it works:</b></p>
 * <ol>
 *   <li>The link is visually hidden off-screen by default using the
 *       {@code jux-skip-nav} CSS class (absolute positioning with
 *       negative margins/offsets)</li>
 *   <li>When a keyboard user presses Tab on page load, the skip link
 *       receives focus and becomes visible (via {@code :focus} CSS styles)</li>
 *   <li>Pressing Enter on the link moves focus to the main content area
 *       (the element with {@code id="main-content"})</li>
 *   <li>The user can then continue tabbing through the main content
 *       without having to navigate through the entire header/nav first</li>
 * </ol>
 *
 * <p><b>WCAG rationale:</b></p>
 * <p>Success Criterion 2.4.1 (Bypass Blocks) requires a mechanism to skip
 * past content that is repeated on multiple pages. The skip navigation link
 * is the most common and well-understood implementation. Screen reader users
 * benefit because it is the first focusable element on the page, announced
 * immediately when they begin navigating.</p>
 *
 * <p><b>Placement:</b> This component should be the very first element in
 * the page layout, before any headers, navigation, or banners. The
 * {@code DefaultLayout} in JUX auto-injects this component when
 * {@code jux.a11y.skip-nav} is enabled.</p>
 *
 * <p><b>Target:</b> The skip link targets {@code #main-content}. All JUX
 * page layouts are expected to have a {@code <main id="main-content">}
 * element wrapping the primary page content.</p>
 *
 * <p><b>CSS requirements:</b> The {@code jux-skip-nav} class must implement
 * a visually-hidden-until-focused pattern:</p>
 * <pre>{@code
 * .jux-skip-nav {
 *   position: absolute;
 *   left: -9999px;
 *   top: auto;
 *   width: 1px;
 *   height: 1px;
 *   overflow: hidden;
 *   z-index: 9999;
 * }
 * .jux-skip-nav:focus {
 *   position: fixed;
 *   top: 0;
 *   left: 0;
 *   width: auto;
 *   height: auto;
 *   padding: 0.5rem 1rem;
 *   background: var(--jux-primary);
 *   color: #ffffff;
 *   z-index: 9999;
 * }
 * }</pre>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * // In a layout component's render():
 * return div().children(
 *     child(new JuxSkipLinks()),   // must be first
 *     child(new Header()),
 *     child(new Navigation()),
 *     main_().id("main-content").children(
 *         // page content
 *     )
 * );
 * }</pre>
 *
 * @see <a href="https://www.w3.org/WAI/WCAG22/Understanding/bypass-blocks.html">
 *     Understanding WCAG 2.4.1 Bypass Blocks</a>
 */
@JuxComponent
public class JuxSkipLinks extends Component {

    /**
     * The ID of the main content target element.
     *
     * <p>This is the element that receives focus when the user activates
     * the skip link. All JUX layouts use {@code "main-content"} as the
     * standard ID for the primary content container.</p>
     */
    private static final String TARGET_ID = "main-content";

    /**
     * The visible link text displayed when the skip link is focused.
     *
     * <p>"Skip to main content" is the most widely recognized and
     * understood skip link text. Screen reader users and keyboard users
     * are trained to expect this exact phrase or a close variant.</p>
     */
    private static final String LINK_TEXT = "Skip to main content";

    /**
     * Creates a new skip navigation link component.
     *
     * <p>No configuration is needed -- the target element ID and link
     * text use well-established conventions that should not be customized
     * without good reason.</p>
     */
    public JuxSkipLinks() {
        /* No-arg constructor. Configuration uses standard conventions. */
    }

    /**
     * Builds the skip navigation link Element.
     *
     * <p>The rendered structure is:</p>
     * <pre>{@code
     * <a href="#main-content" class="jux-skip-nav">
     *   Skip to main content
     * </a>
     * }</pre>
     *
     * <p>The element uses the {@link xss.it.jux.core.Elements#skipNav(String, String)}
     * helper from the core module, which produces an {@code <a>} element with
     * the {@code jux-skip-nav} class, an {@code href} pointing to the target
     * element, and the specified link text.</p>
     *
     * <p>The link is a plain {@code <a>} element (not a button) because it
     * performs in-page navigation -- moving focus to the target element via
     * the fragment identifier. This is the semantically correct element for
     * this behavior.</p>
     *
     * @return the skip navigation anchor Element, never null
     */
    @Override
    public Element render() {
        /*
         * Delegate to the Elements.skipNav() helper which creates an <a>
         * with class="jux-skip-nav", href="#main-content", and the
         * specified text. This ensures consistency with any other code
         * that uses the same helper.
         */
        return skipNav(TARGET_ID, LINK_TEXT);
    }
}

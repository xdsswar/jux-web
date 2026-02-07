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

package xss.it.jux.annotation;

/**
 * Determines where a CSS {@code <link>} tag is injected in the rendered HTML document.
 *
 * <p>This enum is used by the {@link Css#position()} and {@link InlineCss} annotations
 * to control the placement of stylesheet references in the HTML output. The position
 * affects when the browser discovers and begins loading the stylesheet, which impacts
 * both rendering performance and the risk of a flash of unstyled content (FOUC).</p>
 *
 * <p>In most cases, {@link #HEAD} is the correct choice (and the default), as it ensures
 * styles are loaded before the page content is rendered. {@link #BODY_END} may be used
 * for non-critical stylesheets that can be loaded after the initial paint.</p>
 *
 * @see Css#position()
 */
public enum CssPosition {

    /**
     * Inject the {@code <link>} tag in the {@code <head>} section of the HTML document.
     *
     * <p>This is the default and recommended position for CSS stylesheets. Placing
     * stylesheets in the {@code <head>} ensures they are discovered early by the browser's
     * parser and loaded before the page body is rendered, preventing a flash of unstyled
     * content (FOUC).</p>
     */
    HEAD,

    /**
     * Inject the {@code <link>} tag just before the closing {@code </body>} tag.
     *
     * <p>Use this position for non-critical stylesheets that are not needed for the
     * initial above-the-fold render. Loading these stylesheets late avoids blocking
     * the initial paint, improving perceived performance. Suitable for below-the-fold
     * styles, print stylesheets, or third-party widget styles.</p>
     */
    BODY_END
}

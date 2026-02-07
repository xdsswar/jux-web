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
 * Determines where a JavaScript {@code <script>} tag is injected in the rendered HTML document.
 *
 * <p>This enum is used by the {@link Js#position()} and {@link InlineJs#position()} annotations
 * to control the placement of script tags in the HTML output. The position affects when the
 * browser discovers, downloads, and executes the script relative to HTML parsing.</p>
 *
 * <p>In most cases, {@link #BODY_END} is the correct choice (and the default), as it allows
 * the HTML to be fully parsed before scripts execute. {@link #HEAD} should be used only for
 * scripts that must run before the page content is rendered (e.g., analytics configuration,
 * critical polyfills, or feature detection).</p>
 *
 * @see Js#position()
 * @see InlineJs#position()
 */
public enum JsPosition {

    /**
     * Inject the {@code <script>} tag in the {@code <head>} section of the HTML document.
     *
     * <p>Scripts in the {@code <head>} are discovered early by the browser. Unless the
     * {@link Js#defer()} or {@link Js#async()} attributes are set, head scripts block
     * HTML parsing until they are downloaded and executed. Use this position only for
     * scripts that must run before any page content is rendered, such as analytics
     * initialization, critical polyfills, or configuration injection.</p>
     */
    HEAD,

    /**
     * Inject the {@code <script>} tag just before the closing {@code </body>} tag.
     *
     * <p>This is the default and recommended position for JavaScript. Placing scripts
     * at the end of the body ensures the entire HTML document is parsed and the DOM is
     * available before the script executes. This approach avoids render-blocking and
     * provides the best perceived performance for most use cases.</p>
     */
    BODY_END
}

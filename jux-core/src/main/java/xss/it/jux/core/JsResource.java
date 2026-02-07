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

package xss.it.jux.core;

import xss.it.jux.annotation.JsPosition;

/**
 * Immutable descriptor for a JavaScript resource in the JUX resource pipeline.
 *
 * <p>Represents a single {@code <script>} tag to be injected into the rendered
 * HTML page. JavaScript resources are collected from {@code @Js} annotations on
 * pages, layouts, and child components, as well as from {@link PageMeta#js(String)}
 * programmatic additions. The resource pipeline deduplicates by path, sorts by
 * {@link #order}, evaluates SpEL {@link #condition conditions}, and renders the
 * final {@code <script>} tags.</p>
 *
 * <p><b>Loading behavior defaults:</b> Scripts default to {@code defer=true} and
 * {@code position=BODY_END}, which is optimal for performance -- the script is
 * fetched in parallel with HTML parsing and executed after the DOM is ready.</p>
 *
 * @param path      path to the JS file relative to {@code /static}, or an absolute CDN URL.
 *                  Examples: {@code "pages/dashboard.js"}, {@code "https://cdn.jsdelivr.net/npm/chart.js"}
 * @param position  where to inject the {@code <script>} tag: {@link JsPosition#HEAD HEAD} or
 *                  {@link JsPosition#BODY_END BODY_END} (default). BODY_END is preferred for
 *                  performance as the script does not block HTML parsing
 * @param order     sort order within the same position -- lower numbers load first.
 *                  Default is 100. Use lower values for dependencies that must load before
 *                  other scripts
 * @param async     if {@code true}, the script loads asynchronously (fetched in parallel,
 *                  executed as soon as available). Execution order is not guaranteed with
 *                  other async scripts. Mutually exclusive with {@link #defer} in practice
 * @param defer     if {@code true} (default), the script loads deferred (fetched in parallel
 *                  with parsing, executed after DOM is fully parsed). Execution order among
 *                  deferred scripts is preserved
 * @param module    if {@code true}, the script is loaded as an ES module
 *                  ({@code type="module"}), enabling {@code import}/{@code export} syntax.
 *                  Modules are deferred by default
 * @param integrity Subresource Integrity (SRI) hash for CDN resources, ensuring the fetched
 *                  file has not been tampered with. Example: {@code "sha384-abc123..."}. Empty
 *                  string means no integrity check
 * @param condition SpEL expression evaluated at render time -- the script is only included if
 *                  this evaluates to {@code true}. Example: {@code "#{profile == 'prod'}"}.
 *                  Empty string means always included
 *
 * @see PageMeta#js(String)
 * @see PageMeta#js(JsResource)
 * @see CssResource
 */
public record JsResource(
    String path,
    JsPosition position,
    int order,
    boolean async,
    boolean defer,
    boolean module,
    String integrity,
    String condition
) {
    /**
     * Convenience constructor for a JS resource with default settings.
     *
     * <p>Creates a resource positioned at {@code BODY_END}, with sort order 100,
     * deferred loading, no async, no module, no integrity hash, and no condition.</p>
     *
     * @param path path to the JS file relative to {@code /static}, or absolute CDN URL
     */
    public JsResource(String path) {
        this(path, JsPosition.BODY_END, 100, false, true, false, "", "");
    }

    /**
     * Convenience constructor for a JS resource with a custom sort order.
     *
     * <p>Creates a resource positioned at {@code BODY_END} with the specified
     * order, deferred loading, and all other settings at defaults.</p>
     *
     * @param path  path to the JS file relative to {@code /static}, or absolute CDN URL
     * @param order sort order -- lower numbers load first (default is 100)
     */
    public JsResource(String path, int order) {
        this(path, JsPosition.BODY_END, order, false, true, false, "", "");
    }

    /**
     * Convenience constructor for a JS resource with custom order and position.
     *
     * @param path     path to the JS file relative to {@code /static}, or absolute CDN URL
     * @param order    sort order -- lower numbers load first
     * @param position where to inject the {@code <script>} tag: HEAD or BODY_END
     */
    public JsResource(String path, int order, JsPosition position) {
        this(path, position, order, false, true, false, "", "");
    }
}

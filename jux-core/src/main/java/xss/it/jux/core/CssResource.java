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

import xss.it.jux.annotation.CssPosition;

/**
 * Immutable descriptor for a CSS stylesheet resource in the JUX resource pipeline.
 *
 * <p>Represents a single {@code <link rel="stylesheet">} tag to be injected into
 * the rendered HTML page. CSS resources are collected from {@code @Css} annotations
 * on pages, layouts, and child components, as well as from {@link PageMeta#css(String)}
 * programmatic additions. The resource pipeline deduplicates by path, sorts by
 * {@link #order}, evaluates SpEL {@link #condition conditions}, and renders the
 * final {@code <link>} tags.</p>
 *
 * <p><b>Resource pipeline processing order:</b></p>
 * <ol>
 *   <li>Gather from Layout, Page, and child Components (recursive)</li>
 *   <li>Add PageMeta CSS entries</li>
 *   <li>Remove entries listed in {@link PageMeta#removeCss(String)}</li>
 *   <li>Deduplicate by path</li>
 *   <li>Partition by {@link #position} (HEAD vs BODY_END)</li>
 *   <li>Sort by {@link #order} (ascending) within each partition</li>
 *   <li>Evaluate SpEL {@link #condition conditions}</li>
 *   <li>Render {@code <link>} tags</li>
 * </ol>
 *
 * @param path      path to the CSS file relative to {@code /static}, or an absolute CDN URL.
 *                  Examples: {@code "themes/default.css"}, {@code "https://cdn.example.com/lib.css"}
 * @param position  where to inject the {@code <link>} tag in the HTML document:
 *                  {@link CssPosition#HEAD HEAD} (default) or {@link CssPosition#BODY_END BODY_END}
 * @param order     sort order within the same position -- lower numbers load first.
 *                  Default is 100. Use lower values (e.g. 1-10) for theme/reset CSS,
 *                  higher values (e.g. 200+) for page-specific overrides
 * @param media     CSS media query applied to the {@code <link>} tag (e.g. {@code "print"},
 *                  {@code "screen and (max-width: 768px)"}). Empty string means all media
 * @param async     if {@code true}, the stylesheet is loaded asynchronously (non-render-blocking)
 *                  using {@code media="print" onload="this.media='all'"} pattern
 * @param integrity Subresource Integrity (SRI) hash for CDN resources, ensuring the fetched
 *                  file has not been tampered with. Example: {@code "sha384-abc123..."}. Empty
 *                  string means no integrity check
 * @param condition SpEL expression evaluated at render time -- the stylesheet is only included
 *                  if this evaluates to {@code true}. Example: {@code "#{profile == 'prod'}"}.
 *                  Empty string means always included
 *
 * @see PageMeta#css(String)
 * @see PageMeta#css(CssResource)
 * @see JsResource
 */
public record CssResource(
    String path,
    CssPosition position,
    int order,
    String media,
    boolean async,
    String integrity,
    String condition
) {
    /**
     * Convenience constructor for a CSS resource with default settings.
     *
     * <p>Creates a resource positioned in the {@code <head>}, with sort order 100,
     * no media query, synchronous loading, no integrity hash, and no condition.</p>
     *
     * @param path path to the CSS file relative to {@code /static}, or absolute CDN URL
     */
    public CssResource(String path) {
        this(path, CssPosition.HEAD, 100, "", false, "", "");
    }

    /**
     * Convenience constructor for a CSS resource with a custom sort order.
     *
     * <p>Creates a resource positioned in the {@code <head>} with the specified
     * order and all other settings at defaults.</p>
     *
     * @param path  path to the CSS file relative to {@code /static}, or absolute CDN URL
     * @param order sort order -- lower numbers load first (default is 100)
     */
    public CssResource(String path, int order) {
        this(path, CssPosition.HEAD, order, "", false, "", "");
    }

    /**
     * Convenience constructor for a CSS resource with custom order and position.
     *
     * @param path     path to the CSS file relative to {@code /static}, or absolute CDN URL
     * @param order    sort order -- lower numbers load first
     * @param position where to inject the {@code <link>} tag: HEAD or BODY_END
     */
    public CssResource(String path, int order, CssPosition position) {
        this(path, position, order, "", false, "", "");
    }
}

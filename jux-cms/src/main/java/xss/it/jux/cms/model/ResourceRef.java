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

package xss.it.jux.cms.model;

/**
 * A reference to a CSS or JavaScript resource associated with a CMS page.
 *
 * <p>CMS-managed pages can declare additional stylesheets and scripts beyond
 * those provided by the layout and theme. These references are stored in the
 * database as part of a {@link PageDefinition} and injected into the HTML
 * {@code <head>} or before {@code </body>} during server-side rendering.</p>
 *
 * <p>Resource references are collected alongside annotation-declared resources
 * ({@code @Css}, {@code @Js}) from the page's layout and component tree,
 * deduplicated by path, sorted by {@link #order}, and rendered as
 * {@code <link>} or {@code <script>} tags.</p>
 *
 * <p><b>Example database JSON:</b></p>
 * <pre>{@code
 * {
 *   "path": "pages/custom-hero.css",
 *   "order": 50,
 *   "position": "HEAD"
 * }
 * }</pre>
 *
 * @param path     file path relative to {@code /static}, or an absolute CDN URL;
 *                 examples: {@code "pages/about.css"}, {@code "vendor/prism.js"},
 *                 {@code "https://cdn.example.com/lib.min.js"}
 * @param order    sort order within the resource pipeline; lower numbers load first;
 *                 default is 100; use values below 100 to load before default resources
 *                 and above 100 to load after
 * @param position injection position in the HTML document; {@code "HEAD"} places the
 *                 resource in the {@code <head>} section (typical for CSS);
 *                 {@code "BODY_END"} places it just before {@code </body>}
 *                 (typical for JS)
 *
 * @see PageDefinition
 */
public record ResourceRef(

        /**
         * File path or CDN URL for the resource.
         *
         * <p>Relative paths are resolved against the {@code /static} directory.
         * Absolute URLs (starting with {@code http://} or {@code https://}) are
         * used as-is.</p>
         */
        String path,

        /**
         * Sort order for this resource in the rendering pipeline.
         *
         * <p>Resources are sorted by order ascending within their position group
         * (HEAD or BODY_END). Lower numbers are loaded before higher numbers.</p>
         */
        int order,

        /**
         * Injection position in the HTML document.
         *
         * <p>Valid values are {@code "HEAD"} for the {@code <head>} section and
         * {@code "BODY_END"} for just before {@code </body>}.</p>
         */
        String position

) {}

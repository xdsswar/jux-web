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

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A CMS-managed page stored in the database.
 *
 * <p>This is the top-level model for a dynamic page in the JUX CMS system.
 * Each page has a unique {@link #slug} that serves as its URL path, a
 * {@link #layout} reference for shared chrome (header, footer, navigation),
 * and per-locale {@link #content} containing the title, description, and
 * ordered widget tree for each supported language.</p>
 *
 * <p>Pages can be in a published or draft state. Published pages are publicly
 * accessible via the CMS catch-all route. Draft pages are only visible in the
 * admin panel preview.</p>
 *
 * <p><b>URL resolution:</b></p>
 * <ul>
 *   <li>The CMS home route serves the page whose slug matches the configured
 *       {@code jux.cms.home-slug} property at the root URL ({@code /})</li>
 *   <li>All other pages are served by the CMS catch-all route at
 *       {@code /{slug}}, with locale prefixing for {@code @Localized} routes
 *       (e.g. {@code /es/about})</li>
 *   <li>Consumer project {@code @Route}-annotated components take priority
 *       over CMS pages because the CMS routes have the lowest priority</li>
 * </ul>
 *
 * <p><b>Multi-language:</b> The {@link #content} map holds independent content
 * per locale. Use {@link #content(Locale, Locale)} to resolve content with
 * automatic fallback to the default locale when a translation is missing.</p>
 *
 * <p><b>Example database record:</b></p>
 * <pre>{@code
 * {
 *   "slug": "about",
 *   "layout": "DefaultLayout",
 *   "published": true,
 *   "updatedAt": "2026-02-06T12:00:00Z",
 *   "content": {
 *     "en": { "title": "About Us", "widgets": [...] },
 *     "es": { "title": "Sobre Nosotros", "widgets": [...] }
 *   },
 *   "css": [{ "path": "pages/about.css", "order": 50, "position": "HEAD" }],
 *   "js": []
 * }
 * }</pre>
 *
 * @param slug      unique URL slug for this page; used in routing as the path segment
 *                  (e.g. {@code "about"} maps to {@code /about}); must be unique across
 *                  all CMS pages; may contain slashes for nested paths
 *                  (e.g. {@code "services/web-design"})
 * @param layout    layout class name; resolved from the layout registry at render time;
 *                  examples: {@code "DefaultLayout"}, {@code "LandingLayout"},
 *                  {@code "AdminLayout"}; if empty or null, the default layout from
 *                  {@code jux.cms.default-layout} config is used
 * @param content   per-locale content map; each key is a {@link Locale} and each value
 *                  is the complete localized content for that language including title,
 *                  description, OG tags, and widget tree
 * @param css       page-level CSS resources loaded in addition to layout/theme CSS;
 *                  collected alongside annotation-declared resources and sorted by order
 * @param js        page-level JavaScript resources loaded in addition to layout/theme JS;
 *                  collected alongside annotation-declared resources and sorted by order
 * @param published whether this page is publicly accessible; {@code false} means the page
 *                  is a draft visible only in the admin panel preview
 * @param updatedAt last modification timestamp; used for cache invalidation, HTTP
 *                  {@code Last-Modified} headers, and sitemap generation
 *
 * @see LocalizedContent
 * @see ResourceRef
 * @see xss.it.jux.cms.service.PageService
 * @see xss.it.jux.cms.routing.CmsPageRoute
 */
public record PageDefinition(

        /**
         * Unique URL slug.
         *
         * <p>Serves as the URL path segment for this page. The CMS catch-all
         * route resolves incoming requests by matching the URL path against
         * stored slugs.</p>
         */
        String slug,

        /**
         * Layout class name.
         *
         * <p>Resolved from the layout registry at render time to wrap
         * the page content with shared chrome (header, nav, footer).</p>
         */
        String layout,

        /**
         * Per-locale content.
         *
         * <p>Each entry maps a locale to a complete set of localized content
         * including the page title, description, OG tags, and widget tree.</p>
         */
        Map<Locale, LocalizedContent> content,

        /**
         * Page-level CSS resources.
         *
         * <p>Loaded in addition to resources from the layout, theme, and
         * individual widgets.</p>
         */
        List<ResourceRef> css,

        /**
         * Page-level JavaScript resources.
         *
         * <p>Loaded in addition to resources from the layout, theme, and
         * individual widgets.</p>
         */
        List<ResourceRef> js,

        /**
         * Publication status.
         *
         * <p>{@code true} means the page is publicly accessible.
         * {@code false} means it is a draft visible only in the admin panel.</p>
         */
        boolean published,

        /**
         * Last modification timestamp.
         *
         * <p>Used for cache invalidation, sitemap {@code <lastmod>} elements,
         * and HTTP conditional request headers.</p>
         */
        Instant updatedAt

) {

    /**
     * Resolve localized content with automatic fallback.
     *
     * <p>Attempts to find content for the requested locale first. If no content
     * exists for that locale, falls back to the provided default locale. This
     * ensures that pages always render something even when a translation is
     * incomplete or missing.</p>
     *
     * <p><b>Fallback behavior:</b></p>
     * <ul>
     *   <li>If content exists for the requested locale, it is returned directly</li>
     *   <li>If not, the fallback locale's content is returned</li>
     *   <li>If neither exists, {@code null} is returned (should not happen in a
     *       well-configured system where the default locale always has content)</li>
     * </ul>
     *
     * @param locale   the desired locale, typically resolved from the URL prefix
     *                 or Accept-Language header
     * @param fallback the default locale to use if content is missing for the
     *                 requested locale; typically from {@code jux.i18n.default-locale}
     * @return the localized content for the best-matching locale, or {@code null}
     *         if no content exists for either locale
     */
    public LocalizedContent content(Locale locale, Locale fallback) {
        /*
         * Try the exact requested locale first. If no content is found,
         * fall back to the system default locale. This two-step lookup
         * mirrors the i18n fallback strategy used throughout JUX.
         */
        return content.getOrDefault(locale, content.get(fallback));
    }
}

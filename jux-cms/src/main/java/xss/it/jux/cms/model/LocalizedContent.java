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

import java.util.List;
import java.util.Map;

/**
 * Content for a single locale within a {@link PageDefinition}.
 *
 * <p>Each CMS page supports full multi-language content. Rather than simply
 * translating strings, each locale gets its own complete content definition:
 * a title, description, OpenGraph image, and an independently ordered list
 * of widget instances. This means different locales can have entirely different
 * widget arrangements if needed -- for example, a Spanish version might
 * include a region-specific testimonials section not present in the English version.</p>
 *
 * <p>This record is stored in the database as a value in the
 * {@link PageDefinition#content()} map, keyed by {@link java.util.Locale}.
 * The CMS admin panel provides a tabbed editor (one tab per locale) for
 * editing localized content.</p>
 *
 * <p><b>Rendering flow:</b></p>
 * <ol>
 *   <li>The CMS route component resolves the current locale from the request</li>
 *   <li>It calls {@link PageDefinition#content(java.util.Locale, java.util.Locale)}
 *       to get this record (with fallback to the default locale)</li>
 *   <li>The {@link #title} and {@link #description} are used to build
 *       {@link xss.it.jux.core.PageMeta}</li>
 *   <li>Each {@link WidgetInstance} in {@link #widgets} is rendered by
 *       {@link xss.it.jux.cms.service.WidgetRenderer}</li>
 * </ol>
 *
 * @param title       page title rendered in the {@code <title>} tag and used as
 *                    the default for {@code og:title}; should be concise and
 *                    descriptive (WCAG 2.4.2)
 * @param description meta description used for SEO and as the default for
 *                    {@code og:description}; should summarize the page content
 *                    in 150-160 characters
 * @param ogImage     OpenGraph image URL for social media link previews;
 *                    should be an absolute URL; {@code null} if no OG image is set
 * @param ogImageAlt  alt text for the OpenGraph image; required by ADA when
 *                    {@link #ogImage} is set (WCAG 1.1.1); {@code null} if no
 *                    OG image is set
 * @param widgets     ordered list of widget instances that make up the page body;
 *                    rendered top-to-bottom in the order they appear; an empty list
 *                    produces an empty page body
 * @param meta        additional {@code <meta>} tags specific to this locale;
 *                    keys are meta names (e.g. {@code "keywords"}, {@code "author"})
 *                    and values are the corresponding content; merged with
 *                    page-level and framework-level meta tags
 *
 * @see PageDefinition
 * @see WidgetInstance
 */
public record LocalizedContent(

        /**
         * Page title for this locale.
         *
         * <p>Rendered in the {@code <title>} tag. Used as the default
         * {@code og:title} if no explicit OG title is set.</p>
         */
        String title,

        /**
         * Meta description for this locale.
         *
         * <p>Used by search engines for snippet generation and as the
         * default {@code og:description}. Keep under 160 characters.</p>
         */
        String description,

        /**
         * OpenGraph image URL for social media previews.
         *
         * <p>Should be an absolute URL pointing to an image at least
         * 1200x630 pixels for optimal display on social platforms.</p>
         */
        String ogImage,

        /**
         * Alt text for the OpenGraph image.
         *
         * <p>Required by ADA when {@link #ogImage} is present.
         * Describes the image content for screen readers and
         * when the image fails to load.</p>
         */
        String ogImageAlt,

        /**
         * Ordered list of widget instances forming the page body.
         *
         * <p>Each widget is rendered in order by the
         * {@link xss.it.jux.cms.service.WidgetRenderer} and inserted
         * into the page's main content area.</p>
         */
        List<WidgetInstance> widgets,

        /**
         * Locale-specific meta tags.
         *
         * <p>Map of meta name to content value. These are merged with
         * page-level defaults and framework defaults. Locale-specific
         * values override page-level values for matching keys.</p>
         */
        Map<String, String> meta

) {}

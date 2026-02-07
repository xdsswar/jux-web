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

import java.util.Map;

/**
 * A single widget placed on a CMS-managed page.
 *
 * <p>This is the serialized form stored in the database. Each widget instance
 * represents one visual block on a page -- a hero banner, a text section, a
 * pricing table, a contact form, etc. Widget instances are ordered within a
 * {@link LocalizedContent}'s widget list and rendered top-to-bottom in that order.</p>
 *
 * <p>At render time, the {@link xss.it.jux.cms.service.WidgetRenderer} resolves
 * the {@link #type} against the {@link xss.it.jux.cms.widget.WidgetRegistry} to
 * find the corresponding {@link xss.it.jux.core.Component} class, instantiates it
 * via Spring DI, injects the {@link #props} into {@code @Prop}-annotated fields,
 * renders it, and wraps the output in a styled {@code <section>} using
 * {@link #style} and {@link #a11y}.</p>
 *
 * <p><b>Example database JSON:</b></p>
 * <pre>{@code
 * {
 *   "id": "hero-1",
 *   "type": "hero",
 *   "props": {
 *     "heading": "Welcome to Our Site",
 *     "subheading": "We build amazing things",
 *     "ctaText": "Get Started",
 *     "ctaUrl": "/contact",
 *     "backgroundImage": "/uploads/hero-bg.webp",
 *     "backgroundImageAlt": "Office workspace with laptops"
 *   },
 *   "style": {
 *     "background": { "type": "IMAGE", "imageUrl": "/uploads/hero-bg.webp" },
 *     "padding": "6rem 2rem",
 *     "minHeight": "80vh"
 *   },
 *   "a11y": { "ariaLabel": "Hero banner", "role": "banner" }
 * }
 * }</pre>
 *
 * @param id    unique identifier within the page; used for the DOM {@code id} attribute,
 *              anchor links (e.g. {@code /page#hero-1}), admin panel drag-drop targeting,
 *              and widget-specific CSS selectors; must be unique within the page
 * @param type  widget type key; must match a registered type in the
 *              {@link xss.it.jux.cms.widget.WidgetRegistry}; built-in types include
 *              {@code "hero"}, {@code "text"}, {@code "rich-text"}, {@code "image"},
 *              {@code "gallery"}, {@code "form"}, {@code "cta"}, {@code "cards"},
 *              {@code "pricing"}, {@code "testimonials"}, {@code "faq"}, {@code "video"},
 *              {@code "map"}, {@code "divider"}, {@code "spacer"}, {@code "html"},
 *              {@code "columns"}, {@code "tabs"}, {@code "accordion"}, {@code "stats"},
 *              {@code "team"}, {@code "logos"}, {@code "newsletter"}
 * @param props widget-specific properties; keys and expected types are defined by the
 *              widget's {@link xss.it.jux.cms.widget.PropSchema}; values are primitives,
 *              strings, lists, or nested maps; validated against the schema at save time
 *              in the admin panel
 * @param style visual styling applied as a wrapper around the widget; controls background,
 *              padding, margin, min-height, border radius, shadow, animation, and more;
 *              {@code null} means use the widget's default styling
 * @param a11y  per-instance accessibility overrides; {@code null} means use the widget's
 *              built-in ARIA defaults
 *
 * @see LocalizedContent
 * @see StyleConfig
 * @see A11yConfig
 * @see xss.it.jux.cms.widget.WidgetRegistry
 * @see xss.it.jux.cms.service.WidgetRenderer
 */
public record WidgetInstance(

        /**
         * Unique ID within the page.
         *
         * <p>Used for the wrapper element's {@code id} attribute, enabling anchor
         * links, admin panel targeting, and CSS scoping. Must be unique within
         * the page's widget list.</p>
         */
        String id,

        /**
         * Widget type key.
         *
         * <p>Resolved against the {@link xss.it.jux.cms.widget.WidgetRegistry}
         * at render time to find the Component class that renders this widget.</p>
         */
        String type,

        /**
         * Widget-specific properties map.
         *
         * <p>Keys correspond to {@code @Prop}-annotated fields on the widget
         * Component class. Values are deserialized from JSON and injected via
         * reflection during rendering.</p>
         */
        Map<String, Object> props,

        /**
         * Visual styling configuration for the widget's wrapper section.
         *
         * <p>Applied as inline CSS styles on the wrapping {@code <section>} element.
         * Null means no special styling beyond theme defaults.</p>
         */
        StyleConfig style,

        /**
         * Accessibility overrides for this specific widget instance.
         *
         * <p>Null means the widget uses its built-in ARIA attributes and roles.</p>
         */
        A11yConfig a11y

) {}

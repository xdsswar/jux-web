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
 * Per-widget accessibility overrides for CMS-managed content.
 *
 * <p>Every built-in JUX widget already ships with sensible ARIA defaults:
 * hero sections have {@code role="banner"}, navigation widgets have
 * {@code role="navigation"}, forms have proper labels, etc. This record
 * allows CMS editors to customize accessibility attributes for specific
 * widget instances -- useful when the same widget type appears multiple
 * times on a page and needs distinct screen reader labels.</p>
 *
 * <p>All fields are optional. A {@code null} value means "use the widget's
 * built-in default." Only set a field when you need to override the default
 * behavior for a particular widget instance.</p>
 *
 * <p><b>Common use cases:</b></p>
 * <ul>
 *   <li>Two hero sections on one page: label the first {@code "Main hero banner"}
 *       and the second {@code "Secondary promotion banner"}</li>
 *   <li>A Spanish quote widget on an English page: set {@code lang = "es"}
 *       to satisfy WCAG 3.1.2 (Language of Parts)</li>
 *   <li>A decorative divider: set {@code role = "presentation"} to exclude
 *       it from the accessibility tree</li>
 * </ul>
 *
 * <p><b>Example database JSON:</b></p>
 * <pre>{@code
 * {
 *   "ariaLabel": "Main hero banner",
 *   "role": "banner",
 *   "lang": "en"
 * }
 * }</pre>
 *
 * @param ariaLabel      custom {@code aria-label} for this widget instance; provides
 *                       a concise text label announced by screen readers; null means
 *                       the widget uses its built-in labelling strategy
 * @param ariaDescribedBy ID of another element that provides a detailed description
 *                       of this widget; rendered as {@code aria-describedby="..."} on
 *                       the wrapper element; null means no description reference
 * @param role           ARIA landmark role override; most widgets auto-detect their
 *                       role (e.g. hero maps to "banner", nav maps to "navigation");
 *                       set explicitly only when the default is wrong for a specific
 *                       use case; null means use the widget's default role
 * @param lang           BCP 47 language tag for this widget's content when it differs
 *                       from the page language; rendered as {@code lang="..."} on the
 *                       wrapper element; required by WCAG 3.1.2 (Language of Parts)
 *                       when content is in a different language than the page; null
 *                       means the widget inherits the page language
 *
 * @see xss.it.jux.cms.model.WidgetInstance
 * @see xss.it.jux.cms.service.WidgetRenderer
 */
public record A11yConfig(

        /**
         * Custom aria-label for this widget instance.
         *
         * <p>Overrides any built-in labelling. Announced by screen readers
         * when the user navigates to this widget's landmark region.</p>
         */
        String ariaLabel,

        /**
         * ID of the element that describes this widget in detail.
         *
         * <p>Rendered as {@code aria-describedby="..."} on the wrapper element.
         * The referenced element must exist on the same page with a matching
         * {@code id} attribute.</p>
         */
        String ariaDescribedBy,

        /**
         * ARIA landmark role override.
         *
         * <p>Common roles: {@code "banner"}, {@code "navigation"}, {@code "main"},
         * {@code "complementary"}, {@code "contentinfo"}, {@code "region"},
         * {@code "presentation"}.</p>
         */
        String role,

        /**
         * BCP 47 language tag for this widget's content.
         *
         * <p>Set when this widget's content is in a different language than
         * the page. Examples: {@code "es"}, {@code "fr"}, {@code "ar"},
         * {@code "zh-CN"}.</p>
         */
        String lang

) {}

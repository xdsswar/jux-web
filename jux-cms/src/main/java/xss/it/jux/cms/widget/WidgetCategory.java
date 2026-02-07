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

package xss.it.jux.cms.widget;

/**
 * Categories for organizing widgets in the CMS admin panel picker.
 *
 * <p>When a CMS editor adds a new widget to a page, the admin panel presents
 * a widget picker organized by these categories. Each category groups
 * semantically related widget types to help editors find the right widget
 * quickly.</p>
 *
 * <p>Every widget type registered in the {@link WidgetRegistry} must declare
 * a category. The {@link WidgetRegistry#allByCategory()} method returns all
 * registered widgets grouped by their category for the admin panel UI.</p>
 *
 * <p><b>Category display order in the admin panel follows the enum declaration
 * order:</b> Hero and Headers first, then Content, Media, Forms, etc. This
 * order mirrors the typical top-to-bottom flow of a landing page.</p>
 *
 * @see WidgetDefinition
 * @see WidgetRegistry
 */
public enum WidgetCategory {

    /**
     * Hero banners, full-width headers, and page intro sections.
     *
     * <p>Widgets in this category are typically placed at the very top
     * of a page: large hero banners with background images, page headers
     * with breadcrumbs, and feature showcase sections.</p>
     */
    HERO("Hero & Headers"),

    /**
     * Text blocks, rich text, markdown, and multi-column layouts.
     *
     * <p>Core content widgets for body text: plain text sections, rich text
     * with formatting, and multi-column layout containers.</p>
     */
    CONTENT("Content"),

    /**
     * Images, galleries, video embeds, and other media.
     *
     * <p>Widgets for visual and multimedia content: single images with
     * captions, image galleries (grid or carousel), video players,
     * and media embeds.</p>
     */
    MEDIA("Media"),

    /**
     * Contact forms, newsletter signup, and search widgets.
     *
     * <p>Interactive form widgets for user input: general-purpose forms
     * with configurable fields, newsletter email capture, and search
     * interfaces.</p>
     */
    FORMS("Forms"),

    /**
     * CTA buttons, pricing tables, and feature grids.
     *
     * <p>Conversion-focused widgets designed to drive user action: prominent
     * call-to-action sections, pricing plan comparisons, and feature/benefit
     * card grids.</p>
     */
    CONVERSION("Conversion"),

    /**
     * Testimonials, team bios, and client logos.
     *
     * <p>Social proof widgets that build trust: customer testimonials (cards
     * or carousel), team member profiles, and client/partner logo rows.</p>
     */
    SOCIAL_PROOF("Social Proof"),

    /**
     * Tabs, accordions, FAQ, and animated stat counters.
     *
     * <p>Interactive widgets that require client-side behavior: tabbed content
     * panels, expandable accordions/FAQ sections, and animated number counters.</p>
     */
    INTERACTIVE("Interactive"),

    /**
     * Maps, dividers, spacers, and raw HTML escape hatches.
     *
     * <p>Utility widgets for layout and miscellaneous content: embedded maps,
     * visual dividers between sections, vertical spacers, and raw HTML blocks
     * for custom content that does not fit any other widget type.</p>
     */
    UTILITY("Utility"),

    /**
     * Navigation elements, breadcrumbs, footers, and sidebars.
     *
     * <p>Structural navigation widgets: breadcrumb trails, secondary navigation
     * menus, footer sections, and sidebar content blocks.</p>
     */
    NAVIGATION("Navigation");

    /** Human-readable label displayed in the admin panel widget picker. */
    private final String label;

    /**
     * Construct a category with its display label.
     *
     * @param label the human-readable name shown in the admin panel
     */
    WidgetCategory(String label) {
        this.label = label;
    }

    /**
     * Returns the human-readable label for this category.
     *
     * <p>Used by the admin panel to display category headings in the
     * widget picker dialog.</p>
     *
     * @return the display label, never null
     */
    public String label() {
        return label;
    }
}

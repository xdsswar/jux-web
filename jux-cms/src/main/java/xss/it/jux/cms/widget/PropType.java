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
 * Enumerates the data types supported by widget prop fields.
 *
 * <p>Each prop type maps to a specific form control in the CMS admin panel
 * and determines how the prop value is validated, serialized, and injected
 * into the widget component's {@code @Prop}-annotated fields.</p>
 *
 * <p>The admin panel auto-generates an editing form for each widget based on
 * its {@link PropSchema}. Each {@link PropField} declares a {@code PropType}
 * that controls which form control is rendered:</p>
 * <ul>
 *   <li>Text types ({@link #STRING}, {@link #TEXT}, {@link #RICH_TEXT}) render
 *       text inputs, textareas, or WYSIWYG editors</li>
 *   <li>Numeric types ({@link #INT}, {@link #DOUBLE}) render number inputs
 *       with min/max validation</li>
 *   <li>Media types ({@link #IMAGE}, {@link #VIDEO}) open the media library picker</li>
 *   <li>Choice types ({@link #ENUM}, {@link #BOOLEAN}) render dropdowns or toggles</li>
 *   <li>Composite types ({@link #LIST}, {@link #OBJECT}) render nested editors
 *       with add/remove/reorder controls</li>
 * </ul>
 *
 * @see PropField
 * @see PropSchema
 */
public enum PropType {

    /**
     * Single-line text input.
     *
     * <p>Renders as an {@code <input type="text">} in the admin panel.
     * Suitable for short text values: headings, labels, button text.</p>
     */
    STRING,

    /**
     * Multi-line text area.
     *
     * <p>Renders as a {@code <textarea>} in the admin panel.
     * Suitable for longer plain text: descriptions, paragraphs, bios.</p>
     */
    TEXT,

    /**
     * Rich text editor producing sanitized HTML.
     *
     * <p>Renders a WYSIWYG editor in the admin panel. The output is
     * sanitized server-side to prevent XSS. Supports headings, bold,
     * italic, links, lists, images, and blockquotes.</p>
     */
    RICH_TEXT,

    /**
     * Integer numeric input.
     *
     * <p>Renders as {@code <input type="number">} with step=1.
     * Suitable for counts, quantities, column numbers, indices.</p>
     */
    INT,

    /**
     * Decimal numeric input.
     *
     * <p>Renders as {@code <input type="number">} with decimal step.
     * Suitable for prices, percentages, measurements.</p>
     */
    DOUBLE,

    /**
     * Boolean toggle switch.
     *
     * <p>Renders as a toggle/checkbox in the admin panel.
     * Suitable for on/off flags: autoplay, show/hide, enabled/disabled.</p>
     */
    BOOLEAN,

    /**
     * URL input with validation.
     *
     * <p>Renders as {@code <input type="url">} with URL format validation.
     * Suitable for links, CTA destinations, external resource references.</p>
     */
    URL,

    /**
     * Email input with validation.
     *
     * <p>Renders as {@code <input type="email">} with email format validation.
     * Suitable for contact emails, notification recipients.</p>
     */
    EMAIL,

    /**
     * Image picker.
     *
     * <p>Opens the CMS media library for image selection. Returns the
     * selected image path. Supports upload, browse, and search.
     * The admin panel shows a thumbnail preview of the selected image.</p>
     */
    IMAGE,

    /**
     * Video picker.
     *
     * <p>Opens the CMS media library or a URL input for video selection.
     * Supports self-hosted videos, YouTube URLs, and Vimeo URLs.</p>
     */
    VIDEO,

    /**
     * Color picker.
     *
     * <p>Renders a color picker control in the admin panel. Returns a
     * CSS color string: hex ({@code "#ff0000"}), rgba ({@code "rgba(255,0,0,0.8)"}),
     * or named color.</p>
     */
    COLOR,

    /**
     * Dropdown select from a predefined list of values.
     *
     * <p>Renders as a {@code <select>} dropdown. The allowed values are defined
     * in the {@link PropField#enumValues()} list. Suitable for alignment choices,
     * layout modes, style variants.</p>
     */
    ENUM,

    /**
     * Date picker.
     *
     * <p>Renders a date picker control. Returns an ISO 8601 date string
     * (e.g. {@code "2026-02-06"}). Suitable for event dates, publication dates.</p>
     */
    DATE,

    /**
     * Ordered list of items.
     *
     * <p>Renders an add/remove/reorder UI in the admin panel. Each list item
     * can contain sub-fields (defined by nested schema). Suitable for
     * testimonial lists, feature lists, image galleries, FAQ items.</p>
     */
    LIST,

    /**
     * Nested object with sub-fields.
     *
     * <p>Renders a sub-form in the admin panel with its own fields.
     * Suitable for complex structured data like background configuration,
     * author information, or address blocks.</p>
     */
    OBJECT,

    /**
     * Icon picker from the built-in icon set.
     *
     * <p>Renders an icon browser in the admin panel. Returns the icon
     * identifier string. The framework provides a set of commonly used
     * icons for UI elements.</p>
     */
    ICON
}

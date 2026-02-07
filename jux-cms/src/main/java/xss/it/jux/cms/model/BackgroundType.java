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
 * Enumerates the supported background modes for CMS widget sections.
 *
 * <p>Each CMS widget can be wrapped in a styled container with a configurable
 * background. This enum determines how the background is rendered in the HTML
 * output. The {@link BackgroundConfig} record uses this type to select the
 * appropriate rendering strategy.</p>
 *
 * <p><b>Rendering behavior by type:</b></p>
 * <ul>
 *   <li>{@link #SOLID} — a single CSS {@code background-color} property</li>
 *   <li>{@link #GRADIENT} — a full CSS gradient string via {@code background}</li>
 *   <li>{@link #IMAGE} — a {@code background-image} with optional color overlay</li>
 *   <li>{@link #VIDEO} — a muted autoplay {@code <video>} element behind the content</li>
 * </ul>
 *
 * @see BackgroundConfig
 * @see StyleConfig
 */
public enum BackgroundType {

    /**
     * Solid color background.
     *
     * <p>Renders as a single {@code background-color} CSS property.
     * The color value is taken from {@link BackgroundConfig#color()}.
     * Accepts any valid CSS color: hex ({@code #ff0000}), rgb ({@code rgb(255,0,0)}),
     * hsl ({@code hsl(0,100%,50%)}), or named colors ({@code red}).</p>
     */
    SOLID,

    /**
     * CSS gradient background.
     *
     * <p>Renders as a {@code background} CSS property using the full gradient
     * string from {@link BackgroundConfig#gradientCss()}. Supports linear,
     * radial, and conic gradients.</p>
     *
     * <p>Example gradient strings:</p>
     * <ul>
     *   <li>{@code "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"}</li>
     *   <li>{@code "radial-gradient(circle at center, #fff 0%, #000 100%)"}</li>
     * </ul>
     */
    GRADIENT,

    /**
     * Background image with optional color overlay.
     *
     * <p>Renders as {@code background-image: url('...')} with configurable
     * {@code background-size}, {@code background-position}, and optional
     * {@code background-attachment: fixed} for parallax scrolling.
     * An overlay color can be applied via a CSS pseudo-element.</p>
     *
     * <p>ADA note: if the background image conveys meaning, the
     * {@link BackgroundConfig#imageAlt()} field must contain descriptive
     * alt text. For purely decorative backgrounds, set alt to an empty string.</p>
     */
    IMAGE,

    /**
     * Background video (muted, autoplay, loop).
     *
     * <p>Renders as a {@code <video>} element positioned absolutely behind
     * the widget content. The video plays silently on loop. A poster image
     * is used as a fallback for browsers that do not autoplay video or for
     * users with {@code prefers-reduced-motion: reduce}.</p>
     *
     * <p>ADA note: background videos must not contain essential information.
     * They are treated as decorative and hidden from assistive technology
     * via {@code aria-hidden="true"}.</p>
     */
    VIDEO
}

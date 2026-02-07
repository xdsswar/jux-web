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
 * Configuration for a widget section's background rendering.
 *
 * <p>Supports four distinct background modes selected by {@link #type}:</p>
 * <ul>
 *   <li>{@link BackgroundType#SOLID} — uses {@link #color} as {@code background-color}</li>
 *   <li>{@link BackgroundType#GRADIENT} — uses {@link #gradientCss} as the full
 *       {@code background} CSS value</li>
 *   <li>{@link BackgroundType#IMAGE} — uses {@link #imageUrl} with optional overlay
 *       from {@link #color}, parallax via {@link #parallax}, and sizing/positioning
 *       via {@link #imageSize} and {@link #imagePosition}</li>
 *   <li>{@link BackgroundType#VIDEO} — uses {@link #videoUrl} for a muted autoplay
 *       background video, with {@link #imageUrl} as a poster/fallback frame</li>
 * </ul>
 *
 * <p>This record is immutable and used as part of {@link StyleConfig} to define
 * the visual wrapper around any CMS widget instance. It is stored in the database
 * as a serialized JSON object within the widget instance's style configuration.</p>
 *
 * <p><b>ADA considerations:</b></p>
 * <ul>
 *   <li>Background images that convey meaning must have {@link #imageAlt} set
 *       with descriptive text (WCAG 1.1.1)</li>
 *   <li>Decorative backgrounds should have {@link #imageAlt} set to an empty string</li>
 *   <li>Background videos are always marked {@code aria-hidden="true"} since they
 *       must not contain essential information</li>
 * </ul>
 *
 * @param type          the background rendering mode; determines which other fields are used
 * @param color         for {@link BackgroundType#SOLID}: the background color (e.g. "#ffffff");
 *                      for {@link BackgroundType#IMAGE}/{@link BackgroundType#VIDEO}: the overlay
 *                      color (e.g. "rgba(0,0,0,0.6)"); ignored for {@link BackgroundType#GRADIENT}
 * @param gradientCss   full CSS gradient string, only used when {@code type} is
 *                      {@link BackgroundType#GRADIENT}; example:
 *                      {@code "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"}
 * @param imageUrl      image URL for background, used when {@code type} is
 *                      {@link BackgroundType#IMAGE}; also serves as the poster frame
 *                      when {@code type} is {@link BackgroundType#VIDEO}; supports
 *                      relative paths ({@code "/uploads/hero.webp"}) or absolute CDN URLs
 * @param imageAlt      alt text for the background image; required by ADA if the image
 *                      conveys meaning; set to {@code ""} for purely decorative backgrounds
 * @param imageSize     CSS {@code background-size} value; defaults to {@code "cover"} if
 *                      null; common values: {@code "cover"}, {@code "contain"}, {@code "auto"}
 * @param imagePosition CSS {@code background-position} value; defaults to
 *                      {@code "center center"} if null; examples: {@code "center top"},
 *                      {@code "50% 30%"}, {@code "left bottom"}
 * @param parallax      whether the background image should use parallax scrolling;
 *                      if {@code true}, renders with {@code background-attachment: fixed}
 * @param videoUrl      video URL for background, only used when {@code type} is
 *                      {@link BackgroundType#VIDEO}; rendered as a muted autoplay looping
 *                      {@code <video>} behind the content
 *
 * @see BackgroundType
 * @see StyleConfig
 */
public record BackgroundConfig(

        /** The background rendering mode. Determines which other fields are meaningful. */
        BackgroundType type,

        /**
         * Color value with context-dependent meaning.
         *
         * <ul>
         *   <li>For SOLID: the sole background color (e.g. "#ffffff", "rgb(24,24,27)")</li>
         *   <li>For IMAGE/VIDEO: the overlay color applied on top of the media
         *       (e.g. "rgba(0,0,0,0.6)" for a dark overlay)</li>
         *   <li>For GRADIENT: ignored; use {@link #gradientCss} instead</li>
         * </ul>
         */
        String color,

        /**
         * Full CSS gradient string for {@link BackgroundType#GRADIENT} backgrounds.
         *
         * <p>Must be a complete, valid CSS gradient expression including the function
         * name. Example: {@code "linear-gradient(135deg, #667eea 0%, #764ba2 100%)"}.</p>
         */
        String gradientCss,

        /**
         * Image URL for {@link BackgroundType#IMAGE} backgrounds.
         *
         * <p>Also serves as the poster frame for {@link BackgroundType#VIDEO} backgrounds.
         * Can be a relative path (resolved against {@code /static}) or an absolute CDN URL.</p>
         */
        String imageUrl,

        /**
         * Alt text for the background image (ADA compliance).
         *
         * <p>Required by WCAG 1.1.1 when the image conveys information.
         * Set to an empty string ({@code ""}) for purely decorative backgrounds.</p>
         */
        String imageAlt,

        /**
         * CSS {@code background-size} value.
         *
         * <p>Common values: {@code "cover"} (fill container, crop if needed),
         * {@code "contain"} (fit inside, may leave gaps), {@code "auto"} (natural size).
         * Null defaults to {@code "cover"} at render time.</p>
         */
        String imageSize,

        /**
         * CSS {@code background-position} value.
         *
         * <p>Controls the focal point of the background image. Null defaults to
         * {@code "center center"} at render time. Examples: {@code "center top"},
         * {@code "50% 30%"}, {@code "left bottom"}.</p>
         */
        String imagePosition,

        /**
         * Whether the background image uses parallax scrolling.
         *
         * <p>When {@code true}, the widget renderer applies
         * {@code background-attachment: fixed}, creating a parallax scroll effect
         * where the background appears to move more slowly than the foreground content.</p>
         */
        boolean parallax,

        /**
         * Video URL for {@link BackgroundType#VIDEO} backgrounds.
         *
         * <p>The video is rendered as a muted, autoplay, looping {@code <video>}
         * element positioned behind the widget content. The {@link #imageUrl} field
         * serves as a poster fallback.</p>
         */
        String videoUrl

) {}

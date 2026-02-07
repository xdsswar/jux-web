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
 * Visual styling wrapper configuration for any CMS widget instance.
 *
 * <p>When a widget is rendered, it is wrapped in a {@code <section>} container element.
 * This record defines the visual properties of that wrapper: background, spacing, sizing,
 * effects, and animation. All fields are optional -- {@code null} or zero values mean
 * "use the widget's default styling" or "inherit from the theme."</p>
 *
 * <p>The CMS admin panel provides a graphical style editor (background picker, padding
 * sliders, overlay controls, animation selector) that populates this record. It is
 * serialized as JSON in the database alongside the widget instance data.</p>
 *
 * <p><b>Rendering:</b> The {@code WidgetRenderer} converts these fields into inline
 * CSS styles and CSS classes on the wrapper {@code <section>} element. This approach
 * ensures that CMS-configured styles are applied without external stylesheet changes.</p>
 *
 * <p><b>Example database JSON:</b></p>
 * <pre>{@code
 * {
 *   "background": { "type": "GRADIENT", "gradientCss": "linear-gradient(135deg, #667eea, #764ba2)" },
 *   "opacity": 1.0,
 *   "blur": 0,
 *   "padding": "6rem 2rem",
 *   "minHeight": "80vh",
 *   "textAlign": "center"
 * }
 * }</pre>
 *
 * @param background   background configuration (color, gradient, image, or video);
 *                     {@code null} means transparent / inherit from theme
 * @param opacity      CSS opacity from 0.0 (fully transparent) to 1.0 (fully opaque);
 *                     1.0 is the default and means no opacity override is applied
 * @param blur         CSS backdrop blur filter in pixels; 0 means no blur;
 *                     used for glassmorphism effects (e.g. {@code backdrop-filter: blur(8px)})
 * @param borderRadius CSS {@code border-radius} value; examples: {@code "0"}, {@code "8px"},
 *                     {@code "50%"}, {@code "1rem"}; {@code null} means no border radius
 * @param padding      CSS {@code padding} value; examples: {@code "2rem"},
 *                     {@code "4rem 2rem"}, {@code "60px 40px"}; {@code null} means no override
 * @param margin       CSS {@code margin} value; examples: {@code "0"},
 *                     {@code "2rem auto"}, {@code "0 0 4rem 0"}; {@code null} means no override
 * @param minHeight    CSS {@code min-height} value; useful for hero sections;
 *                     examples: {@code "auto"}, {@code "80vh"}, {@code "600px"}, {@code "100vh"};
 *                     {@code null} means auto-sized to content
 * @param shadow       CSS {@code box-shadow} value; example:
 *                     {@code "0 4px 24px rgba(0,0,0,0.12)"}; {@code null} means no shadow
 * @param animation    CSS animation class name appended as {@code jux-animate-{animation}};
 *                     framework provides built-in animations: {@code "fade-in"},
 *                     {@code "slide-up"}, {@code "slide-left"}, {@code "zoom-in"},
 *                     {@code "parallax"}; {@code null} means no animation
 * @param blendMode    CSS {@code mix-blend-mode} value; examples: {@code "multiply"},
 *                     {@code "overlay"}, {@code "screen"}; {@code null} means normal blending
 * @param maxWidth     CSS {@code max-width} constraint; examples: {@code "1200px"},
 *                     {@code "100%"}, {@code "80rem"}; {@code null} means no constraint
 * @param textAlign    CSS {@code text-align} override; values: {@code "left"},
 *                     {@code "center"}, {@code "right"}; {@code null} means inherit
 *
 * @see BackgroundConfig
 * @see xss.it.jux.cms.model.WidgetInstance
 * @see xss.it.jux.cms.service.WidgetRenderer
 */
public record StyleConfig(

        /** Background configuration. Null means transparent / inherit from theme. */
        BackgroundConfig background,

        /**
         * CSS opacity from 0.0 (fully transparent) to 1.0 (fully opaque).
         * A value of 1.0 indicates no opacity override should be applied.
         */
        double opacity,

        /**
         * CSS backdrop blur filter in pixels. Zero means no blur effect.
         * Positive values create a glassmorphism frosted-glass appearance.
         */
        int blur,

        /** CSS border-radius value. Null means no border radius. */
        String borderRadius,

        /** CSS padding value. Null means no padding override from theme defaults. */
        String padding,

        /** CSS margin value. Null means no margin override from theme defaults. */
        String margin,

        /** CSS min-height value. Null means the section sizes to its content. */
        String minHeight,

        /** CSS box-shadow value. Null means no shadow effect. */
        String shadow,

        /**
         * Animation class name. Appended to the wrapper element as {@code jux-animate-{name}}.
         * Null means no entry/scroll animation is applied.
         */
        String animation,

        /** CSS mix-blend-mode value. Null means normal blending. */
        String blendMode,

        /** CSS max-width constraint. Null means no width constraint. */
        String maxWidth,

        /** CSS text-align override. Null means text alignment is inherited. */
        String textAlign

) {}

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

package xss.it.jux.theme;

import java.util.Objects;

/**
 * CSS custom properties (design tokens) for the JUX theme system.
 *
 * <p>Design tokens are the smallest units of a design system -- named values
 * that encode visual design decisions (colors, spacing, typography, shadows).
 * This class provides two categories of functionality:</p>
 *
 * <ol>
 *   <li><b>Token reference methods</b> -- static methods that return CSS
 *       {@code var(--jux-*)} references for use in inline styles or
 *       generated CSS. These are compile-time safe references to tokens
 *       that will be resolved at runtime by the browser.</li>
 *   <li><b>Token generation</b> -- the {@link #generateCssVariables(ThemeProperties)}
 *       method that produces a complete {@code :root { ... }} CSS block
 *       defining all token values based on the active theme configuration.</li>
 * </ol>
 *
 * <p><b>Token naming convention:</b> All tokens use the {@code --jux-} prefix
 * to avoid collisions with consumer project CSS. Token names use kebab-case
 * following the CSS custom property convention.</p>
 *
 * <p><b>WCAG compliance:</b> Default token values are chosen to satisfy
 * WCAG 2.2 AA contrast requirements (4.5:1 for normal text, 3:1 for large
 * text). The dark mode palette is independently validated for contrast.</p>
 *
 * <p><b>Usage in components:</b></p>
 * <pre>{@code
 * // In a Component's render() method:
 * div().style("color", DesignTokens.textColor())
 *      .style("background", DesignTokens.backgroundColor())
 *      .style("padding", DesignTokens.spacing(4))
 *      .style("box-shadow", DesignTokens.shadow("md"))
 *      .style("border-radius", DesignTokens.borderRadius())
 * }</pre>
 *
 * @see ThemeProperties
 */
public final class DesignTokens {

    /**
     * Common prefix for all JUX CSS custom properties.
     *
     * <p>Using a shared prefix ensures no collisions with consumer
     * project CSS variables or third-party library tokens.</p>
     */
    private static final String PREFIX = "--jux-";

    /** Private constructor -- this is a utility class with only static methods. */
    private DesignTokens() {
        // Prevent instantiation of utility class.
    }

    // ═══════════════════════════════════════════════════════════════
    //  TOKEN REFERENCE METHODS
    //
    //  Each method returns a CSS var() reference string. These are
    //  used in component render() methods and theme CSS generation
    //  to reference tokens without hard-coding values.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Returns a CSS variable reference for the primary brand color.
     *
     * <p>The primary color is used for interactive elements (links, buttons,
     * active states), primary backgrounds, and accent decorations. It is
     * the most prominent color in the design system.</p>
     *
     * @return {@code "var(--jux-primary)"} -- the CSS variable reference
     */
    public static String primaryColor() {
        return "var(" + PREFIX + "primary)";
    }

    /**
     * Returns a CSS variable reference for the secondary/accent color.
     *
     * <p>The secondary color complements the primary and is used for
     * secondary actions, hover states, badges, and subtle accents.
     * In the default theme, this is a muted gray-blue that pairs
     * well with the primary blue.</p>
     *
     * @return {@code "var(--jux-secondary)"} -- the CSS variable reference
     */
    public static String secondaryColor() {
        return "var(" + PREFIX + "secondary)";
    }

    /**
     * Returns a CSS variable reference for the page background color.
     *
     * <p>This is the base background color applied to the {@code <body>}
     * element. All other backgrounds are layered on top of this. In light
     * mode, this is white; in dark mode, a near-black gray.</p>
     *
     * @return {@code "var(--jux-bg)"} -- the CSS variable reference
     */
    public static String backgroundColor() {
        return "var(" + PREFIX + "bg)";
    }

    /**
     * Returns a CSS variable reference for the primary text color.
     *
     * <p>This color is used for body text, headings, and any content
     * text that should have maximum readability. It is guaranteed to
     * meet WCAG 2.2 AA contrast requirements against the background
     * color in both light and dark modes.</p>
     *
     * @return {@code "var(--jux-text)"} -- the CSS variable reference
     */
    public static String textColor() {
        return "var(" + PREFIX + "text)";
    }

    /**
     * Returns a CSS variable reference for the primary font family stack.
     *
     * <p>This token controls the typeface for all body text, headings,
     * form inputs, and buttons. Heading-specific overrides can be set
     * via {@code --jux-font-heading} if a different display face is desired.</p>
     *
     * @return {@code "var(--jux-font-family)"} -- the CSS variable reference
     */
    public static String fontFamily() {
        return "var(" + PREFIX + "font-family)";
    }

    /**
     * Returns a CSS variable reference for the default border radius.
     *
     * <p>Applied to buttons, cards, inputs, dropdowns, and other
     * contained elements. The default value produces subtly rounded
     * corners (0.375rem / 6px at default font size).</p>
     *
     * @return {@code "var(--jux-radius)"} -- the CSS variable reference
     */
    public static String borderRadius() {
        return "var(" + PREFIX + "radius)";
    }

    /**
     * Returns a CSS variable reference for a spacing level.
     *
     * <p>The spacing scale follows a consistent multiplier pattern
     * (base unit * level). This ensures uniform rhythm throughout the
     * UI. Available levels and their default values:</p>
     *
     * <table>
     *   <caption>Spacing scale</caption>
     *   <tr><th>Level</th><th>Token</th><th>Default Value</th></tr>
     *   <tr><td>1</td><td>--jux-space-1</td><td>0.25rem (4px)</td></tr>
     *   <tr><td>2</td><td>--jux-space-2</td><td>0.5rem (8px)</td></tr>
     *   <tr><td>3</td><td>--jux-space-3</td><td>0.75rem (12px)</td></tr>
     *   <tr><td>4</td><td>--jux-space-4</td><td>1rem (16px)</td></tr>
     *   <tr><td>5</td><td>--jux-space-5</td><td>1.5rem (24px)</td></tr>
     *   <tr><td>6</td><td>--jux-space-6</td><td>2rem (32px)</td></tr>
     *   <tr><td>7</td><td>--jux-space-7</td><td>3rem (48px)</td></tr>
     *   <tr><td>8</td><td>--jux-space-8</td><td>4rem (64px)</td></tr>
     * </table>
     *
     * @param level the spacing level (1 through 8, inclusive)
     * @return the CSS variable reference for the requested spacing level
     * @throws IllegalArgumentException if level is outside the 1-8 range
     */
    public static String spacing(int level) {
        if (level < 1 || level > 8) {
            throw new IllegalArgumentException(
                    "Spacing level must be between 1 and 8, got: " + level);
        }
        return "var(" + PREFIX + "space-" + level + ")";
    }

    /**
     * Returns a CSS variable reference for a box shadow of the given size.
     *
     * <p>The shadow scale provides elevation cues for layered UI elements
     * (cards, dropdowns, modals). Larger shadows suggest higher elevation.
     * Available sizes:</p>
     *
     * <ul>
     *   <li>{@code "sm"} -- subtle shadow for cards and contained elements</li>
     *   <li>{@code "md"} -- medium shadow for dropdowns and popovers</li>
     *   <li>{@code "lg"} -- prominent shadow for modals and overlays</li>
     * </ul>
     *
     * @param size the shadow size: "sm", "md", or "lg"
     * @return the CSS variable reference for the requested shadow
     * @throws NullPointerException     if size is null
     * @throws IllegalArgumentException if size is not one of "sm", "md", "lg"
     */
    public static String shadow(String size) {
        Objects.requireNonNull(size, "shadow size must not be null");
        return switch (size) {
            case "sm", "md", "lg" -> "var(" + PREFIX + "shadow-" + size + ")";
            default -> throw new IllegalArgumentException(
                    "Shadow size must be 'sm', 'md', or 'lg', got: '" + size + "'");
        };
    }

    /**
     * Returns a CSS variable reference for the focus ring style.
     *
     * <p>The focus ring is the visible outline applied to focused interactive
     * elements (buttons, links, inputs). It is critical for keyboard
     * accessibility (WCAG 2.4.7 -- Focus Visible). The default value
     * produces a 2px solid ring using the primary color with an offset,
     * ensuring it is visible against any background.</p>
     *
     * <p>This token is used as a complete {@code outline} CSS value:</p>
     * <pre>{@code
     * button().style("outline", DesignTokens.focusRing())
     * }</pre>
     *
     * @return {@code "var(--jux-focus-ring)"} -- the CSS variable reference
     */
    public static String focusRing() {
        return "var(" + PREFIX + "focus-ring)";
    }

    // ═══════════════════════════════════════════════════════════════
    //  CSS GENERATION
    //
    //  Produces the complete :root { ... } block that defines all
    //  token values. This CSS is injected into every page's <head>.
    // ═══════════════════════════════════════════════════════════════

    /**
     * Generates a complete CSS block defining all design token values
     * based on the provided theme properties.
     *
     * <p>The generated CSS includes both light and dark mode tokens.
     * Light mode tokens are placed in {@code :root} (default), and dark
     * mode overrides are placed in {@code [data-theme="dark"]}. This
     * enables instant theme switching by swapping the {@code data-theme}
     * attribute on the {@code <html>} element -- no page reload needed.</p>
     *
     * <p>The generated CSS includes:</p>
     * <ul>
     *   <li>Color tokens (primary, secondary, background, text, surface, border)</li>
     *   <li>Typography tokens (font family, font sizes)</li>
     *   <li>Spacing scale (8 levels from 0.25rem to 4rem)</li>
     *   <li>Border radius</li>
     *   <li>Shadow scale (sm, md, lg) -- adjusted per mode</li>
     *   <li>Focus ring style</li>
     *   <li>Transition duration</li>
     * </ul>
     *
     * <p><b>Example output:</b></p>
     * <pre>{@code
     * :root {
     *   --jux-primary: #3b82f6;
     *   --jux-bg: #ffffff;
     *   --jux-text: #0f172a;
     *   ...
     * }
     * [data-theme="dark"] {
     *   --jux-secondary: #94a3b8;
     *   --jux-bg: #0f172a;
     *   --jux-text: #f1f5f9;
     *   ...
     * }
     * }</pre>
     *
     * @param props the theme properties to generate tokens from; must not be null
     * @return a complete CSS string containing both light and dark token blocks
     * @throws NullPointerException if props is null
     */
    public static String generateCssVariables(ThemeProperties props) {
        Objects.requireNonNull(props, "ThemeProperties must not be null");

        var sb = new StringBuilder();

        // ── Light mode (:root) ─────────────────────────────────
        sb.append(":root {\n");
        appendColorTokens(sb, props, false);
        appendTypographyTokens(sb, props);
        appendSpacingTokens(sb);
        appendShadowTokens(sb, false);
        appendFocusAndTransition(sb, props);
        sb.append("}\n");

        // ── Dark mode override ─────────────────────────────────
        // Only overrides color-sensitive tokens. Typography, spacing,
        // radius, and transition remain unchanged between modes.
        sb.append("[data-theme=\"dark\"] {\n");
        appendColorTokens(sb, props, true);
        appendShadowTokens(sb, true);
        sb.append("}\n");

        return sb.toString();
    }

    /**
     * Generates a single-mode CSS block (legacy method for backwards compatibility).
     *
     * <p>When {@link ThemeProperties#isDarkMode()} is {@code true}, generates
     * dark mode tokens in a single {@code :root} block. Use
     * {@link #generateCssVariables(ThemeProperties)} instead for dual-mode
     * CSS that supports instant theme switching.</p>
     *
     * @param props    the theme properties
     * @param darkMode whether to generate dark mode tokens
     * @return a CSS string containing a single :root block
     */
    public static String generateCssVariablesSingleMode(ThemeProperties props, boolean darkMode) {
        Objects.requireNonNull(props, "ThemeProperties must not be null");

        var sb = new StringBuilder();
        sb.append(":root {\n");
        appendColorTokens(sb, props, darkMode);
        appendTypographyTokens(sb, props);
        appendSpacingTokens(sb);
        appendShadowTokens(sb, darkMode);
        appendFocusAndTransition(sb, props);
        sb.append("}\n");
        return sb.toString();
    }

    // ── Internal helpers ──────────────────────────────────────────

    private static void appendColorTokens(StringBuilder sb, ThemeProperties props, boolean dark) {
        sb.append("  ").append(PREFIX).append("primary: ")
                .append(props.getPrimaryColor()).append(";\n");
        sb.append("  ").append(PREFIX).append("secondary: ")
                .append(dark ? "#94a3b8" : "#64748b").append(";\n");
        sb.append("  ").append(PREFIX).append("bg: ")
                .append(dark ? "#0f172a" : "#ffffff").append(";\n");
        sb.append("  ").append(PREFIX).append("text: ")
                .append(dark ? "#f1f5f9" : "#0f172a").append(";\n");
        sb.append("  ").append(PREFIX).append("surface: ")
                .append(dark ? "#1e293b" : "#f8fafc").append(";\n");
        sb.append("  ").append(PREFIX).append("border: ")
                .append(dark ? "#334155" : "#e2e8f0").append(";\n");
    }

    private static void appendTypographyTokens(StringBuilder sb, ThemeProperties props) {
        sb.append("  ").append(PREFIX).append("font-family: ")
                .append(props.getFontFamily()).append(";\n");
        sb.append("  ").append(PREFIX).append("font-size-base: 1rem;\n");
        sb.append("  ").append(PREFIX).append("font-size-sm: 0.875rem;\n");
        sb.append("  ").append(PREFIX).append("font-size-lg: 1.125rem;\n");
        sb.append("  ").append(PREFIX).append("font-size-xl: 1.25rem;\n");
        sb.append("  ").append(PREFIX).append("font-size-2xl: 1.5rem;\n");
        sb.append("  ").append(PREFIX).append("font-size-3xl: 2rem;\n");
    }

    private static void appendSpacingTokens(StringBuilder sb) {
        sb.append("  ").append(PREFIX).append("space-1: 0.25rem;\n");
        sb.append("  ").append(PREFIX).append("space-2: 0.5rem;\n");
        sb.append("  ").append(PREFIX).append("space-3: 0.75rem;\n");
        sb.append("  ").append(PREFIX).append("space-4: 1rem;\n");
        sb.append("  ").append(PREFIX).append("space-5: 1.5rem;\n");
        sb.append("  ").append(PREFIX).append("space-6: 2rem;\n");
        sb.append("  ").append(PREFIX).append("space-7: 3rem;\n");
        sb.append("  ").append(PREFIX).append("space-8: 4rem;\n");
        sb.append("  ").append(PREFIX).append("radius: 0.375rem;\n");
    }

    private static void appendShadowTokens(StringBuilder sb, boolean dark) {
        if (dark) {
            sb.append("  ").append(PREFIX).append("shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.3);\n");
            sb.append("  ").append(PREFIX).append("shadow-md: 0 4px 6px rgba(0, 0, 0, 0.4);\n");
            sb.append("  ").append(PREFIX).append("shadow-lg: 0 10px 25px rgba(0, 0, 0, 0.5);\n");
        } else {
            sb.append("  ").append(PREFIX).append("shadow-sm: 0 1px 2px rgba(0, 0, 0, 0.05);\n");
            sb.append("  ").append(PREFIX).append("shadow-md: 0 4px 6px rgba(0, 0, 0, 0.1);\n");
            sb.append("  ").append(PREFIX).append("shadow-lg: 0 10px 25px rgba(0, 0, 0, 0.15);\n");
        }
    }

    private static void appendFocusAndTransition(StringBuilder sb, ThemeProperties props) {
        sb.append("  ").append(PREFIX).append("focus-ring: 2px solid ")
                .append(props.getPrimaryColor()).append(";\n");
        sb.append("  ").append(PREFIX).append("focus-ring-offset: 2px;\n");
        sb.append("  ").append(PREFIX).append("transition: 150ms ease;\n");
    }
}

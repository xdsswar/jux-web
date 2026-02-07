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

/**
 * Configuration properties for the JUX theme system.
 *
 * <p>This POJO holds all user-configurable theme settings that control the
 * visual appearance of a JUX application. Values are typically bound from
 * {@code application.yml} under the {@code jux.theme} prefix by Spring Boot's
 * configuration properties mechanism.</p>
 *
 * <p><b>Default values</b> are intentionally opinionated toward a clean,
 * accessible, modern design. Every default has been chosen to meet WCAG 2.2
 * AA contrast requirements when combined with the default background and
 * text colors.</p>
 *
 * <p><b>Example configuration in {@code application.yml}:</b></p>
 * <pre>{@code
 * jux:
 *   theme:
 *     name: default
 *     dark-mode: false
 *     primary-color: "#3b82f6"
 *     font-family: "Inter, system-ui, sans-serif"
 *     custom-css-path: "/css/overrides.css"
 * }</pre>
 *
 * @see DesignTokens
 */
public class ThemeProperties {

    /**
     * The name of the active theme.
     *
     * <p>This name is used to resolve theme-specific CSS files and design
     * token sets. The built-in theme name is {@code "default"}, which ships
     * with the framework. Consumer projects can create custom themes by
     * providing their own token sets and referencing them by name here.</p>
     *
     * <p>Default: {@code "default"}</p>
     */
    private String name = "default";

    /**
     * Optional path to a custom CSS file that is loaded after the theme CSS.
     *
     * <p>This allows consumer projects to layer additional styles on top of
     * the theme without replacing it entirely. The path is relative to the
     * {@code /static} directory (e.g. {@code "css/overrides.css"}) or can
     * be an absolute URL for CDN-hosted stylesheets.</p>
     *
     * <p>Default: {@code null} (no custom CSS)</p>
     */
    private String customCssPath;

    /**
     * Whether dark mode is enabled.
     *
     * <p>When {@code true}, the framework generates dark-mode design tokens
     * (inverted backgrounds, lighter text, adjusted contrast ratios) and
     * adds a {@code data-theme="dark"} attribute to the {@code <html>} element.
     * All built-in components respect this flag and adapt their styling.</p>
     *
     * <p>WCAG 2.2 AA contrast requirements are maintained in both light
     * and dark modes. The dark mode palette is carefully chosen to ensure
     * a minimum 4.5:1 contrast ratio for normal text.</p>
     *
     * <p>Default: {@code false}</p>
     */
    private boolean darkMode = false;

    /**
     * The primary brand color used throughout the theme.
     *
     * <p>This color is applied to interactive elements (links, buttons,
     * focused inputs, active tabs), primary call-to-action backgrounds,
     * and accent decorations. It maps to the {@code --jux-primary} CSS
     * custom property.</p>
     *
     * <p>The value should be a valid CSS color string: hex ({@code "#3b82f6"}),
     * RGB ({@code "rgb(59, 130, 246)"}), or HSL ({@code "hsl(217, 91%, 60%)"}).
     * The framework does not validate the format at configuration time, but
     * invalid values will produce broken CSS.</p>
     *
     * <p>Default: {@code "#3b82f6"} (a medium blue that meets AA contrast
     * on both white and dark backgrounds)</p>
     */
    private String primaryColor = "#3b82f6";

    /**
     * The primary font family stack for body text.
     *
     * <p>This value is inserted directly into the {@code --jux-font-family}
     * CSS custom property and applied to the {@code <body>} element. It
     * should be a valid CSS {@code font-family} value with fallback fonts.</p>
     *
     * <p>The default uses {@code system-ui} as the primary font, which
     * resolves to the operating system's native UI font (San Francisco on
     * macOS/iOS, Segoe UI on Windows, Roboto on Android). This ensures
     * optimal readability and zero font-loading latency.</p>
     *
     * <p>Default: {@code "system-ui, sans-serif"}</p>
     */
    private String fontFamily = "system-ui, sans-serif";

    // ── Getters ──────────────────────────────────────────────────

    /**
     * Returns the name of the active theme.
     *
     * @return the theme name, never null
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the path to the custom CSS override file.
     *
     * @return the custom CSS path, or null if none is configured
     */
    public String getCustomCssPath() {
        return customCssPath;
    }

    /**
     * Returns whether dark mode is enabled.
     *
     * @return {@code true} if dark mode is active, {@code false} otherwise
     */
    public boolean isDarkMode() {
        return darkMode;
    }

    /**
     * Returns the primary brand color.
     *
     * @return the CSS color string for the primary color, never null
     */
    public String getPrimaryColor() {
        return primaryColor;
    }

    /**
     * Returns the primary font family stack.
     *
     * @return the CSS font-family string, never null
     */
    public String getFontFamily() {
        return fontFamily;
    }

    // ── Setters ──────────────────────────────────────────────────

    /**
     * Sets the name of the active theme.
     *
     * @param name the theme name (e.g. "default", "corporate", "minimal")
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Sets the path to a custom CSS override file.
     *
     * @param customCssPath path relative to /static or an absolute URL,
     *                      or null to disable custom CSS
     */
    public void setCustomCssPath(String customCssPath) {
        this.customCssPath = customCssPath;
    }

    /**
     * Enables or disables dark mode.
     *
     * @param darkMode {@code true} to enable dark mode, {@code false} for light mode
     */
    public void setDarkMode(boolean darkMode) {
        this.darkMode = darkMode;
    }

    /**
     * Sets the primary brand color.
     *
     * @param primaryColor a valid CSS color string (hex, rgb, hsl)
     */
    public void setPrimaryColor(String primaryColor) {
        this.primaryColor = primaryColor;
    }

    /**
     * Sets the primary font family stack.
     *
     * @param fontFamily a valid CSS font-family value with fallbacks
     */
    public void setFontFamily(String fontFamily) {
        this.fontFamily = fontFamily;
    }
}

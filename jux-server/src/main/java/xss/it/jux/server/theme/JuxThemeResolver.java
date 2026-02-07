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

package xss.it.jux.server.theme;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import xss.it.jux.server.autoconfigure.JuxProperties;

import java.util.List;

/**
 * Resolves the active theme mode for the current request.
 *
 * <p>Resolution chain (first match wins):</p>
 * <ol>
 *   <li>Cookie value ({@code jux-theme} by default)</li>
 *   <li>Default theme from configuration ({@code jux.theme.default-theme})</li>
 * </ol>
 *
 * <p>The resolved theme is validated against the configured
 * {@code availableThemes} list. Invalid values fall back to the default.</p>
 *
 * @see JuxProperties.Theme
 */
public class JuxThemeResolver {

    private final JuxProperties.Theme themeProperties;

    public JuxThemeResolver(JuxProperties.Theme themeProperties) {
        this.themeProperties = themeProperties;
    }

    /**
     * Resolve the active theme mode for the given request.
     *
     * @param request the HTTP servlet request
     * @return the resolved theme mode (e.g. "light", "dark"), never null
     */
    public String resolve(HttpServletRequest request) {
        // 1. Check cookie
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            String cookieName = themeProperties.getCookieName();
            for (Cookie cookie : cookies) {
                if (cookieName.equals(cookie.getName())) {
                    String value = cookie.getValue();
                    if (value != null && isValidTheme(value)) {
                        return value;
                    }
                }
            }
        }

        // 2. Fall back to configured default
        return themeProperties.getDefaultTheme();
    }

    /**
     * Check whether the given theme name is in the configured available themes list.
     *
     * @param theme the theme name to validate
     * @return true if the theme is valid
     */
    public boolean isValidTheme(String theme) {
        if (theme == null) return false;
        List<String> available = themeProperties.getAvailableThemes();
        return available != null && available.contains(theme);
    }

    /**
     * Returns the default theme mode from configuration.
     *
     * @return the default theme name
     */
    public String getDefaultTheme() {
        return themeProperties.getDefaultTheme();
    }

    /**
     * Returns the cookie name used for theme persistence.
     *
     * @return the cookie name
     */
    public String getCookieName() {
        return themeProperties.getCookieName();
    }

    /**
     * Returns the cookie max-age in seconds.
     *
     * @return the cookie max-age
     */
    public int getCookieMaxAge() {
        return themeProperties.getCookieMaxAge();
    }
}

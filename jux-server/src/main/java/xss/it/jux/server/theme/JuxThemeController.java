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
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * REST endpoint for switching the active theme without a page reload.
 *
 * <p>Client-side code calls {@code POST /api/theme?value=dark} to persist
 * the theme preference in a cookie. The response is a simple JSON
 * acknowledgment -- no redirect, no page reload. The client then swaps
 * the {@code data-theme} attribute on the {@code <html>} element to
 * apply the new theme immediately via CSS.</p>
 *
 * <p><b>Example client-side usage:</b></p>
 * <pre>{@code
 * // Vanilla JS theme switcher (works with TeaVM JSO too):
 * fetch('/api/theme?value=dark', { method: 'POST' })
 *   .then(() => document.documentElement.setAttribute('data-theme', 'dark'));
 * }</pre>
 */
@RestController
public class JuxThemeController {

    private final JuxThemeResolver themeResolver;

    public JuxThemeController(JuxThemeResolver themeResolver) {
        this.themeResolver = themeResolver;
    }

    /**
     * Set the theme preference cookie.
     *
     * @param value    the theme mode to activate (e.g. "light", "dark")
     * @param response the HTTP response (cookie is added here)
     * @return 200 with the applied theme, or 400 if the theme is invalid
     */
    @PostMapping("/api/theme")
    public ResponseEntity<Map<String, String>> setTheme(
            @RequestParam String value,
            HttpServletResponse response) {

        if (!themeResolver.isValidTheme(value)) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", "Invalid theme: " + value));
        }

        Cookie cookie = new Cookie(themeResolver.getCookieName(), value);
        cookie.setMaxAge(themeResolver.getCookieMaxAge());
        cookie.setPath("/");
        response.addCookie(cookie);

        return ResponseEntity.ok(Map.of("theme", value));
    }
}

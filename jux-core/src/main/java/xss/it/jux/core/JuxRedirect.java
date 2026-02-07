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

package xss.it.jux.core;

/**
 * Immutable redirect descriptor that encapsulates a target URL and HTTP redirect status code.
 *
 * <p>Returned by {@link xss.it.jux.core.routing.JuxRouter#redirect(String)} and
 * {@link xss.it.jux.core.routing.JuxRouter#redirect(String, java.util.Map)} to
 * represent a server-side redirect response. Also used internally by
 * {@link PageMeta#redirectTo(String)} and {@link PageMeta#redirectTo(String, int)}
 * when a component needs to redirect during the render phase.</p>
 *
 * <p><b>Common HTTP redirect status codes:</b></p>
 * <ul>
 *   <li>{@code 301} -- Moved Permanently (use for permanent URL changes, search engines update their index)</li>
 *   <li>{@code 302} -- Found / Temporary Redirect (default; the original URL may return different content later)</li>
 *   <li>{@code 307} -- Temporary Redirect (preserves HTTP method; POST stays POST)</li>
 *   <li>{@code 308} -- Permanent Redirect (preserves HTTP method; like 301 but POST stays POST)</li>
 * </ul>
 *
 * <p><b>Example:</b></p>
 * <pre>{@code
 * // Redirect to another route via the router
 * JuxRedirect redirect = router.redirect("home", Map.of());
 * // redirect.url() → "/"
 * // redirect.status() → 302
 *
 * // Permanent redirect
 * JuxRedirect permanent = new JuxRedirect("/new-location", 301);
 * }</pre>
 *
 * @param url    the target URL to redirect to. May be a relative path (e.g. {@code "/about"})
 *               or an absolute URL (e.g. {@code "https://example.com/page"})
 * @param status the HTTP redirect status code (typically 301, 302, 307, or 308)
 *
 * @see xss.it.jux.core.routing.JuxRouter#redirect(String)
 * @see PageMeta#redirectTo(String)
 * @see PageMeta#redirectTo(String, int)
 */
public record JuxRedirect(
    String url,
    int status
) {
    /**
     * Convenience constructor that creates a temporary redirect (HTTP 302).
     *
     * <p>Equivalent to {@code new JuxRedirect(url, 302)}.</p>
     *
     * @param url the target URL to redirect to
     */
    public JuxRedirect(String url) {
        this(url, 302);
    }
}

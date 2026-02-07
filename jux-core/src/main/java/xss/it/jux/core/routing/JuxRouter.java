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

package xss.it.jux.core.routing;

import xss.it.jux.core.Component;
import xss.it.jux.core.JuxRedirect;

import java.util.Locale;
import java.util.Map;

/**
 * Interface for URL generation (reverse routing) and redirect creation.
 *
 * <p>The router translates route names or Component classes back into URL strings,
 * substituting path and query parameters. This avoids hard-coding URLs throughout
 * the application -- if a route path changes, only the {@code @Route} annotation
 * needs updating; all generated URLs adjust automatically.</p>
 *
 * <p>The implementation is provided by the {@code jux-server} module and registered
 * as a Spring bean. Components and services obtain it via {@code @Autowired}:</p>
 * <pre>{@code
 * @Autowired private JuxRouter router;
 *
 * // Generate URL by route name
 * String url = router.url("blog-post", Map.of("slug", "hello-world"));
 * // → "/blog/hello-world"
 *
 * // Generate URL by Component class
 * String url = router.url(BlogPostPage.class, Map.of("slug", "hello-world"));
 * // → "/blog/hello-world"
 *
 * // Generate URL with query parameters
 * String url = router.url("search", Map.of(), Map.of("q", "java", "page", "2"));
 * // → "/search?q=java&page=2"
 *
 * // Generate localized URL
 * String url = router.localizedUrl(Locale.forLanguageTag("es"));
 * // → "/es/current-page"
 * }</pre>
 *
 * <p><b>Route name resolution:</b> The route name is taken from
 * {@code @Route(name = "...")}. If no explicit name is set, the Component's
 * simple class name is used (e.g. {@code BlogPostPage}).</p>
 *
 * @see JuxRedirect
 * @see Component
 */
public interface JuxRouter {

    /**
     * Generate a URL for a named route, substituting the given path parameters.
     *
     * <p>Path parameters are matched against {@code {name}} placeholders in the
     * route's URL pattern. For example, route pattern {@code "/blog/{slug}"}
     * with params {@code Map.of("slug", "hello")} produces {@code "/blog/hello"}.</p>
     *
     * @param routeName the route name (from {@code @Route(name = "...")}, or the
     *                  Component's simple class name if no explicit name is set)
     * @param params    path parameter values keyed by placeholder name; may be empty
     *                  for routes with no path variables
     * @return the generated URL path string
     * @throws IllegalArgumentException if the route name is not registered or
     *                                  required path parameters are missing
     */
    String url(String routeName, Map<String, Object> params);

    /**
     * Generate a URL for a page Component class, substituting the given path parameters.
     *
     * <p>This is the type-safe alternative to {@link #url(String, Map)} -- the
     * route is resolved from the {@code @Route} annotation on the Component class,
     * eliminating the possibility of route name typos.</p>
     *
     * @param page   the Component class annotated with {@code @Route}
     * @param params path parameter values keyed by placeholder name; may be empty
     * @return the generated URL path string
     * @throws IllegalArgumentException if the class has no {@code @Route} annotation
     *                                  or required path parameters are missing
     */
    String url(Class<? extends Component> page, Map<String, Object> params);

    /**
     * Generate a URL for a named route with separate path and query parameters.
     *
     * <p>Path parameters are substituted into the route pattern. Query parameters
     * are URL-encoded and appended as a query string. Example:</p>
     * <pre>{@code
     * router.url("search", Map.of(), Map.of("q", "hello world", "page", "2"))
     * // → "/search?q=hello+world&page=2"
     * }</pre>
     *
     * @param name  the route name
     * @param path  path parameter values keyed by placeholder name; may be empty
     * @param query query parameter values to append; may be empty
     * @return the generated URL string with path and query components
     * @throws IllegalArgumentException if the route name is not registered or
     *                                  required path parameters are missing
     */
    String url(String name, Map<String, Object> path, Map<String, Object> query);

    /**
     * Create a {@link JuxRedirect} to a named route with the given path parameters.
     *
     * <p>Uses HTTP 302 (temporary redirect) by default. The redirect URL is
     * generated using the same logic as {@link #url(String, Map)}.</p>
     *
     * @param routeName the route name to redirect to
     * @param params    path parameter values for the target route
     * @return a redirect descriptor with the generated URL and 302 status
     * @throws IllegalArgumentException if the route name is not registered
     */
    JuxRedirect redirect(String routeName, Map<String, Object> params);

    /**
     * Create a {@link JuxRedirect} to a raw URL string.
     *
     * <p>Uses HTTP 302 (temporary redirect) by default. The URL is used as-is
     * without any route resolution or parameter substitution.</p>
     *
     * @param rawUrl the target URL (relative path or absolute URL)
     * @return a redirect descriptor with the given URL and 302 status
     */
    JuxRedirect redirect(String rawUrl);

    /**
     * Generate a URL for the current route in a different locale.
     *
     * <p>Used by language switcher components to generate links that switch
     * the language while staying on the same page. The locale prefix is
     * applied according to the {@code jux.i18n.url-strategy} configuration
     * (prefix, subdomain, or parameter).</p>
     *
     * <p>Only works within a request context where the current route is known.
     * For {@code @Localized} routes, this produces the locale-prefixed variant
     * (e.g. {@code /es/about} for Spanish).</p>
     *
     * @param locale the target locale to generate the URL for
     * @return the localized URL for the current route in the given locale
     * @throws IllegalStateException if called outside a request context
     */
    String localizedUrl(Locale locale);

    /**
     * Generate a URL for a named route in a specific locale.
     *
     * <p>Combines route URL generation with locale prefix application.
     * The locale prefix is determined by the {@code jux.i18n.url-strategy}
     * configuration.</p>
     *
     * @param routeName the route name
     * @param locale    the target locale
     * @param params    path parameter values for the route
     * @return the localized URL string
     * @throws IllegalArgumentException if the route name is not registered
     */
    String localizedUrl(String routeName, Locale locale, Map<String, Object> params);

    /**
     * Generate a URL for a page Component class in a specific locale.
     *
     * <p>Type-safe variant of {@link #localizedUrl(String, Locale, Map)}.
     * The route is resolved from the {@code @Route} annotation on the
     * Component class.</p>
     *
     * @param page   the Component class annotated with {@code @Route}
     * @param locale the target locale
     * @param params path parameter values for the route
     * @return the localized URL string
     * @throws IllegalArgumentException if the class has no {@code @Route} annotation
     */
    String localizedUrl(Class<? extends Component> page, Locale locale, Map<String, Object> params);
}

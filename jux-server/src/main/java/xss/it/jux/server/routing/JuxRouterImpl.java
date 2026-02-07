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

package xss.it.jux.server.routing;

import xss.it.jux.core.Component;
import xss.it.jux.core.JuxRedirect;
import xss.it.jux.core.routing.JuxRouter;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Default implementation of {@link JuxRouter} that generates URLs from
 * route names, component classes, and parameter maps.
 *
 * <p>This implementation resolves route definitions from the {@link JuxRouteRegistrar}
 * and substitutes path variables in the route pattern with the provided parameter
 * values. It also supports locale-prefixed URL generation for internationalized
 * routes.</p>
 *
 * <p><b>URL generation process:</b></p>
 * <ol>
 *   <li>Look up the {@link RouteDefinition} by name or component class</li>
 *   <li>Replace {@code {variable}} and {@code {variable:type}} placeholders
 *       in the pattern with corresponding parameter values</li>
 *   <li>Optionally append query parameters</li>
 *   <li>Optionally prepend a locale prefix for localized routes</li>
 * </ol>
 *
 * <p><b>Path variable substitution</b> uses a regex that matches both plain
 * variables ({@code {slug}}) and typed variables ({@code {id:long}}),
 * extracting only the variable name for lookup in the parameter map.</p>
 *
 * @see JuxRouter
 * @see JuxRouteRegistrar
 */
public class JuxRouterImpl implements JuxRouter {

    /**
     * Regex pattern matching JUX path variable placeholders.
     * Captures the variable name (group 1) from both plain ({@code {slug}})
     * and typed ({@code {id:long}}) variable syntax. The type hint after
     * the colon is matched but not captured.
     */
    private static final Pattern PATH_VAR_PATTERN = Pattern.compile("\\{([^}:]+)(?::[^}]+)?}");

    /** The route registrar for looking up route definitions by name or class. */
    private final JuxRouteRegistrar registrar;

    /** The default locale language code (e.g. "en") for locale prefix decisions. */
    private final String defaultLocale;

    /**
     * Create a new router implementation.
     *
     * @param registrar     the route registrar containing all discovered routes
     * @param defaultLocale the default locale language code (used to determine
     *                      when to omit the locale prefix from URLs)
     */
    public JuxRouterImpl(JuxRouteRegistrar registrar, String defaultLocale) {
        this.registrar = registrar;
        this.defaultLocale = defaultLocale;
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if no route is registered with the given name
     */
    @Override
    public String url(String routeName, Map<String, Object> params) {
        RouteDefinition def = registrar.findByName(routeName)
            .orElseThrow(() -> new IllegalArgumentException("Unknown route: " + routeName));
        return buildUrl(def.pattern(), params);
    }

    /**
     * {@inheritDoc}
     *
     * @throws IllegalArgumentException if no route is registered for the given component class
     */
    @Override
    public String url(Class<? extends Component> page, Map<String, Object> params) {
        RouteDefinition def = registrar.findByClass(page)
            .orElseThrow(() -> new IllegalArgumentException("No route for class: " + page.getName()));
        return buildUrl(def.pattern(), params);
    }

    /**
     * {@inheritDoc}
     *
     * <p>Generates the base URL from path parameters, then appends query parameters
     * as a standard {@code ?key=value&key2=value2} query string.</p>
     */
    @Override
    public String url(String name, Map<String, Object> path, Map<String, Object> query) {
        String url = url(name, path);
        if (query != null && !query.isEmpty()) {
            StringBuilder sb = new StringBuilder(url);
            sb.append('?');
            boolean first = true;
            for (Map.Entry<String, Object> entry : query.entrySet()) {
                if (!first) sb.append('&');
                sb.append(entry.getKey()).append('=').append(entry.getValue());
                first = false;
            }
            url = sb.toString();
        }
        return url;
    }

    /** {@inheritDoc} */
    @Override
    public JuxRedirect redirect(String routeName, Map<String, Object> params) {
        return new JuxRedirect(url(routeName, params));
    }

    /** {@inheritDoc} */
    @Override
    public JuxRedirect redirect(String rawUrl) {
        return new JuxRedirect(rawUrl);
    }

    /**
     * {@inheritDoc}
     *
     * <p><b>Note:</b> This simplified implementation generates a locale-prefixed
     * root path. A full implementation would need access to the current request
     * context to reconstruct the current route's URL with the new locale prefix.</p>
     */
    @Override
    public String localizedUrl(Locale locale) {
        // Simplified: generates locale-prefixed root path only.
        // Full implementation would require current request context to
        // reconstruct the current route's URL with the new locale prefix.
        return "/" + locale.getLanguage() + "/";
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the default locale, returns the base URL without a locale prefix.
     * For other locales, prepends the locale language code as a path prefix
     * (e.g. {@code "/es/blog/hello"}).</p>
     */
    @Override
    public String localizedUrl(String routeName, Locale locale, Map<String, Object> params) {
        String baseUrl = url(routeName, params);
        // Default locale uses the bare path (no prefix)
        if (locale.getLanguage().equals(defaultLocale)) {
            return baseUrl;
        }
        // Non-default locales get a language prefix: /es/about, /fr/about
        return "/" + locale.getLanguage() + baseUrl;
    }

    /**
     * {@inheritDoc}
     *
     * <p>For the default locale, returns the base URL without a locale prefix.
     * For other locales, prepends the locale language code as a path prefix.</p>
     */
    @Override
    public String localizedUrl(Class<? extends Component> page, Locale locale, Map<String, Object> params) {
        String baseUrl = url(page, params);
        // Default locale uses the bare path (no prefix)
        if (locale.getLanguage().equals(defaultLocale)) {
            return baseUrl;
        }
        // Non-default locales get a language prefix
        return "/" + locale.getLanguage() + baseUrl;
    }

    /**
     * Build a URL by substituting path variable placeholders in a route pattern
     * with values from the parameter map.
     *
     * <p>Uses {@link #PATH_VAR_PATTERN} to find and replace all {@code {variable}}
     * and {@code {variable:type}} placeholders. Also handles the wildcard
     * placeholder ({@code **}) as a special case.</p>
     *
     * <p>If the parameter map is null or empty, all placeholders are removed,
     * which may produce an invalid URL if the route requires path variables.</p>
     *
     * @param pattern the route pattern with placeholders (e.g. {@code "/blog/{slug}"})
     * @param params  the parameter map with variable names as keys and values to substitute;
     *                may be null or empty
     * @return the constructed URL with placeholders replaced by parameter values
     */
    private String buildUrl(String pattern, Map<String, Object> params) {
        if (params == null || params.isEmpty()) {
            // Remove all path variable patterns
            return PATH_VAR_PATTERN.matcher(pattern).replaceAll("");
        }

        Matcher matcher = PATH_VAR_PATTERN.matcher(pattern);
        StringBuilder result = new StringBuilder();
        while (matcher.find()) {
            String varName = matcher.group(1);
            Object value = params.get(varName);
            matcher.appendReplacement(result, value != null ? value.toString() : "");
        }
        matcher.appendTail(result);

        // Handle wildcard
        if (pattern.contains("**") && params.containsKey("**")) {
            return result.toString().replace("**", params.get("**").toString());
        }

        return result.toString();
    }
}

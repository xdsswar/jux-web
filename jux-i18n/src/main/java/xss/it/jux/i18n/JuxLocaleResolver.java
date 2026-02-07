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

package xss.it.jux.i18n;

import xss.it.jux.core.JuxRequestContext;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the active {@link Locale} for an incoming HTTP request by walking
 * a prioritized resolution chain.
 *
 * <p>The chain is evaluated top-to-bottom; the first strategy that produces a
 * supported locale wins:</p>
 * <ol>
 *   <li><b>URL prefix</b> -- e.g. {@code /es/about} yields {@code Locale("es")}.
 *       Only active when {@link I18nProperties#getUrlStrategy()} is {@code "prefix"}.</li>
 *   <li><b>Query parameter</b> -- e.g. {@code ?lang=fr}.</li>
 *   <li><b>Cookie</b> -- the cookie whose name is configured in
 *       {@link I18nProperties#getCookieName()} (default: {@code "jux-lang"}).</li>
 *   <li><b>Session attribute</b> -- stored under the key {@code "jux.locale"}.</li>
 *   <li><b>{@code Accept-Language} header</b> -- the first language from the
 *       header that matches a supported locale (with region-to-language fallback).</li>
 *   <li><b>Default</b> -- {@link I18nProperties#getDefaultLocaleObj()}.</li>
 * </ol>
 *
 * <p>At every step the candidate locale is validated against the list of
 * supported locales ({@link I18nProperties#getLocales()}) via {@link #isSupported(Locale)}.
 * An unsupported candidate is silently skipped and the next strategy is tried.</p>
 *
 * <p>This class has no Spring dependency and can be instantiated in plain unit
 * tests. In a Spring Boot application the {@code jux-server} auto-configuration
 * creates and registers it as a bean.</p>
 *
 * <p><b>Example (non-Spring):</b></p>
 * <pre>{@code
 * I18nProperties props = new I18nProperties();
 * props.setLocales(List.of("en", "es", "fr"));
 * JuxLocaleResolver resolver = new JuxLocaleResolver(props);
 *
 * Locale resolved = resolver.resolve(requestContext);
 * }</pre>
 *
 * @see I18nProperties
 * @see Messages#setCurrentLocale(Locale)
 */
public class JuxLocaleResolver {

    /** Configuration properties that control locale resolution behaviour. */
    private final I18nProperties properties;

    /**
     * Creates a new resolver backed by the given properties.
     *
     * @param properties i18n configuration; must not be {@code null}
     */
    public JuxLocaleResolver(I18nProperties properties) {
        this.properties = properties;
    }

    /**
     * Resolves the locale for the current request by walking the resolution chain.
     *
     * <p>If {@code ctx} is {@code null} (e.g. during background processing that is
     * not tied to an HTTP request) or the i18n subsystem is disabled, the
     * {@linkplain I18nProperties#getDefaultLocaleObj() default locale} is returned
     * immediately without consulting any strategy.</p>
     *
     * <p>The resolution chain is described in the
     * {@linkplain JuxLocaleResolver class-level Javadoc}.</p>
     *
     * @param ctx the current request context, or {@code null} for non-request scenarios
     * @return the resolved locale; never {@code null}
     */
    public Locale resolve(JuxRequestContext ctx) {
        if (ctx == null || !properties.isEnabled()) {
            return properties.getDefaultLocaleObj();
        }

        // 1. URL prefix -- highest priority; locale embedded in the path
        Locale fromUrl = resolveFromUrlPrefix(ctx);
        if (fromUrl != null) return fromUrl;

        // 2. Query param (?lang=fr) -- explicit per-request override
        Optional<String> langParam = ctx.queryParam("lang");
        if (langParam.isPresent()) {
            Locale candidate = Locale.forLanguageTag(langParam.get());
            if (isSupported(candidate)) return candidate;
        }

        // 3. Cookie -- persisted user preference across sessions
        Optional<String> cookie = ctx.cookie(properties.getCookieName());
        if (cookie.isPresent()) {
            Locale candidate = Locale.forLanguageTag(cookie.get());
            if (isSupported(candidate)) return candidate;
        }

        // 4. Session attribute -- server-side preference for this session
        Optional<Object> sessionLocale = ctx.session("jux.locale");
        if (sessionLocale.isPresent() && sessionLocale.get() instanceof Locale loc) {
            if (isSupported(loc)) return loc;
        }

        // 5. Accept-Language header -- browser's declared preference
        Optional<String> acceptLang = ctx.header("Accept-Language");
        if (acceptLang.isPresent()) {
            Locale candidate = parseAcceptLanguage(acceptLang.get());
            if (candidate != null && isSupported(candidate)) return candidate;
        }

        // 6. Default -- ultimate fallback, always returns a valid locale
        return properties.getDefaultLocaleObj();
    }

    /**
     * Attempts to extract a locale from the first segment of the URL path.
     *
     * <p>This strategy is only active when the configured
     * {@linkplain I18nProperties#getUrlStrategy() URL strategy} is {@code "prefix"}.
     * Given a request path such as {@code /es/about}, the method extracts
     * {@code "es"}, converts it to a {@link Locale}, and checks whether it
     * is in the supported list.</p>
     *
     * @param ctx the current request context; must not be {@code null}
     * @return a supported {@link Locale} parsed from the URL prefix,
     *         or {@code null} if no valid locale prefix is present or the
     *         URL strategy is not {@code "prefix"}
     */
    private Locale resolveFromUrlPrefix(JuxRequestContext ctx) {
        // Only applies when the locale is carried as a path prefix
        if (!"prefix".equals(properties.getUrlStrategy())) return null;

        String path = ctx.requestPath();
        if (path == null || path.length() < 2) return null;

        // Extract the first path segment after the leading '/'
        String withoutLeadingSlash = path.substring(1);
        int slashIdx = withoutLeadingSlash.indexOf('/');
        String segment = slashIdx > 0 ? withoutLeadingSlash.substring(0, slashIdx) : withoutLeadingSlash;

        // Treat the segment as a BCP 47 tag and see if it matches a supported locale
        Locale candidate = Locale.forLanguageTag(segment);
        if (isSupported(candidate)) return candidate;

        return null;
    }

    /**
     * Parses an {@code Accept-Language} HTTP header and returns the first
     * supported locale found.
     *
     * <p>The header is split by comma into individual language entries.
     * Quality-factor suffixes ({@code ;q=0.9}) are stripped but <b>not</b>
     * used for ordering -- the method trusts the order in which the browser
     * listed its preferences (highest-priority first). Wildcard entries
     * ({@code *}) are ignored.</p>
     *
     * <p>For each entry the method first tries an exact language+region
     * match (e.g. {@code en-US}). If that is not in the supported list it
     * falls back to the language-only code (e.g. {@code en}).</p>
     *
     * @param header the raw {@code Accept-Language} header value,
     *               e.g. {@code "fr-CH, fr;q=0.9, en;q=0.8, *;q=0.5"}
     * @return the first supported {@link Locale} found, or {@code null} if
     *         none of the header entries match a supported locale
     */
    private Locale parseAcceptLanguage(String header) {
        if (header == null || header.isBlank()) return null;

        // Split by comma: each part is a language tag optionally followed by ;q=...
        for (String part : header.split(",")) {
            String lang = part.trim();

            // Strip the quality factor suffix (e.g. ";q=0.9")
            int semi = lang.indexOf(';');
            if (semi > 0) lang = lang.substring(0, semi).trim();

            if (!lang.isEmpty() && !"*".equals(lang)) {
                Locale candidate = Locale.forLanguageTag(lang);
                if (isSupported(candidate)) return candidate;

                // Region-to-language fallback: en-US -> en
                if (!candidate.getLanguage().isEmpty()) {
                    Locale langOnly = Locale.forLanguageTag(candidate.getLanguage());
                    if (isSupported(langOnly)) return langOnly;
                }
            }
        }
        return null;
    }

    /**
     * Checks whether the given locale is in the configured list of supported locales.
     *
     * <p>The comparison is performed on the {@linkplain Locale#getLanguage() language code}
     * only -- region and variant subtags are ignored. This means {@code en-US} is
     * considered supported if {@code en} is in the list, and vice versa.</p>
     *
     * @param locale the locale to test, may be {@code null}
     * @return {@code true} if the locale's language code matches any entry in
     *         {@link I18nProperties#getLocales()}; {@code false} if {@code locale}
     *         is {@code null} or not in the supported set
     */
    public boolean isSupported(Locale locale) {
        if (locale == null) return false;
        return properties.getLocaleObjects().stream()
            .anyMatch(supported -> supported.getLanguage().equals(locale.getLanguage()));
    }
}

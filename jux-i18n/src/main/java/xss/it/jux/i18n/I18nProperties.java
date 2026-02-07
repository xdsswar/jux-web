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

import java.util.List;
import java.util.Locale;

/**
 * Configuration properties for the JUX internationalization (i18n) system.
 *
 * <p>This POJO holds every setting that controls multi-language behaviour:
 * which locales are supported, how the locale is resolved from incoming
 * requests, how missing translations are handled, and whether SEO-related
 * tags ({@code <link rel="alternate" hreflang="...">}) are generated
 * automatically.</p>
 *
 * <p>In a Spring Boot application the {@code jux-server} module binds these
 * values via {@code @ConfigurationProperties(prefix = "jux.i18n")}. The
 * class is intentionally a plain POJO with no Spring dependency so that
 * the {@code jux-i18n} module can be used (and tested) without Spring.</p>
 *
 * <p><b>Typical {@code application.yml}:</b></p>
 * <pre>{@code
 * jux:
 *   i18n:
 *     enabled: true
 *     default-locale: "en"
 *     locales:
 *       - "en"
 *       - "es"
 *       - "fr"
 *       - "ar"
 *     fallback-strategy: "default"
 *     url-strategy: "prefix"
 *     cookie-name: "jux-lang"
 *     cookie-max-age: 31536000
 *     redirect-on-missing: true
 *     generate-hreflang: true
 *     generate-sitemap-alternates: true
 * }</pre>
 *
 * @see JuxLocaleResolver
 * @see MessageBundleRegistry
 * @see Messages
 */
public class I18nProperties {

    /**
     * Master switch for the entire i18n subsystem. When {@code false}, locale
     * resolution is skipped and every request uses {@link #defaultLocale}.
     * Default: {@code true}.
     */
    private boolean enabled = true;

    /**
     * BCP 47 language tag for the default locale (e.g. {@code "en"}, {@code "pt-BR"}).
     * Used as the final fallback when no other locale can be resolved from the
     * request and as the language for base {@code @MessageBundle} interfaces.
     * Default: {@code "en"}.
     */
    private String defaultLocale = "en";

    /**
     * List of BCP 47 language tags for all locales the application supports
     * (e.g. {@code ["en", "es", "fr", "ar", "pt-BR"]}). Routes annotated
     * with {@code @Localized} register prefixed variants for each locale in
     * this list. The list also determines which locale-specific message
     * bundles are expected at startup validation.
     * Default: {@code ["en"]}.
     */
    private List<String> locales = List.of("en");

    /**
     * Strategy used when a translation bundle is missing for the requested locale.
     *
     * <ul>
     *   <li>{@code "default"} -- fall back directly to {@link #defaultLocale}.
     *       Example: {@code es-MX} with no match falls straight to {@code en}.</li>
     *   <li>{@code "chain"} -- walk up the locale hierarchy before falling back.
     *       Example: {@code es-MX} tries {@code es}, then {@code en}.</li>
     *   <li>{@code "none"} -- no fallback; a missing locale results in a 404.</li>
     * </ul>
     * Default: {@code "default"}.
     */
    private String fallbackStrategy = "default";

    /**
     * Strategy for embedding the locale in URLs.
     *
     * <ul>
     *   <li>{@code "prefix"} -- locale as a URL path prefix: {@code /es/about}.</li>
     *   <li>{@code "subdomain"} -- locale as a subdomain: {@code es.example.com/about}.</li>
     *   <li>{@code "parameter"} -- locale as a query parameter: {@code /about?lang=es}.</li>
     * </ul>
     * Default: {@code "prefix"}.
     */
    private String urlStrategy = "prefix";

    /**
     * Name of the HTTP cookie used to persist the user's locale preference
     * across requests. Set by the framework when the user explicitly chooses
     * a language (e.g. via a language-switcher component).
     * Default: {@code "jux-lang"}.
     */
    private String cookieName = "jux-lang";

    /**
     * Maximum age of the locale cookie in seconds. A value of {@code 31536000}
     * (the default) keeps the preference for approximately one year.
     */
    private int cookieMaxAge = 31536000;

    /**
     * Whether to redirect the user to the default locale when a page is not
     * available in the requested locale. When {@code true} (default), a request
     * for {@code /fr/terms} on a page that only has English content will
     * redirect to {@code /en/terms} (or {@code /terms} if the default locale
     * has no prefix). When {@code false}, a 404 is returned instead.
     */
    private boolean redirectOnMissing = true;

    /**
     * Whether to automatically generate {@code <link rel="alternate" hreflang="...">}
     * tags in the {@code <head>} section for every {@code @Localized} route.
     * These tags tell search engines which language versions of a page exist.
     * Default: {@code true}.
     *
     * @see <a href="https://developers.google.com/search/docs/specialty/international/localized-versions">
     *     Google: Tell Google about localized versions of your page</a>
     */
    private boolean generateHreflang = true;

    /**
     * Whether to include {@code <xhtml:link rel="alternate" hreflang="...">}
     * entries in the generated {@code sitemap.xml} for localized routes.
     * Default: {@code true}.
     */
    private boolean generateSitemapAlternates = true;

    /**
     * Base name for {@link java.util.ResourceBundle} property files.
     *
     * <p>When set (e.g. {@code "lang"}), the {@link Messages#getString(String)}
     * method loads translations from classpath property files named
     * {@code lang_en.properties}, {@code lang_es.properties}, etc.
     * This is an alternative to the {@code @MessageBundle} Java-interface
     * approach for simpler use-cases. Empty string (default) disables
     * property-file-based lookup.</p>
     */
    private String baseName = "";

    /**
     * Returns whether the i18n subsystem is enabled.
     *
     * @return {@code true} if i18n is active; {@code false} to disable all
     *         locale resolution and always use the default locale
     */
    public boolean isEnabled() { return enabled; }

    /**
     * Enables or disables the i18n subsystem.
     *
     * @param enabled {@code true} to activate i18n; {@code false} to disable it
     */
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    /**
     * Returns the BCP 47 language tag for the default locale.
     *
     * @return default locale tag, e.g. {@code "en"} or {@code "pt-BR"}
     */
    public String getDefaultLocale() { return defaultLocale; }

    /**
     * Sets the default locale as a BCP 47 language tag.
     *
     * @param defaultLocale language tag such as {@code "en"}, {@code "es"}, or {@code "pt-BR"}
     */
    public void setDefaultLocale(String defaultLocale) { this.defaultLocale = defaultLocale; }

    /**
     * Returns the list of BCP 47 language tags for all supported locales.
     *
     * @return immutable list of locale tags, never {@code null}
     */
    public List<String> getLocales() { return locales; }

    /**
     * Sets the list of supported locale tags.
     *
     * @param locales list of BCP 47 language tags (e.g. {@code ["en", "es", "fr"]})
     */
    public void setLocales(List<String> locales) { this.locales = locales; }

    /**
     * Returns the fallback strategy identifier.
     *
     * @return one of {@code "default"}, {@code "chain"}, or {@code "none"}
     */
    public String getFallbackStrategy() { return fallbackStrategy; }

    /**
     * Sets the fallback strategy for missing locale bundles.
     *
     * @param fallbackStrategy {@code "default"}, {@code "chain"}, or {@code "none"}
     */
    public void setFallbackStrategy(String fallbackStrategy) { this.fallbackStrategy = fallbackStrategy; }

    /**
     * Returns the URL strategy identifier.
     *
     * @return one of {@code "prefix"}, {@code "subdomain"}, or {@code "parameter"}
     */
    public String getUrlStrategy() { return urlStrategy; }

    /**
     * Sets the URL strategy for embedding the locale in URLs.
     *
     * @param urlStrategy {@code "prefix"}, {@code "subdomain"}, or {@code "parameter"}
     */
    public void setUrlStrategy(String urlStrategy) { this.urlStrategy = urlStrategy; }

    /**
     * Returns the name of the HTTP cookie that stores the user's locale preference.
     *
     * @return cookie name, e.g. {@code "jux-lang"}
     */
    public String getCookieName() { return cookieName; }

    /**
     * Sets the locale-preference cookie name.
     *
     * @param cookieName the cookie name to use
     */
    public void setCookieName(String cookieName) { this.cookieName = cookieName; }

    /**
     * Returns the maximum age of the locale cookie in seconds.
     *
     * @return cookie max-age in seconds (e.g. {@code 31536000} for one year)
     */
    public int getCookieMaxAge() { return cookieMaxAge; }

    /**
     * Sets the maximum age of the locale cookie in seconds.
     *
     * @param cookieMaxAge max-age in seconds; {@code 0} to expire immediately,
     *                     {@code -1} for a session cookie
     */
    public void setCookieMaxAge(int cookieMaxAge) { this.cookieMaxAge = cookieMaxAge; }

    /**
     * Returns whether the framework should redirect to the default locale when
     * a page is not available in the requested locale.
     *
     * @return {@code true} if missing-locale redirects are enabled
     */
    public boolean isRedirectOnMissing() { return redirectOnMissing; }

    /**
     * Enables or disables automatic redirect to the default locale when the
     * requested locale version of a page does not exist.
     *
     * @param redirectOnMissing {@code true} to redirect; {@code false} to return 404
     */
    public void setRedirectOnMissing(boolean redirectOnMissing) { this.redirectOnMissing = redirectOnMissing; }

    /**
     * Returns whether {@code <link rel="alternate" hreflang="...">} tags are
     * auto-generated for {@code @Localized} routes.
     *
     * @return {@code true} if hreflang generation is enabled
     */
    public boolean isGenerateHreflang() { return generateHreflang; }

    /**
     * Enables or disables automatic hreflang tag generation.
     *
     * @param generateHreflang {@code true} to generate hreflang tags in {@code <head>}
     */
    public void setGenerateHreflang(boolean generateHreflang) { this.generateHreflang = generateHreflang; }

    /**
     * Returns whether locale alternates are included in the generated {@code sitemap.xml}.
     *
     * @return {@code true} if sitemap alternate generation is enabled
     */
    public boolean isGenerateSitemapAlternates() { return generateSitemapAlternates; }

    /**
     * Enables or disables inclusion of locale alternates in {@code sitemap.xml}.
     *
     * @param generateSitemapAlternates {@code true} to include alternate links in the sitemap
     */
    public void setGenerateSitemapAlternates(boolean generateSitemapAlternates) { this.generateSitemapAlternates = generateSitemapAlternates; }

    /**
     * Returns the base name for {@link java.util.ResourceBundle} property files.
     *
     * @return the base name (e.g. {@code "lang"}), or empty string if disabled
     */
    public String getBaseName() { return baseName; }

    /**
     * Sets the base name for property-file-based translations.
     *
     * @param baseName the resource bundle base name (e.g. {@code "lang"})
     */
    public void setBaseName(String baseName) { this.baseName = baseName; }

    /**
     * Converts the {@link #defaultLocale} language tag to a {@link Locale} object.
     *
     * <p>This is a convenience method that avoids repeating
     * {@code Locale.forLanguageTag(getDefaultLocale())} throughout the
     * framework. The result is created fresh on each call (no caching)
     * so that changes to {@link #defaultLocale} are immediately reflected.</p>
     *
     * @return the default locale as a {@link Locale}, never {@code null}
     * @see Locale#forLanguageTag(String)
     */
    public Locale getDefaultLocaleObj() {
        return Locale.forLanguageTag(defaultLocale);
    }

    /**
     * Converts every entry in the {@link #locales} list to a {@link Locale} object.
     *
     * <p>The returned list preserves the order of the configured tags.
     * It is used by {@link Messages#availableLocales()}, the {@code @Localized}
     * route registrar, and the hreflang tag generator.</p>
     *
     * @return an unmodifiable list of {@link Locale} instances corresponding
     *         to the configured locale tags, never {@code null}
     */
    public List<Locale> getLocaleObjects() {
        return locales.stream().map(Locale::forLanguageTag).toList();
    }
}

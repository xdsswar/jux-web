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

package xss.it.jux.server.autoconfigure;

import xss.it.jux.i18n.I18nProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Configuration properties for the JUX framework, bound to the {@code jux.*} prefix
 * in {@code application.yml} or {@code application.properties}.
 *
 * <p>This is the central configuration class for all JUX framework behaviour.
 * It aggregates nested configuration groups for SSR caching, client-side output,
 * resource pipeline settings, theming, default meta tags, WCAG accessibility,
 * and internationalization.</p>
 *
 * <p><b>Example configuration (application.yml):</b></p>
 * <pre>{@code
 * jux:
 *   base-package: com.example.pages
 *   ssr:
 *     cache:
 *       enabled: true
 *       max-size: 1000
 *       ttl: 300s
 *   a11y:
 *     enabled: true
 *     fail-on-error: false
 *   i18n:
 *     default-locale: en
 *     locales: [en, es, fr]
 * }</pre>
 *
 * @see JuxAutoConfiguration
 */
@ConfigurationProperties(prefix = "jux")
public class JuxProperties {

    /**
     * Base Java package to scan for {@code @Route}-annotated components.
     * All subpackages are scanned recursively. If empty, the entire classpath
     * is scanned (not recommended for production due to startup cost).
     */
    private String basePackage = "";

    /** Server-side rendering configuration (cache settings). */
    private final Ssr ssr = new Ssr();

    /** Client-side TeaVM output configuration. */
    private final Client client = new Client();

    /** CSS/JS resource pipeline configuration (versioning, minification). */
    private final Resources resources = new Resources();

    /** Theme system configuration. */
    private final Theme theme = new Theme();

    /** Default HTML meta tag values applied to all pages unless overridden. */
    private final MetaDefaults metaDefaults = new MetaDefaults();

    /** WCAG 2.2 Level AA accessibility engine configuration. */
    private final A11y a11y = new A11y();

    /** Internationalization (i18n) configuration: locales, routing strategy, fallback. */
    private final I18nProperties i18n = new I18nProperties();

    /**
     * Get the base package to scan for {@code @Route} components.
     *
     * @return the base package path, or empty string for whole-classpath scan
     */
    public String getBasePackage() { return basePackage; }

    /**
     * Set the base package to scan for {@code @Route} components.
     *
     * @param basePackage the fully qualified package name (e.g. {@code "com.example.pages"})
     */
    public void setBasePackage(String basePackage) { this.basePackage = basePackage; }

    /**
     * Get the SSR (server-side rendering) configuration group.
     *
     * @return the SSR configuration, never null
     */
    public Ssr getSsr() { return ssr; }

    /**
     * Get the client-side (TeaVM) configuration group.
     *
     * @return the client configuration, never null
     */
    public Client getClient() { return client; }

    /**
     * Get the CSS/JS resource pipeline configuration group.
     *
     * @return the resources configuration, never null
     */
    public Resources getResources() { return resources; }

    /**
     * Get the theme system configuration group.
     *
     * @return the theme configuration, never null
     */
    public Theme getTheme() { return theme; }

    /**
     * Get the default meta tag configuration group.
     *
     * @return the meta defaults configuration, never null
     */
    public MetaDefaults getMetaDefaults() { return metaDefaults; }

    /**
     * Get the WCAG accessibility engine configuration group.
     *
     * @return the accessibility configuration, never null
     */
    public A11y getA11y() { return a11y; }

    /**
     * Get the internationalization configuration group.
     *
     * @return the i18n configuration, never null
     */
    public I18nProperties getI18n() { return i18n; }

    /**
     * Server-side rendering configuration group, bound to {@code jux.ssr.*}.
     *
     * <p>Controls the SSR HTML cache backed by Caffeine. Caching is enabled
     * by default and dramatically reduces render latency for static or
     * semi-static pages. Per-route TTL can be configured via
     * {@link xss.it.jux.annotation.Route#cacheTtl()}.</p>
     */
    public static class Ssr {

        /** Nested cache configuration for the SSR output cache. */
        private final Cache cache = new Cache();

        /**
         * Get the SSR cache configuration.
         *
         * @return the cache configuration, never null
         */
        public Cache getCache() { return cache; }

        /**
         * SSR HTML output cache configuration, bound to {@code jux.ssr.cache.*}.
         *
         * <p>The cache stores rendered HTML strings keyed by request path,
         * query string, and locale. It uses Caffeine for high-performance
         * concurrent caching with LRU eviction.</p>
         *
         * @see xss.it.jux.server.cache.SsrCache
         */
        public static class Cache {

            /**
             * Whether the SSR cache is enabled. When disabled, every request
             * triggers a full render cycle. Default: {@code true}.
             */
            private boolean enabled = true;

            /**
             * Maximum number of cached page entries. When exceeded, the least
             * recently used entries are evicted. Default: {@code 1000}.
             */
            private int maxSize = 1000;

            /**
             * Default cache time-to-live duration string. Supports suffixes:
             * {@code "s"} for seconds, {@code "m"} for minutes, {@code "h"} for hours.
             * A plain number is interpreted as seconds. Default: {@code "300s"} (5 minutes).
             * Per-route TTL set via {@code @Route(cacheTtl = ...)} overrides this default.
             */
            private String ttl = "300s";

            /** @return {@code true} if the SSR cache is enabled */
            public boolean isEnabled() { return enabled; }

            /** @param enabled whether to enable the SSR cache */
            public void setEnabled(boolean enabled) { this.enabled = enabled; }

            /** @return the maximum number of cached entries */
            public int getMaxSize() { return maxSize; }

            /** @param maxSize the maximum number of cached entries */
            public void setMaxSize(int maxSize) { this.maxSize = maxSize; }

            /** @return the default cache TTL as a duration string (e.g. "300s") */
            public String getTtl() { return ttl; }

            /** @param ttl the default cache TTL duration string */
            public void setTtl(String ttl) { this.ttl = ttl; }
        }
    }

    /**
     * Client-side TeaVM configuration group, bound to {@code jux.client.*}.
     *
     * <p>Controls where TeaVM-compiled JavaScript bundles are served from
     * and whether source maps are generated. Source maps should be enabled
     * during development for easier debugging and disabled in production
     * for smaller bundle sizes.</p>
     */
    public static class Client {

        /**
         * Filesystem path (relative to the static resources root) where
         * TeaVM-compiled JavaScript bundles are served from.
         * Default: {@code "/static/js/"}.
         */
        private String outputPath = "/static/js/";

        /**
         * Whether to generate JavaScript source maps alongside the TeaVM output.
         * Useful for development debugging; should be {@code false} in production.
         * Default: {@code false}.
         */
        private boolean sourceMaps = false;

        /** @return the output path for TeaVM-compiled JavaScript bundles */
        public String getOutputPath() { return outputPath; }

        /** @param outputPath the output path for TeaVM-compiled JavaScript bundles */
        public void setOutputPath(String outputPath) { this.outputPath = outputPath; }

        /** @return {@code true} if source maps are generated */
        public boolean isSourceMaps() { return sourceMaps; }

        /** @param sourceMaps whether to generate source maps */
        public void setSourceMaps(boolean sourceMaps) { this.sourceMaps = sourceMaps; }
    }

    /**
     * CSS/JS resource pipeline configuration group, bound to {@code jux.resources.*}.
     *
     * <p>Controls resource versioning (content-hash cache busting) and
     * minification of CSS/JS assets in production builds. Both are enabled
     * by default for optimal production performance.</p>
     */
    public static class Resources {

        /**
         * Whether to append a content hash to resource URLs for cache busting.
         * When enabled, changing a CSS/JS file produces a new URL, forcing
         * browsers to fetch the updated version. Default: {@code true}.
         */
        private boolean versioning = true;

        /**
         * Whether to minify CSS and JavaScript resources in production builds.
         * Reduces file size by removing whitespace, comments, and shortening
         * identifiers. Default: {@code true}.
         */
        private boolean minify = true;

        /** @return {@code true} if resource URL versioning (cache busting) is enabled */
        public boolean isVersioning() { return versioning; }

        /** @param versioning whether to enable resource URL versioning */
        public void setVersioning(boolean versioning) { this.versioning = versioning; }

        /** @return {@code true} if CSS/JS minification is enabled */
        public boolean isMinify() { return minify; }

        /** @param minify whether to enable CSS/JS minification */
        public void setMinify(boolean minify) { this.minify = minify; }
    }

    /**
     * Theme system configuration group, bound to {@code jux.theme.*}.
     *
     * <p>Selects the active theme from the {@code jux-themes} module.
     * The theme determines design tokens, default colour palette,
     * typography, spacing, and accessible focus styles applied to
     * all pages and built-in components.</p>
     */
    public static class Theme {

        /**
         * The name of the active theme. Must correspond to a theme
         * registered in the {@code jux-themes} module. The built-in
         * {@code "default"} theme provides WCAG 2.2 AA compliant styles.
         * Default: {@code "default"}.
         */
        private String name = "default";

        /**
         * The default theme mode applied when no cookie is set.
         * Typically {@code "light"} or {@code "dark"}.
         * Default: {@code "light"}.
         */
        private String defaultTheme = "light";

        /**
         * Available theme modes that users can switch between.
         * Values are used to validate cookie input and to generate
         * CSS blocks for each mode. Default: {@code ["light", "dark"]}.
         */
        private List<String> availableThemes = List.of("light", "dark");

        /**
         * Cookie name used to persist the user's theme preference.
         * Default: {@code "jux-theme"}.
         */
        private String cookieName = "jux-theme";

        /**
         * Cookie max-age in seconds. Default: {@code 31536000} (1 year).
         */
        private int cookieMaxAge = 31536000;

        /** @return the active theme name */
        public String getName() { return name; }

        /** @param name the theme name to activate */
        public void setName(String name) { this.name = name; }

        /** @return the default theme mode */
        public String getDefaultTheme() { return defaultTheme; }

        /** @param defaultTheme the default theme mode (e.g. "light", "dark") */
        public void setDefaultTheme(String defaultTheme) { this.defaultTheme = defaultTheme; }

        /** @return the list of available theme modes */
        public List<String> getAvailableThemes() { return availableThemes; }

        /** @param availableThemes the available theme modes */
        public void setAvailableThemes(List<String> availableThemes) { this.availableThemes = availableThemes; }

        /** @return the cookie name for theme persistence */
        public String getCookieName() { return cookieName; }

        /** @param cookieName the cookie name */
        public void setCookieName(String cookieName) { this.cookieName = cookieName; }

        /** @return the cookie max-age in seconds */
        public int getCookieMaxAge() { return cookieMaxAge; }

        /** @param cookieMaxAge the cookie max-age in seconds */
        public void setCookieMaxAge(int cookieMaxAge) { this.cookieMaxAge = cookieMaxAge; }
    }

    /**
     * Default HTML meta tag configuration group, bound to {@code jux.meta.defaults.*}.
     *
     * <p>These values are applied to every rendered page unless explicitly
     * overridden by {@code @Meta} annotations or {@link xss.it.jux.core.PageMeta}
     * programmatic metadata. They provide sensible baseline values for
     * character encoding and responsive viewport behaviour.</p>
     */
    public static class MetaDefaults {

        /**
         * Default character encoding for the {@code <meta charset="...">} tag.
         * Standard web practice is UTF-8 for universal character support.
         * Default: {@code "UTF-8"}.
         */
        private String charset = "UTF-8";

        /**
         * Default viewport meta tag content for responsive design.
         * This value is placed in {@code <meta name="viewport" content="...">}.
         * Default: {@code "width=device-width, initial-scale=1"}.
         */
        private String viewport = "width=device-width, initial-scale=1";

        /** @return the default charset for the meta charset tag */
        public String getCharset() { return charset; }

        /** @param charset the default charset (e.g. "UTF-8") */
        public void setCharset(String charset) { this.charset = charset; }

        /** @return the default viewport meta tag content */
        public String getViewport() { return viewport; }

        /** @param viewport the default viewport meta tag content */
        public void setViewport(String viewport) { this.viewport = viewport; }
    }

    /**
     * WCAG 2.2 Level AA accessibility engine configuration group,
     * bound to {@code jux.a11y.*}.
     *
     * <p>Controls the behaviour of the {@link xss.it.jux.a11y.JuxAccessibilityEngine},
     * which audits rendered element trees for accessibility violations.
     * In development mode the audit runs on every SSR render; in production
     * it is typically disabled for zero overhead.</p>
     *
     * @see xss.it.jux.a11y.JuxAccessibilityEngine
     * @see xss.it.jux.a11y.A11yViolation
     */
    public static class A11y {

        /**
         * Master switch for the accessibility engine. When {@code false},
         * no audits are performed and no auto-fixes are applied.
         * Default: {@code true}.
         */
        private boolean enabled = true;

        /**
         * Whether to run the WCAG audit on every server-side render.
         * Recommended for development; should be {@code false} in production
         * to avoid the audit overhead on every request.
         * Default: {@code true}.
         */
        private boolean auditOnRender = true;

        /**
         * Whether ERROR-level accessibility violations cause the page render
         * to fail with an HTTP 500 response. When {@code true}, the framework
         * enforces strict WCAG compliance at render time.
         * Default: {@code false}.
         */
        private boolean failOnError = false;

        /**
         * Whether to log accessibility violations to the application logger.
         * ERROR-level violations are logged at ERROR level; WARNING and INFO
         * violations are logged at WARN level.
         * Default: {@code true}.
         */
        private boolean logViolations = true;

        /**
         * Fallback value for the {@code <html lang="...">} attribute when
         * no page-level language is set. Required by WCAG 3.1.1
         * (Language of Page). Default: {@code "en"}.
         */
        private String defaultLang = "en";

        /**
         * Whether to auto-inject a skip-navigation link in layouts.
         * Skip-nav allows keyboard users to bypass repeated navigation
         * and jump to main content (WCAG 2.4.1 - Bypass Blocks).
         * Default: {@code true}.
         */
        private boolean skipNav = true;

        /**
         * Whether to ensure {@code :focus-visible} outlines are present
         * in the default theme CSS. Focus indicators are required by
         * WCAG 2.4.7 (Focus Visible). Default: {@code true}.
         */
        private boolean focusVisible = true;

        /**
         * Minimum touch/click target size in pixels (WCAG 2.5.8 - Target Size).
         * The audit flags interactive elements smaller than this value.
         * Default: {@code 24} (px).
         */
        private int minTargetSize = 24;

        /**
         * Whether to audit inline styles for text contrast ratio
         * (WCAG 1.4.3 - Contrast Minimum: 4.5:1 for normal text).
         * Default: {@code true}.
         */
        private boolean contrastCheck = true;

        /**
         * Whether to auto-fix safe, unambiguous accessibility issues
         * (e.g. adding {@code role="presentation"} to decorative images,
         * converting px font sizes to rem). Issues requiring human judgment
         * (alt text content, label text) are never auto-fixed.
         * Default: {@code true}.
         */
        private boolean autoFix = true;

        /**
         * REST endpoint path for on-demand accessibility audit reports.
         * When non-null, a controller is registered at this path that
         * returns JSON audit results for any page URL.
         * Default: {@code null} (disabled).
         */
        private String reportEndpoint = null;

        /** @return {@code true} if the accessibility engine is enabled */
        public boolean isEnabled() { return enabled; }

        /** @param enabled whether to enable the accessibility engine */
        public void setEnabled(boolean enabled) { this.enabled = enabled; }

        /** @return {@code true} if auditing runs on every SSR render */
        public boolean isAuditOnRender() { return auditOnRender; }

        /** @param auditOnRender whether to audit on every render cycle */
        public void setAuditOnRender(boolean auditOnRender) { this.auditOnRender = auditOnRender; }

        /** @return {@code true} if ERROR violations cause HTTP 500 responses */
        public boolean isFailOnError() { return failOnError; }

        /** @param failOnError whether to fail rendering on ERROR violations */
        public void setFailOnError(boolean failOnError) { this.failOnError = failOnError; }

        /** @return {@code true} if violations are logged */
        public boolean isLogViolations() { return logViolations; }

        /** @param logViolations whether to log accessibility violations */
        public void setLogViolations(boolean logViolations) { this.logViolations = logViolations; }

        /** @return the fallback HTML lang attribute value */
        public String getDefaultLang() { return defaultLang; }

        /** @param defaultLang the fallback HTML lang attribute value (e.g. "en") */
        public void setDefaultLang(String defaultLang) { this.defaultLang = defaultLang; }

        /** @return {@code true} if skip-navigation links are auto-injected */
        public boolean isSkipNav() { return skipNav; }

        /** @param skipNav whether to auto-inject skip-navigation links */
        public void setSkipNav(boolean skipNav) { this.skipNav = skipNav; }

        /** @return {@code true} if focus-visible outlines are enforced in the theme */
        public boolean isFocusVisible() { return focusVisible; }

        /** @param focusVisible whether to enforce focus-visible outlines */
        public void setFocusVisible(boolean focusVisible) { this.focusVisible = focusVisible; }

        /** @return the minimum touch target size in pixels */
        public int getMinTargetSize() { return minTargetSize; }

        /** @param minTargetSize the minimum touch target size in pixels */
        public void setMinTargetSize(int minTargetSize) { this.minTargetSize = minTargetSize; }

        /** @return {@code true} if inline style contrast checking is enabled */
        public boolean isContrastCheck() { return contrastCheck; }

        /** @param contrastCheck whether to check inline styles for contrast ratio */
        public void setContrastCheck(boolean contrastCheck) { this.contrastCheck = contrastCheck; }

        /** @return {@code true} if safe auto-fixes are applied */
        public boolean isAutoFix() { return autoFix; }

        /** @param autoFix whether to auto-fix safe accessibility issues */
        public void setAutoFix(boolean autoFix) { this.autoFix = autoFix; }

        /** @return the REST endpoint path for on-demand audits, or null if disabled */
        public String getReportEndpoint() { return reportEndpoint; }

        /** @param reportEndpoint the REST endpoint path (e.g. "/api/a11y/audit"), or null to disable */
        public void setReportEndpoint(String reportEndpoint) { this.reportEndpoint = reportEndpoint; }
    }
}

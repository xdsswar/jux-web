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

package xss.it.jux.server.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import xss.it.jux.server.autoconfigure.JuxProperties;

import java.time.Duration;
import java.util.Locale;
import java.util.Optional;

/**
 * High-performance SSR HTML cache backed by
 * <a href="https://github.com/ben-manes/caffeine">Caffeine</a>.
 *
 * <p>Caches rendered HTML5 document strings to avoid re-executing the
 * full rendering pipeline (component instantiation, parameter injection,
 * metadata resolution, element tree building, HTML serialization) on
 * repeated requests for the same page.</p>
 *
 * <p><b>Cache key structure:</b> {@code path|query|locale}</p>
 * <ul>
 *   <li>{@code path} - the request URI (e.g. {@code "/blog/hello"})</li>
 *   <li>{@code query} - the query string (e.g. {@code "ref=twitter"}), or empty</li>
 *   <li>{@code locale} - the BCP 47 language tag (e.g. {@code "en"}, {@code "es"})</li>
 * </ul>
 *
 * <p>This ensures that the same page in different languages or with different
 * query parameters gets separate cache entries.</p>
 *
 * <p><b>Eviction:</b> Uses LRU eviction when {@code maxSize} is exceeded,
 * and time-based expiration after the configured TTL. Per-route TTL from
 * {@code @Route(cacheTtl)} controls when individual entries are stored,
 * but the global TTL from configuration controls the Caffeine expiration.</p>
 *
 * <p><b>Thread safety:</b> Caffeine caches are fully thread-safe and
 * lock-free for concurrent reads. This class is safe for use by multiple
 * request-handling threads simultaneously.</p>
 *
 * @see xss.it.jux.server.autoconfigure.JuxProperties.Ssr.Cache
 * @see xss.it.jux.server.routing.JuxRouteHandler
 */
public class SsrCache {

    /** The underlying Caffeine cache instance; null when caching is disabled. */
    private final Cache<String, String> cache;

    /** Whether the cache is enabled. When false, all operations are no-ops. */
    private final boolean enabled;

    /**
     * Create a new SSR cache configured from the provided settings.
     *
     * <p>If caching is disabled in the configuration, the Caffeine cache
     * is not created at all to avoid any memory overhead.</p>
     *
     * @param config the cache configuration (enabled, maxSize, ttl)
     */
    public SsrCache(JuxProperties.Ssr.Cache config) {
        this.enabled = config.isEnabled();
        if (enabled) {
            this.cache = Caffeine.newBuilder()
                .maximumSize(config.getMaxSize())
                .expireAfterWrite(parseDuration(config.getTtl()))
                .build();
        } else {
            this.cache = null;
        }
    }

    /**
     * Get a cached HTML response.
     *
     * @param path   the request path
     * @param query  the query string (may be null)
     * @param locale the request locale
     * @return cached HTML, or empty if not cached
     */
    public Optional<String> get(String path, String query, Locale locale) {
        if (!enabled) return Optional.empty();
        return Optional.ofNullable(cache.getIfPresent(cacheKey(path, query, locale)));
    }

    /**
     * Store rendered HTML in the cache.
     *
     * @param path   the request path
     * @param query  the query string
     * @param locale the request locale
     * @param html   the rendered HTML
     * @param ttl    the cache TTL in seconds (from @Route cacheTtl)
     */
    public void put(String path, String query, Locale locale, String html, int ttl) {
        if (!enabled || ttl <= 0) return;
        cache.put(cacheKey(path, query, locale), html);
    }

    /**
     * Invalidate all cached entries, forcing all pages to be re-rendered
     * on the next request.
     *
     * <p>Useful after a global content change (e.g. theme switch, CMS publish-all)
     * or during development to clear stale cached pages.</p>
     */
    public void invalidateAll() {
        if (cache != null) {
            cache.invalidateAll();
        }
    }

    /**
     * Invalidate all cached entries for a specific request path, regardless
     * of query string or locale.
     *
     * <p>Removes all cache keys that start with {@code "path|"}, effectively
     * invalidating all locale and query-string variants of that path.
     * Useful after a single page's content is updated (e.g. CMS page save).</p>
     *
     * @param path the request path to invalidate (e.g. {@code "/blog/hello"})
     */
    public void invalidate(String path) {
        if (cache != null) {
            cache.asMap().keySet().removeIf(key -> key.startsWith(path + "|"));
        }
    }

    /**
     * Build a composite cache key from path, query string, and locale.
     *
     * <p>The key format is {@code "path|query|locale"}, using pipe ({@code |})
     * as the delimiter since it is not valid in URL paths or query strings.
     * A null query string is represented as an empty string.</p>
     *
     * @param path   the request URI path
     * @param query  the query string, or null if absent
     * @param locale the resolved request locale
     * @return the composite cache key
     */
    private String cacheKey(String path, String query, Locale locale) {
        return path + "|" + (query != null ? query : "") + "|" + locale.toLanguageTag();
    }

    /**
     * Parse a human-readable duration string into a {@link Duration}.
     *
     * <p>Supports the following suffixes:</p>
     * <ul>
     *   <li>{@code "s"} - seconds (e.g. {@code "300s"} = 5 minutes)</li>
     *   <li>{@code "m"} - minutes (e.g. {@code "5m"} = 5 minutes)</li>
     *   <li>{@code "h"} - hours (e.g. {@code "1h"} = 1 hour)</li>
     *   <li>No suffix - interpreted as seconds (e.g. {@code "300"} = 5 minutes)</li>
     * </ul>
     *
     * @param ttl the duration string to parse
     * @return the parsed Duration
     * @throws NumberFormatException if the numeric portion is not a valid long
     */
    private Duration parseDuration(String ttl) {
        if (ttl.endsWith("s")) {
            return Duration.ofSeconds(Long.parseLong(ttl.replace("s", "")));
        }
        if (ttl.endsWith("m")) {
            return Duration.ofMinutes(Long.parseLong(ttl.replace("m", "")));
        }
        if (ttl.endsWith("h")) {
            return Duration.ofHours(Long.parseLong(ttl.replace("h", "")));
        }
        return Duration.ofSeconds(Long.parseLong(ttl));
    }
}

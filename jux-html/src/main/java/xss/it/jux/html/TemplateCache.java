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

package xss.it.jux.html;

import xss.it.jux.core.Element;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Thread-safe cache for parsed HTML templates.
 *
 * <p>Stores prototype {@link Element} trees keyed by template path. Every call to
 * {@link #get(String)} returns a <b>deep clone</b> of the cached tree so that each
 * caller receives an independent copy safe for mutation (attribute changes, child
 * additions, text injection, etc.).</p>
 *
 * <p>The cache can be disabled at runtime (e.g. during development) so that
 * templates are re-parsed on every request, enabling live-reload workflows.</p>
 *
 * <p>This class is thread-safe. Multiple threads can read from and write to the
 * cache concurrently.</p>
 *
 * @see HtmlLoader
 * @see HtmlParser
 */
public class TemplateCache {

    /** Cached prototype Element trees keyed by template path. */
    private final Map<String, Element> cache = new ConcurrentHashMap<>();

    /** Whether caching is currently enabled. Volatile for visibility across threads. */
    private volatile boolean enabled = true;

    /**
     * Retrieve a deep clone of the cached template for the given path.
     *
     * <p>Returns {@code null} if caching is disabled, or if no template is cached
     * for the given path. The returned Element tree is a fully independent deep
     * copy -- mutations to it do not affect the cached prototype.</p>
     *
     * @param path the template path (as declared in {@code @Html})
     * @return a deep clone of the cached Element tree, or null if not cached or disabled
     */
    public Element get(String path) {
        if (!enabled) {
            return null;
        }
        Element prototype = cache.get(path);
        if (prototype == null) {
            return null;
        }
        return deepClone(prototype);
    }

    /**
     * Store a prototype Element tree in the cache.
     *
     * <p>If caching is disabled, this method is a no-op. The stored element is
     * used as the prototype for future {@link #get(String)} calls -- it should
     * not be mutated after being placed in the cache.</p>
     *
     * @param path     the template path (as declared in {@code @Html})
     * @param template the parsed Element tree to cache
     */
    public void put(String path, Element template) {
        if (!enabled) {
            return;
        }
        if (path != null && template != null) {
            cache.put(path, template);
        }
    }

    /**
     * Remove all cached templates.
     *
     * <p>Call this to force re-parsing of all templates on the next load.
     * Useful when templates are modified on disk during development.</p>
     */
    public void clear() {
        cache.clear();
    }

    /**
     * Enable or disable the cache.
     *
     * <p>When disabled, {@link #get(String)} always returns null and
     * {@link #put(String, Element)} becomes a no-op. Existing cached
     * entries are preserved (not cleared) -- they become accessible again
     * when the cache is re-enabled.</p>
     *
     * @param enabled true to enable caching, false to disable
     */
    @SuppressWarnings("all")
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns whether the cache is currently enabled.
     *
     * @return true if caching is enabled
     */
    @SuppressWarnings("all")
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Returns the number of templates currently cached.
     *
     * @return the cache size
     */
    public int size() {
        return cache.size();
    }

    // ── Deep cloning ──────────────────────────────────────────────

    /**
     * Create a deep clone of an Element tree.
     *
     * <p>Recursively copies the element, its attributes (including id, class, style),
     * its text content, and all children. Event handlers are <b>not</b> cloned since
     * templates are SSR-focused and handlers are set up programmatically.</p>
     *
     * @param original the element to clone
     * @return a fully independent deep copy
     */
    static Element deepClone(Element original) {
        if (original == null) {
            return null;
        }

        Element clone = Element.of(original.getTag());

        /* Clone all attributes (the merged map includes id, role, lang, aria-*, etc.). */
        Map<String, String> attrs = original.getAttributes();
        for (Map.Entry<String, String> entry : attrs.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();

            /*
             * The getAttributes() map merges class and style into computed attributes.
             * We need to apply them through the proper API to maintain Element's internal
             * structure (cssClasses list and styles map).
             */
            switch (key) {
                case "class" -> {
                    if (value != null && !value.isEmpty()) {
                        clone.cls(value.split("\\s+"));
                    }
                }
                case "style" -> {
                    if (value != null && !value.isEmpty()) {
                        /* Parse style string back into individual properties. */
                        String[] declarations = value.split(";");
                        for (String decl : declarations) {
                            String trimmed = decl.trim();
                            int colonIdx = trimmed.indexOf(':');
                            if (colonIdx > 0 && colonIdx < trimmed.length() - 1) {
                                String prop = trimmed.substring(0, colonIdx).trim();
                                String val = trimmed.substring(colonIdx + 1).trim();
                                if (!prop.isEmpty() && !val.isEmpty()) {
                                    clone.style(prop, val);
                                }
                            }
                        }
                    }
                }
                default -> clone.attr(key, value);
            }
        }

        /* Clone text content. */
        if (original.getTextContent() != null) {
            clone.text(original.getTextContent());
        }

        /* Recursively clone children. */
        for (Element child : original.getChildren()) {
            clone.children(deepClone(child));
        }

        return clone;
    }
}

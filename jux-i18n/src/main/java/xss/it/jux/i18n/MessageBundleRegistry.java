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

import xss.it.jux.annotation.MessageBundle;
import xss.it.jux.annotation.MessageLocale;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central registry of {@code @MessageBundle} interfaces and their locale-specific
 * {@code @MessageLocale} implementations.
 *
 * <p>At application startup the {@code jux-server} auto-configuration scans the
 * classpath for interfaces annotated with {@link MessageBundle}. For each bundle
 * it also discovers sub-interfaces annotated with {@link MessageLocale} that
 * provide translations in a specific language. The registry stores these
 * mappings and serves as the single source of truth when the framework needs
 * a translated message bundle for a given locale.</p>
 *
 * <p><b>Internal structure (simplified):</b></p>
 * <pre>{@code
 *   HomeMessages.class  ->  { "es" -> HomeMessagesEs.class,
 *                             "fr" -> HomeMessagesFr.class }
 *   CartMessages.class  ->  { "es" -> CartMessagesEs.class }
 * }</pre>
 *
 * <p>When a component asks for a bundle via {@link #getBundle(Class, Locale)},
 * the registry resolves the best-matching locale class (exact tag, language-only
 * fallback, or default), creates a {@link MessageBundleProxy} if one does not
 * already exist in the cache, and returns a typed proxy instance that the caller
 * can use directly.</p>
 *
 * <p>All data structures are thread-safe ({@link ConcurrentHashMap}). The proxy
 * cache is unbounded because the number of bundle-class/locale combinations is
 * small and fixed after startup.</p>
 *
 * @see MessageBundle
 * @see MessageLocale
 * @see MessageBundleProxy
 * @see Messages#get(Class)
 */
public class MessageBundleRegistry {

    /**
     * Two-level map: bundle interface class to a secondary map of
     * BCP 47 locale tag to the locale-specific sub-interface class.
     *
     * <p>Example:</p>
     * <pre>
     *   HomeMessages.class  ->  { "es" -> HomeMessagesEs.class,
     *                             "fr" -> HomeMessagesFr.class }
     * </pre>
     *
     * <p>The outer map is keyed by the <em>base</em> bundle interface (the one
     * annotated only with {@code @MessageBundle}, not {@code @MessageLocale}).
     * The inner map is keyed by the locale tag declared in the
     * {@code @MessageLocale} annotation on each sub-interface.</p>
     */
    private final Map<Class<?>, Map<String, Class<?>>> bundleLocales = new ConcurrentHashMap<>();

    /**
     * Cache of proxy instances keyed by {@code "fully.qualified.ClassName:locale-tag"}.
     *
     * <p>Once a proxy is created for a given bundle/locale pair it is reused for
     * all subsequent requests. The cache is thread-safe and unbounded because the
     * cardinality of bundle/locale combinations is small and known at startup.</p>
     */
    private final Map<String, Object> proxyCache = new ConcurrentHashMap<>();

    /** Configuration properties used to determine the fallback strategy. */
    private final I18nProperties properties;

    /**
     * Creates a new registry backed by the given i18n configuration.
     *
     * @param properties i18n properties that control fallback behaviour;
     *                   must not be {@code null}
     */
    public MessageBundleRegistry(I18nProperties properties) {
        this.properties = properties;
    }

    /**
     * Registers a base message bundle interface (the default-language version).
     *
     * <p>The class must be an interface annotated with {@link MessageBundle}.
     * If it is not an interface or the annotation is missing, the call is
     * silently ignored. Calling this method multiple times with the same class
     * is idempotent.</p>
     *
     * <p>Registration only creates a slot in the internal map; no proxy is
     * created until {@link #getBundle(Class, Locale)} is called.</p>
     *
     * @param bundleClass the interface annotated with {@code @MessageBundle};
     *                    ignored if {@code null}, not an interface, or not annotated
     */
    public void registerBundle(Class<?> bundleClass) {
        if (!bundleClass.isInterface()) return;
        if (!bundleClass.isAnnotationPresent(MessageBundle.class)) return;
        bundleLocales.putIfAbsent(bundleClass, new ConcurrentHashMap<>());
    }

    /**
     * Registers a locale-specific sub-interface of a message bundle.
     *
     * <p>The class must be an interface that:</p>
     * <ul>
     *   <li>Is annotated with {@link MessageLocale} (declaring its locale tag).</li>
     *   <li>Extends a parent interface annotated with {@link MessageBundle}.</li>
     * </ul>
     *
     * <p>The method walks the direct super-interfaces of {@code localeClass}
     * and registers it under the first parent that carries {@code @MessageBundle}.
     * If the parent bundle has not yet been registered, a slot is created
     * automatically via {@code computeIfAbsent}.</p>
     *
     * <p>If {@code localeClass} is not an interface or lacks the
     * {@code @MessageLocale} annotation, the call is silently ignored.</p>
     *
     * @param localeClass the locale-specific interface annotated with both
     *                    {@code @MessageBundle} and {@code @MessageLocale};
     *                    ignored if not an interface or not properly annotated
     */
    public void registerLocaleBundle(Class<?> localeClass) {
        if (!localeClass.isInterface()) return;
        MessageLocale localeMeta = localeClass.getAnnotation(MessageLocale.class);
        if (localeMeta == null) return;

        // Walk direct super-interfaces to find the base @MessageBundle parent
        for (Class<?> parent : localeClass.getInterfaces()) {
            if (parent.isAnnotationPresent(MessageBundle.class)) {
                bundleLocales.computeIfAbsent(parent, k -> new ConcurrentHashMap<>());
                bundleLocales.get(parent).put(localeMeta.value(), localeClass);
                break; // Only register under the first matching parent
            }
        }
    }

    /**
     * Returns a typed proxy that implements the given bundle interface with
     * messages resolved for the specified locale.
     *
     * <p>Proxies are cached by their composite key
     * ({@code "fully.qualified.BundleName:locale-tag"}) and created lazily on
     * first access via {@link MessageBundleProxy#create(Class, Class, Locale)}.
     * Subsequent calls with the same bundle type and locale return the same
     * proxy instance.</p>
     *
     * <p>The locale resolution follows the configured
     * {@linkplain I18nProperties#getFallbackStrategy() fallback strategy}:
     * exact tag match, then language-only match, then chain walk (if strategy
     * is {@code "chain"}), then the base bundle itself (default language).</p>
     *
     * @param bundleType the base bundle interface class (annotated with
     *                   {@code @MessageBundle})
     * @param locale     the desired locale for message resolution
     * @param <T>        the bundle interface type
     * @return a proxy instance implementing {@code T} whose methods return
     *         localized strings; never {@code null}
     */
    @SuppressWarnings("unchecked")
    public <T> T getBundle(Class<T> bundleType, Locale locale) {
        String cacheKey = bundleType.getName() + ":" + locale.toLanguageTag();

        return (T) proxyCache.computeIfAbsent(cacheKey, k -> {
            // Resolve the closest matching locale-specific interface class
            Class<?> targetClass = resolveLocaleClass(bundleType, locale);
            return MessageBundleProxy.create(bundleType, targetClass, locale);
        });
    }

    /**
     * Resolves the best-matching locale-specific interface for a bundle and locale.
     *
     * <p>The resolution attempts the following, in order:</p>
     * <ol>
     *   <li><b>Exact tag match</b> -- e.g. {@code "pt-BR"} maps to
     *       {@code HomeMessagesPtBr.class}.</li>
     *   <li><b>Language-only match</b> -- e.g. {@code "pt"} maps to
     *       {@code HomeMessagesPt.class}.</li>
     *   <li><b>Chain walk</b> (only when {@code fallbackStrategy} is
     *       {@code "chain"}) -- recursively strip region/variant subtags
     *       and retry. Example: {@code es-MX} tries {@code es} then the default.</li>
     *   <li><b>Base bundle</b> -- the original {@code @MessageBundle} interface
     *       whose {@code @Message} annotations contain the default-language text.</li>
     * </ol>
     *
     * @param bundleType the base bundle interface class
     * @param locale     the desired locale
     * @return the best-matching interface class, which may be {@code bundleType}
     *         itself if no locale-specific version is registered
     */
    private Class<?> resolveLocaleClass(Class<?> bundleType, Locale locale) {
        Map<String, Class<?>> locales = bundleLocales.getOrDefault(bundleType, Map.of());

        // 1. Exact tag match: "pt-BR"
        String tag = locale.toLanguageTag();
        if (locales.containsKey(tag)) return locales.get(tag);

        // 2. Language-only match: "pt"
        String lang = locale.getLanguage();
        if (locales.containsKey(lang)) return locales.get(lang);

        // 3. Chain fallback: recursively strip subtags (es-MX -> es -> default)
        if ("chain".equals(properties.getFallbackStrategy())) {
            Locale parent = Locale.forLanguageTag(lang);
            // Guard: only recurse if the parent is actually different to avoid infinite loop
            if (!parent.getLanguage().equals(locale.getLanguage())) {
                return resolveLocaleClass(bundleType, parent);
            }
        }

        // 4. No locale-specific class found; return the base bundle (default language)
        return bundleType;
    }

    /**
     * Returns an unmodifiable view of all registered base bundle interface classes.
     *
     * <p>This is primarily used by startup validators that check whether every
     * configured locale has a corresponding {@code @MessageLocale} implementation
     * for every registered bundle.</p>
     *
     * @return unmodifiable set of base bundle interface classes; never {@code null}
     */
    public Set<Class<?>> getBundleClasses() {
        return Collections.unmodifiableSet(bundleLocales.keySet());
    }

    /**
     * Returns the set of BCP 47 locale tags for which locale-specific interfaces
     * have been registered for the given bundle.
     *
     * <p>If the bundle type has not been registered at all, an empty set is returned.</p>
     *
     * @param bundleType the base bundle interface class
     * @return unmodifiable set of locale tags (e.g. {@code {"es", "fr", "pt-BR"}});
     *         never {@code null}
     */
    public Set<String> getLocaleTags(Class<?> bundleType) {
        Map<String, Class<?>> locales = bundleLocales.get(bundleType);
        return locales != null ? Collections.unmodifiableSet(locales.keySet()) : Set.of();
    }
}
